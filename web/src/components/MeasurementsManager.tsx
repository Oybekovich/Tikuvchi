"use client";

import { useState } from "react";
import Button from "@/components/Button";
import EmptyState from "@/components/EmptyState";
import { IconEdit, IconPlus, IconTrash } from "@/components/Icons";
import {
  PhRuler,
} from "@/components/PhosphorIcons";
import { createClient } from "@/lib/supabase/client";
import { formatDate } from "@/lib/format";
import { t } from "@/lib/i18n";
import type { Tables } from "@/lib/database.types";

type Measurement = Tables<"measurements">;

const NUMERIC_FIELDS = [
  "chest",
  "waist",
  "hips",
  "height",
  "shoulder",
  "sleeve_length",
] as const;

type FormState = {
  label: string;
  notes: string;
} & Record<(typeof NUMERIC_FIELDS)[number], string>;

const EMPTY_FORM: FormState = {
  label: "",
  notes: "",
  chest: "",
  waist: "",
  hips: "",
  height: "",
  shoulder: "",
  sleeve_length: "",
};

function toForm(m: Measurement): FormState {
  return {
    label: m.label,
    notes: m.notes ?? "",
    chest: m.chest?.toString() ?? "",
    waist: m.waist?.toString() ?? "",
    hips: m.hips?.toString() ?? "",
    height: m.height?.toString() ?? "",
    shoulder: m.shoulder?.toString() ?? "",
    sleeve_length: m.sleeve_length?.toString() ?? "",
  };
}

