
FROM node:20-alpine AS builder

WORKDIR /app

COPY package.json yarn.lock ./
RUN yarn install --frozen-lockfile

COPY . .

ARG NEXT_PUBLIC_SPAM_API_URL=http://localhost:8080
ENV NEXT_PUBLIC_SPAM_API_URL=$NEXT_PUBLIC_SPAM_API_URL

RUN yarn build

FROM node:20-alpine

RUN apk add --no-cache wget

WORKDIR /app

ENV NODE_ENV=production

COPY --from=builder /app/public ./public
COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static

EXPOSE 3000

CMD ["node", "server.js"]
