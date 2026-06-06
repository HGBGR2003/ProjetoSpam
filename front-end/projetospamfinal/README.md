This is a [Next.js](https://nextjs.org) project bootstrapped with [`create-next-app`](https://nextjs.org/docs/app/api-reference/cli/create-next-app).

## Conexão com o backend

1. Copie `.env.example` para `.env.local`:
   ```bash
   cp .env.example .env.local
   ```
2. Ajuste a URL se necessário:
   ```env
   NEXT_PUBLIC_SPAM_API_URL=http://localhost:8080
   ```
3. Inicie o backend Spring Boot na porta 8080 (com modelo treinado).
4. Reinicie o servidor de desenvolvimento do Next.js após alterar variáveis de ambiente.

A interface chama `POST /api/classify` com `{ "text": "..." }`. Se o backend estiver offline, um alerta fixo aparece no rodapé da página.

Para testar sem backend, defina `NEXT_PUBLIC_USE_MOCK=true` no `.env.local`.

## Getting Started

First, run the development server:

```bash
npm run dev
# or
yarn dev
# or
pnpm dev
# or
bun dev
```

Open [http://localhost:3000](http://localhost:3000) with your browser to see the result.

You can start editing the page by modifying `app/page.tsx`. The page auto-updates as you edit the file.

This project uses [`next/font`](https://nextjs.org/docs/app/building-your-application/optimizing/fonts) to automatically optimize and load [Geist](https://vercel.com/font), a new font family for Vercel.

## Learn More

To learn more about Next.js, take a look at the following resources:

- [Next.js Documentation](https://nextjs.org/docs) - learn about Next.js features and API.
- [Learn Next.js](https://nextjs.org/learn) - an interactive Next.js tutorial.

You can check out [the Next.js GitHub repository](https://github.com/vercel/next.js) - your feedback and contributions are welcome!

## Deploy on Vercel

The easiest way to deploy your Next.js app is to use the [Vercel Platform](https://vercel.com/new?utm_medium=default-template&filter=next.js&utm_source=create-next-app&utm_campaign=create-next-app-readme) from the creators of Next.js.

Check out our [Next.js deployment documentation](https://nextjs.org/docs/app/building-your-application/deploying) for more details.
