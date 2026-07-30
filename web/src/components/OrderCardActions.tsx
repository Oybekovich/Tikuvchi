"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import Button from "@/components/Button";
import {
  canClientComplete,
  changeOrderStatus,
  nextUstaStatus,
} from "@/lib/orderFlow";
import { t } from "@/lib/i18n";
import type { Enums } from "@/lib/database.types";

type Props = {
  orderId: string;
  status: Enums<"order_status">;
  isUsta: boolean;
};

/** Buyurtmalar ro'yxatidagi tez amallar — rolga qarab boshqacha */
export default function OrderCardActions({ orderId, status, isUsta }: Props) {
  const router = useRouter();
  const [busy, setBusy] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  async function transition(to: Enums<"order_status">) {
    setBusy(to);
    setError(null);
    const { ok, stale } = await changeOrderStatus(orderId, status, to);
    setBusy(null);
    if (ok) {
      setError(null);
      router.refresh();
    } else {
      setError(stale ? t("orders.staleError") : t("common.error"));
      if (stale) router.refresh();
    }
  }

  const errorLine = error ? (
    <p className="mt-2 rounded-xl bg-red-50 px-3 py-2 text-xs font-semibold text-red-700">
      {error}
    </p>
  ) : null;

  // Usta: yangi buyurtmani qabul qilish yoki rad etish
  if (isUsta && status === "pending") {
    return (
      <div>
        <div className="flex gap-2">
          <Button
            onClick={() => transition("accepted")}
            loading={busy === "accepted"}
            disabled={busy !== null}
            size="md"
            className="flex-1"
          >
            {t("orders.accept")}
          </Button>
          <Button
            variant="danger"
            onClick={() => transition("cancelled")}
            loading={busy === "cancelled"}
            disabled={busy !== null}
            size="md"
            className="flex-1"
          >
            {t("orders.reject")}
          </Button>
        </div>
        {errorLine}
      </div>
    );
  }

  // Usta: ishni ilgari surish (ready dan keyin to'xtaydi)
  if (isUsta) {
    const progress = nextUstaStatus(status);
    if (!progress) return null;
    return (
      <div>
        <Button
          onClick={() => transition(progress.next)}
          loading={busy === progress.next}
          disabled={busy !== null}
          size="md"
          className="w-full"
        >
          {t(progress.labelKey)}
        </Button>
        {errorLine}
      </div>
    );
  }

  // Mijoz: tayyor buyurtmani qabul qilib olganini tasdiqlash
  if (canClientComplete(status)) {
    return (
      <div>
        <Button
          onClick={() => transition("completed")}
          loading={busy === "completed"}
          disabled={busy !== null}
          size="md"
          className="w-full"
        >
          {t("orders.confirmReceived")}
        </Button>
        {errorLine}
      </div>
    );
  }

  return null;
}
