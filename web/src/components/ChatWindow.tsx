"use client";

import Link from "next/link";
import { useCallback, useEffect, useRef, useState } from "react";
import AppHeader from "@/components/AppHeader";
import Avatar from "@/components/Avatar";
import PriceOfferBubble from "@/components/PriceOfferBubble";
import { Spinner } from "@/components/Button";
import { IconImage, IconSend } from "@/components/Icons";
import { createClient } from "@/lib/supabase/client";
import { formatChatTime } from "@/lib/format";
import { t } from "@/lib/i18n";
import type { Tables } from "@/lib/database.types";

type Message = Tables<"messages">;

type Props = {
  ustaId: string;
  ustaName: string;
  ustaAvatarUrl: string | null;
  initialConversationId: string | null;
  currentUserId: string;
};

/**
 * Chat oynasi: matn, rasm va narx-taklif kartalari, Supabase Realtime bilan.
 * B yo'l: usta yuborgan narx taklifi qabul qilinsa, avtomatik buyurtma yaratiladi.
 */
export default function ChatWindow({
  ustaId,
  ustaName,
  ustaAvatarUrl,
  initialConversationId,
  currentUserId,
}: Props) {
  const [conversationId, setConversationId] = useState(initialConversationId);
  const [messages, setMessages] = useState<Message[]>([]);
  const [loading, setLoading] = useState(Boolean(initialConversationId));
  const [input, setInput] = useState("");
  const [sending, setSending] = useState(false);
  const [respondingTo, setRespondingTo] = useState<string | null>(null);
  const [acceptedOrderId, setAcceptedOrderId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const bottomRef = useRef<HTMLDivElement>(null);
  const fileRef = useRef<HTMLInputElement>(null);
  const supabaseRef = useRef(createClient());

  // Xabarlarni yuklash
  useEffect(() => {
    if (!conversationId) return;
    const supabase = supabaseRef.current;
    let cancelled = false;

    supabase
      .from("messages")
      .select("*")
      .eq("conversation_id", conversationId)
      .order("created_at")
      .then(({ data }) => {
        if (!cancelled && data) setMessages(data);
        setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [conversationId]);

  // Realtime obuna: yangi xabarlar va taklif holati o'zgarishlari
  useEffect(() => {
    if (!conversationId) return;
    const supabase = supabaseRef.current;

    const channel = supabase
      .channel(`messages:${conversationId}`)
      .on(
        "postgres_changes",
        {
          event: "INSERT",
          schema: "public",
          table: "messages",
          filter: `conversation_id=eq.${conversationId}`,
        },
        (payload) => {
          const msg = payload.new as Message;
          setMessages((prev) =>
            prev.some((m) => m.id === msg.id) ? prev : [...prev, msg]
          );
        }
      )
      .on(
        "postgres_changes",
        {
          event: "UPDATE",
          schema: "public",
          table: "messages",
          filter: `conversation_id=eq.${conversationId}`,
        },
        (payload) => {
          const msg = payload.new as Message;
          setMessages((prev) => prev.map((m) => (m.id === msg.id ? msg : m)));
        }
      )
      .subscribe();

    return () => {
      supabase.removeChannel(channel);
    };
  }, [conversationId]);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages.length]);

  // Suhbat hali bo'lmasa, birinchi xabarda yaratamiz
  const ensureConversation = useCallback(async (): Promise<string> => {
    if (conversationId) return conversationId;
    const supabase = supabaseRef.current;
    const { data, error: convErr } = await supabase
      .from("conversations")
      .upsert(
        { client_id: currentUserId, usta_id: ustaId },
        { onConflict: "client_id,usta_id" }
      )
      .select("id")
      .single();
    if (convErr || !data) throw convErr ?? new Error("conversation");
    setConversationId(data.id);
    return data.id;
  }, [conversationId, currentUserId, ustaId]);

  async function sendText() {
    const content = input.trim();
    if (!content || sending) return;
    setSending(true);
    setError(null);
    try {
      const convId = await ensureConversation();
      const { data, error: msgErr } = await supabaseRef.current
        .from("messages")
        .insert({
          conversation_id: convId,
          sender_id: currentUserId,
          content,
          message_type: "text",
        })
        .select("*")
        .single();
      if (msgErr) throw msgErr;
      if (data) {
        setMessages((prev) =>
          prev.some((m) => m.id === data.id) ? prev : [...prev, data]
        );
      }
      setInput("");
    } catch {
      setError(t("common.error"));
    } finally {
      setSending(false);
    }
  }

  async function sendImage(file: File) {
    setSending(true);
    setError(null);
    try {
      const supabase = supabaseRef.current;
      const convId = await ensureConversation();
      const path = `${currentUserId}/${Date.now()}-${file.name.replace(/[^\w.-]/g, "_")}`;
      const { error: upErr } = await supabase.storage
        .from("chat-images")
        .upload(path, file);
      if (upErr) throw upErr;
      const {
        data: { publicUrl },
      } = supabase.storage.from("chat-images").getPublicUrl(path);

      const { data, error: msgErr } = await supabase
        .from("messages")
        .insert({
          conversation_id: convId,
          sender_id: currentUserId,
          content: publicUrl,
          message_type: "image",
        })
        .select("*")
        .single();
      if (msgErr) throw msgErr;
      if (data) {
        setMessages((prev) =>
          prev.some((m) => m.id === data.id) ? prev : [...prev, data]
        );
      }
    } catch {
      setError(t("common.error"));
    } finally {
      setSending(false);
      if (fileRef.current) fileRef.current.value = "";
    }
  }

  /** Narx taklifini qabul qilish → xabar holati + avtomatik buyurtma */
  async function respondToOffer(msg: Message, accept: boolean) {
    setRespondingTo(msg.id);
    setError(null);
    const supabase = supabaseRef.current;
    try {
      const { error: updErr } = await supabase
        .from("messages")
        .update({ price_offer_status: accept ? "accepted" : "declined" })
        .eq("id", msg.id);
      if (updErr) throw updErr;

      setMessages((prev) =>
        prev.map((m) =>
          m.id === msg.id
            ? { ...m, price_offer_status: accept ? "accepted" : "declined" }
            : m
        )
      );

      if (accept && msg.price_offer_amount) {
        const readyDate = new Date();
        readyDate.setDate(readyDate.getDate() + (msg.price_offer_duration_days ?? 14));

        const { data: order, error: ordErr } = await supabase
          .from("orders")
          .insert({
            client_id: currentUserId,
            usta_id: ustaId,
            source: "chat_negotiation",
            status: "accepted",
            total_price: msg.price_offer_amount,
            payment_status: "pending",
            estimated_ready_at: readyDate.toISOString().slice(0, 10),
          })
          .select("id")
          .single();
        if (ordErr || !order) throw ordErr ?? new Error("order");

        await supabase.from("order_items").insert({
          order_id: order.id,
          title: msg.price_offer_note ?? t("chat.priceOffer"),
          price: msg.price_offer_amount,
        });

        setAcceptedOrderId(order.id);
      }
    } catch {
      setError(t("common.error"));
    } finally {
      setRespondingTo(null);
    }
  }

  return (
    <div className="flex h-dvh flex-col">
      <AppHeader back backHref="/chat" title={ustaName} />

      <main className="mx-auto w-full max-w-3xl flex-1 space-y-3 overflow-y-auto px-4 py-4">
        {loading ? (
          <div className="flex justify-center py-10">
            <Spinner className="h-6 w-6 text-terra-600" />
          </div>
        ) : (
          messages.map((msg) => {
            const mine = msg.sender_id === currentUserId;
            return (
              <div
                key={msg.id}
                className={`flex items-end gap-2 ${mine ? "justify-end" : "justify-start"}`}
              >
                {!mine && (
                  <Avatar name={ustaName} src={ustaAvatarUrl} size="sm" />
                )}
                <div className={`max-w-[80%] ${mine ? "items-end" : "items-start"}`}>
                  {msg.message_type === "price_offer" &&
                  msg.price_offer_amount ? (
                    <PriceOfferBubble
                      amount={msg.price_offer_amount}
                      durationDays={msg.price_offer_duration_days}
                      note={msg.price_offer_note}
                      status={msg.price_offer_status}
                      canRespond={!mine}
                      responding={respondingTo === msg.id}
                      onAccept={() => respondToOffer(msg, true)}
                      onDecline={() => respondToOffer(msg, false)}
                    />
                  ) : msg.message_type === "image" ? (
                    // eslint-disable-next-line @next/next/no-img-element
                    <img
                      src={msg.content ?? ""}
                      alt={t("chat.photo")}
                      className="max-h-64 rounded-2xl object-cover shadow-card"
                    />
                  ) : (
                    <p
                      className={`rounded-2xl px-4 py-2.5 text-sm leading-relaxed shadow-card ${
                        mine
                          ? "rounded-br-md bg-terra-600 text-white"
                          : "rounded-bl-md bg-white text-ink-900"
                      }`}
                    >
                      {msg.content}
                    </p>
                  )}
                  <span
                    className={`mt-1 block text-[10px] text-ink-300 ${mine ? "text-right" : ""}`}
                  >
                    {formatChatTime(msg.created_at)}
                  </span>
                </div>
              </div>
            );
          })
        )}

        {acceptedOrderId && (
          <Link
            href={`/orders/${acceptedOrderId}`}
            className="block rounded-2xl bg-green-100 px-4 py-3 text-center text-sm font-bold text-green-800 hover:bg-green-200"
          >
            ✅ {t("chat.offerAcceptedInfo")}
          </Link>
        )}
        {error && (
          <p className="rounded-xl bg-red-50 px-4 py-2 text-center text-sm font-semibold text-red-700">
            {error}
          </p>
        )}
        <div ref={bottomRef} />
      </main>

      {/* Xabar yozish paneli */}
      <footer className="border-t border-cream-200 bg-cream-50">
        <div className="mx-auto flex max-w-3xl items-center gap-2 px-3 py-2.5 pb-safe">
          <input
            ref={fileRef}
            type="file"
            accept="image/*"
            hidden
            onChange={(e) => {
              const file = e.target.files?.[0];
              if (file) sendImage(file);
            }}
          />
          <button
            onClick={() => fileRef.current?.click()}
            disabled={sending}
            aria-label={t("chat.sendImage")}
            className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full text-ink-500 hover:bg-cream-200 disabled:opacity-50"
          >
            <IconImage size={21} />
          </button>
          <input
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === "Enter" && !e.shiftKey) {
                e.preventDefault();
                sendText();
              }
            }}
            placeholder={t("chat.inputPlaceholder")}
            className="h-11 flex-1 rounded-full border border-cream-200 bg-white px-4 text-sm text-ink-900 outline-none placeholder:text-ink-300 focus:border-terra-400"
          />
          <button
            onClick={sendText}
            disabled={sending || !input.trim()}
            aria-label={t("chat.send")}
            className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-terra-600 text-white transition-colors hover:bg-terra-700 disabled:bg-terra-300"
          >
            {sending ? <Spinner /> : <IconSend size={19} />}
          </button>
        </div>
      </footer>
    </div>
  );
}
