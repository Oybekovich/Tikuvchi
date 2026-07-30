import { readFileSync, readdirSync } from "node:fs";
import { join } from "node:path";
import { describe, expect, it } from "vitest";

/**
 * Migratsiyalar ketma-ketligi va idempotentligi. Supabase migratsiyalarni
 * fayl nomi bo'yicha tartibda qo'llaydi — raqamda bo'shliq yoki takror
 * bo'lsa, qo'llash tartibi buziladi.
 */

const DIR = new URL("../supabase/migrations", import.meta.url).pathname;
const FAYLLAR = readdirSync(DIR).filter((f) => f.endsWith(".sql")).sort();

describe("migratsiya fayllari", () => {
  it("kamida bittasi bor", () => {
    expect(FAYLLAR.length).toBeGreaterThan(0);
  });

  it("raqamlar ketma-ket, bo'shliqsiz va takrorsiz", () => {
    const raqamlar = FAYLLAR.map((f) => {
      const m = f.match(/^(\d{4})_/);
      expect(m, `noto'g'ri nom: ${f}`).not.toBeNull();
      return Number(m![1]);
    });

    expect(new Set(raqamlar).size).toBe(raqamlar.length);
    raqamlar.forEach((n, i) => expect(n).toBe(i + 1));
  });

  it("har bir 'create policy' oldidan 'drop policy if exists' bor", () => {
    // Aks holda migratsiyani qayta qo'llash "already exists" bilan yiqiladi.
    // 0001–0007 boshlang'ich migratsiyalar, ular bir marta qo'llanadi;
    // qoidani keyin qo'shilganlarga (0008+) talab qilamiz.
    const yangi = FAYLLAR.filter((f) => Number(f.slice(0, 4)) >= 8);

    for (const fayl of yangi) {
      const matn = readFileSync(join(DIR, fayl), "utf8");
      const yaratilgan = [
        ...matn.matchAll(/create policy\s+"([^"]+)"\s+on\s+([\w.]+)/gi),
      ].map((m) => ({ nom: m[1], jadval: m[2] }));

      for (const p of yaratilgan) {
        const dropRe = new RegExp(
          `drop policy if exists\\s+"${p.nom}"\\s+on\\s+${p.jadval.replace(".", "\\.")}`,
          "i"
        );
        expect(
          dropRe.test(matn),
          `${fayl}: "${p.nom}" uchun 'drop policy if exists' yo'q`
        ).toBe(true);
      }
    }
  });

  it("yangi migratsiyalarda xavfli 'drop table' yo'q", () => {
    for (const fayl of FAYLLAR.filter((f) => Number(f.slice(0, 4)) >= 8)) {
      const matn = readFileSync(join(DIR, fayl), "utf8");
      expect(/drop\s+table/i.test(matn), `${fayl}`).toBe(false);
    }
  });

  it("security definer funksiyalarda search_path belgilangan", () => {
    // search_path'siz SECURITY DEFINER — klassik imtiyoz oshirish teshigi
    for (const fayl of FAYLLAR) {
      const matn = readFileSync(join(DIR, fayl), "utf8");
      const bloklar = matn.split(/create or replace function|create function/i).slice(1);
      for (const blok of bloklar) {
        const bosh = blok.slice(0, 400);
        if (/security definer/i.test(bosh)) {
          expect(
            /set\s+search_path/i.test(bosh),
            `${fayl}: security definer funksiyada search_path yo'q`
          ).toBe(true);
        }
      }
    }
  });
});
