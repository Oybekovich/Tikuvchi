import { describe, expect, it } from "vitest";
import { canClientComplete, nextUstaStatus } from "@/lib/orderFlow";
import type { Enums } from "@/lib/database.types";

type Status = Enums<"order_status">;

const BARCHA_HOLATLAR: Status[] = [
  "pending",
  "accepted",
  "in_progress",
  "ready",
  "completed",
  "cancelled",
];

/**
 * Bu qoidalar docs/01-mahsulot.md ("O'zgarmas cheklovlar") va
 * docs/04-buyurtma-oqimi.md ("Qat'iy qoidalar") dan keladi.
 * Ular baza triggerida ham majburlanadi (0008_role_guards.sql) —
 * bu yerda UI tomoni tekshiriladi.
 */

describe("nextUstaStatus — ustaning yo'li", () => {
  it("accepted dan keyin ish boshlanadi", () => {
    expect(nextUstaStatus("accepted")).toEqual({
      next: "in_progress",
      labelKey: "orders.start",
    });
  });

  it("in_progress dan keyin tayyor deb belgilanadi", () => {
    expect(nextUstaStatus("in_progress")).toEqual({
      next: "ready",
      labelKey: "orders.markReady",
    });
  });

  it("USTA BUYURTMANI YAKUNLAY OLMAYDI — ready dan keyin to'xtaydi", () => {
    // Eng muhim qoida: yakunlashni faqat mijoz tasdiqlaydi
    expect(nextUstaStatus("ready")).toBeNull();
  });

  it("pending holatida ilgari surish yo'q (avval qabul qilinadi)", () => {
    expect(nextUstaStatus("pending")).toBeNull();
  });

  it("yakunlangan va bekor qilingan buyurtma o'zgarmaydi", () => {
    expect(nextUstaStatus("completed")).toBeNull();
    expect(nextUstaStatus("cancelled")).toBeNull();
  });

  it("hech qachon 'completed' ga olib bormaydi", () => {
    for (const s of BARCHA_HOLATLAR) {
      expect(nextUstaStatus(s)?.next).not.toBe("completed");
    }
  });

  it("zanjir aylanma emas — orqaga qaytish yo'q", () => {
    const tartib: Status[] = ["accepted", "in_progress", "ready"];
    for (const s of tartib) {
      const keyingi = nextUstaStatus(s)?.next;
      if (keyingi) {
        expect(tartib.indexOf(keyingi)).toBeGreaterThan(tartib.indexOf(s));
      }
    }
  });

  it("har bir qadamning tarjima kaliti bor", () => {
    for (const s of BARCHA_HOLATLAR) {
      const qadam = nextUstaStatus(s);
      if (qadam) expect(qadam.labelKey).toMatch(/^orders\./);
    }
  });
});

describe("canClientComplete — mijozning yakunlashi", () => {
  it("faqat tayyor buyurtmani yakunlay oladi", () => {
    expect(canClientComplete("ready")).toBe(true);
  });

  it("boshqa hech qanday holatda yakunlay olmaydi", () => {
    const boshqalar = BARCHA_HOLATLAR.filter((s) => s !== "ready");
    for (const s of boshqalar) {
      expect(canClientComplete(s)).toBe(false);
    }
  });
});

describe("rollar bir-birini takrorlamaydi", () => {
  it("bir holatda ikkala rol ham amal qila olmaydi", () => {
    for (const s of BARCHA_HOLATLAR) {
      const usta = nextUstaStatus(s) !== null;
      const mijoz = canClientComplete(s);
      expect(usta && mijoz).toBe(false);
    }
  });

  it("to'liq zanjir ikki rol orqali oxiriga yetadi", () => {
    let holat: Status = "accepted";
    const yol: Status[] = [holat];

    // Usta olib boradigan qism
    for (;;) {
      const keyingi = nextUstaStatus(holat);
      if (!keyingi) break;
      holat = keyingi.next;
      yol.push(holat);
    }
    expect(holat).toBe("ready");

    // Oxirgi qadamni mijoz qo'yadi
    expect(canClientComplete(holat)).toBe(true);
    yol.push("completed");

    expect(yol).toEqual([
      "accepted",
      "in_progress",
      "ready",
      "completed",
    ]);
  });
});
