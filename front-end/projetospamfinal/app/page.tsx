"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import {
  FileText,
  Loader2,
  ShieldAlert,
  ShieldCheck,
  Upload,
} from "lucide-react";
import { BackendStatusBanner } from "@/components/BackendStatusBanner";
import { checkBackendHealth, classifyEmail } from "@/lib/spam-api";
import type { ClassificationResult } from "@/lib/types/spam";

const HEALTH_CHECK_INTERVAL_MS = 30_000;

function formatPercent(value: number): string {
  return `${(value * 100).toFixed(2)}%`;
}

function ProbabilityBar({
  label,
  value,
  barClass,
}: {
  label: string;
  value: number;
  barClass: string;
}) {
  const percent = Math.round(value * 100);

  return (
    <div className="space-y-2">
      <div className="flex justify-between text-sm text-zinc-300">
        <span>{label}</span>
        <span className="font-mono text-zinc-100">{formatPercent(value)}</span>
      </div>
      <div className="h-2 overflow-hidden rounded-full bg-zinc-700">
        <div
          className={`h-full rounded-full transition-[width] ${barClass}`}
          style={{ width: `${percent}%` }}
        />
      </div>
    </div>
  );
}

export default function Home() {
  const inputRef = useRef<HTMLInputElement>(null);
  const apiUrl = process.env.NEXT_PUBLIC_SPAM_API_URL?.replace(/\/$/, "");

  const [nomeArquivo, setNomeArquivo] = useState<string | null>(null);
  const [conteudoEmail, setConteudoEmail] = useState("");
  const [carregando, setCarregando] = useState(false);
  const [resultado, setResultado] = useState<ClassificationResult | null>(null);
  const [erro, setErro] = useState<string | null>(null);
  const [backendOffline, setBackendOffline] = useState(false);

  useEffect(() => {
    if (!apiUrl) {
      setBackendOffline(false);
      return;
    }

    let cancelled = false;

    async function runHealthCheck() {
      const healthy = await checkBackendHealth();
      if (!cancelled) {
        setBackendOffline(!healthy);
      }
    }

    runHealthCheck();
    const intervalId = setInterval(runHealthCheck, HEALTH_CHECK_INTERVAL_MS);

    return () => {
      cancelled = true;
      clearInterval(intervalId);
    };
  }, [apiUrl]);

  function handleSelecionarArquivo() {
    inputRef.current?.click();
  }

  function handleArquivoChange(event: React.ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    if (!file) return;

    const isTxt =
      file.name.toLowerCase().endsWith(".txt") ||
      file.type === "text/plain" ||
      file.type === "";

    if (!isTxt) {
      setErro("Selecione apenas arquivos .txt.");
      event.target.value = "";
      return;
    }

    const reader = new FileReader();
    reader.onload = () => {
      const text = typeof reader.result === "string" ? reader.result : "";
      setConteudoEmail(text);
      setNomeArquivo(file.name);
      setResultado(null);
      setErro(null);
    };
    reader.onerror = () => {
      setErro("Não foi possível ler o arquivo. Tente novamente.");
    };
    reader.readAsText(file, "UTF-8");
    event.target.value = "";
  }

  function handleConteudoChange(event: React.ChangeEvent<HTMLTextAreaElement>) {
    setConteudoEmail(event.target.value);
    setNomeArquivo(null);
    setResultado(null);
    setErro(null);
  }

  const handleAnalisar = useCallback(async () => {
    const texto = conteudoEmail.trim();
    if (!texto || carregando) return;

    setCarregando(true);
    setErro(null);
    setResultado(null);

    try {
      const res = await classifyEmail(texto);
      setResultado(res);
    } catch (e) {
      setErro(
        e instanceof Error
          ? e.message
          : "Erro inesperado ao analisar o e-mail.",
      );
    } finally {
      setCarregando(false);
    }
  }, [conteudoEmail, carregando]);

  const podeAnalisar = conteudoEmail.trim().length > 0 && !carregando;
  const isSpam = resultado?.classe === "SPAM";
  const showBackendBanner = Boolean(apiUrl && backendOffline);

  return (
    <div className="min-h-screen bg-zinc-900 text-zinc-100">
      <main
        className={`mx-auto flex min-h-screen max-w-3xl flex-col gap-8 px-6 py-12 md:px-8 ${
          showBackendBanner ? "pb-24" : ""
        }`}
      >
        <header className="space-y-2">
          <div className="flex items-center gap-3 text-zinc-400">
            <FileText className="h-6 w-6" aria-hidden />
            <span className="text-sm font-medium uppercase tracking-wide">
              Redes Bayesianas
            </span>
          </div>
          <h1 className="text-3xl font-semibold tracking-tight text-zinc-100">
            Detecção de Spam
          </h1>
          <p className="text-zinc-400">
            Carregue um arquivo .txt ou digite/cole o texto do e-mail abaixo e
            execute a classificação.
          </p>
        </header>

        <section className="space-y-4 rounded-xl border border-zinc-700 bg-zinc-800/50 p-6">
          <input
            ref={inputRef}
            type="file"
            accept=".txt,text/plain"
            className="hidden"
            aria-label="Upload de arquivo de e-mail"
            onChange={handleArquivoChange}
          />

          <button
            type="button"
            onClick={handleSelecionarArquivo}
            className="inline-flex items-center gap-2 rounded-lg border border-zinc-600 bg-zinc-800 px-4 py-2.5 text-sm font-medium text-zinc-100 transition-colors hover:bg-zinc-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-zinc-400"
          >
            <Upload className="h-4 w-4" aria-hidden />
            Carregar arquivo .txt
          </button>

          {nomeArquivo && (
            <p className="text-sm text-zinc-400">
              Arquivo:{" "}
              <span className="font-mono text-zinc-300">{nomeArquivo}</span>
            </p>
          )}

          <label className="block space-y-2">
            <span className="text-sm text-zinc-400">
              Texto do e-mail
            </span>
            <textarea
              value={conteudoEmail}
              onChange={handleConteudoChange}
              placeholder="Digite, cole ou carregue um arquivo .txt com o conteúdo do e-mail"
              rows={12}
              className="w-full resize-y rounded-lg border border-zinc-600 bg-zinc-900/80 px-4 py-3 font-mono text-sm leading-relaxed text-zinc-100 placeholder:text-zinc-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-zinc-400"
            />
          </label>
        </section>

        <div className="flex flex-col gap-4 sm:flex-row sm:items-center">
          <button
            type="button"
            onClick={handleAnalisar}
            disabled={!podeAnalisar}
            className="inline-flex items-center justify-center gap-2 rounded-lg bg-zinc-100 px-6 py-3 text-sm font-semibold text-zinc-900 transition-colors hover:bg-zinc-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-zinc-400 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {carregando ? (
              <>
                <Loader2 className="h-4 w-4 animate-spin" aria-hidden />
                Analisando...
              </>
            ) : (
              "Analisar Email"
            )}
          </button>
        </div>

        {erro && (
          <p
            role="alert"
            className="rounded-lg border border-rose-800/60 bg-rose-950/40 px-4 py-3 text-sm text-rose-200"
          >
            {erro}
          </p>
        )}

        {resultado && (
          <section
            className="space-y-6 rounded-xl border border-zinc-700 bg-zinc-800/50 p-6"
            aria-live="polite"
          >
            <div className="flex items-start gap-4">
              {isSpam ? (
                <ShieldAlert
                  className="h-8 w-8 shrink-0 text-rose-400"
                  aria-hidden
                />
              ) : (
                <ShieldCheck
                  className="h-8 w-8 shrink-0 text-emerald-400"
                  aria-hidden
                />
              )}
              <div className="space-y-1">
                <p className="text-sm text-zinc-400">Classe final</p>
                <p
                  className={`text-2xl font-bold ${
                    isSpam ? "text-rose-400" : "text-emerald-400"
                  }`}
                >
                  {isSpam ? "SPAM" : "NÃO SPAM (HAM)"}
                </p>
              </div>
            </div>

            <div className="space-y-4 border-t border-zinc-700 pt-6">
              <h2 className="text-sm font-medium uppercase tracking-wide text-zinc-400">
                Probabilidades
              </h2>
              <ProbabilityBar
                label="P(SPAM)"
                value={resultado.probabilidadeSpam}
                barClass="bg-rose-500"
              />
              <ProbabilityBar
                label="P(HAM)"
                value={resultado.probabilidadeHam}
                barClass="bg-emerald-500"
              />
            </div>
          </section>
        )}
      </main>

      {showBackendBanner && apiUrl && (
        <BackendStatusBanner apiUrl={apiUrl} />
      )}
    </div>
  );
}