/** Saqlangan tana o'lchovlari: qo'shish, tahrirlash, o'chirish */
export default function MeasurementsManager({
  initial,
  userId,
}: {
  initial: Measurement[];
  userId: string;
}) {
  const [items, setItems] = useState(initial);
  const [editingId, setEditingId] = useState<string | "new" | null>(null);
  const [form, setForm] = useState<FormState>(EMPTY_FORM);
  const [busy, setBusy] = useState(false);
  const [deletingId, setDeletingId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const inputCls =
    "w-full rounded-xl border border-cream-200 bg-white px-3 py-2.5 text-sm text-ink-900 outline-none focus:border-terra-400";

  function startNew() {
    setForm(EMPTY_FORM);
    setEditingId("new");
    setError(null);
  }

  function startEdit(m: Measurement) {
    setForm(toForm(m));
    setEditingId(m.id);
    setError(null);
  }

  async function save() {
    if (!form.label.trim()) {
      setError(t("measurements.labelPlaceholder"));
      return;
    }
    setBusy(true);
    setError(null);
    const supabase = createClient();

    const values = {
      label: form.label.trim(),
      notes: form.notes.trim() || null,
      updated_at: new Date().toISOString(),
      ...Object.fromEntries(
        NUMERIC_FIELDS.map((f) => [f, form[f] ? Number(form[f]) : null])
      ),
    };

    try {
      if (editingId === "new") {
        const { data, error: err } = await supabase
          .from("measurements")
          .insert({ client_id: userId, ...values })
          .select("*")
          .single();
        if (err || !data) throw err;
        setItems([data, ...items]);
      } else {
        const { data, error: err } = await supabase
          .from("measurements")
          .update(values)
          .eq("id", editingId!)
          .select("*")
          .single();
        if (err || !data) throw err;
        setItems(items.map((m) => (m.id === data.id ? data : m)));
      }
      setEditingId(null);
    } catch {
      setError(t("common.error"));
    } finally {
      setBusy(false);
    }
  }

  async function remove(id: string) {
    setBusy(true);
    const supabase = createClient();
    const { error: err } = await supabase
      .from("measurements")
      .delete()
      .eq("id", id);
    if (!err) setItems(items.filter((m) => m.id !== id));
    setDeletingId(null);
    setBusy(false);
  }

  return (
    <div className="mt-4 space-y-3">
      {editingId === null && (
        <Button onClick={startNew} size="lg">
          <IconPlus size={18} />
          {t("measurements.add")}
        </Button>
      )}

      {/* Qo'shish/tahrirlash formasi */}
      {editingId !== null && (
        <div className="space-y-3 rounded-2xl bg-white p-4 shadow-card">
          <h2 className="font-extrabold text-ink-900">
            {editingId === "new"
              ? t("measurements.add")
              : t("measurements.editTitle")}
          </h2>
          <label className="block">
            <span className="mb-1 block text-xs font-bold text-ink-500">
              {t("measurements.label")}
            </span>
            <input
              value={form.label}
              onChange={(e) => setForm({ ...form, label: e.target.value })}
              placeholder={t("measurements.labelPlaceholder")}
              className={inputCls}
            />
          </label>
          <div className="grid grid-cols-2 gap-3">
            {NUMERIC_FIELDS.map((field) => (
              <label key={field} className="block">
                <span className="mb-1 block text-xs font-bold text-ink-500">
                  {t(`measurements.${field}`)} ({t("measurements.cm")})
                </span>
                <input
                  type="number"
                  inputMode="decimal"
                  min={0}
                  value={form[field]}
                  onChange={(e) => setForm({ ...form, [field]: e.target.value })}
                  className={inputCls}
                />
              </label>
            ))}
          </div>
          <label className="block">
            <span className="mb-1 block text-xs font-bold text-ink-500">
              {t("measurements.notes")}
            </span>
            <textarea
              value={form.notes}
              onChange={(e) => setForm({ ...form, notes: e.target.value })}
              rows={2}
              className={inputCls}
            />
          </label>
          {error && (
            <p className="rounded-xl bg-red-50 px-3 py-2 text-sm font-semibold text-red-700">
              {error}
            </p>
          )}
          <div className="flex gap-2">
            <Button onClick={save} loading={busy} className="flex-1">
              {t("common.save")}
            </Button>
            <Button
              variant="ghost"
              onClick={() => setEditingId(null)}
              disabled={busy}
            >
              {t("common.cancel")}
            </Button>
          </div>
        </div>
      )}

      {/* Ro'yxat */}
      {items.length === 0 && editingId === null ? (
        <EmptyState
          icon={<PhRuler size={30} />}
          title={t("measurements.empty")}
          hint={t("measurements.emptyHint")}
        />
      ) : (
        items.map((m) => (
          <div key={m.id} className="rounded-2xl bg-white p-4 shadow-card">
            <div className="flex items-center justify-between gap-2">
              <h3 className="font-bold text-ink-900">{m.label}</h3>
              <div className="flex gap-1">
                <button
                  onClick={() => startEdit(m)}
                  aria-label={t("common.edit")}
                  className="flex h-9 w-9 items-center justify-center rounded-full text-ink-500 hover:bg-cream-200"
                >
                  <IconEdit size={17} />
                </button>
                <button
                  onClick={() => setDeletingId(m.id)}
                  aria-label={t("common.delete")}
                  className="flex h-9 w-9 items-center justify-center rounded-full text-red-600 hover:bg-red-50"
                >
                  <IconTrash size={17} />
                </button>
              </div>
            </div>

            <dl className="mt-2 grid grid-cols-3 gap-2 sm:grid-cols-6">
              {NUMERIC_FIELDS.map((field) =>
                m[field] !== null ? (
                  <div key={field} className="rounded-xl bg-cream-100 px-2 py-1.5 text-center">
                    <dt className="text-[10px] font-semibold text-ink-500">
                      {t(`measurements.${field}`)}
                    </dt>
                    <dd className="text-sm font-extrabold text-ink-900">
                      {m[field]}
                    </dd>
                  </div>
                ) : null
              )}
            </dl>

            {m.notes && <p className="mt-2 text-xs text-ink-500">{m.notes}</p>}
            <p className="mt-2 text-[11px] text-ink-300">
              {t("measurements.updatedAt", { date: formatDate(m.updated_at) })}
            </p>

            {deletingId === m.id && (
              <div className="mt-3 flex items-center gap-2 rounded-xl bg-red-50 p-3">
                <span className="flex-1 text-xs font-semibold text-red-700">
                  {t("measurements.deleteConfirm")}
                </span>
                <Button variant="danger" onClick={() => remove(m.id)} loading={busy}>
                  {t("common.delete")}
                </Button>
                <Button
                  variant="ghost"
                  onClick={() => setDeletingId(null)}
                  disabled={busy}
                >
                  {t("common.close")}
                </Button>
              </div>
            )}
          </div>
        ))
      )}
    </div>
  );
}
