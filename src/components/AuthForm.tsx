"use client";

import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { useState } from "react";
import Button from "@/components/Button";
import { IconScissors } from "@/components/Icons";
import { createClient } from "@/lib/supabase/client";
import { t } from "@/lib/i18n";

type Props = { mode: "login" | "register" };

/** Kirish / ro'yxatdan o'tish formasi (email + parol, keyinchalik telefon-OTP uchun joy) */
export default function AuthForm({ mode }: Props) {
  const router = useRouter();
  const searchParams = useSearchParams();
  const nextPath = searchParams.get("next") ?? "/";

  const [fullName, setFullName] = useState("");
  const [phone, setPhone] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [info, setInfo] = useState<string | null>(null);

  const inputCls =
    "w-full rounded-xl border border-cream-200 bg-white px-3.5 py-3 text-sm text-ink-900 outline-none focus:border-terra-400";

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    setInfo(null);
    const supabase = createClient();

    try {
      if (mode === "login") {
        const { error: err } = await supabase.auth.signInWithPassword({
          email: email.trim(),
          password,
        });
        if (err) {
          setError(
            err.message.includes("Invalid login credentials")
              ? t("auth.invalidCredentials")
              : t("auth.genericError")
          );
          return;
        }
      } else {
        const { data, error: err } = await supabase.auth.signUp({
          email: email.trim(),
          password,
          options: {
            data: {
              full_name: fullName.trim(),
              role: "client",
              phone: phone.trim() || null,
            },
          },
        });
        if (err) {
          setError(
            err.message.includes("already registered")
              ? t("auth.emailTaken")
              : t("auth.genericError")
          );
          return;
        }
        // Email tasdiqlash yoqilgan bo'lsa sessiya bo'lmaydi
        if (!data.session) {
          setInfo(t("auth.checkEmail"));
          return;
        }
      }
      router.push(nextPath);
      router.refresh();
    } finally {
      setBusy(false);
    }
  }

  return (
    <main className="mx-auto flex min-h-dvh w-full max-w-md flex-col justify-center px-5 py-10">
      <div className="mb-8 text-center">
        <span className="inline-flex items-center gap-2 text-2xl font-extrabold tracking-tight text-ink-900">
          <IconScissors size={26} className="text-terra-600" />
          {t("app.name")}
        </span>
        <h1 className="mt-4 text-xl font-extrabold text-ink-900">
          {mode === "login" ? t("auth.loginTitle") : t("auth.registerTitle")}
        </h1>
        <p className="mt-1 text-sm text-ink-500">
          {mode === "login" ? t("auth.loginSubtitle") : t("auth.registerSubtitle")}
        </p>
      </div>

      <form onSubmit={submit} className="space-y-3">
        {mode === "register" && (
          <>
            <label className="block">
              <span className="mb-1 block text-xs font-bold text-ink-500">
                {t("auth.fullName")}
              </span>
              <input
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                required
                placeholder="Nilufar Karimova"
                autoComplete="name"
                className={inputCls}
              />
            </label>
            <label className="block">
              <span className="mb-1 block text-xs font-bold text-ink-500">
                {t("auth.phone")}
              </span>
              <input
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                placeholder="+998 90 123 45 67"
                inputMode="tel"
                className={inputCls}
              />
            </label>
          </>
        )}
        <label className="block">
          <span className="mb-1 block text-xs font-bold text-ink-500">
            {t("auth.email")}
          </span>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            placeholder="ism@mail.uz"
            inputMode="email"
            autoComplete="email"
            className={inputCls}
          />
        </label>
        <label className="block">
          <span className="mb-1 block text-xs font-bold text-ink-500">
            {t("auth.password")}
          </span>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            minLength={6}
            placeholder={t("auth.passwordMin")}
            autoComplete={mode === "login" ? "current-password" : "new-password"}
            className={inputCls}
          />
        </label>

        {error && (
          <p className="rounded-xl bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">
            {error}
          </p>
        )}
        {info && (
          <p className="rounded-xl bg-gold-100 px-4 py-3 text-sm font-semibold text-ink-700">
            {info}
          </p>
        )}

        <Button type="submit" size="lg" loading={busy}>
          {mode === "login" ? t("auth.loginBtn") : t("auth.registerBtn")}
        </Button>
      </form>

      <p className="mt-5 text-center text-sm text-ink-500">
        {mode === "login" ? (
          <>
            {t("auth.noAccount")}{" "}
            <Link
              href="/auth/register"
              className="font-bold text-terra-700 hover:underline"
            >
              {t("auth.registerLink")}
            </Link>
          </>
        ) : (
          <>
            {t("auth.haveAccount")}{" "}
            <Link
              href="/auth/login"
              className="font-bold text-terra-700 hover:underline"
            >
              {t("auth.loginLink")}
            </Link>
          </>
        )}
      </p>

      {mode === "login" && (
        <div className="mt-6 rounded-2xl bg-white p-4 text-center shadow-card">
          <p className="text-xs font-bold text-ink-500">{t("auth.demoTitle")}</p>
          <p className="mt-1 text-sm font-semibold text-ink-700">
            {t("auth.demoHint")}
          </p>
        </div>
      )}
    </main>
  );
}
