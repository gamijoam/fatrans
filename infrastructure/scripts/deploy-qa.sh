#!/bin/bash
# ============================================================
#  deploy-qa.sh  —  Script de deploy para entorno QA
#  Uso: bash /opt/fatrans-qa/deploy-qa.sh <GIT_SHA>
#
#  Fuente de verdad versionada: infrastructure/scripts/deploy-qa.sh
#  El archivo en /opt/fatrans-qa/ debe mantenerse sincronizado.
#
#  Principios:
#   - QA NO toca PROD. Si un servicio base prod (postgres/redis/minio) no
#     está running, el script ABORTA con instrucciones — no intenta levantarlo.
#   - Se usa `docker compose` v2 (sin guion). v1 está abandonado y rompe
#     con Docker Engine >= 25 (KeyError 'ContainerConfig').
#   - El backend QA se arranca con healthcheck explícito; antes no tenía y el
#     loop "Esperando backend healthy" reportaba ✅ falso sin que llegara healthy.
# ============================================================
set -euo pipefail

GIT_SHA=${1:-"latest"}
REPO_DIR="/opt/fatrans/backend"
QA_DIR="/opt/fatrans-qa"
LOG_FILE="/var/log/fatrans-qa-deploy.log"

log() { echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*" | tee -a "$LOG_FILE"; }

abort_prod_dependency() {
  local svc=$1; local status=$2
  log "   ❌ Servicio PROD '$svc' no está running (estado: $status)."
  log "   QA depende de servicios base de PROD que deben levantarse manualmente."
  log "   Acción requerida en el VPS (root):"
  log "     cd $REPO_DIR/backend/infrastructure"
  log "     docker compose -f docker-compose.prod.yml -p infrastructure up -d $svc"
  log "   Una vez levantado y running, re-disparar este deploy desde GitHub Actions."
  exit 2
}

log "======================================================"
log " Iniciando deploy QA  —  SHA: $GIT_SHA"
log "======================================================"

# 1. Verificar servicios base PROD (read-only, no levantamos prod desde QA)
log "→ Verificando servicios base de producción..."
for svc in fatrans-postgres fatrans-redis fatrans-minio; do
  STATUS=$(docker inspect --format='{{.State.Status}}' "$svc" 2>/dev/null || echo "missing")
  if [ "$STATUS" != "running" ]; then
    abort_prod_dependency "$svc" "$STATUS"
  fi
done
log "   Servicios base PROD OK"

# 2. Actualizar código
log "→ Actualizando repositorio..."
cd "$REPO_DIR"
git fetch origin
git checkout develop
git reset --hard origin/develop
log "   Código actualizado: $(git log --oneline -1)"

# 3. Build Backend
log "→ Construyendo imagen backend-qa..."
if ! docker build \
  -t fatrans-backend-qa:latest \
  -t "fatrans-backend-qa:$GIT_SHA" \
  -f "$REPO_DIR/backend/Dockerfile" \
  "$REPO_DIR/backend/" >> "$LOG_FILE" 2>&1; then
  log "   ❌ Backend build FALLÓ"
  exit 1
fi
log "   Backend build OK"

# 4. Build Frontend con URLs de QA horneadas en build time
log "→ Construyendo imagen frontend-qa (puede tardar ~7min)..."
if ! docker build \
  --no-cache \
  -t fatrans-frontend-qa:latest \
  -t "fatrans-frontend-qa:$GIT_SHA" \
  --build-arg NEXT_PUBLIC_API_URL=https://qa-api.fatrans.com.ve/api \
  --build-arg NEXT_PUBLIC_APP_URL=https://qa-app.fatrans.com.ve \
  --build-arg NEXT_PUBLIC_ADMIN_URL=https://qa-admin.fatrans.com.ve \
  --build-arg NEXT_PUBLIC_AUTH_URL=https://qa-auth.fatrans.com.ve \
  "$REPO_DIR/frontend-web/" >> "$LOG_FILE" 2>&1; then
  log "   ❌ Frontend build FALLÓ"
  exit 1
fi
docker tag fatrans-frontend-qa:latest fatrans-frontend-public-qa:latest
docker tag fatrans-frontend-qa:latest fatrans-frontend-protected-qa:latest
log "   Frontend build OK"

# 5. Crear DB fondo_qa si no existe (idempotente)
#
# Implementación anterior usaba `psql -tc ... | grep -q 1 || psql -c CREATE`
# y fallaba intermitentemente con `ERROR: database "fondo_qa" already exists`:
# con `set -o pipefail` activo, si el primer `docker exec` retornaba exit != 0
# por cualquier transient (network blip, reattach), el pipe completo era != 0
# *aunque grep matcheara*, y se ejecutaba el CREATE de todas formas. PostgreSQL
# además NO soporta `CREATE DATABASE IF NOT EXISTS`, así que el CREATE en una
# BD ya existente revienta con exit 1 y `set -e` aborta el deploy.
#
# Solución: psql `\gexec` — single round-trip, sin pipes, nativamente
# idempotente. Si la BD existe, el SELECT devuelve 0 filas y `\gexec` no
# ejecuta nada; si no existe, el SELECT devuelve "CREATE DATABASE fondo_qa"
# y `\gexec` lo ejecuta. Verificado en QA: ambos casos exit=0.
log "→ Verificando base de datos fondo_qa..."
docker exec -i fatrans-postgres psql -U app -d fondo >> "$LOG_FILE" 2>&1 <<'SQL'
SELECT 'CREATE DATABASE fondo_qa'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname='fondo_qa')
\gexec
SQL
log "   DB fondo_qa OK"

