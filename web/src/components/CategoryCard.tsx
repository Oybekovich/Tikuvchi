import Link from "next/link";
import type { ComponentType, SVGProps } from "react";
import {
  PhKechki,
  PhKundalik,
  PhMilliy,
  PhMuslima,
  PhNeedle,
  PhToy,
} from "@/components/PhosphorIcons";

type IconComponent = ComponentType<SVGProps<SVGSVGElement> & { size?: number }>;

/** Android ilovadagi CategoryIcons bilan bir xil moslik. */
const ICONS: Record<string, IconComponent> = {
  milliy: PhMilliy,
  kechki: PhKechki,
  kundalik: PhKundalik,
  muslima: PhMuslima,
  toy: PhToy,
};

type Props = {
  category: { id: number; name: string; icon: string | null };
};

export default function CategoryCard({ category }: Props) {
  // Yangi kategoriya qo'shilib, ikonkasi hali tanlanmagan bo'lishi mumkin
  const Icon = ICONS[category.icon ?? ""] ?? PhNeedle;

  return (
    <Link
      href={`/search?category=${category.id}`}
      className="flex w-24 shrink-0 flex-col items-center gap-2 rounded-2xl bg-white p-3 shadow-card transition-transform hover:-translate-y-0.5 active:translate-y-0"
    >
      <span className="flex h-12 w-12 items-center justify-center rounded-full bg-terra-50 text-terra-600">
        <Icon size={24} />
      </span>
      <span className="text-center text-xs font-semibold leading-tight text-ink-700">
        {category.name}
      </span>
    </Link>
  );
}
