import Link from "next/link";
import type { ReactNode } from "react";

type Props = {
  icon: ReactNode;
  title: string;
  hint?: string;
  actionLabel?: string;
  actionHref?: string;
};

export default function EmptyState({
  icon,
  title,
  hint,
  actionLabel,
  actionHref,
}: Props) {
  return (
    <div className="flex flex-col items-center gap-3 rounded-2xl bg-white px-6 py-12 text-center shadow-card">
      <span className="flex h-16 w-16 items-center justify-center rounded-full bg-terra-50 text-3xl text-terra-400">
        {icon}
      </span>
      <p className="text-base font-bold text-ink-900">{title}</p>
      {hint && <p className="text-sm text-ink-500">{hint}</p>}
      {actionLabel && actionHref && (
        <Link
          href={actionHref}
          className="mt-2 rounded-xl bg-terra-600 px-5 py-2.5 text-sm font-bold text-white transition-colors hover:bg-terra-700"
        >
          {actionLabel}
        </Link>
      )}
    </div>
  );
}
