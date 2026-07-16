import Link from "next/link";
import { notFound } from "next/navigation";
import AppHeader from "@/components/AppHeader";
import Avatar from "@/components/Avatar";
import EmptyState from "@/components/EmptyState";
import PriceTag from "@/components/PriceTag";
import RatingBadge from "@/components/RatingBadge";
import ReviewCard, { type ReviewData } from "@/components/ReviewCard";
import { IconChat, IconClock, IconLocation } from "@/components/Icons";
import { createClient } from "@/lib/supabase/server";
import { formatTime } from "@/lib/format";
import { t } from "@/lib/i18n";

export default async function UstaPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const supabase = await createClient();

  const { data: usta } = await supabase
    .from("usta_profiles")
    .select(
      `user_id, bio, cover_image_url, rating_avg, rating_count, location_text,
       district, work_hours_start, work_hours_end, tags,
       profiles!inner(full_name, avatar_url),
       usta_services(id, title, description, base_price),
       portfolio_items(id, image_url, caption, sort_order)`
    )
    .eq("user_id", id)
    .maybeSingle();

  if (!usta) notFound();

  const { data: reviews } = await supabase
    .from("reviews")
    .select("id, rating, comment, created_at, profiles!reviews_client_id_fkey(full_name, avatar_url)")
    .eq("usta_id", id)
    .order("created_at", { ascending: false });

  const portfolio = [...usta.portfolio_items].sort(
    (a, b) => a.sort_order - b.sort_order
  );
  const name = usta.profiles.full_name;

  return (
    <>
      <AppHeader back backHref="/" />
      <main className="mx-auto max-w-3xl pb-28">
        {/* Hero cover — reyting badge pastki chap burchakda, yarim shaffof fonda */}
        <div className="relative h-52 overflow-hidden sm:h-64 sm:rounded-b-3xl">
          {usta.cover_image_url ? (
            // eslint-disable-next-line @next/next/no-img-element
            <img
              src={usta.cover_image_url}
              alt={name}
              className="h-full w-full object-cover"
            />
          ) : (
            <div className="h-full w-full bg-gradient-to-br from-terra-200 to-terra-400" />
          )}
          <RatingBadge
            rating={usta.rating_avg}
            count={usta.rating_count}
            variant="overlay"
            // Pastdagi karta hero ustiga -mt-8 (32px) chiqadi — badge shundan yuqorida turishi kerak
            className="absolute bottom-11 left-4"
          />
        </div>

        <div className="px-4">
          {/* Asosiy ma'lumot */}
          <section className="-mt-8 relative rounded-2xl bg-white p-4 shadow-card">
            <div className="flex items-center gap-3">
              <Avatar name={name} src={usta.profiles.avatar_url} size="xl" />
              <div className="min-w-0">
                <h1 className="text-xl font-extrabold text-ink-900">{name}</h1>
                {usta.location_text && (
                  <p className="mt-1 flex items-center gap-1.5 text-sm text-ink-500">
                    <IconLocation size={15} />
                    {usta.location_text}
                  </p>
                )}
                {usta.work_hours_start && usta.work_hours_end && (
                  <p className="mt-0.5 flex items-center gap-1.5 text-sm text-ink-500">
                    <IconClock size={15} />
                    {t("usta.workHours", {
                      start: formatTime(usta.work_hours_start),
                      end: formatTime(usta.work_hours_end),
                    })}
                  </p>
                )}
              </div>
            </div>
            {usta.tags.length > 0 && (
              <div className="mt-3 flex flex-wrap gap-1.5">
                {usta.tags.map((tag) => (
                  <span
                    key={tag}
                    className="rounded-full bg-cream-200 px-3 py-1 text-xs font-semibold text-ink-700"
                  >
                    {tag}
                  </span>
                ))}
              </div>
            )}
            {usta.bio && (
              <p className="mt-3 text-sm leading-relaxed text-ink-700">
                {usta.bio}
              </p>
            )}
          </section>

          {/* Xizmatlar */}
          <section className="mt-6">
            <h2 className="mb-3 text-base font-extrabold text-ink-900">
              {t("usta.services")}
            </h2>
            <div className="space-y-2">
              {usta.usta_services.map((s) => (
                <div
                  key={s.id}
                  className="flex items-center justify-between gap-3 rounded-2xl bg-white p-4 shadow-card"
                >
                  <div className="min-w-0">
                    <p className="font-bold text-ink-900">{s.title}</p>
                    {s.description && (
                      <p className="mt-0.5 text-xs text-ink-500">{s.description}</p>
                    )}
                  </div>
                  <PriceTag amount={s.base_price} size="sm" />
                </div>
              ))}
            </div>
          </section>

          {/* Portfolio */}
          <section className="mt-6">
            <h2 className="mb-3 text-base font-extrabold text-ink-900">
              {t("usta.portfolio")}
            </h2>
            {portfolio.length > 0 ? (
              <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
                {portfolio.map((item) => (
                  <figure
                    key={item.id}
                    className="overflow-hidden rounded-2xl bg-white shadow-card"
                  >
                    {/* eslint-disable-next-line @next/next/no-img-element */}
                    <img
                      src={item.image_url}
                      alt={item.caption ?? name}
                      loading="lazy"
                      className="aspect-[3/4] w-full object-cover"
                    />
                    {item.caption && (
                      <figcaption className="px-3 py-2 text-xs font-medium text-ink-500">
                        {item.caption}
                      </figcaption>
                    )}
                  </figure>
                ))}
              </div>
            ) : (
              <EmptyState icon="🖼️" title={t("usta.noPortfolio")} />
            )}
          </section>

          {/* Sharhlar */}
          <section className="mt-6">
            <h2 className="mb-3 text-base font-extrabold text-ink-900">
              {t("usta.reviews")}{" "}
              <span className="text-sm font-semibold text-ink-500">
                {t("usta.reviewsCount", { count: usta.rating_count })}
              </span>
            </h2>
            {reviews && reviews.length > 0 ? (
              <div className="space-y-3">
                {(reviews as unknown as ReviewData[]).map((r) => (
                  <ReviewCard key={r.id} review={r} />
                ))}
              </div>
            ) : (
              <EmptyState icon="💬" title={t("usta.noReviews")} />
            )}
          </section>
        </div>
      </main>

      {/* Yopishqoq CTA — pastki nav ustida */}
      <div className="fixed inset-x-0 bottom-[72px] z-30 px-4 pb-2">
        <div className="mx-auto flex max-w-3xl gap-2">
          <Link
            href={`/usta/${id}/buyurtma`}
            className="flex flex-1 items-center justify-center rounded-2xl bg-terra-600 px-5 py-3.5 text-base font-bold text-white shadow-lg transition-colors hover:bg-terra-700 active:bg-terra-800"
          >
            {t("usta.orderCta")}
          </Link>
          <Link
            href={`/chat/${id}`}
            aria-label={t("usta.chatCta")}
            className="flex items-center justify-center gap-2 rounded-2xl border-2 border-terra-600 bg-cream-50 px-5 py-3.5 text-base font-bold text-terra-700 shadow-lg transition-colors hover:bg-terra-50"
          >
            <IconChat size={20} />
            {t("usta.chatCta")}
          </Link>
        </div>
      </div>
    </>
  );
}
