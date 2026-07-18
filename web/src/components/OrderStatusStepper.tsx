import { IconCheck } from "@/components/Icons";
import { t } from "@/lib/i18n";
import type { Enums } from "@/lib/database.types";

const STEPS = ["accepted", "in_progress", "ready"] as const;

/** Har bir holat uchun: nechta bosqich tugagan va progress foizi */
function progressFor(status: Enums<"order_status">): {
  doneCount: number;
  activeIdx: number;
  percent: number;
} {
  switch (status) {
    case "pending":
      return { doneCount: 0, activeIdx: 0, percent: 8 };
    case "accepted":
      return { doneCount: 1, activeIdx: 1, percent: 33 };
    case "in_progress":
      return { doneCount: 1, activeIdx: 1, percent: 62 };
    case "ready":
    case "completed":
      return { doneCount: 3, activeIdx: -1, percent: 100 };
    case "cancelled":
      return { doneCount: 0, activeIdx: -1, percent: 0 };
  }
}

/**
 * 3 bosqichli holat ko'rsatkichi: Qabul qilindi → Tayyorlanmoqda → Tayyor.
 * Yorliqlar har doim bitta qatorda, ustma-ust tushmaydi (har biriga alohida
 * grid ustuni, nowrap + kichik shrift).
 */
export default function OrderStatusStepper({
  status,
}: {
  status: Enums<"order_status">;
}) {
  const { doneCount, activeIdx, percent } = progressFor(status);
  const isDone = (i: number) => i < doneCount;

  return (
    <div aria-label={t(`orderStatus.${status}`)}>
      <div className="relative mx-auto w-[86%]">
        {/* Orqa chiziq va progress */}
        <div className="absolute left-0 right-0 top-1/2 h-1.5 -translate-y-1/2 rounded-full bg-cream-200" />
        <div
          className="absolute left-0 top-1/2 h-1.5 -translate-y-1/2 rounded-full bg-terra-600 transition-all duration-500"
          style={{ width: `${percent}%` }}
        />
        {/* Doirachalar */}
        <div className="relative flex items-center justify-between">
          {STEPS.map((step, i) => {
            const done = isDone(i);
            const active = i === activeIdx && status !== "cancelled";
            return (
              <span
                key={step}
                className={`flex h-9 w-9 items-center justify-center rounded-full border-2 text-xs font-bold transition-colors ${
                  done
                    ? "border-terra-600 bg-terra-600 text-white"
                    : active
                      ? "border-terra-600 bg-white text-terra-700"
                      : "border-cream-300 bg-white text-ink-300"
                }`}
              >
                {done ? <IconCheck size={16} /> : i + 1}
              </span>
            );
          })}
        </div>
      </div>
      {/* Yorliqlar: alohida ustunlar, bir qator, qisqartirilmaydi */}
      <div className="mt-2 grid grid-cols-3">
        {STEPS.map((step, i) => {
          const done = isDone(i);
          const active = i === activeIdx && status !== "cancelled";
          return (
            <span
              key={step}
              className={`whitespace-nowrap text-center text-[11px] font-semibold sm:text-xs ${
                done || active ? "text-terra-700" : "text-ink-500"
              }`}
            >
              {t(`stepper.${step}`)}
            </span>
          );
        })}
      </div>
    </div>
  );
}
