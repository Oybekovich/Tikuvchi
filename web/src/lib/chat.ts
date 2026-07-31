import type { SupabaseClient } from "@supabase/supabase-js";
import type { Database } from "@/lib/database.types";

export type ConversationRow = {
  id: string;
  client_id: string;
  usta_id: string;
  last_message_at: string;
  messages: { content: string | null; message_type: string; created_at: string }[];
};

export type ChatProfile = {
  id: string;
  full_name: string;
  avatar_url: string | null;
};

/**
 * Foydalanuvchining suhbatlari va suhbatdoshlar profillari.
 *
 * Ikkala chat sahifasi ham shundan foydalanadi: `/chat` (ro'yxat) va
 * `/chat/[peerId]` — chunki `lg` dan boshlab suhbat oynasining yonida
 * ro'yxat ham turadi. Bitta joyda yozilgani uchun ikkalasida tartib va
 * "oxirgi xabar" bir xil bo'ladi.
 */
export async function getConversations(
  supabase: SupabaseClient<Database>,
  userId: string
): Promise<{
  conversations: ConversationRow[];
  profileMap: Record<string, ChatProfile>;
}> {
  const { data } = await supabase
    .from("conversations")
    .select(
      `id, client_id, usta_id, last_message_at,
       messages(content, message_type, created_at)`
    )
    .or(`client_id.eq.${userId},usta_id.eq.${userId}`)
    .order("last_message_at", { ascending: false })
    .order("created_at", { referencedTable: "messages", ascending: false })
    .limit(1, { referencedTable: "messages" });

  const conversations = (data ?? []) as ConversationRow[];
  const profileMap: Record<string, ChatProfile> = {};

  if (conversations.length > 0) {
    const otherIds = conversations.map((c) =>
      c.client_id === userId ? c.usta_id : c.client_id
    );
    const { data: profiles } = await supabase
      .from("profiles")
      .select("id, full_name, avatar_url")
      .in("id", otherIds);
    for (const p of profiles ?? []) {
      profileMap[p.id] = p;
    }
  }

  return { conversations, profileMap };
}
