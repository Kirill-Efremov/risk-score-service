export function LoadingState({ label = "Loading..." }: { label?: string }) {
  return (
    <div className="panel flex min-h-40 items-center justify-center px-6 py-10 text-sm text-slate-500">
      {label}
    </div>
  );
}