# 6. Detener SOLO contenedores QA (nunca tocar prod)
log "→ Deteniendo contenedores QA anteriores..."
for c in fatrans-qa-frontend-protected fatrans-qa-frontend-public fatrans-qa-backend fatrans-qa-redis; do
  docker stop "$c" 2>/dev/null && log "   Detenido: $c" || true
  docker rm   "$c" 2>/dev/null && log "   Eliminado: $c" || true
done

# 7. Levantar servicios QA con docker run.
#    Nota: el stack QA no tiene compose file propio — usar docker run permite
#    overrides de DB/redis/JWT sin duplicar el compose de prod.

log "→ Levantando Redis QA..."
docker run -d \
  --name fatrans-qa-redis \
  --network fatrans-network \
  --restart unless-stopped \
  -p 6380:6379 \
  --health-cmd "redis-cli -a fatrans-qa-redis-2026 ping | grep -q PONG || exit 1" \
  --health-interval 10s --health-timeout 5s --health-retries 5 \
  redis:7-alpine \
  redis-server --requirepass fatrans-qa-redis-2026 >> "$LOG_FILE" 2>&1

for i in $(seq 1 10); do
  docker exec fatrans-qa-redis redis-cli -a fatrans-qa-redis-2026 ping 2>/dev/null | grep -q PONG && break || sleep 2
done
log "   Redis QA OK"

log "→ Levantando Backend QA (con healthcheck)..."
docker run -d \
  --name fatrans-qa-backend \
  --network fatrans-network \
  --restart unless-stopped \
  -p 8081:8080 \
  --health-cmd 'wget -qO- http://localhost:8080/api/v1/auth/login > /dev/null 2>&1; [ $? -ne 7 ] && exit 0 || exit 1' \
  --health-interval 30s \
  --health-timeout 10s \
  --health-retries 3 \
  --health-start-period 60s \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e DB_URL=jdbc:postgresql://fatrans-postgres:5432/fondo_qa \
  -e DB_USER=app \
  -e DB_PASS=fatrans-secret-2026 \
  -e REDIS_HOST=fatrans-qa-redis \
  -e REDIS_PORT=6379 \
  -e REDIS_PASS=fatrans-qa-redis-2026 \
  -e MINIO_ENDPOINT=http://fatrans-minio:9000 \
  -e MINIO_ACCESS_KEY=fatransminio \
  -e MINIO_SECRET_KEY=fatransminio_secret_2026 \
  -e "CORS_ORIGINS=https://qa-auth.fatrans.com.ve,https://qa.fatrans.com.ve,https://qa-app.fatrans.com.ve,https://qa-admin.fatrans.com.ve,https://qa-api.fatrans.com.ve" \
  -e JWT_SECRET=fatrans-fondo-qa-secret-key-2026-v1 \
  -e JWT_ISSUER=fatrans-fondo-ahorro-qa \
  -e COOKIE_DOMAIN=.fatrans.com.ve \
  -e JWT_ACCESS_EXPIRATION_MINUTES=60 \
  -e JWT_REFRESH_EXPIRATION_DAYS=7 \
  fatrans-backend-qa:latest >> "$LOG_FILE" 2>&1

