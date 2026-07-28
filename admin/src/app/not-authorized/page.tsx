import { redirect } from "next/navigation";
import { createClient } from "@/lib/supabase/server";
import LogoutButton from "@/components/LogoutButton";

export default async function NotAuthorizedPage() {
  const supabase = await createClient();
  const {
    data: { user },
  } = await supabase.auth.getUser();
  if (!user) redirect("/login");

  return (
    <main className="mx-auto flex min-h-dvh w-full max-w-sm flex-col items-center justify-center px-5 py-10 text-center">
      <h1 className="text-xl font-extrabold text-ink-900">Ruxsat yo&apos;q</h1>
      <p className="mt-2 text-sm text-ink-500">
        Bu hisobda admin huquqi yo&apos;q. Agar bu xato deb hisoblasangiz,
        boshqa admin bilan bog&apos;laning.
      </p>
      <div className="mt-6 w-full">
        <LogoutButton />
      </div>
    </main>
  );
}
