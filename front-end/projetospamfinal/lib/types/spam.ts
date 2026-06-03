export type SpamClass = "SPAM" | "HAM";

export type ClassificationResult = {
  classe: SpamClass;
  probabilidadeSpam: number;
  probabilidadeHam: number;
};
