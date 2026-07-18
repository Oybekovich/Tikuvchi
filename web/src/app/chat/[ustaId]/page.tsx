import { notFound } from "next/navigation";
import ChatWindow from "@/components/ChatWindow";
import { createClient } from "@/lib/supabase/server";

export default async function ChatPage({
  params,
}: {
  params: Promise<{ ustaId: string }>;
}) {
  const { ustaId } = await params;
  const supabase = await createClient();

  const {
    data: { user },
  } = await supabase.auth.getUser();

  const [{ data: usta }, { data: conversation }] = await Promise.all([
    supabase
      .from("usta_profiles")
      .select("user_id, profiles!inner(full_name, avatar_url)")
      .eq("user_id", ustaId)
      .maybeSingle(),
    supabase
      .from("conversations")
      .select("id")
      .eq("client_id", user!.id)
      .eq("usta_id", ustaId)
      .maybeSingle(),
  ]);

  if (!usta) notFound();

  return (
    <ChatWindow
      ustaId={ustaId}
      ustaName={usta.profiles.full_name}
      ustaAvatarUrl={usta.profiles.avatar_url}
      initialConversationId={conversation?.id ?? null}
      currentUserId={user!.id}
    />
  );
}
