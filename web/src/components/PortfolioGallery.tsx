"use client";

import { useState } from "react";
import Link from "next/link";
import { PhX } from "@/components/PhosphorIcons";
import { t } from "@/lib/i18n";

interface PortfolioItem {
  id: number;
  image_url: string;
  caption: string | null;
}

export default function PortfolioGallery({
  items,
  ustaId,
  ustaName,
}: {
  items: PortfolioItem[];
  ustaId: string;
  ustaName: string;
}) {
  const [selectedItem, setSelectedItem] = useState<PortfolioItem | null>(null);

  return (
    <>
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
        {items.map((item) => (
          <figure
            key={item.id}
            onClick={() => setSelectedItem(item)}
            className="group cursor-pointer overflow-hidden rounded-2xl bg-white shadow-card transition-transform active:scale-[0.98]"
          >
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img
              src={item.image_url}
              alt={item.caption ?? ustaName}
              loading="lazy"
              className="aspect-[3/4] w-full object-cover transition-transform duration-300 group-hover:scale-105"
            />
            {item.caption && (
              <figcaption className="px-3 py-2 text-xs font-medium text-ink-500 line-clamp-1">
                {item.caption}
              </figcaption>
            )}
          </figure>
        ))}
      </div>

      {selectedItem && (
        <div
          className="fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-black/60 p-0 sm:p-4 animate-in fade-in duration-200"
          onClick={() => setSelectedItem(null)}
        >
          <div
            className="relative w-full max-w-lg rounded-t-3xl sm:rounded-3xl bg-white p-5 shadow-xl max-h-[90vh] overflow-y-auto"
            onClick={(e) => e.stopPropagation()}
          >
            <button
              onClick={() => setSelectedItem(null)}
              className="absolute right-4 top-4 flex h-8 w-8 items-center justify-center rounded-full bg-cream-200 text-ink-700 hover:bg-cream-300"
            >
              <PhX size={18} />
            </button>

            <div className="overflow-hidden rounded-2xl bg-cream-100">
              {/* eslint-disable-next-line @next/next/no-img-element */}
              <img
                src={selectedItem.image_url}
                alt={selectedItem.caption ?? ustaName}
                className="aspect-[3/4] w-full object-cover"
              />
            </div>

            <div className="mt-4">
              <h3 className="text-lg font-extrabold text-ink-900">
                {selectedItem.caption || t("usta.portfolioItemFallback", { name: ustaName })}
              </h3>
              <p className="mt-1 text-sm text-ink-500">
                {t("usta.portfolioItemHint")}
              </p>
            </div>

            <div className="mt-5">
              <Link
                href={`/usta/${ustaId}/buyurtma`}
                className="flex items-center justify-center w-full rounded-2xl bg-terra-600 px-5 py-3.5 text-base font-bold text-white shadow-lg transition-colors hover:bg-terra-700 active:bg-terra-800"
              >
                {t("usta.orderCta")}
              </Link>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
