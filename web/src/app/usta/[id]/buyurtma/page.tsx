import { notFound } from "next/navigation";
import OrderWizard from "@/components/OrderWizard";
import { createClient } from "@/lib/supabase/server";

export default async function BuyurtmaPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const supabase = await createClient();

  const {
    data: { user },
  } = await supabase.auth.getUser();

  const [{ data: usta }, { data: measurements }] = await Promise.all([
    supabase
      .from("usta_profiles")
      .select("user_id, profiles!inner(full_name)")
      .eq("user_id", id)
      .maybeSingle(),
    supabase
      .from("measurements")
      .select("id, label, chest, waist, hips, height, shoulder, sleeve_length")
      .eq("client_id", user!.id)
      .order("updated_at", { ascending: false }),
  ]);

  if (!usta) notFound();

  return (
    <OrderWizard
      ustaId={usta.user_id}
      ustaName={usta.profiles.full_name}
      measurements={measurements ?? []}
    />
  );
}
