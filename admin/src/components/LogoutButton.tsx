"use client";

import { useRouter } from "next/navigation";
import { createClient } from "@/lib/supabase/client";

export default function LogoutButton() {
  const router = useRouter();

  async function logout() {
    const supabase = createClient();
    await supabase.auth.signOut();
    router.push("/login");
    router.refresh();
  }

  return (
    <button
      onClick={logout}
      className="w-full rounded-xl border border-cream-200 px-4 py-3 text-sm font-bold text-ink-700 transition hover:bg-cream-200"
    >
      Chiqish
    </button>
  );
}
