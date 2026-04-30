FROM node:18-alpine AS builder
WORKDIR /app

# Estas variables se hornean en build time
# Los valores por defecto son de producción
# Para QA se pasan como --build-arg en el deploy
ARG NEXT_PUBLIC_API_URL=https://api.fatrans.com.ve/api
ARG NEXT_PUBLIC_APP_URL=https://app.fatrans.com.ve
ARG NEXT_PUBLIC_ADMIN_URL=https://admin.fatrans.com.ve
ARG NEXT_PUBLIC_AUTH_URL=https://auth.fatrans.com.ve

ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL
ENV NEXT_PUBLIC_APP_URL=$NEXT_PUBLIC_APP_URL
ENV NEXT_PUBLIC_ADMIN_URL=$NEXT_PUBLIC_ADMIN_URL
ENV NEXT_PUBLIC_AUTH_URL=$NEXT_PUBLIC_AUTH_URL

COPY package.json package-lock.json* ./
RUN npm ci

COPY . .
RUN npm run build

FROM node:18-alpine AS runner
WORKDIR /app

ENV NODE_ENV=production
ENV PORT=3000

COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static
COPY --from=builder /app/public ./public

# Script de entrypoint que reemplaza las URLs horneadas en build
# con las variables de entorno reales del contenedor en runtime
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

EXPOSE 3000

ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["node", "server.js"]
