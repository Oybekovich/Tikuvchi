import type { Metadata } from "next";
import OfflineRetry from "@/components/OfflineRetry";
import { PhWifiOff } from "@/components/PhosphorIcons";
import { t } from "@/lib/i18n";

export const metadata: Metadata = { title: "Internet aloqasi yo'q" };

/** Service worker tarmoq bo'lmaganda shu sahifani ko'rsatadi */
export default function OfflinePage() {
  return (
    <main className="mx-auto flex min-h-dvh max-w-md flex-col items-center justify-center gap-4 px-6 text-center">
      <span className="flex h-20 w-20 items-center justify-center rounded-full bg-terra-50 text-terra-400">
        <PhWifiOff size={36} />
      </span>
      <h1 className="text-xl font-extrabold text-ink-900">
        {t("offline.title")}
      </h1>
      <p className="text-sm text-ink-500">{t("offline.message")}</p>
      <OfflineRetry />
    </main>
  );
}
