"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import Button from "@/components/Button";
import PriceTag from "@/components/PriceTag";
import { IconCheck, IconClose } from "@/components/Icons";
import { createClient } from "@/lib/supabase/client";
import { formatDate } from "@/lib/format";
import { t } from "@/lib/i18n";

type Service = {
  id: number;
  title: string;
  description: string | null;
  base_price: number;
};

type Measurement = {
  id: string;
  label: string;
  chest: number | null;
  waist: number | null;
  hips: number | null;
  height: number | null;
  shoulder: number | null;
  sleeve_length: number | null;
};

type Props = {
  ustaId: string;
  ustaName: string;
  services: Service[];
  measurements: Measurement[];
};

const STEPS = ["orderFlow.stepType", "orderFlow.stepSize", "orderFlow.stepFinish"];

const NEW_MEASUREMENT_FIELDS = [
  "chest",
  "waist",
  "hips",
  "height",
  "shoulder",
  "sleeve_length",
] as const;

function defaultReadyDate(): string {
  const d = new Date();
  d.setDate(d.getDate() + 14);
  return d.toISOString().slice(0, 10);
}

/**
 * A yo'l (katalog orqali): 3 bosqichli buyurtma yaratish oqimi.
 * To'liq ekranli — pastki tab-navigatsiya AppShell tomonidan yashiriladi,
 * faqat yuqorida progress-stepper, pastda Orqaga/Davom etish tugmalari.
 */
