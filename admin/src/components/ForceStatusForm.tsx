"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import { createClient } from "@/lib/supabase/client";

const STATUSES = [
  "pending",
  "accepted",
  "in_progress",
  "ready",
  "completed",
  "cancelled",
] as const;

export default function ForceStatusForm({
  orderId,
  currentStatus,
}: {
  orderId: string;
  currentStatus: string;
}) {
  const router = useRouter();
  const [status, setStatus] = useState(currentStatus);
  const [comment, setComment] = useState("");
  const [busy, setBusy] = useState(false);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setBusy(true);
    const supabase = createClient();
    await supabase.rpc("admin_force_order_status", {
      p_order_id: orderId,
      p_new_status: status,
      p_comment: comment.trim() || null,
    });
    setBusy(false);
    setComment("");
    router.refresh();
  }

  return (
    <form onSubmit={submit} className="mt-2 flex flex-col gap-2 sm:flex-row sm:items-end">
      <label className="block">
        <span className="mb-1 block text-xs font-bold text-ink-500">Yangi status</span>
        <select
          value={status}
          onChange={(e) => setStatus(e.target.value)}
          className="rounded-xl border border-cream-200 bg-white px-3 py-2 text-sm text-ink-900"
        >
          {STATUSES.map((s) => (
            <option key={s} value={s}>
              {s}
            </option>
          ))}
        </select>
      </label>
      <label className="block flex-1">
        <span className="mb-1 block text-xs font-bold text-ink-500">
          Izoh (ixtiyoriy)
        </span>
        <input
          value={comment}
          onChange={(e) => setComment(e.target.value)}
          placeholder="Nima uchun o'zgartirilmoqda"
          className="w-full rounded-xl border border-cream-200 bg-white px-3 py-2 text-sm text-ink-900"
        />
      </label>
      <button
        type="submit"
        disabled={busy || status === currentStatus}
        className="rounded-xl bg-terra-600 px-4 py-2 text-sm font-bold text-white disabled:opacity-60"
      >
        {busy ? "…" : "Qo'llash"}
      </button>
    </form>
  );
}
