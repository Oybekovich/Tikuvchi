"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import Avatar from "@/components/Avatar";
import Button from "@/components/Button";
import { IconLogout } from "@/components/Icons";
import { createClient } from "@/lib/supabase/client";
import { signOut } from "@/lib/auth";
import { t } from "@/lib/i18n";
import type { Enums } from "@/lib/database.types";

type Props = {
  email: string;
  fullName: string;
  phone: string;
  role: Enums<"user_role">;
  userId: string;
};

export default function ProfileForm({ email, fullName, phone, role, userId }: Props) {
  const router = useRouter();
  const [name, setName] = useState(fullName);
  const [phoneValue, setPhoneValue] = useState(phone);
  const [saving, setSaving] = useState(false);
  const [saved, setSaved] = useState(false);
  const [loggingOut, setLoggingOut] = useState(false);
  const [confirmingLogout, setConfirmingLogout] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const inputCls =
    "w-full rounded-xl border border-cream-200 bg-white px-3 py-2.5 text-sm text-ink-900 outline-none focus:border-terra-400 disabled:bg-cream-100 disabled:text-ink-500";

  async function save() {
    if (!name.trim()) return;
    setSaving(true);
    setSaved(false);
    setError(null);
    const supabase = createClient();
    const { error: err } = await supabase
      .from("profiles")
      .update({ full_name: name.trim(), phone: phoneValue.trim() || null })
      .eq("id", userId);
    if (err) {
      setError(t("common.error"));
    } else {
      setSaved(true);
      router.refresh();
    }
    setSaving(false);
  }

  async function logout() {
    setLoggingOut(true);
    await signOut();
    router.push("/auth/login");
    router.refresh();
  }

  return (
    <div className="mt-4 space-y-4">
      {/* Profil kartasi */}
      <div className="flex items-center gap-4 rounded-2xl bg-white p-4 shadow-card">
        <Avatar name={name || email} size="xl" />
        <div className="min-w-0">
          <p className="text-lg font-extrabold text-ink-900">{name}</p>
          <p className="text-sm text-ink-500">{email}</p>
          <span className="mt-1 inline-block rounded-full bg-terra-50 px-3 py-0.5 text-xs font-bold text-terra-700">
            {t(`profile.role_${role}`)}
          </span>
        </div>
      </div>

      {/* Sozlamalar */}
      <div className="space-y-3 rounded-2xl bg-white p-4 shadow-card">
        <h2 className="font-extrabold text-ink-900">{t("profile.settings")}</h2>
        <label className="block">
          <span className="mb-1 block text-xs font-bold text-ink-500">
            {t("profile.fullName")}
          </span>
          <input
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Nilufar Karimova"
            autoComplete="name"
            className={inputCls}
          />
        </label>
        <label className="block">
          <span className="mb-1 block text-xs font-bold text-ink-500">
            {t("profile.phone")}
          </span>
          <input
            value={phoneValue}
            onChange={(e) => setPhoneValue(e.target.value)}
            placeholder="+998 90 123 45 67"
            inputMode="tel"
            className={inputCls}
          />
        </label>
        <label className="block">
          <span className="mb-1 block text-xs font-bold text-ink-500">
            {t("profile.email")}
          </span>
          <input value={email} disabled className={inputCls} />
        </label>
        {error && (
          <p className="rounded-xl bg-red-50 px-3 py-2 text-sm font-semibold text-red-700">
            {error}
          </p>
        )}
        <Button onClick={save} loading={saving} size="lg">
          {saved ? `✓ ${t("profile.saved")}` : t("profile.saveChanges")}
        </Button>
      </div>

      {/* Chiqish */}
      {confirmingLogout ? (
        <div className="flex items-center gap-2 rounded-2xl bg-red-50 p-3">
          <span className="flex-1 text-sm font-semibold text-red-700">
            {t("profile.logoutConfirm")}
          </span>
          <Button variant="danger" onClick={logout} loading={loggingOut}>
            {t("common.confirm")}
          </Button>
          <Button
            variant="ghost"
            onClick={() => setConfirmingLogout(false)}
            disabled={loggingOut}
          >
            {t("common.close")}
          </Button>
        </div>
      ) : (
        <Button
          variant="danger"
          size="lg"
          onClick={() => setConfirmingLogout(true)}
        >
          <IconLogout size={18} />
          {t("profile.logout")}
        </Button>
      )}
    </div>
  );
}