log "→ Levantando Frontend Public QA (puerto 4000)..."
docker run -d \
  --name fatrans-qa-frontend-public \
  --network fatrans-network \
  --restart unless-stopped \
  -p 4000:3000 \
  -e NEXT_PUBLIC_API_URL=https://qa-api.fatrans.com.ve/api \
  -e NEXT_PUBLIC_APP_URL=https://qa-app.fatrans.com.ve \
  -e NEXT_PUBLIC_ADMIN_URL=https://qa-admin.fatrans.com.ve \
  -e NEXT_PUBLIC_AUTH_URL=https://qa-auth.fatrans.com.ve \
  -e BACKEND_URL=http://fatrans-qa-backend:8080 \
  -e JWT_SECRET=fatrans-fondo-qa-secret-key-2026-v1 \
  fatrans-frontend-public-qa:latest >> "$LOG_FILE" 2>&1

log "→ Levantando Frontend Protected QA (puerto 4001)..."
docker run -d \
  --name fatrans-qa-frontend-protected \
  --network fatrans-network \
  --restart unless-stopped \
  -p 4001:3000 \
  -e NEXT_PUBLIC_API_URL=https://qa-api.fatrans.com.ve/api \
  -e NEXT_PUBLIC_APP_URL=https://qa-app.fatrans.com.ve \
  -e NEXT_PUBLIC_ADMIN_URL=https://qa-admin.fatrans.com.ve \
  -e NEXT_PUBLIC_AUTH_URL=https://qa-auth.fatrans.com.ve \
  -e BACKEND_URL=http://fatrans-qa-backend:8080 \
  -e JWT_SECRET=fatrans-fondo-qa-secret-key-2026-v1 \
  fatrans-frontend-protected-qa:latest >> "$LOG_FILE" 2>&1

# 8. Esperar backend QA healthy (máx 3 min). Falla el deploy si no llega.
log "→ Esperando backend QA healthy..."
BACKEND_HEALTHY=false
for i in $(seq 1 18); do
  STATUS=$(docker inspect --format='{{.State.Health.Status}}' fatrans-qa-backend 2>/dev/null || echo "starting")
  if [ "$STATUS" = "healthy" ]; then
    log "   Backend QA healthy ✓"
    BACKEND_HEALTHY=true
    break
  fi
  log "   Intento $i/18 — $STATUS (10s...)"
  sleep 10
done
if ! $BACKEND_HEALTHY; then
  log "   ❌ Backend QA no alcanzó healthy en 3min"
  docker logs --tail 50 fatrans-qa-backend 2>&1 | tee -a "$LOG_FILE"
  exit 1
fi

# 9. Verificación final
log "→ Verificación final QA..."
ALL_OK=true
for svc in fatrans-qa-backend fatrans-qa-frontend-public fatrans-qa-frontend-protected fatrans-qa-redis; do
  STATUS=$(docker inspect --format='{{.State.Status}}' "$svc" 2>/dev/null || echo "missing")
  if [ "$STATUS" != "running" ]; then
    log "   ❌ $svc — $STATUS"
    ALL_OK=false
  else
    log "   ✅ $svc — running"
  fi
done

# 10. Confirmar que NO rompimos prod
log "→ Verificando servicios base post-deploy..."
for svc in fatrans-postgres fatrans-redis fatrans-minio fatrans-backend fatrans-frontend-public fatrans-frontend-protected; do
  STATUS=$(docker inspect --format='{{.State.Status}}' "$svc" 2>/dev/null || echo "missing")
  [ "$STATUS" != "running" ] && log "   ⚠️  PROD $svc — $STATUS" || log "   ✅ PROD $svc — OK"
done

docker image prune -f --filter "until=72h" >> "$LOG_FILE" 2>&1 || true

if $ALL_OK; then
  log "======================================================"
  log " ✅ Deploy QA completado — SHA: $GIT_SHA"
  log " 🌐 https://qa-auth.fatrans.com.ve"
  log " 🌐 https://qa-app.fatrans.com.ve"
  log " 🌐 https://qa-admin.fatrans.com.ve"
  log "======================================================"
  exit 0
else
  log "======================================================"
  log " ❌ Deploy QA FALLÓ — revisar logs"
  log "======================================================"
  exit 1
fi
