import Link from "next/link";

const ICONS: Record<string, string> = {
  milliy: "🧵",
  kechki: "✨",
  kundalik: "👗",
  muslima: "🧕",
  toy: "💍",
};

type Props = {
  category: { id: number; name: string; icon: string | null };
};

export default function CategoryCard({ category }: Props) {
  return (
    <Link
      href={`/search?category=${category.id}`}
      className="flex w-24 shrink-0 flex-col items-center gap-2 rounded-2xl bg-white p-3 shadow-card transition-transform hover:-translate-y-0.5 active:translate-y-0"
    >
      <span className="flex h-12 w-12 items-center justify-center rounded-full bg-terra-50 text-2xl">
        {ICONS[category.icon ?? ""] ?? "🪡"}
      </span>
      <span className="text-center text-xs font-semibold leading-tight text-ink-700">
        {category.name}
      </span>
    </Link>
  );
}
