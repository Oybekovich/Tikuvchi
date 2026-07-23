import Link from "next/link";
import { notFound } from "next/navigation";
import AppHeader from "@/components/AppHeader";
import Avatar from "@/components/Avatar";
import EmptyState from "@/components/EmptyState";
import PortfolioGallery from "@/components/PortfolioGallery";
import RatingBadge from "@/components/RatingBadge";
import ReviewCard, { type ReviewData } from "@/components/ReviewCard";
import { IconChat, IconClock, IconLocation } from "@/components/Icons";
import {
  PhChat,
  PhImages,
} from "@/components/PhosphorIcons";
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
            className="absolute bottom-11 left-4"
          />
        </div>

        <div className="px-4">
          {/* Asosiy ma'lumot va Bio */}
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

            {/* Yozish tugmasi biosida */}
            <div className="mt-4 pt-3 border-t border-cream-200">
              <Link
                href={`/chat/${id}`}
                className="flex items-center justify-center gap-2 w-full rounded-2xl bg-terra-600 px-4 py-3 text-sm font-bold text-white transition-colors hover:bg-terra-700 active:bg-terra-800"
              >
                <IconChat size={18} />
                {t("usta.chatCta")}
              </Link>
            </div>
          </section>

          {/* Portfolio */}
          <section className="mt-6">
            <h2 className="mb-3 text-base font-extrabold text-ink-900">
              {t("usta.portfolio")}
            </h2>
            {portfolio.length > 0 ? (
              <PortfolioGallery items={portfolio} ustaId={id} ustaName={name} />
            ) : (
              <EmptyState icon={<PhImages size={30} />} title={t("usta.noPortfolio")} />
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
              <EmptyState icon={<PhChat size={30} />} title={t("usta.noReviews")} />
            )}
          </section>
        </div>
      </main>
    </>
  );
}
