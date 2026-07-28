"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import Button from "@/components/Button";
import { IconCheck } from "@/components/Icons";
import { createClient } from "@/lib/supabase/client";
import { depositAmount, nextPaymentStatus } from "@/lib/payments";
import { formatCurrency } from "@/lib/format";
import { nextOrderStatus, insertOrderEvent } from "@/lib/orderFlow";
import { t } from "@/lib/i18n";
import type { Enums } from "@/lib/database.types";

type Props = {
  orderId: string;
  ustaId: string;
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
  const [confirmingCancel, setConfirmingCancel] = useState(false);
  const [confirmingReject, setConfirmingReject] = useState(false);

  const currentIdx = PAYMENT_STEPS.indexOf(paymentStatus);
  const next = nextPaymentStatus(paymentStatus);
  const active = status !== "cancelled";
  const isPending = status === "pending";
  const progress = nextOrderStatus(status);

  async function advancePayment() {
    if (!next) return;
    setBusy(true);
    const supabase = createClient();
    await supabase
      .from("orders")
      .update({ payment_status: next })
      .eq("id", orderId);
    router.refresh();
    setBusy(false);
  }

  async function updateStatus(newStatus: Enums<"order_status">) {
    setBusy(true);
    const supabase = createClient();
    await supabase
      .from("orders")
      .update({ status: newStatus })
      .eq("id", orderId);
    router.refresh();
    setBusy(false);
    setConfirmingCancel(false);
    setConfirmingReject(false);
  }

  async function rejectOrder() {
    setBusy(true);
    const supabase = createClient();
    await supabase
      .from("orders")
      .update({ status: "cancelled" })
      .eq("id", orderId);
    await insertOrderEvent(orderId, "pending", "cancelled");
    router.refresh();
    setBusy(false);
    setConfirmingReject(false);
  }

  async function progressOrder() {
    if (!progress) return;
    setBusy(true);
    const supabase = createClient();
    await supabase
      .from("orders")
      .update({ status: progress.next })
      .eq("id", orderId);
    await insertOrderEvent(orderId, status, progress.next);
    router.refresh();
    setBusy(false);
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
            const done = i <= currentIdx;
            return (
              <li key={step} className="flex items-center gap-2.5">
                <span
                  className={`flex h-6 w-6 shrink-0 items-center justify-center rounded-full text-[11px] font-bold ${
                    done
                      ? "bg-green-600 text-white"
                      : "border-2 border-cream-300 text-ink-300"
                  }`}
                >
                  {done ? <IconCheck size={13} /> : i + 1}
                </span>
                <span
                  className={`text-sm font-semibold ${
                    done ? "text-ink-900" : "text-ink-500"
                  }`}
                >
                  {t(`payment.${step}`)}
                </span>
              </li>
            );
          })}
        </ol>

        {active && next && (
          <Button
            onClick={advancePayment}
            loading={busy}
            size="lg"
            className="mt-4"
          >
            {next === "partial"
              ? `${t("payment.payDeposit")} — ${formatCurrency(depositAmount(totalPrice))}`
              : t("payment.payRest")}
          </Button>
        )}
        <p className="mt-3 text-xs text-ink-500">
          {t("payment.integrationNote")}
        </p>
      </section>

      {/* Usta: Qabul qilish / Rad etish */}
      {isUsta && isPending && (
        <section className="rounded-2xl bg-white p-4 shadow-card">
          <h2 className="text-sm font-extrabold text-ink-900">
            {t("orders.title")}
          </h2>
          <div className="mt-3 flex gap-2">
            <Button
              onClick={() => updateStatus("accepted")}
              loading={busy}
              className="flex-1"
            >
              {t("orders.accept")}
            </Button>
            <Button
              variant="danger"
              onClick={() => setConfirmingReject(true)}
              className="flex-1"
            >
              {t("orders.reject")}
            </Button>
          </div>
        </section>
      )}

      {/* Mijoz: Bekor qilish */}
      {!isUsta && isPending &&
        (confirmingCancel ? (
          <div className="flex items-center gap-2 rounded-2xl bg-red-50 p-3">
            <span className="flex-1 text-sm font-semibold text-red-700">
              {t("orders.cancelConfirm")}
            </span>
            <Button variant="danger" onClick={() => updateStatus("cancelled")} loading={busy}>
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

      {/* Usta: ishni davom ettirish (accepted -> in_progress -> ready -> completed) */}
      {isUsta && active && !isPending && progress && (
        <section className="rounded-2xl bg-white p-4 shadow-card">
          <Button onClick={progressOrder} loading={busy} size="lg" className="w-full">
            {t(progress.labelKey)}
          </Button>
        </section>
      )}

      {/* Rad etish tasdiqlash dialogi */}
      {confirmingReject && (
        <div className="flex items-center gap-2 rounded-2xl bg-red-50 p-3">
          <span className="flex-1 text-sm font-semibold text-red-700">
            {t("orders.rejectConfirm")}
          </span>
          <Button variant="danger" onClick={rejectOrder} loading={busy}>
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
    </div>
  );
}
