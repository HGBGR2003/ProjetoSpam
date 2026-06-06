type BackendStatusBannerProps = {
  apiUrl: string;
};

export function BackendStatusBanner({ apiUrl }: BackendStatusBannerProps) {
  return (
    <div
      role="alert"
      aria-live="assertive"
      className="fixed bottom-0 left-0 right-0 z-50 border-t border-amber-700/60 bg-amber-950/95 px-4 py-3 text-center text-sm text-amber-100 shadow-lg"
    >
      Não foi possível conectar ao servidor de classificação. Verifique se o
      backend está rodando em{" "}
      <span className="font-mono text-amber-50">{apiUrl}</span>.
    </div>
  );
}
