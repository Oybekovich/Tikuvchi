import { t } from "@/lib/i18n";
import type { Enums } from "@/lib/database.types";

const STATUS_STYLES: Record<Enums<"order_status">, string> = {
  pending: "bg-gold-100 text-ink-700",
  accepted: "bg-terra-100 text-terra-800",
  in_progress: "bg-terra-100 text-terra-800",
  ready: "bg-green-100 text-green-800",
  completed: "bg-cream-200 text-ink-700",
  cancelled: "bg-red-50 text-red-700",
};

export default function StatusChip({
  status,
}: {
  status: Enums<"order_status">;
}) {
  return (
    <span
      className={`inline-flex whitespace-nowrap rounded-full px-3 py-1 text-xs font-bold ${STATUS_STYLES[status]}`}
    >
      {t(`orderStatus.${status}`)}
    </span>
  );
}
