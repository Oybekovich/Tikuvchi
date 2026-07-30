import { readFileSync, readdirSync, statSync } from "node:fs";
import { join } from "node:path";
import { describe, expect, it } from "vitest";
import uz from "@/locales/uz.json";

/**
 * Eng qimmatli test: kodda ishlatilgan har bir tarjima kaliti uz.json da
 * borligini tekshiradi.
 *
 * i18n'dagi t() topilmagan kalitni XATO BERMASDAN o'zini qaytaradi — ya'ni
 * ekranda "orders.confirmReceived" degan yozuv paydo bo'ladi va buni faqat
 * qo'lda ko'rib sezish mumkin. Bu test uni build paytida tutadi.
 */

const SRC = new URL("../src", import.meta.url).pathname;

function walk(dir: string): string[] {
  return readdirSync(dir).flatMap((entry) => {
    const full = join(dir, entry);
    if (statSync(full).isDirectory()) return walk(full);
    return /\.tsx?$/.test(full) ? [full] : [];
  });
}

function has(key: string): boolean {
  let node: unknown = uz;
  for (const part of key.split(".")) {
    if (typeof node !== "object" || node === null) return false;
    node = (node as Record<string, unknown>)[part];
  }
  return typeof node === "string";
}

const FAYLLAR = walk(SRC).filter((f) => !f.includes("/locales/"));

describe("tarjima kalitlari", () => {
  it("kod bo'ylab kamida bir nechta kalit topildi (skaner ishlayapti)", () => {
    const matnlar = FAYLLAR.map((f) => readFileSync(f, "utf8")).join("\n");
    const topilgan = matnlar.match(/\bt\(\s*"[^"]+"/g) ?? [];
    expect(topilgan.length).toBeGreaterThan(50);
  });

  it("statik kalitlarning HAMMASI uz.json da bor", () => {
    const yoq: string[] = [];

    for (const fayl of FAYLLAR) {
      const matn = readFileSync(fayl, "utf8");
      for (const m of matn.matchAll(/\bt\(\s*"([^"]+)"/g)) {
        const key = m[1];
        if (!has(key)) yoq.push(`${fayl.replace(SRC, "src")} -> ${key}`);
      }
    }

    expect(yoq).toEqual([]);
  });

  /**
   * Shablon bilan quriladigan kalitlar (t(`payment.${step}`)) skanerga
   * tushmaydi, shuning uchun har bir oila to'liq sanab tekshiriladi.
   */
  it("dinamik kalit oilalari to'liq", () => {
    const oilalar: Record<string, string[]> = {
      orderStatus: [
        "pending", "accepted", "in_progress", "ready", "completed", "cancelled",
      ],
      stepper: ["accepted", "in_progress", "ready"],
      payment: ["pending", "partial", "paid"],
      measurements: [
        "chest", "waist", "hips", "height", "shoulder", "sleeve_length",
      ],
    };

    const yoq: string[] = [];
    for (const [oila, qiymatlar] of Object.entries(oilalar)) {
      for (const q of qiymatlar) {
        if (!has(`${oila}.${q}`)) yoq.push(`${oila}.${q}`);
      }
    }

    // Prefiksli oilalar
    for (const s of ["catalog", "chat_negotiation"]) {
      if (!has(`orders.source_${s}`)) yoq.push(`orders.source_${s}`);
    }
    for (const r of ["client", "usta"]) {
      if (!has(`profile.role_${r}`)) yoq.push(`profile.role_${r}`);
    }

    expect(yoq).toEqual([]);
  });

  it("uz.json da bo'sh tarjima yo'q", () => {
    const bosh: string[] = [];
    const walkJson = (node: unknown, yol: string) => {
      if (typeof node === "string") {
        if (node.trim() === "") bosh.push(yol);
        return;
      }
      if (typeof node === "object" && node !== null) {
        for (const [k, v] of Object.entries(node)) {
          walkJson(v, yol ? `${yol}.${k}` : k);
        }
      }
    };
    walkJson(uz, "");
    expect(bosh).toEqual([]);
  });
});
