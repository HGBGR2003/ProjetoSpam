import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // Gera saída standalone para o Docker (copia apenas o necessário para produção)
  output: "standalone",
};

export default nextConfig;
