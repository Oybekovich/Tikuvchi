"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import { createClient } from "@/lib/supabase/client";

export default function UstaVisibilityToggle({
  ustaId,
  visible,
  available,
}: {
  ustaId: string;
  visible: boolean;
  available: boolean;
}) {
  const router = useRouter();
  const [busy, setBusy] = useState(false);

  async function toggleVisible() {
    setBusy(true);
    const supabase = createClient();
    await supabase.rpc("admin_set_usta_visibility", {
      target_id: ustaId,
      p_visible: !visible,
      p_available: available,
    });
    router.refresh();
    setBusy(false);
  }

  return (
    <button
      onClick={toggleVisible}
      disabled={busy}
      className={`rounded-lg px-3 py-1.5 text-xs font-bold disabled:opacity-60 ${
        visible
          ? "bg-red-50 text-red-700 hover:bg-red-100"
          : "bg-sage-500/10 text-sage-500 hover:bg-sage-500/20"
      }`}
    >
      {busy ? "…" : visible ? "Yashirish" : "Ko'rsatish"}
    </button>
  );
}
