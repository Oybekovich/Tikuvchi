import { IconStar } from "@/components/Icons";
import { t } from "@/lib/i18n";

type Props = {
  rating: number;
  count?: number;
  /** overlay — hero-rasm ustida (yarim shaffof to'q fon), soft — oddiy joyda */
  variant?: "overlay" | "soft";
  className?: string;
};

export default function RatingBadge({
  rating,
  count,
  variant = "soft",
  className = "",
}: Props) {
  const value = Number(rating).toFixed(1);
  const styles =
    variant === "overlay"
      ? "bg-ink-900/70 text-white backdrop-blur-sm"
      : "bg-gold-100 text-ink-900";

  return (
    <span
      aria-label={t("rating.label", { rating: value, count: count ?? 0 })}
      className={`inline-flex items-center gap-1.5 rounded-full px-3 py-1.5 text-sm font-bold ${styles} ${className}`}
    >
      <IconStar size={15} className="text-gold-400" />
      {value}
      {count !== undefined && (
        <span
          className={`text-xs font-semibold ${
            variant === "overlay" ? "text-white/75" : "text-ink-500"
          }`}
        >
          ({count})
        </span>
      )}
    </span>
  );
}
