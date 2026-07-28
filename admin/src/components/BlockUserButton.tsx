"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import { createClient } from "@/lib/supabase/client";

export default function BlockUserButton({
  userId,
  isBlocked,
}: {
  userId: string;
  isBlocked: boolean;
}) {
  const router = useRouter();
  const [busy, setBusy] = useState(false);

  async function toggle() {
    setBusy(true);
    const supabase = createClient();
    await supabase.rpc("admin_set_user_blocked", {
      target_id: userId,
      blocked: !isBlocked,
    });
    router.refresh();
    setBusy(false);
  }

  return (
    <button
      onClick={toggle}
      disabled={busy}
      className={`rounded-lg px-3 py-1.5 text-xs font-bold disabled:opacity-60 ${
        isBlocked
          ? "bg-sage-500/10 text-sage-500 hover:bg-sage-500/20"
          : "bg-red-50 text-red-700 hover:bg-red-100"
      }`}
    >
      {busy ? "…" : isBlocked ? "Blokdan chiqarish" : "Bloklash"}
    </button>
  );
}
