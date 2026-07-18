import type { Metadata } from "next";
import AppHeader from "@/components/AppHeader";
import MeasurementsManager from "@/components/MeasurementsManager";
import { createClient } from "@/lib/supabase/server";
import { t } from "@/lib/i18n";

export const metadata: Metadata = { title: "Mening o'lchamlarim" };

export default async function MeasurementsPage() {
  const supabase = await createClient();

  const {
    data: { user },
  } = await supabase.auth.getUser();

  const { data: measurements } = await supabase
    .from("measurements")
    .select("*")
    .eq("client_id", user!.id)
    .order("updated_at", { ascending: false });

  return (
    <>
      <AppHeader />
      <main className="mx-auto max-w-3xl px-4 pt-4 pb-6">
        <h1 className="text-xl font-extrabold text-ink-900">
          {t("measurements.title")}
        </h1>
        <MeasurementsManager
          initial={measurements ?? []}
          userId={user!.id}
        />
      </main>
    </>
  );
}
