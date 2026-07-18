import Avatar from "@/components/Avatar";
import { IconStar } from "@/components/Icons";
import { formatDate } from "@/lib/format";

export type ReviewData = {
  id: number;
  rating: number;
  comment: string | null;
  created_at: string;
  profiles: { full_name: string; avatar_url: string | null };
};

function Stars({ rating }: { rating: number }) {
  return (
    <span className="flex gap-0.5" aria-label={`${rating}/5`}>
      {[1, 2, 3, 4, 5].map((i) => (
        <IconStar
          key={i}
          size={13}
          className={i <= rating ? "text-gold-400" : "text-cream-300"}
        />
      ))}
    </span>
  );
}

export default function ReviewCard({ review }: { review: ReviewData }) {
  return (
    <article className="rounded-2xl bg-white p-4 shadow-card">
      <div className="flex items-center gap-3">
        <Avatar
          name={review.profiles.full_name}
          src={review.profiles.avatar_url}
          size="md"
        />
        <div className="min-w-0 flex-1">
          <p className="truncate text-sm font-bold text-ink-900">
            {review.profiles.full_name}
          </p>
          <p className="text-xs text-ink-500">{formatDate(review.created_at)}</p>
        </div>
        <Stars rating={review.rating} />
      </div>
      {review.comment && (
        <p className="mt-3 text-sm leading-relaxed text-ink-700">
          {review.comment}
        </p>
      )}
    </article>
  );
}
