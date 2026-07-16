import { t } from "@/lib/i18n";

const FALLBACK_COLORS = [
  "#c0674a",
  "#7b4b94",
  "#2f5d50",
  "#b08d3d",
  "#5b6b84",
  "#a05a44",
  "#48695e",
  "#8a5a83",
];

const SIZES = {
  sm: "h-8 w-8 text-xs",
  md: "h-10 w-10 text-sm",
  lg: "h-14 w-14 text-lg",
  xl: "h-20 w-20 text-2xl",
} as const;

function colorFor(name: string): string {
  let hash = 0;
  for (let i = 0; i < name.length; i++) {
    hash = (hash * 31 + name.charCodeAt(i)) | 0;
  }
  return FALLBACK_COLORS[Math.abs(hash) % FALLBACK_COLORS.length];
}

type Props = {
  name: string;
  src?: string | null;
  size?: keyof typeof SIZES;
  className?: string;
};

/**
 * Rasm bo'lmasa hech qachon bo'sh "img" bloki ko'rsatilmaydi:
 * ismning bosh harfi + ismdan hosil qilingan rangli fon chiqadi.
 */
export default function Avatar({ name, src, size = "md", className = "" }: Props) {
  const sizeCls = SIZES[size];

  if (src) {
    return (
      // eslint-disable-next-line @next/next/no-img-element
      <img
        src={src}
        alt={t("avatar.alt", { name })}
        className={`${sizeCls} shrink-0 rounded-full object-cover ${className}`}
      />
    );
  }

  return (
    <span
      aria-label={t("avatar.alt", { name })}
      role="img"
      className={`${sizeCls} shrink-0 select-none rounded-full font-bold text-white inline-flex items-center justify-center ${className}`}
      style={{ backgroundColor: colorFor(name) }}
    >
      {name.trim().charAt(0).toUpperCase() || "•"}
    </span>
  );
}