export default function OrderWizard({
  ustaId,
  ustaName,
  services,
  measurements,
}: Props) {
  const router = useRouter();
  const [step, setStep] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  // 1-bosqich: tur
  const [serviceId, setServiceId] = useState<number | null>(null);
  const [material, setMaterial] = useState("");
  const [modelNote, setModelNote] = useState("");

  // 2-bosqich: o'lcham
  const [measurementId, setMeasurementId] = useState<string | "new" | null>(
    measurements.length > 0 ? measurements[0].id : null
  );
  const [newMeasurement, setNewMeasurement] = useState({
    label: "",
    chest: "",
    waist: "",
    hips: "",
    height: "",
    shoulder: "",
    sleeve_length: "",
  });
  const [sizeNote, setSizeNote] = useState("");

  // 3-bosqich: yakun
  const [readyDate, setReadyDate] = useState(defaultReadyDate());

  const service = services.find((s) => s.id === serviceId) ?? null;

  function validateStep(): string | null {
    if (step === 0 && !service) return t("orderFlow.selectServiceFirst");
    if (step === 1) {
      if (!measurementId) return t("orderFlow.selectMeasurementFirst");
      if (measurementId === "new" && !newMeasurement.label.trim()) {
        return t("orderFlow.selectMeasurementFirst");
      }
    }
    return null;
  }

  function next() {
    const problem = validateStep();
    if (problem) {
      setError(problem);
      return;
    }
    setError(null);
    setStep((s) => Math.min(s + 1, 2));
  }

  function back() {
    setError(null);
    if (step === 0) {
      router.push(`/usta/${ustaId}`);
    } else {
      setStep((s) => s - 1);
    }
  }

  async function submit() {
    if (!service) return;
    setSubmitting(true);
    setError(null);
    const supabase = createClient();

    try {
      const {
        data: { user },
      } = await supabase.auth.getUser();
      if (!user) throw new Error("auth");

      // Yangi o'lcham kiritilgan bo'lsa, avval uni saqlaymiz
      let measurementLabel = "";
      if (measurementId === "new") {
        const values = Object.fromEntries(
          NEW_MEASUREMENT_FIELDS.map((f) => [
            f,
            newMeasurement[f] ? Number(newMeasurement[f]) : null,
          ])
        );
        const { error: mErr } = await supabase.from("measurements").insert({
          client_id: user.id,
          label: newMeasurement.label.trim(),
          ...values,
        });
        if (mErr) throw mErr;
        measurementLabel = newMeasurement.label.trim();
      } else {
        measurementLabel =
          measurements.find((m) => m.id === measurementId)?.label ?? "";
      }

      const { data: order, error: oErr } = await supabase
        .from("orders")
        .insert({
          client_id: user.id,
          usta_id: ustaId,
          source: "catalog",
          status: "pending",
          total_price: service.base_price,
          payment_status: "pending",
          estimated_ready_at: readyDate,
        })
        .select("id")
        .single();
      if (oErr || !order) throw oErr ?? new Error("order");

      const { error: iErr } = await supabase.from("order_items").insert({
        order_id: order.id,
        title: service.title,
        material: material.trim() || null,
        size_note: [measurementLabel, sizeNote.trim()].filter(Boolean).join(" — ") || null,
        model_note: modelNote.trim() || null,
        price: service.base_price,
      });
      if (iErr) throw iErr;

      router.push(`/orders/${order.id}`);
    } catch {
      setError(t("common.error"));
      setSubmitting(false);
    }
  }

  const inputCls =
    "w-full rounded-xl border border-cream-200 bg-white px-3 py-2.5 text-sm text-ink-900 outline-none focus:border-terra-400";
  const selectedMeasurement =
    measurementId && measurementId !== "new"
      ? measurements.find((m) => m.id === measurementId)
      : null;

  return (
    <div className="flex min-h-dvh flex-col">
      {/* Yuqori panel: yopish + progress-stepper */}
      <header className="sticky top-0 z-40 border-b border-cream-200 bg-cream-50/95 backdrop-blur">
        <div className="mx-auto max-w-3xl px-3 pb-3 pt-2">
          <div className="flex items-center justify-between">
            <button
              onClick={() => router.push(`/usta/${ustaId}`)}
              aria-label={t("common.cancel")}
              className="flex h-10 w-10 items-center justify-center rounded-full text-ink-900 hover:bg-cream-200"
            >
              <IconClose />
            </button>
            <span className="text-base font-bold text-ink-900">
              {t("orderFlow.title")}
            </span>
            <span className="w-10" />
          </div>
          {/* Progress-stepper */}
          <div className="mt-1 flex items-center gap-2">
            {STEPS.map((label, i) => (
              <div key={label} className="flex flex-1 flex-col items-center gap-1">
                <div
                  className={`h-1.5 w-full rounded-full transition-colors ${
                    i <= step ? "bg-terra-600" : "bg-cream-300"
                  }`}
                />
                <span
                  className={`whitespace-nowrap text-[11px] font-bold ${
                    i <= step ? "text-terra-700" : "text-ink-500"
                  }`}
                >
                  {i + 1}. {t(label)}
                </span>
              </div>
            ))}
          </div>
        </div>
      </header>

      <main className="mx-auto w-full max-w-3xl flex-1 px-4 py-5 pb-32">
        {step === 0 && (
          <section className="space-y-4">
            <h2 className="text-lg font-extrabold text-ink-900">
              {t("orderFlow.chooseService")}
            </h2>
            <div className="space-y-2">
              {services.map((s) => {
                const active = s.id === serviceId;
                return (
                  <button
                    key={s.id}
                    onClick={() => setServiceId(s.id)}
                    className={`w-full rounded-2xl border-2 bg-white p-4 text-left shadow-card transition-colors ${
                      active ? "border-terra-600" : "border-transparent hover:border-terra-200"
                    }`}
                  >
                    <div className="flex items-center justify-between gap-3">
                      <div className="min-w-0">
                        <p className="font-bold text-ink-900">{s.title}</p>
                        {s.description && (
                          <p className="mt-0.5 text-xs text-ink-500">
                            {s.description}
                          </p>
                        )}
                      </div>
                      <div className="flex shrink-0 items-center gap-2">
                        <PriceTag amount={s.base_price} size="sm" />
                        {active && (
                          <span className="flex h-6 w-6 items-center justify-center rounded-full bg-terra-600 text-white">
                            <IconCheck size={14} />
                          </span>
                        )}
                      </div>
                    </div>
                  </button>
                );
              })}
            </div>

            <label className="block">
              <span className="mb-1 block text-sm font-bold text-ink-700">
                {t("orderFlow.material")}
              </span>
              <input
                value={material}
                onChange={(e) => setMaterial(e.target.value)}
                placeholder={t("orderFlow.materialPlaceholder")}
                className={inputCls}
              />
            </label>

            <label className="block">
              <span className="mb-1 block text-sm font-bold text-ink-700">
                {t("orderFlow.modelNote")}
              </span>
              <textarea
                value={modelNote}
                onChange={(e) => setModelNote(e.target.value)}
                placeholder={t("orderFlow.modelNotePlaceholder")}
                rows={3}
                className={inputCls}
              />
            </label>
          </section>
        )}

        {step === 1 && (
          <section className="space-y-4">
            <h2 className="text-lg font-extrabold text-ink-900">
              {t("orderFlow.chooseMeasurement")}
            </h2>

            {measurements.length === 0 && (
              <p className="rounded-2xl bg-gold-100 px-4 py-3 text-sm font-semibold text-ink-700">
                {t("orderFlow.noMeasurements")}
              </p>
            )}

            <div className="space-y-2">
              {measurements.map((m) => {
                const active = measurementId === m.id;
                return (
                  <button
                    key={m.id}
                    onClick={() => setMeasurementId(m.id)}
                    className={`w-full rounded-2xl border-2 bg-white p-4 text-left shadow-card transition-colors ${
                      active ? "border-terra-600" : "border-transparent hover:border-terra-200"
                    }`}
                  >
                    <div className="flex items-center justify-between">
                      <p className="font-bold text-ink-900">{m.label}</p>
                      {active && (
                        <span className="flex h-6 w-6 items-center justify-center rounded-full bg-terra-600 text-white">
                          <IconCheck size={14} />
                        </span>
                      )}
                    </div>
                    <p className="mt-1 text-xs text-ink-500">
                      {[
                        m.chest && `${t("measurements.chest")}: ${m.chest}`,
                        m.waist && `${t("measurements.waist")}: ${m.waist}`,
                        m.hips && `${t("measurements.hips")}: ${m.hips}`,
                      ]
                        .filter(Boolean)
                        .join(" · ")}
                    </p>
                  </button>
                );
              })}

              <button
                onClick={() => setMeasurementId("new")}
                className={`w-full rounded-2xl border-2 border-dashed p-4 text-left transition-colors ${
                  measurementId === "new"
                    ? "border-terra-600 bg-white"
                    : "border-cream-300 bg-cream-50 hover:border-terra-300"
                }`}
              >
                <p className="font-bold text-terra-700">
                  + {t("orderFlow.newMeasurement")}
                </p>
              </button>
            </div>

            {measurementId === "new" && (
              <div className="space-y-3 rounded-2xl bg-white p-4 shadow-card">
                <label className="block">
                  <span className="mb-1 block text-xs font-bold text-ink-500">
                    {t("measurements.label")}
                  </span>
                  <input
                    value={newMeasurement.label}
                    onChange={(e) =>
                      setNewMeasurement({ ...newMeasurement, label: e.target.value })
                    }
                    placeholder={t("measurements.labelPlaceholder")}
                    className={inputCls}
                  />
                </label>
                <div className="grid grid-cols-2 gap-3">
                  {NEW_MEASUREMENT_FIELDS.map((field) => (
                    <label key={field} className="block">
                      <span className="mb-1 block text-xs font-bold text-ink-500">
                        {t(`measurements.${field}`)} ({t("measurements.cm")})
                      </span>
                      <input
                        type="number"
                        inputMode="decimal"
                        min={0}
                        value={newMeasurement[field]}
                        onChange={(e) =>
                          setNewMeasurement({
                            ...newMeasurement,
                            [field]: e.target.value,
                          })
                        }
                        className={inputCls}
                      />
                    </label>
                  ))}
                </div>
              </div>
            )}

            <label className="block">
              <span className="mb-1 block text-sm font-bold text-ink-700">
                {t("orderFlow.sizeNote")}
              </span>
              <input
                value={sizeNote}
                onChange={(e) => setSizeNote(e.target.value)}
                placeholder={t("orderFlow.sizeNotePlaceholder")}
                className={inputCls}
              />
            </label>
          </section>
        )}

        {step === 2 && service && (
          <section className="space-y-4">
            <h2 className="text-lg font-extrabold text-ink-900">
              {t("orderFlow.summary")}
            </h2>
            <div className="space-y-3 rounded-2xl bg-white p-4 shadow-card">
              <Row label={t("orderFlow.usta")} value={ustaName} />
              <Row label={t("orderFlow.service")} value={service.title} />
              {material.trim() && (
                <Row label={t("orderFlow.material")} value={material.trim()} />
              )}
              <Row
                label={t("orderFlow.measurement")}
                value={
                  measurementId === "new"
                    ? newMeasurement.label
                    : selectedMeasurement?.label ?? "—"
                }
              />
              <div className="flex items-center justify-between border-t border-cream-200 pt-3">
                <span className="text-sm font-bold text-ink-700">
                  {t("orderFlow.price")}
                </span>
                <PriceTag amount={service.base_price} size="lg" />
              </div>
            </div>

            <label className="block">
              <span className="mb-1 block text-sm font-bold text-ink-700">
                {t("orderFlow.estimatedReady")}
              </span>
              <input
                type="date"
                value={readyDate}
                min={new Date().toISOString().slice(0, 10)}
                onChange={(e) => setReadyDate(e.target.value)}
                className={inputCls}
              />
              <span className="mt-1 block text-xs text-ink-500">
                {formatDate(readyDate)}
              </span>
            </label>
          </section>
        )}

        {error && (
          <p className="mt-4 rounded-xl bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">
            {error}
          </p>
        )}
      </main>

      {/* Pastki panel: Orqaga / Davom etish */}
      <footer className="fixed inset-x-0 bottom-0 z-40 border-t border-cream-200 bg-cream-50/95 backdrop-blur">
        <div className="mx-auto flex max-w-3xl gap-2 px-4 py-3 pb-safe">
          <Button variant="outline" onClick={back} disabled={submitting} className="flex-1">
            {t("common.back")}
          </Button>
          {step < 2 ? (
            <Button onClick={next} className="flex-[2]">
              {t("common.next")}
            </Button>
          ) : (
            <Button onClick={submit} loading={submitting} className="flex-[2]">
              {submitting ? t("orderFlow.creating") : t("orderFlow.confirmOrder")}
            </Button>
          )}
        </div>
      </footer>
    </div>
  );
}

function Row({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex items-start justify-between gap-3">
      <span className="text-sm font-semibold text-ink-500">{label}</span>
      <span className="text-right text-sm font-bold text-ink-900">{value}</span>
    </div>
  );
}
