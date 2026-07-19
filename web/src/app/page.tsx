import { Suspense } from "react";
import AppHeader from "@/components/AppHeader";
import CategoryCard from "@/components/CategoryCard";
import EmptyState from "@/components/EmptyState";
import SearchBar from "@/components/SearchBar";
import UstaCard from "@/components/UstaCard";
import {
  PhNeedle,
} from "@/components/PhosphorIcons";
import { createClient } from "@/lib/supabase/server";
import { t } from "@/lib/i18n";

export default async function HomePage() {
  const supabase = await createClient();

  const [{ data: categories }, { data: ustas }] = await Promise.all([
    supabase
      .from("service_categories")
      .select("id, name, icon")
      .eq("gender_segment", "women")
      .order("id"),
    supabase
      .from("usta_profiles")
      .select(
        "user_id, district, cover_image_url, rating_avg, rating_count, tags, profiles!inner(full_name, avatar_url), usta_services(base_price)"
      )
      .order("rating_avg", { ascending: false }),
  ]);

  return (
    <>
      <AppHeader />
      <main className="mx-auto max-w-3xl px-4 pt-4">
        {/* Qidiruv paneli — header ostida, uni almashtirmaydi */}
        <Suspense>
          <SearchBar />
        </Suspense>

        {/* Kategoriyalar */}
        <section className="mt-6">
          <h2 className="mb-3 text-base font-extrabold text-ink-900">
            {t("home.categories")}
          </h2>
          <div className="no-scrollbar -mx-4 flex gap-3 overflow-x-auto px-4 pb-1">
            {(categories ?? []).map((c) => (
              <CategoryCard key={c.id} category={c} />
            ))}
          </div>
        </section>

        {/* Tavsiya etilgan ustalar */}
        <section className="mt-6 pb-6">
          <h2 className="mb-3 text-base font-extrabold text-ink-900">
            {t("home.featured")}
          </h2>
          {ustas && ustas.length > 0 ? (
            <div className="grid gap-3 sm:grid-cols-2">
              {ustas.map((u) => (
                <UstaCard key={u.user_id} usta={u} />
              ))}
            </div>
          ) : (
            <EmptyState icon={<PhNeedle size={30} />} title={t("home.emptyUstas")} />
          )}
        </section>
      </main>
    </>
  );
}
