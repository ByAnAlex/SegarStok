package com.segarstok.domain.recommendation

import kotlin.math.roundToLong

object PromoCalculator {

    fun calculate(
        normalSellingPricePerUnit: Long?,
        purchasePricePerUnit: Long?,
        discountRate: Double
    ): PromoSimulationResult {
        if (normalSellingPricePerUnit == null || normalSellingPricePerUnit <= 0) {
            return PromoSimulationResult(
                discountRate = discountRate,
                promoPrice = null,
                grossMarginPerUnit = null,
                marginWarning = "Harga jual normal belum diisi. Isi harga jual untuk melihat simulasi promo.",
                formattedText = "Belum ada simulasi harga promo (harga jual kosong)"
            )
        }

        val discountPercentInt = (discountRate * 100).toInt()
        val promoPriceDouble = normalSellingPricePerUnit * (1.0 - discountRate)
        val promoPrice = promoPriceDouble.roundToLong()

        val (grossMargin, marginWarning) = if (purchasePricePerUnit != null) {
            val margin = promoPrice - purchasePricePerUnit
            val warning = if (margin <= 0) {
                "⚠️ Margin kotor nol atau negatif (${formatRupiah(margin)})! Potensi rugi modal."
            } else {
                null
            }
            Pair(margin, warning)
        } else {
            Pair(null, "Harga beli belum diisi (estimasi margin kotor tidak dapat dihitung)")
        }

        val formattedText = buildString {
            append("Diskon $discountPercentInt%: Harga Promo ${formatRupiah(promoPrice)}")
            if (grossMargin != null) {
                append(" | Est. Margin Kotor: ${formatRupiah(grossMargin)}/unit")
            }
        }

        return PromoSimulationResult(
            discountRate = discountRate,
            promoPrice = promoPrice,
            grossMarginPerUnit = grossMargin,
            marginWarning = marginWarning,
            formattedText = formattedText
        )
    }

    private fun formatRupiah(amount: Long): String {
        val absolute = if (amount < 0) -amount else amount
        val formatted = "%,d".format(absolute).replace(',', '.')
        return if (amount < 0) "-Rp $formatted" else "Rp $formatted"
    }
}
