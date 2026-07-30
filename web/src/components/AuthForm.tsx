"use client";

import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { useState } from "react";
import Button from "@/components/Button";
import { IconScissors } from "@/components/Icons";
import { createClient } from "@/lib/supabase/client";
import { PHONE_PREFIX, formatPhone } from "@/lib/format";
import {
  type ValidationError,
  validateEmail,
  validateFullName,
  validatePassword,
  validatePhone,
} from "@/lib/validation";
import { t } from "@/lib/i18n";

type Props = { mode: "login" | "register" };

type FieldName = "fullName" | "phone" | "email" | "password";
type Errors = Partial<Record<FieldName, ValidationError | null>>;

/** Maydon ostidagi xato satri — brauzerning inglizcha oynasi o'rniga */
function FieldError({ error }: { error: ValidationError | null | undefined }) {
  if (!error) return null;
  return (
    <span className="mt-1 block text-xs font-semibold text-red-700">
      {t(error.key, error.vars)}
    </span>
  );
}

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
  const [errors, setErrors] = useState<Errors>({});
  // Xatolarni faqat birinchi yuborishdan keyin ko'rsatamiz — yozayotgan
  // paytda darrov qizarish bezovta qiladi
  const [checked, setChecked] = useState(false);

  const inputCls =
    "w-full rounded-xl border bg-white px-3.5 py-3 text-sm text-ink-900 outline-none focus:border-terra-400";
  const fieldCls = (name: FieldName) =>
    `${inputCls} ${errors[name] ? "border-red-400" : "border-cream-200"}`;

  function collectErrors(): Errors {
    const next: Errors = {
      email: validateEmail(email),
      password: validatePassword(password),
    };
    if (mode === "register") {
      next.fullName = validateFullName(fullName);
      next.phone = validatePhone(phone);
    }
    return next;
  }

  /** Yuborishdan keyin har o'zgarishda qayta tekshiramiz */
  function revalidate(patch: Partial<Record<FieldName, string>>) {
    if (!checked) return;
    setErrors((prev) => {
      const next = { ...prev };
      if ("email" in patch) next.email = validateEmail(patch.email!);
      if ("password" in patch) next.password = validatePassword(patch.password!);
      if ("fullName" in patch) next.fullName = validateFullName(patch.fullName!);
      if ("phone" in patch) next.phone = validatePhone(patch.phone!);
      return next;
    });
  }

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setChecked(true);
    setError(null);
    setInfo(null);

    const found = collectErrors();
    setErrors(found);
    if (Object.values(found).some(Boolean)) return;

    setBusy(true);
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
        // Enter bilan yuborilsa onBlur ishlamay qolishi mumkin — prefiksni bu yerda ham tekshiramiz
        const phoneValue = phone.trim() === PHONE_PREFIX.trim() ? "" : phone.trim();
        const { data, error: err } = await supabase.auth.signUp({
          email: email.trim(),
          password,
          options: {
            data: {
              full_name: fullName.trim(),
              role: "client",
              phone: phoneValue || null,
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

  // Pastdagi qo'shimcha bo'shliq kontentni markazdan biroz yuqoriga suradi —
  // matematik markaz vizual jihatdan pastroq ko'rinadi (Android bilan bir xil)
  return (
    <main className="mx-auto flex min-h-dvh w-full max-w-md flex-col justify-center px-5 pt-10 pb-[calc(2.5rem+10dvh)]">
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

      {/* noValidate: brauzerning inglizcha tekshiruv oynasi o'chiriladi,
          xabarlar o'zbekcha va maydon ostida ko'rsatiladi */}
      <form onSubmit={submit} noValidate className="space-y-3">
        {mode === "register" && (
          <>
            <label className="block">
              <span className="mb-1 block text-xs font-bold text-ink-500">
                {t("auth.fullName")}
              </span>
              <input
                value={fullName}
                onChange={(e) => {
                  setFullName(e.target.value);
                  revalidate({ fullName: e.target.value });
                }}
                placeholder="Ism familiya"
                autoComplete="name"
                aria-invalid={Boolean(errors.fullName)}
                className={fieldCls("fullName")}
              />
              <FieldError error={errors.fullName} />
            </label>
            <label className="block">
              <span className="mb-1 block text-xs font-bold text-ink-500">
                {t("auth.phone")}
              </span>
              <input
                value={phone}
                onChange={(e) => {
                  const next = formatPhone(e.target.value);
                  setPhone(next);
                  revalidate({ phone: next });
                }}
                onFocus={() => {
                  if (!phone) setPhone(PHONE_PREFIX);
                }}
                onBlur={() => {
                  // Faqat prefiks qolgan bo'lsa — raqam kiritilmagan hisoblanadi
                  if (phone.trim() === PHONE_PREFIX.trim()) setPhone("");
                }}
                placeholder="+998 90 123 45 67"
                inputMode="tel"
                autoComplete="tel"
                aria-invalid={Boolean(errors.phone)}
                className={fieldCls("phone")}
              />
              <FieldError error={errors.phone} />
            </label>
          </>
        )}
        <label className="block">
          <span className="mb-1 block text-xs font-bold text-ink-500">
            {t("auth.email")}
          </span>
          <input
            // type="email" mobil klaviaturani to'g'ri ochadi; tekshiruvni
            // esa formadagi noValidate o'chiradi
            type="email"
            value={email}
            onChange={(e) => {
              setEmail(e.target.value);
              revalidate({ email: e.target.value });
            }}
            placeholder="ism@mail.uz"
            inputMode="email"
            autoComplete="email"
            aria-invalid={Boolean(errors.email)}
            className={fieldCls("email")}
          />
          <FieldError error={errors.email} />
        </label>
        <label className="block">
          <span className="mb-1 block text-xs font-bold text-ink-500">
            {t("auth.password")}
          </span>
          <input
            type="password"
            value={password}
            onChange={(e) => {
              setPassword(e.target.value);
              revalidate({ password: e.target.value });
            }}
            placeholder={t("auth.passwordMin")}
            autoComplete={mode === "login" ? "current-password" : "new-password"}
            aria-invalid={Boolean(errors.password)}
            className={fieldCls("password")}
          />
          <FieldError error={errors.password} />
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
    </main>
  );
}
