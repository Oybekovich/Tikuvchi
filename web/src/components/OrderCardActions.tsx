"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import Button from "@/components/Button";
import { createClient } from "@/lib/supabase/client";
import { nextOrderStatus, insertOrderEvent } from "@/lib/orderFlow";
import { t } from "@/lib/i18n";
import type { Enums } from "@/lib/database.types";

type Props = {
  orderId: string;
  status: Enums<"order_status">;
};

export default function OrderCardActions({ orderId, status }: Props) {
  const router = useRouter();
  const [busy, setBusy] = useState<string | null>(null);

  async function accept() {
    setBusy("accepted");
    const supabase = createClient();
    await supabase.from("orders").update({ status: "accepted" }).eq("id", orderId);
    router.refresh();
    setBusy(null);
  }

  async function reject() {
    setBusy("cancelled");
    const supabase = createClient();
    await supabase.from("orders").update({ status: "cancelled" }).eq("id", orderId);
    await insertOrderEvent(orderId, "pending", "cancelled");
    router.refresh();
    setBusy(null);
  }

  if (status === "pending") {
    return (
      <div className="flex gap-2">
        <Button onClick={accept} loading={busy === "accepted"} size="md" className="flex-1">
          {t("orders.accept")}
        </Button>
        <Button
          variant="danger"
          onClick={reject}
          loading={busy === "cancelled"}
          size="md"
          className="flex-1"
        >
          {t("orders.reject")}
        </Button>
      </div>
    );
  }

  const progress = nextOrderStatus(status);
  if (!progress) return null;

  async function advance() {
    if (!progress) return;
    setBusy(progress.next);
    const supabase = createClient();
    await supabase.from("orders").update({ status: progress.next }).eq("id", orderId);
    await insertOrderEvent(orderId, status, progress.next);
    router.refresh();
    setBusy(null);
  }

  return (
    <Button onClick={advance} loading={busy === progress.next} size="md" className="w-full">
      {t(progress.labelKey)}
    </Button>
  );
}
