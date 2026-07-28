"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { createClient } from "@/lib/supabase/client";
import type { RealtimeChannel } from "@supabase/supabase-js";

const STORAGE_KEY = "tikuvchi:chat_last_read";

type ReadMap = Record<string, string>;

function getReadMap(): ReadMap {
  if (typeof window === "undefined") return {};
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) ?? "{}");
  } catch {
    return {};
  }
}

function saveReadMap(map: ReadMap) {
  if (typeof window === "undefined") return;
  localStorage.setItem(STORAGE_KEY, JSON.stringify(map));
}

export function useUnreadChat() {
  const [unreadConvs, setUnreadConvs] = useState<string[]>([]);
  const supabaseRef = useRef(createClient());
  const channelRef = useRef<RealtimeChannel | null>(null);
  const userIdRef = useRef<string | null>(null);
  const latestPerConvRef = useRef<Record<string, string>>({});

  function recalcFromStorage() {
    const readMap = getReadMap();
    const latestMap = latestPerConvRef.current;
    const unread: string[] = [];
    for (const convId of Object.keys(latestMap)) {
      const latest = latestMap[convId];
      const lastRead = readMap[convId];
      if (!lastRead || latest > lastRead) {
        unread.push(convId);
      }
    }
    setUnreadConvs(unread);
  }

  useEffect(() => {
    const supabase = supabaseRef.current;
    let cancelled = false;

    async function init() {
      const { data: { user } } = await supabase.auth.getUser();
      if (!user || cancelled) return;
      userIdRef.current = user.id;

      const { data: convs } = await supabase
        .from("conversations")
        .select("id")
        .or(`client_id.eq.${user.id},usta_id.eq.${user.id}`);

      if (!convs || convs.length === 0 || cancelled) return;

      const readMap = getReadMap();
      const convIds = convs.map((c) => c.id);

      const { data: msgs } = await supabase
        .from("messages")
        .select("conversation_id, created_at")
        .in("conversation_id", convIds)
        .neq("sender_id", user.id);

      if (!msgs || cancelled) return;

      const unread: string[] = [];
      const latestPerConv: Record<string, string> = {};

      for (const msg of msgs) {
        const prev = latestPerConv[msg.conversation_id];
        if (!prev || msg.created_at > prev) {
          latestPerConv[msg.conversation_id] = msg.created_at;
        }
      }

      latestPerConvRef.current = latestPerConv;

      for (const convId of convIds) {
        const latest = latestPerConv[convId];
        if (!latest) continue;
        const lastRead = readMap[convId];
        if (!lastRead || latest > lastRead) {
          unread.push(convId);
        }
      }

      if (!cancelled) setUnreadConvs(unread);

      const channel = supabase
        .channel("unread-messages")
        .on(
          "postgres_changes",
          {
            event: "INSERT",
            schema: "public",
            table: "messages",
            filter: `conversation_id=in.(${convIds.join(",")})`,
          },
          (payload) => {
            const msg = payload.new as {
              sender_id: string;
              conversation_id: string;
              created_at: string;
            };
            if (msg.sender_id === user.id) return;
            latestPerConvRef.current[msg.conversation_id] = msg.created_at;
            setUnreadConvs((prev) =>
              prev.includes(msg.conversation_id)
                ? prev
                : [...prev, msg.conversation_id]
            );
          },
        )
        .subscribe();

      channelRef.current = channel;
    }

    init();

    const onVisibility = () => {
      if (document.visibilityState === "visible") recalcFromStorage();
    };
    document.addEventListener("visibilitychange", onVisibility);

    return () => {
      cancelled = true;
      document.removeEventListener("visibilitychange", onVisibility);
      if (channelRef.current) {
        supabase.removeChannel(channelRef.current);
        channelRef.current = null;
      }
    };
  }, []);

  const markConversationRead = useCallback((convId: string) => {
    const map = getReadMap();
    map[convId] = new Date().toISOString();
    saveReadMap(map);
    setUnreadConvs((prev) => prev.filter((id) => id !== convId));
  }, []);

  const isUnread = useCallback(
    (convId: string): boolean => unreadConvs.includes(convId),
    [unreadConvs],
  );

  return {
    unreadCount: unreadConvs.length,
    unreadConvs,
    markConversationRead,
    isUnread,
  };
}
