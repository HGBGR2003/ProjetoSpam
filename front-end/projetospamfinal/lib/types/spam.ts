export type ClassificationRequest = {
  text: string;
};

export type ClassificationResponse = {
  classe: string;
  probabilidade_spam: number;
  probabilidade_ham: number;
};

export type SpamClass = "SPAM" | "HAM";

export type ClassificationResult = {
  classe: SpamClass;
  probabilidadeSpam: number;
  probabilidadeHam: number;
};
