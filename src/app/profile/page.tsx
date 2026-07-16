import type { Metadata } from "next";
import AppHeader from "@/components/AppHeader";
import ProfileForm from "@/components/ProfileForm";
import { createClient } from "@/lib/supabase/server";
import { t } from "@/lib/i18n";

export const metadata: Metadata = { title: "Profil" };

export default async function ProfilePage() {
  const supabase = await createClient();

  const {
    data: { user },
  } = await supabase.auth.getUser();

  const { data: profile } = await supabase
    .from("profiles")
    .select("*")
    .eq("id", user!.id)
    .maybeSingle();

  return (
    <>
      <AppHeader />
      <main className="mx-auto max-w-3xl px-4 pt-4 pb-6">
        <h1 className="text-xl font-extrabold text-ink-900">
          {t("profile.title")}
        </h1>
        <ProfileForm
          email={user!.email ?? ""}
          fullName={profile?.full_name ?? ""}
          phone={profile?.phone ?? ""}
          role={profile?.role ?? "client"}
          userId={user!.id}
        />
      </main>
    </>
  );
}
