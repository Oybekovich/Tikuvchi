import { notFound } from "next/navigation";
import ChatWindow from "@/components/ChatWindow";
import { createClient } from "@/lib/supabase/server";

/**
 * Suhbat sahifasi. Marshrut parametri — suhbatdoshning id'si; u mijoz ham,
 * usta ham bo'lishi mumkin. Usta o'z chat ro'yxatidan mijozni ochganda ham
 * shu sahifa ishlaydi.
 */
export default async function ChatPage({
  params,
}: {
  params: Promise<{ peerId: string }>;
}) {
  const { peerId } = await params;
  const supabase = await createClient();

  const {
    data: { user },
  } = await supabase.auth.getUser();
  if (!user) notFound();

  // O'zi bilan suhbat bo'lishi mumkin emas
  if (peerId === user.id) notFound();

  const [{ data: peer }, { data: peerUsta }, { data: conversation }] =
    await Promise.all([
      supabase
        .from("profiles")
        .select("id, full_name, avatar_url")
        .eq("id", peerId)
        .maybeSingle(),
      supabase
        .from("usta_profiles")
        .select("user_id")
        .eq("user_id", peerId)
        .maybeSingle(),
      supabase
        .from("conversations")
        .select("id, client_id, usta_id")
        .or(
          `and(client_id.eq.${user.id},usta_id.eq.${peerId}),and(client_id.eq.${peerId},usta_id.eq.${user.id})`
        )
        .maybeSingle(),
    ]);

  if (!peer) notFound();

  // Rollarni aniqlash: mavjud suhbat bo'lsa undan, aks holda suhbatdoshning
  // usta profili bor-yo'qligidan.
  let viewerIsUsta: boolean;
  if (conversation) {
    viewerIsUsta = conversation.usta_id === user.id;
  } else if (peerUsta) {
    viewerIsUsta = false;
  } else {
    // Suhbatdosh usta emas — demak men usta bo'lishim kerak. Ikkisi ham
    // mijoz bo'lsa suhbat ochilmaydi.
    const { data: myUsta } = await supabase
      .from("usta_profiles")
      .select("user_id")
      .eq("user_id", user.id)
      .maybeSingle();
    if (!myUsta) notFound();
    viewerIsUsta = true;
  }

  return (
    <ChatWindow
      peerId={peerId}
      peerName={peer.full_name}
      peerAvatarUrl={peer.avatar_url}
      viewerIsUsta={viewerIsUsta}
      initialConversationId={conversation?.id ?? null}
      currentUserId={user.id}
    />
  );
}
