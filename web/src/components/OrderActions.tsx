"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import Button from "@/components/Button";
import { IconCheck } from "@/components/Icons";
import { createClient } from "@/lib/supabase/client";
import { depositAmount, nextPaymentStatus } from "@/lib/payments";
import { formatCurrency } from "@/lib/format";
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
  paymentStatus: Enums<"payment_status">;
  totalPrice: number;
  isUsta: boolean;
};

const PAYMENT_STEPS: Enums<"payment_status">[] = ["pending", "partial", "paid"];

export default function OrderActions({
  orderId,
  status,
  paymentStatus,
  totalPrice,
  isUsta,
}: Props) {
  const router = useRouter();
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [confirmingCancel, setConfirmingCancel] = useState(false);
  const [confirmingReject, setConfirmingReject] = useState(false);

  const currentIdx = PAYMENT_STEPS.indexOf(paymentStatus);
  const nextPayment = nextPaymentStatus(paymentStatus);
  const active = status !== "cancelled" && status !== "completed";
  const isPending = status === "pending";
  const ustaProgress = nextUstaStatus(status);
  const clientCanComplete = canClientComplete(status);

  /** To'lovni faqat usta belgilaydi — pulni u oladi (docs/05-pul.md) */
  const canEditPayment = isUsta && active && Boolean(nextPayment);

  function done(ok: boolean, stale: boolean) {
    setBusy(false);
    setConfirmingCancel(false);
    setConfirmingReject(false);
    if (ok) {
      setError(null);
      router.refresh();
    } else {
      setError(stale ? t("orders.staleError") : t("common.error"));
      if (stale) router.refresh();
    }
  }

  async function advancePayment() {
    if (!nextPayment || !canEditPayment) return;
    setBusy(true);
    setError(null);
    const supabase = createClient();
    const { error: updErr } = await supabase
      .from("orders")
      .update({ payment_status: nextPayment })
      .eq("id", orderId)
      .eq("payment_status", paymentStatus);
    done(!updErr, false);
  }

  async function transition(to: Enums<"order_status">) {
    setBusy(true);
    setError(null);
    const { ok, stale } = await changeOrderStatus(orderId, status, to);
    done(ok, stale);
  }

  return (
    <div className="space-y-4">
      {/* To'lov holati */}
      <section className="rounded-2xl bg-white p-4 shadow-card">
        <h2 className="text-sm font-extrabold text-ink-900">
          {t("payment.title")}
        </h2>
        <ol className="mt-3 space-y-2">
          {PAYMENT_STEPS.map((step, i) => {
            const stepDone = i <= currentIdx;
            return (
              <li key={step} className="flex items-center gap-2.5">
                <span
                  className={`flex h-6 w-6 shrink-0 items-center justify-center rounded-full text-[11px] font-bold ${
                    stepDone
                      ? "bg-green-600 text-white"
                      : "border-2 border-cream-300 text-ink-300"
                  }`}
                >
                  {stepDone ? <IconCheck size={13} /> : i + 1}
                </span>
                <span
                  className={`text-sm font-semibold ${
                    stepDone ? "text-ink-900" : "text-ink-500"
                  }`}
                >
                  {t(`payment.${step}`)}
                </span>
              </li>
            );
          })}
        </ol>

        {canEditPayment && (
          <Button
            onClick={advancePayment}
            loading={busy}
            size="lg"
            className="mt-4"
          >
            {nextPayment === "partial"
              ? `${t("payment.markDeposit")} — ${formatCurrency(depositAmount(totalPrice))}`
              : t("payment.markPaid")}
          </Button>
        )}
        <p className="mt-3 text-xs text-ink-500">
          {isUsta ? t("payment.ustaNote") : t("payment.clientNote")}
        </p>
      </section>

      {/* Usta: Qabul qilish / Rad etish */}
      {isUsta && isPending && (
        <section className="rounded-2xl bg-white p-4 shadow-card">
          <h2 className="text-sm font-extrabold text-ink-900">
            {t("orders.newRequest")}
          </h2>
          <div className="mt-3 flex gap-2">
            <Button
              onClick={() => transition("accepted")}
              loading={busy}
              className="flex-1"
            >
              {t("orders.accept")}
            </Button>
            <Button
              variant="danger"
              onClick={() => setConfirmingReject(true)}
              disabled={busy}
              className="flex-1"
            >
              {t("orders.reject")}
            </Button>
          </div>
        </section>
      )}

      {/* Usta: ishni davom ettirish (accepted -> in_progress -> ready) */}
      {isUsta && active && ustaProgress && (
        <section className="rounded-2xl bg-white p-4 shadow-card">
          <Button
            onClick={() => transition(ustaProgress.next)}
            loading={busy}
            size="lg"
            className="w-full"
          >
            {t(ustaProgress.labelKey)}
          </Button>
        </section>
      )}

      {/* Usta: "tayyor" holatida mijozning tasdig'ini kutadi */}
      {isUsta && status === "ready" && (
        <p className="rounded-2xl bg-gold-100 px-4 py-3 text-sm font-semibold text-ink-700">
          {t("orders.waitingClientConfirm")}
        </p>
      )}

      {/* Mijoz: buyurtmani qabul qilib olganini tasdiqlash */}
      {!isUsta && clientCanComplete && (
        <section className="rounded-2xl bg-white p-4 shadow-card">
          <p className="text-sm font-semibold text-ink-700">
            {t("orders.confirmReceivedHint")}
          </p>
          <Button
            onClick={() => transition("completed")}
            loading={busy}
            size="lg"
            className="mt-3 w-full"
          >
            {t("orders.confirmReceived")}
          </Button>
        </section>
      )}

      {/* Mijoz: Bekor qilish — faqat usta ishni boshlamagunicha */}
      {!isUsta && isPending &&
        (confirmingCancel ? (
          <div className="flex items-center gap-2 rounded-2xl bg-red-50 p-3">
            <span className="flex-1 text-sm font-semibold text-red-700">
              {t("orders.cancelConfirm")}
            </span>
            <Button
              variant="danger"
              onClick={() => transition("cancelled")}
              loading={busy}
            >
              {t("common.confirm")}
            </Button>
            <Button
              variant="ghost"
              onClick={() => setConfirmingCancel(false)}
              disabled={busy}
            >
              {t("common.close")}
            </Button>
          </div>
        ) : (
          <Button
            variant="danger"
            size="lg"
            onClick={() => setConfirmingCancel(true)}
          >
            {t("orders.cancelOrder")}
          </Button>
        ))}

      {/* Rad etish tasdiqlash dialogi */}
      {confirmingReject && (
        <div className="flex items-center gap-2 rounded-2xl bg-red-50 p-3">
          <span className="flex-1 text-sm font-semibold text-red-700">
            {t("orders.rejectConfirm")}
          </span>
          <Button
            variant="danger"
            onClick={() => transition("cancelled")}
            loading={busy}
          >
            {t("common.confirm")}
          </Button>
          <Button
            variant="ghost"
            onClick={() => setConfirmingReject(false)}
            disabled={busy}
          >
            {t("common.close")}
          </Button>
        </div>
      )}

      {error && (
        <p className="rounded-xl bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">
          {error}
        </p>
      )}
    </div>
  );
}
