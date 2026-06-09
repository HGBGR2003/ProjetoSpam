# ─────────────────────────────────────────────
# Stage 1: build do Next.js
# ─────────────────────────────────────────────
FROM node:20-alpine AS builder

WORKDIR /app

# Copia manifesto e instala dependências
COPY package.json yarn.lock ./
RUN yarn install --frozen-lockfile

# Copia o restante e faz o build de produção
COPY . .

# A URL do backend é injetada em build-time via ARG → ENV
ARG NEXT_PUBLIC_SPAM_API_URL=http://localhost:8080
ENV NEXT_PUBLIC_SPAM_API_URL=$NEXT_PUBLIC_SPAM_API_URL

RUN yarn build

# ─────────────────────────────────────────────
# Stage 2: runtime mínimo
# ─────────────────────────────────────────────
FROM node:20-alpine

WORKDIR /app

ENV NODE_ENV=production

COPY --from=builder /app/public ./public
COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static

EXPOSE 3000

CMD ["node", "server.js"]
