#!/bin/bash
# ============================================================
#  deploy-prod.sh  —  Script de deploy para Producción
#
#  El workflow `.github/workflows/deploy-prod.yml` copia este
#  archivo del repo al VPS en /opt/fatrans/deploy-prod.sh y lo
#  ejecuta vía SSH. **El repo es la fuente de verdad** — cualquier
#  cambio acá se aplica al próximo deploy automáticamente.
#
#  Uso: bash /opt/fatrans/deploy-prod.sh <GIT_SHA>
# ============================================================
set -euo pipefail

GIT_SHA=${1:-"latest"}
REPO_DIR="/opt/fatrans/backend"
INFRA_DIR="/opt/fatrans/backend/infrastructure"
LOG_FILE="/var/log/fatrans-prod-deploy.log"

log() { echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*" | tee -a "$LOG_FILE"; }

NGINX_CONF_SRC="$REPO_DIR/infrastructure/nginx/fatrans.conf"
NGINX_CONF_DST="/etc/nginx/sites-enabled/fatrans"

log "======================================================"
log " Iniciando deploy PRODUCCIÓN  —  SHA: $GIT_SHA"
log "======================================================"

# 1. Actualizar código
log "→ Actualizando repositorio..."
cd "$REPO_DIR"
git fetch origin
# `-B` crea la rama local si no existe, o la resetea si existe.
# Necesario para clones que históricamente vinieron con refspec
# restringido a develop (la primera promoción a main falla con
# `git checkout main` plain porque la rama local no existe aún).
git checkout -B main origin/main
git reset --hard origin/main
log "   Código actualizado: $(git log --oneline -1)"

# 2. Build del Backend
log "→ Construyendo imagen backend (prod)..."
docker build \
  -t fatrans-backend:latest \
  -t "fatrans-backend:$GIT_SHA" \
  -f "$REPO_DIR/backend/Dockerfile" \
  "$REPO_DIR/backend/"
log "   Backend build OK"

# 3. Build del Frontend
# IMPORTANTE: pasar los CUATRO build-args de NEXT_PUBLIC_*. Los valores
# se inlinean al BUILD (no a runtime), así que si falta alguno acá, el
# bundle queda con el default del Dockerfile y los env vars del
# `docker-compose.prod.yml` no lo cambian en producción.
#
# El 18-may-2026 omitir ADMIN_URL y AUTH_URL hizo que el bundle quedara
# con `NEXT_PUBLIC_AUTH_URL=https://fatrans.com.ve` (default del
# Dockerfile) en vez de `auth.fatrans.com.ve` (compose). Los usuarios
# que entraban por `www.fatrans.com.ve/registro` recibían 403
# "Origen no permitido" porque la whitelist del BFF no incluía `www`.
log "→ Construyendo imagen frontend (prod)..."
docker build \
  -t fatrans-frontend:latest \
  -t "fatrans-frontend:$GIT_SHA" \
  --build-arg NEXT_PUBLIC_API_URL=https://api.fatrans.com.ve/api \
  --build-arg NEXT_PUBLIC_APP_URL=https://app.fatrans.com.ve \
  --build-arg NEXT_PUBLIC_ADMIN_URL=https://admin.fatrans.com.ve \
  --build-arg NEXT_PUBLIC_AUTH_URL=https://fatrans.com.ve \
  "$REPO_DIR/frontend-web/"

docker tag fatrans-frontend:latest fatrans-frontend-public:latest
docker tag fatrans-frontend:latest fatrans-frontend-protected:latest
log "   Frontend build OK"

# 4. Defensa contra containers pre-existentes creados fuera de compose.
#    Si en algún momento se levantaron containers a mano (sin labels
#    `com.docker.compose.project`), `docker compose --force-recreate`
#    no los reconoce como propios y choca contra el `container_name`.
#    Removerlos acá es idempotente — si ya son compose-managed,
#    `compose up` los recrea normalmente; si no existen, no hace nada.
log "→ Limpiando containers PROD pre-existentes (si los hay)..."
for c in fatrans-backend fatrans-frontend-public fatrans-frontend-protected; do
  if docker inspect "$c" >/dev/null 2>&1; then
    LABEL=$(docker inspect --format='{{ index .Config.Labels "com.docker.compose.project" }}' "$c" 2>/dev/null || echo "")
    if [ -z "$LABEL" ]; then
      log "   $c existe sin labels de compose — removiendo para que compose lo cree fresco"
      docker rm -f "$c" >/dev/null
    else
      log "   $c ya es compose-managed (proyecto=$LABEL) — compose se encargará"
    fi
  fi
done

# 4.b. Actualizar config nginx desde el repo (fuente de verdad).
#      Histórico: el config solo vivía en `/etc/nginx/sites-enabled/fatrans`
#      sin version control. El 18-may-2026 le faltaba `qa.fatrans.com.ve`
#      en server_name y nginx mandaba ese host al default_server (PROD
#      landing), provocando que el botón "Registrarse" de QA llevara a
#      PROD. Ahora el repo es la fuente de verdad y este deploy lo
#      sincroniza. Si `nginx -t` falla, ABORTAMOS sin reload — nginx
#      sigue sirviendo el config anterior. Backup previo a cada cambio.
if [ -f "$NGINX_CONF_SRC" ]; then
  log "→ Actualizando nginx config desde el repo..."
  if ! diff -q "$NGINX_CONF_SRC" "$NGINX_CONF_DST" >/dev/null 2>&1; then
    BAK="/root/fatrans.nginx.bak_$(date +%Y%m%d_%H%M%S)"
    cp "$NGINX_CONF_DST" "$BAK"
    log "   Backup del config previo: $BAK"
    cp "$NGINX_CONF_SRC" "$NGINX_CONF_DST"
    if nginx -t 2>>"$LOG_FILE"; then
      nginx -s reload
      log "   ✅ nginx config recargado"
    else
      log "   ❌ nginx -t FALLÓ — restaurando backup"
      cp "$BAK" "$NGINX_CONF_DST"
      nginx -t 2>>"$LOG_FILE"
      exit 1
    fi
  else
    log "   Config nginx sin cambios — skip"
  fi
else
  log "   ⚠️ $NGINX_CONF_SRC no existe en el repo — skip nginx update"
fi

# 5. Restart con zero-downtime (recrear uno a uno)
log "→ Actualizando backend..."
cd "$INFRA_DIR"
docker compose -f docker-compose.prod.yml up -d --no-deps --force-recreate backend

log "→ Esperando backend saludable..."
for i in $(seq 1 18); do
  STATUS=$(docker inspect --format='{{.State.Health.Status}}' fatrans-backend 2>/dev/null || echo "starting")
  if [ "$STATUS" = "healthy" ]; then
    log "   Backend saludable ✓"
    break
  fi
  log "   Intento $i/18 — $STATUS"
  sleep 10
done

log "→ Actualizando frontends..."
docker compose -f docker-compose.prod.yml up -d --no-deps --force-recreate frontend_public frontend_protected

# 6. Verificación
log "→ Verificación final..."
SERVICES=("fatrans-backend" "fatrans-frontend-public" "fatrans-frontend-protected")
ALL_OK=true
for svc in "${SERVICES[@]}"; do
  STATUS=$(docker inspect --format='{{.State.Status}}' "$svc" 2>/dev/null || echo "missing")
  if [ "$STATUS" != "running" ]; then
    log "   ❌ $svc — $STATUS"
    ALL_OK=false
  else
    log "   ✅ $svc — running"
  fi
done

# 7. Limpieza
docker image prune -f --filter "until=72h" 2>/dev/null || true

if $ALL_OK; then
  log "======================================================"
  log " ✅ Deploy PRODUCCIÓN completado — SHA: $GIT_SHA"
  log " 🌐 https://fatrans.com.ve"
  log "======================================================"
  exit 0
else
  log "======================================================"
  log " ❌ Deploy PRODUCCIÓN FALLÓ"
  log "======================================================"
  exit 1
fi
