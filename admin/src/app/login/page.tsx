"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import { createClient } from "@/lib/supabase/client";

export default function LoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const inputCls =
    "w-full rounded-xl border border-cream-200 bg-white px-3.5 py-3 text-sm text-ink-900 outline-none focus:border-terra-400";

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    const supabase = createClient();
    const { error: err } = await supabase.auth.signInWithPassword({
      email: email.trim(),
      password,
    });
    setBusy(false);
    if (err) {
      setError(
        err.message.includes("Invalid login credentials")
          ? "Email yoki parol noto'g'ri"
          : "Xatolik yuz berdi. Qayta urinib ko'ring."
      );
      return;
    }
    router.push("/");
    router.refresh();
  }

  return (
    <main className="mx-auto flex min-h-dvh w-full max-w-sm flex-col justify-center px-5 py-10">
      <div className="mb-8 text-center">
        <span className="text-2xl font-extrabold tracking-tight text-ink-900">
          Tikuvchi Admin
        </span>
        <p className="mt-1 text-sm text-ink-500">
          Faqat admin huquqiga ega hisoblar kira oladi
        </p>
      </div>

      <form onSubmit={submit} className="space-y-3">
        <label className="block">
          <span className="mb-1 block text-xs font-bold text-ink-500">Email</span>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            autoComplete="email"
            className={inputCls}
          />
        </label>
        <label className="block">
          <span className="mb-1 block text-xs font-bold text-ink-500">Parol</span>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            autoComplete="current-password"
            className={inputCls}
          />
        </label>

        {error && (
          <p className="rounded-xl bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">
            {error}
          </p>
        )}

        <button
          type="submit"
          disabled={busy}
          className="w-full rounded-xl bg-terra-600 px-4 py-3 text-sm font-bold text-white transition hover:bg-terra-700 disabled:opacity-60"
        >
          {busy ? "Tekshirilmoqda…" : "Kirish"}
        </button>
      </form>
    </main>
  );
}
