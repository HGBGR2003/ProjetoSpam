import type { ClassificationResult, SpamClass } from "./types/spam";

const MOCK_DELAY_MS = 1800;

type ApiResponse = {
  classe: string;
  probabilidade_spam: number;
  probabilidade_ham: number;
};

function normalizeClasse(classe: string): SpamClass {
  const upper = classe.toUpperCase();
  if (upper === "SPAM") return "SPAM";
  return "HAM";
}

function toClassificationResult(data: ApiResponse): ClassificationResult {
  const probabilidadeSpam = data.probabilidade_spam;
  const probabilidadeHam = data.probabilidade_ham;
  const classe = normalizeClasse(data.classe);

  return {
    classe,
    probabilidadeSpam,
    probabilidadeHam,
  };
}

function mockAnalyze(conteudo: string): Promise<ClassificationResult> {
  return new Promise((resolve) => {
    setTimeout(() => {
      const spamKeywords = [
        "grátis",
        "promoção",
        "clique aqui",
        "ganhe",
        "urgente",
        "oferta",
        "viagra",
        "prêmio",
      ];
      const lower = conteudo.toLowerCase();
      const hits = spamKeywords.filter((k) => lower.includes(k)).length;
      const baseSpam = Math.min(0.15 + hits * 0.18, 0.95);
      const probabilidadeSpam =
        hits > 0 ? baseSpam : 0.12 + (conteudo.length % 17) / 100;
      const probabilidadeHam = 1 - probabilidadeSpam;
      const classe: SpamClass =
        probabilidadeSpam >= probabilidadeHam ? "SPAM" : "HAM";

      resolve({
        classe,
        probabilidadeSpam,
        probabilidadeHam,
      });
    }, MOCK_DELAY_MS);
  });
}

async function apiAnalyze(conteudo: string): Promise<ClassificationResult> {
  const baseUrl = process.env.NEXT_PUBLIC_SPAM_API_URL?.replace(/\/$/, "");
  if (!baseUrl) {
    return mockAnalyze(conteudo);
  }

  const response = await fetch(`${baseUrl}/api/classify`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ text: conteudo }),
  });

  if (!response.ok) {
    throw new Error(
      `Falha na análise (${response.status}). Verifique se o backend está em execução.`,
    );
  }

  const data = (await response.json()) as ApiResponse;
  return toClassificationResult(data);
}

export async function analyzeEmail(
  conteudo: string,
): Promise<ClassificationResult> {
  const apiUrl = process.env.NEXT_PUBLIC_SPAM_API_URL;
  if (!apiUrl) {
    return mockAnalyze(conteudo);
  }
  return apiAnalyze(conteudo);
}
