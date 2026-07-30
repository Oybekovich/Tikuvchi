import { describe, expect, it } from "vitest";
import { DEPOSIT_RATE, depositAmount, nextPaymentStatus } from "@/lib/payments";
import type { Enums } from "@/lib/database.types";

type Payment = Enums<"payment_status">;

describe("nextPaymentStatus", () => {
  it("to'lov zanjiri: pending → partial → paid", () => {
    expect(nextPaymentStatus("pending")).toBe("partial");
    expect(nextPaymentStatus("partial")).toBe("paid");
  });

  it("to'liq to'langandan keyin keyingi qadam yo'q", () => {
    expect(nextPaymentStatus("paid")).toBeNull();
  });

  it("zanjir aylanma emas — paid oxirgi holat", () => {
    let holat: Payment | null = "pending";
    const yol: Payment[] = [holat];
    for (let i = 0; i < 10 && holat; i++) {
      holat = nextPaymentStatus(holat);
      if (holat) yol.push(holat);
    }
    expect(yol).toEqual(["pending", "partial", "paid"]);
  });
});

describe("depositAmount", () => {
  it("bo'nak 30% ni tashkil qiladi", () => {
    expect(DEPOSIT_RATE).toBe(0.3);
    expect(depositAmount(1_000_000)).toBe(300_000);
    expect(depositAmount(250_000)).toBe(75_000);
  });

  it("butun songa yaxlitlanadi — tiyin bo'lmaydi", () => {
    expect(Number.isInteger(depositAmount(333_333))).toBe(true);
    expect(depositAmount(333_333)).toBe(100_000);
  });

  it("nol narxda nol bo'nak", () => {
    expect(depositAmount(0)).toBe(0);
  });

  it("bo'nak hech qachon to'liq narxdan oshmaydi", () => {
    for (const narx of [0, 1, 999, 250_000, 1_450_000, 99_999_999]) {
      expect(depositAmount(narx)).toBeLessThanOrEqual(narx);
    }
  });
});
