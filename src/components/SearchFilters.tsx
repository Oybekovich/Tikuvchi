"use client";

import { useRouter, useSearchParams } from "next/navigation";
import { useState } from "react";
import Button from "@/components/Button";
import { t } from "@/lib/i18n";

type Props = {
  categories: { id: number; name: string }[];
  districts: string[];
};

/** Qidiruv sahifasidagi filtrlar paneli (narx, reyting, tuman, kategoriya) */
export default function SearchFilters({ categories, districts }: Props) {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [open, setOpen] = useState(false);

  const [category, setCategory] = useState(searchParams.get("category") ?? "");
  const [district, setDistrict] = useState(searchParams.get("district") ?? "");
  const [minRating, setMinRating] = useState(searchParams.get("min_rating") ?? "");
  const [minPrice, setMinPrice] = useState(searchParams.get("min_price") ?? "");
  const [maxPrice, setMaxPrice] = useState(searchParams.get("max_price") ?? "");

  const activeCount = [category, district, minRating, minPrice, maxPrice].filter(
    Boolean
  ).length;

  function apply() {
    const params = new URLSearchParams(searchParams.toString());
    const entries: [string, string][] = [
      ["category", category],
      ["district", district],
      ["min_rating", minRating],
      ["min_price", minPrice],
      ["max_price", maxPrice],
    ];
    for (const [key, value] of entries) {
      if (value) params.set(key, value);
      else params.delete(key);
    }
    setOpen(false);
    router.push(`/search?${params.toString()}`);
  }

  function reset() {
    setCategory("");
    setDistrict("");
    setMinRating("");
    setMinPrice("");
    setMaxPrice("");
    const q = searchParams.get("q");
    setOpen(false);
    router.push(q ? `/search?q=${encodeURIComponent(q)}` : "/search");
  }

  const selectCls =
    "w-full rounded-xl border border-cream-200 bg-white px-3 py-2.5 text-sm text-ink-900 outline-none focus:border-terra-400";

  return (
    <div className="mt-3">
      <button
        onClick={() => setOpen(!open)}
        className="inline-flex items-center gap-2 rounded-xl border border-cream-200 bg-white px-4 py-2 text-sm font-bold text-ink-700 shadow-card hover:border-terra-300"
        aria-expanded={open}
      >
        {t("search.filters")}
        {activeCount > 0 && (
          <span className="flex h-5 w-5 items-center justify-center rounded-full bg-terra-600 text-[11px] font-bold text-white">
            {activeCount}
          </span>
        )}
      </button>

      {open && (
        <div className="mt-3 space-y-3 rounded-2xl bg-white p-4 shadow-card">
          <label className="block">
            <span className="mb-1 block text-xs font-bold text-ink-500">
              {t("search.category")}
            </span>
            <select
              value={category}
              onChange={(e) => setCategory(e.target.value)}
              className={selectCls}
            >
              <option value="">{t("common.all")}</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          </label>

          <label className="block">
            <span className="mb-1 block text-xs font-bold text-ink-500">
              {t("search.district")}
            </span>
            <select
              value={district}
              onChange={(e) => setDistrict(e.target.value)}
              className={selectCls}
            >
              <option value="">{t("common.all")}</option>
              {districts.map((d) => (
                <option key={d} value={d}>
                  {d}
                </option>
              ))}
            </select>
          </label>

          <label className="block">
            <span className="mb-1 block text-xs font-bold text-ink-500">
              {t("search.minRating")}
            </span>
            <select
              value={minRating}
              onChange={(e) => setMinRating(e.target.value)}
              className={selectCls}
            >
              <option value="">{t("common.all")}</option>
              <option value="4">4.0+</option>
              <option value="4.5">4.5+</option>
              <option value="4.8">4.8+</option>
            </select>
          </label>

          <div className="grid grid-cols-2 gap-3">
            <label className="block">
              <span className="mb-1 block text-xs font-bold text-ink-500">
                {t("search.priceFrom")}
              </span>
              <input
                type="number"
                inputMode="numeric"
                min={0}
                step={50000}
                value={minPrice}
                onChange={(e) => setMinPrice(e.target.value)}
                placeholder="0"
                className={selectCls}
              />
            </label>
            <label className="block">
              <span className="mb-1 block text-xs font-bold text-ink-500">
                {t("search.priceTo")}
              </span>
              <input
                type="number"
                inputMode="numeric"
                min={0}
                step={50000}
                value={maxPrice}
                onChange={(e) => setMaxPrice(e.target.value)}
                placeholder="5 000 000"
                className={selectCls}
              />
            </label>
          </div>

          <div className="flex gap-2 pt-1">
            <Button onClick={apply} className="flex-1">
              {t("search.apply")}
            </Button>
            <Button variant="ghost" onClick={reset}>
              {t("search.reset")}
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}
