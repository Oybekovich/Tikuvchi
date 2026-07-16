"use client";

import Button from "@/components/Button";
import PriceTag from "@/components/PriceTag";
import { IconCheck, IconClock, IconClose, IconTag } from "@/components/Icons";
import { t } from "@/lib/i18n";
import type { Enums } from "@/lib/database.types";

type Props = {
  amount: number;
  durationDays: number | null;
  note: string | null;
  status: Enums<"price_offer_status"> | null;
  /** Mijoz o'z chatida taklifga javob bera oladi */
  canRespond: boolean;
  responding?: boolean;
  onAccept?: () => void;
  onDecline?: () => void;
};

/** Chatdagi "Narx taklifi" kartasi (B yo'l: chat orqali kelishuv) */
export default function PriceOfferBubble({
  amount,
  durationDays,
  note,
  status,
  canRespond,
  responding = false,
  onAccept,
  onDecline,
}: Props) {
  return (
    <div className="w-72 max-w-full overflow-hidden rounded-2xl border border-terra-200 bg-white shadow-card">
      <div className="flex items-center gap-2 bg-terra-50 px-4 py-2.5">
        <IconTag size={16} className="text-terra-600" />
        <span className="text-sm font-bold text-terra-800">
          {t("chat.priceOffer")}
        </span>
      </div>
      <div className="space-y-2 px-4 py-3">
        <PriceTag amount={amount} size="lg" />
        {durationDays !== null && (
          <p className="flex items-center gap-1.5 text-sm text-ink-700">
            <IconClock size={15} className="text-ink-500" />
            {t("chat.offerDuration")}:{" "}
            <b>{t("chat.offerDurationDays", { days: durationDays })}</b>
          </p>
        )}
        {note && <p className="text-sm leading-relaxed text-ink-700">{note}</p>}
      </div>
      <div className="border-t border-cream-200 px-4 py-3">
        {status === "pending" && canRespond ? (
          <div className="flex gap-2">
            <Button
              onClick={onAccept}
              loading={responding}
              className="flex-1 whitespace-nowrap"
              aria-label={t("chat.accept")}
            >
              <IconCheck size={16} className="shrink-0" />
              {t("chat.accept")}
            </Button>
            <Button
              variant="outline"
              onClick={onDecline}
              disabled={responding}
              className="flex-1 whitespace-nowrap"
              aria-label={t("chat.decline")}
            >
              {t("chat.decline")}
            </Button>
          </div>
        ) : status === "accepted" ? (
          <span className="inline-flex items-center gap-1.5 rounded-full bg-green-100 px-3 py-1 text-xs font-bold text-green-800">
            <IconCheck size={14} />
            {t("chat.offerAccepted")}
          </span>
        ) : status === "declined" ? (
          <span className="inline-flex items-center gap-1.5 rounded-full bg-cream-200 px-3 py-1 text-xs font-bold text-ink-500">
            <IconClose size={14} />
            {t("chat.offerDeclined")}
          </span>
        ) : (
          <span className="inline-flex items-center gap-1.5 rounded-full bg-gold-100 px-3 py-1 text-xs font-bold text-ink-700">
            <IconClock size={14} />
            {t("orderStatus.pending")}
          </span>
        )}
      </div>
    </div>
  );
}
