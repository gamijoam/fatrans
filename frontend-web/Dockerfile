FROM node:18-alpine AS builder
WORKDIR /app

ARG NEXT_PUBLIC_API_URL=https://api.fatrans.com.ve
ARG NEXT_PUBLIC_APP_URL=https://app.fatrans.com.ve
ARG NEXT_PUBLIC_ADMIN_URL=https://admin.fatrans.com.ve

ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL
ENV NEXT_PUBLIC_APP_URL=$NEXT_PUBLIC_APP_URL
ENV NEXT_PUBLIC_ADMIN_URL=$NEXT_PUBLIC_ADMIN_URL

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

EXPOSE 3000

CMD ["node", "server.js"]
