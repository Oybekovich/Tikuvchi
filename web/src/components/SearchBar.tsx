"use client";

import { useRouter, useSearchParams } from "next/navigation";
import { useState } from "react";
import { IconSearch } from "@/components/Icons";
import { t } from "@/lib/i18n";

/**
 * Bosh sahifadagi (header ostidagi) qidiruv paneli.
 * Header'ni almashtirmaydi — natijalar /search sahifasida ochiladi.
 */
export default function SearchBar() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [query, setQuery] = useState(searchParams.get("q") ?? "");

  function submit(e: React.FormEvent) {
    e.preventDefault();
    const params = new URLSearchParams(searchParams.toString());
    if (query.trim()) {
      params.set("q", query.trim());
    } else {
      params.delete("q");
    }
    router.push(`/search?${params.toString()}`);
  }

  return (
    <form onSubmit={submit} role="search" className="relative">
      <IconSearch
        size={18}
        className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-ink-500"
      />
      <input
        type="search"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        placeholder={t("home.searchPlaceholder")}
        className="w-full rounded-2xl border border-cream-200 bg-white py-3 pl-11 pr-4 text-sm text-ink-900 shadow-card outline-none placeholder:text-ink-300 focus:border-terra-400"
      />
    </form>
  );
}
