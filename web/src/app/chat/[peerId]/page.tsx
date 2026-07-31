import { notFound } from "next/navigation";
import AppHeader from "@/components/AppHeader";
import ChatListClient from "@/components/ChatListClient";
import ChatWindow from "@/components/ChatWindow";
import { createClient } from "@/lib/supabase/server";
import { getConversations } from "@/lib/chat";

/**
 * Suhbat sahifasi. Marshrut parametri — suhbatdoshning id'si; u mijoz ham,
 * usta ham bo'lishi mumkin. Usta o'z chat ro'yxatidan mijozni ochganda ham
 * shu sahifa ishlaydi.
 *
 * `lg` dan boshlab yonida suhbatlar ro'yxati ham turadi — desktopda odatiy
 * ikki panelli chat. Undan pastda faqat suhbat oynasi ko'rinadi.
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

  const { conversations, profileMap } = await getConversations(supabase, user.id);

  return (
    <>
      {/* Ekran balandligi header va suhbat qismi orasida flex bilan
          taqsimlanadi. `calc(100dvh - 3.5rem)` ishlatilmadi: header'ning
          pastki chegarasi (1px) hisobga olinmay, sahifa bir piksel
          aylanardi. Ichkarida xabarlar ro'yxati o'zi aylanadi, shuning
          uchun yozish paneli har doim ko'rinib turadi. */}
      <div className="flex h-dvh flex-col">
        <AppHeader back backHref="/chat" title={peer.full_name} />
        <div className="flex min-h-0 flex-1">
          <aside className="hidden w-96 shrink-0 overflow-y-auto border-e border-cream-200 lg:block">
            <ChatListClient
              conversations={conversations}
              profileMap={profileMap}
              userId={user.id}
              activePeerId={peerId}
              variant="pane"
            />
          </aside>

          <div className="min-w-0 flex-1">
            <ChatWindow
              peerId={peerId}
              peerName={peer.full_name}
              peerAvatarUrl={peer.avatar_url}
              viewerIsUsta={viewerIsUsta}
              initialConversationId={conversation?.id ?? null}
              currentUserId={user.id}
            />
          </div>
        </div>
      </div>
    </>
  );
}
