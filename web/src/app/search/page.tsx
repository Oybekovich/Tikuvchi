import { Suspense } from "react";
import type { Metadata } from "next";
import AppHeader from "@/components/AppHeader";
import EmptyState from "@/components/EmptyState";
import SearchBar from "@/components/SearchBar";
import SearchFilters from "@/components/SearchFilters";
import UstaCard, { type UstaCardData } from "@/components/UstaCard";
import {
  PhSearchOff,
} from "@/components/PhosphorIcons";
import { createClient } from "@/lib/supabase/server";
import { t } from "@/lib/i18n";

export const metadata: Metadata = { title: "Qidiruv" };

type SearchParams = {
  q?: string;
  category?: string;
  district?: string;
  min_rating?: string;
  min_price?: string;
  max_price?: string;
};

type UstaRow = Omit<UstaCardData, "usta_services"> & {
  bio: string | null;
  usta_services: { base_price: number; category_id: number | null }[];
};

function matches(usta: UstaRow, params: SearchParams): boolean {
  if (params.q) {
    const q = params.q.toLowerCase();
    const haystack = [
      usta.profiles.full_name,
      usta.district ?? "",
      usta.bio ?? "",
      ...usta.tags,
    ]
      .join(" ")
      .toLowerCase();
    if (!haystack.includes(q)) return false;
  }
  if (params.category) {
    const categoryId = Number(params.category);
    if (!usta.usta_services.some((s) => s.category_id === categoryId)) {
      return false;
    }
  }
  const min = params.min_price ? Number(params.min_price) : null;
  const max = params.max_price ? Number(params.max_price) : null;
  if (min !== null || max !== null) {
    const inRange = usta.usta_services.some(
      (s) =>
        (min === null || s.base_price >= min) &&
        (max === null || s.base_price <= max)
    );
    if (!inRange) return false;
  }
  return true;
}

export default async function SearchPage({
  searchParams,
}: {
  searchParams: Promise<SearchParams>;
}) {
  const params = await searchParams;
  const supabase = await createClient();

  let query = supabase
    .from("usta_profiles")
    .select(
      "user_id, district, bio, cover_image_url, rating_avg, rating_count, tags, profiles!inner(full_name, avatar_url), usta_services(base_price, category_id)"
    )
    .order("rating_avg", { ascending: false });

  if (params.district) query = query.eq("district", params.district);
  if (params.min_rating) query = query.gte("rating_avg", Number(params.min_rating));

  const [{ data: ustas }, { data: categories }, { data: districtRows }] =
    await Promise.all([
      query,
      supabase
        .from("service_categories")
        .select("id, name")
        .eq("gender_segment", "women")
        .order("id"),
      supabase
        .from("usta_profiles")
        .select("district")
        .not("district", "is", null),
    ]);

  const results = ((ustas ?? []) as UstaRow[]).filter((u) => matches(u, params));
  const districts = [
    ...new Set((districtRows ?? []).map((r) => r.district as string)),
  ].sort();

  return (
    <>
      <AppHeader back backHref="/" />
      <main className="mx-auto max-w-3xl px-4 pt-4 pb-6">
        <h1 className="sr-only">{t("search.title")}</h1>
        <Suspense>
          <SearchBar />
          <SearchFilters categories={categories ?? []} districts={districts} />
        </Suspense>

        <p className="mt-4 text-sm font-semibold text-ink-500">
          {t("search.found", { count: results.length })}
        </p>

        <div className="mt-3">
          {results.length > 0 ? (
            <div className="grid gap-3 sm:grid-cols-2">
              {results.map((u) => (
                <UstaCard key={u.user_id} usta={u} />
              ))}
            </div>
          ) : (
            <EmptyState
              icon={<PhSearchOff size={30} />}
              title={t("search.empty")}
              hint={t("search.emptyHint")}
            />
          )}
        </div>
      </main>
    </>
  );
}
