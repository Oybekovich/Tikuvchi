import { formatCurrency } from "@/lib/format";
import { t } from "@/lib/i18n";

type Props = {
  amount: number;
  /** true bo'lsa "… so'm dan boshlab" ko'rinishida */
  from?: boolean;
  size?: "sm" | "md" | "lg";
  className?: string;
};

const SIZES = {
  sm: "text-sm",
  md: "text-base",
  lg: "text-xl",
} as const;

export default function PriceTag({
  amount,
  from = false,
  size = "md",
  className = "",
}: Props) {
  return (
    <span
      className={`whitespace-nowrap font-extrabold text-terra-700 ${SIZES[size]} ${className}`}
    >
      {formatCurrency(amount)}
      {from && (
        <span className="ml-1 text-xs font-medium text-ink-500">
          {t("common.from")}
        </span>
      )}
    </span>
  );
}
