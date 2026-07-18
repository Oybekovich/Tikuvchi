import Link from "next/link";
import Avatar from "@/components/Avatar";
import PriceTag from "@/components/PriceTag";
import RatingBadge from "@/components/RatingBadge";
import { IconChevronRight, IconLocation } from "@/components/Icons";

export type UstaCardData = {
  user_id: string;
  district: string | null;
  rating_avg: number;
  rating_count: number;
  tags: string[];
  profiles: { full_name: string; avatar_url: string | null };
  usta_services: { base_price: number }[];
};

export default function UstaCard({ usta }: { usta: UstaCardData }) {
  const minPrice = usta.usta_services.length
    ? Math.min(...usta.usta_services.map((s) => s.base_price))
    : null;

  return (
    <Link
      href={`/usta/${usta.user_id}`}
      className="block rounded-2xl bg-white p-4 shadow-card transition-transform hover:-translate-y-0.5 active:translate-y-0"
    >
      <div className="flex items-start gap-3">
        <Avatar
          name={usta.profiles.full_name}
          src={usta.profiles.avatar_url}
          size="lg"
        />
        <div className="min-w-0 flex-1">
          <div className="flex items-center justify-between gap-2">
            <h3 className="truncate text-base font-bold text-ink-900">
              {usta.profiles.full_name}
            </h3>
            <RatingBadge rating={usta.rating_avg} />
          </div>
          {usta.district && (
            <p className="mt-0.5 flex items-center gap-1 text-sm text-ink-500">
              <IconLocation size={14} />
              {usta.district}
            </p>
          )}
          <div className="mt-2 flex flex-wrap gap-1.5">
            {usta.tags.slice(0, 3).map((tag) => (
              <span
                key={tag}
                className="rounded-full bg-cream-200 px-2.5 py-0.5 text-xs font-semibold text-ink-700"
              >
                {tag}
              </span>
            ))}
          </div>
        </div>
      </div>
      {minPrice !== null && (
        <div className="mt-3 flex items-center justify-between border-t border-cream-200 pt-3">
          <PriceTag amount={minPrice} from size="sm" />
          <IconChevronRight size={18} className="text-ink-300" />
        </div>
      )}
    </Link>
  );
}
