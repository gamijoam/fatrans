#!/bin/sh
# =============================================================
# docker-entrypoint.sh
# Reemplaza las URLs horneadas en el build de Next.js con las
# variables de entorno reales del contenedor en runtime.
# Esto permite usar la misma imagen para QA y Producción.
# =============================================================

set -e

echo "[entrypoint] Configurando URLs del entorno..."

# URLs horneadas en build (valores por defecto del Dockerfile)
BUILD_API_URL="https://api.fatrans.com.ve/api"
BUILD_APP_URL="https://app.fatrans.com.ve"
BUILD_ADMIN_URL="https://admin.fatrans.com.ve"
BUILD_AUTH_URL="https://auth.fatrans.com.ve"

# URLs reales del entorno (variables de entorno del contenedor)
RUNTIME_API_URL="${NEXT_PUBLIC_API_URL:-$BUILD_API_URL}"
RUNTIME_APP_URL="${NEXT_PUBLIC_APP_URL:-$BUILD_APP_URL}"
RUNTIME_ADMIN_URL="${NEXT_PUBLIC_ADMIN_URL:-$BUILD_ADMIN_URL}"
RUNTIME_AUTH_URL="${NEXT_PUBLIC_AUTH_URL:-$BUILD_AUTH_URL}"

echo "[entrypoint] API:   $RUNTIME_API_URL"
echo "[entrypoint] APP:   $RUNTIME_APP_URL"
echo "[entrypoint] ADMIN: $RUNTIME_ADMIN_URL"
echo "[entrypoint] AUTH:  $RUNTIME_AUTH_URL"

# Solo reemplazar si las URLs son distintas al build
if [ "$RUNTIME_AUTH_URL" != "$BUILD_AUTH_URL" ] || \
   [ "$RUNTIME_API_URL" != "$BUILD_API_URL" ] || \
   [ "$RUNTIME_APP_URL" != "$BUILD_APP_URL" ] || \
   [ "$RUNTIME_ADMIN_URL" != "$BUILD_ADMIN_URL" ]; then

  echo "[entrypoint] URLs difieren del build — aplicando reemplazo en archivos compilados..."

  # Reemplazar en todos los chunks de Next.js
  find /app/.next/static/chunks -name "*.js" -type f | while read file; do
    sed -i \
      "s|$BUILD_API_URL|$RUNTIME_API_URL|g; \
       s|$BUILD_APP_URL|$RUNTIME_APP_URL|g; \
       s|$BUILD_ADMIN_URL|$RUNTIME_ADMIN_URL|g; \
       s|$BUILD_AUTH_URL|$RUNTIME_AUTH_URL|g" \
      "$file"
  done

  # Reemplazar también en el server bundle
  find /app/.next/server -name "*.js" -type f | while read file; do
    sed -i \
      "s|$BUILD_API_URL|$RUNTIME_API_URL|g; \
       s|$BUILD_APP_URL|$RUNTIME_APP_URL|g; \
       s|$BUILD_ADMIN_URL|$RUNTIME_ADMIN_URL|g; \
       s|$BUILD_AUTH_URL|$RUNTIME_AUTH_URL|g" \
      "$file"
  done

  echo "[entrypoint] Reemplazo completado ✓"
else
  echo "[entrypoint] URLs iguales al build — sin cambios necesarios"
fi

# Ejecutar el comando original (node server.js)
exec "$@"
