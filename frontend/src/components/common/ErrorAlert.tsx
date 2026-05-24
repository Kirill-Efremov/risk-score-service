import { ApiError } from "../../api/client";

interface ErrorAlertProps {
  title?: string;
  message?: string | null;
  error?: ApiError | null;
}

export function ErrorAlert({ title, message, error }: ErrorAlertProps) {
  const resolvedTitle = title ?? (error ? "Ошибка запроса" : undefined);
  const resolvedMessage = message ?? error?.message;

  if (!resolvedMessage && !error) return null;

  return (
    <div className="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
      {resolvedTitle ? (
        <p className="font-semibold text-rose-800">{resolvedTitle}</p>
      ) : null}
      {error?.errorCode ? (
        <p className="mt-1 font-mono text-xs uppercase tracking-[0.12em] text-rose-700">
          {error.errorCode}
        </p>
      ) : null}
      {resolvedMessage ? <p className="mt-1">{resolvedMessage}</p> : null}
      {error?.details?.length ? (
        <ul className="mt-2 list-disc space-y-1 pl-5 text-xs">
          {error.details.map((detail, index) => (
            <li key={`${detail.field}-${index}`}>
              {detail.field}: {detail.message}
            </li>
          ))}
        </ul>
      ) : null}
    </div>
  );
}
