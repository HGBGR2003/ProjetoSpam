import type {
  ClassificationRequest,
  ClassificationResponse,
  ClassificationResult,
  SpamClass,
} from "./types/spam";

const MOCK_DELAY_MS = 1800;
const HEALTH_CHECK_TIMEOUT_MS = 5_000;

const CONNECTION_ERROR_MESSAGE =
  "Não foi possível conectar ao servidor. Verifique se o backend está em execução.";

const MISSING_API_URL_MESSAGE =
  "NEXT_PUBLIC_SPAM_API_URL não está configurada. Copie .env.example para .env.local.";

function getBaseUrl(): string | undefined {
  return process.env.NEXT_PUBLIC_SPAM_API_URL?.replace(/\/$/, "");
}

function isMockEnabled(): boolean {
  return process.env.NEXT_PUBLIC_USE_MOCK === "true";
}

function normalizeClasse(classe: string): SpamClass {
  const upper = classe.toUpperCase();
  if (upper === "SPAM") return "SPAM";
  return "HAM";
}

function toClassificationResult(
  data: ClassificationResponse,
): ClassificationResult {
  return {
    classe: normalizeClasse(data.classe),
    probabilidadeSpam: data.probabilidade_spam,
    probabilidadeHam: data.probabilidade_ham,
  };
}

function mapHttpError(status: number): string {
  if (status === 503) {
    return "Modelo ainda não treinado. Execute o treinamento no backend.";
  }
  if (status === 400) {
    return "Texto inválido para análise.";
  }
  return `Falha na análise (${status}).`;
}

async function parseErrorMessage(response: Response): Promise<string> {
  try {
    const body = (await response.json()) as { message?: string };
    if (body.message) {
      return body.message;
    }
  } catch {
    // ignore JSON parse errors
  }
  return mapHttpError(response.status);
}

function mockClassify(text: string): Promise<ClassificationResult> {
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
      const lower = text.toLowerCase();
      const hits = spamKeywords.filter((k) => lower.includes(k)).length;
      const baseSpam = Math.min(0.15 + hits * 0.18, 0.95);
      const probabilidadeSpam =
        hits > 0 ? baseSpam : 0.12 + (text.length % 17) / 100;
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

export async function checkBackendHealth(): Promise<boolean> {
  const baseUrl = getBaseUrl();
  if (!baseUrl) {
    return false;
  }

  const controller = new AbortController();
  const timeoutId = setTimeout(
    () => controller.abort(),
    HEALTH_CHECK_TIMEOUT_MS,
  );

  try {
    const response = await fetch(`${baseUrl}/api/model/train/latest`, {
      method: "GET",
      signal: controller.signal,
    });
    return response.ok || response.status === 404;
  } catch {
    return false;
  } finally {
    clearTimeout(timeoutId);
  }
}

export async function classifyEmail(text: string): Promise<ClassificationResult> {
  const baseUrl = getBaseUrl();

  if (!baseUrl) {
    if (isMockEnabled()) {
      return mockClassify(text);
    }
    throw new Error(MISSING_API_URL_MESSAGE);
  }

  const payload: ClassificationRequest = { text };

  let response: Response;
  try {
    response = await fetch(`${baseUrl}/api/classify`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });
  } catch {
    throw new Error(CONNECTION_ERROR_MESSAGE);
  }

  if (!response.ok) {
    throw new Error(await parseErrorMessage(response));
  }

  const data = (await response.json()) as ClassificationResponse;
  return toClassificationResult(data);
}

/** @deprecated Use classifyEmail */
export async function analyzeEmail(text: string): Promise<ClassificationResult> {
  return classifyEmail(text);
}
