package uz.tikuvchi.data

import uz.tikuvchi.data.model.PaymentStatus

/**
 * web/src/lib/payments.ts ning ko'chirmasi — to'lov integratsiyasi
 * (Payme/Click) uchun joy. Hozircha faqat payment_status maydoni orqali
 * modellashtirilgan: pending -> partial (bo'nak, ~30%) -> paid.
 */
object Payments {
    const val DEPOSIT_RATE = 0.3

    fun depositAmount(totalPrice: Long): Long =
        Math.round(totalPrice * DEPOSIT_RATE)

    fun nextPaymentStatus(current: PaymentStatus): PaymentStatus? = when (current) {
        PaymentStatus.PENDING -> PaymentStatus.PARTIAL
        PaymentStatus.PARTIAL -> PaymentStatus.PAID
        PaymentStatus.PAID -> null
    }
}
