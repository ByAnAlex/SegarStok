package com.segarstok.domain.recommendation

import com.segarstok.domain.model.BatchStatus
import com.segarstok.domain.model.ProductCategory
import com.segarstok.domain.model.StockBatch
import com.segarstok.domain.model.UrgencyLevel
import java.time.LocalDate

object RecommendationEngine {

    fun generateRecommendation(
        batch: StockBatch,
        discountRate: Double = 0.10,
        currentDate: LocalDate = LocalDate.now()
    ): BatchRecommendation? {
        if (batch.status != BatchStatus.AVAILABLE || batch.quantity <= 0) {
            return null
        }

        val daysLeft = batch.daysLeft(currentDate)
        val urgencyLevel = batch.urgencyLevel(currentDate)

        if (daysLeft < 0) {
            return BatchRecommendation(
                batchId = batch.id,
                productName = batch.productName,
                category = batch.category,
                quantity = batch.quantity,
                unit = batch.unit,
                daysLeft = daysLeft,
                urgencyLevel = UrgencyLevel.EXPIRED,
                actionDirection = "Hentikan Penjualan",
                detailedAdvice = "⛔ Hentikan penjualan segera & keluarkan dari stok tersedia. Produk telah melewati tanggal kedaluwarsa pada label.",
                categoryAdvice = "Dilarang menjual, mendiskon, mengonsumsi, membekukan ulang, atau mengolah ulang makanan yang sudah lewat tanggal.",
                promoSimulation = null,
                isExpiredGuardrail = true
            )
        }

        if (daysLeft == 0L) {
            val promoSim = PromoCalculator.calculate(
                normalSellingPricePerUnit = batch.normalSellingPricePerUnit,
                purchasePricePerUnit = batch.purchasePricePerUnit,
                discountRate = discountRate
            )

            return BatchRecommendation(
                batchId = batch.id,
                productName = batch.productName,
                category = batch.category,
                quantity = batch.quantity,
                unit = batch.unit,
                daysLeft = daysLeft,
                urgencyLevel = UrgencyLevel.DUE_TODAY,
                actionDirection = "Pemeriksaan Hari H",
                detailedAdvice = "⚠️ Hari H Kedaluwarsa. Lakukan pemeriksaan ketat kondisi kemasan & label. Tawarkan harga promo khusus hari ini jika kondisi kemasan baik.",
                categoryAdvice = getCategorySpecificAdvice(batch.category, UrgencyLevel.DUE_TODAY),
                promoSimulation = promoSim,
                isExpiredGuardrail = false
            )
        }

        if (daysLeft in 1..3) {
            val promoSim = PromoCalculator.calculate(
                normalSellingPricePerUnit = batch.normalSellingPricePerUnit,
                purchasePricePerUnit = batch.purchasePricePerUnit,
                discountRate = discountRate
            )

            return BatchRecommendation(
                batchId = batch.id,
                productName = batch.productName,
                category = batch.category,
                quantity = batch.quantity,
                unit = batch.unit,
                daysLeft = daysLeft,
                urgencyLevel = UrgencyLevel.URGENT,
                actionDirection = "Promosi Mendesak",
                detailedAdvice = "🔴 Prioritaskan display terdepan (FEFO). Pertimbangkan diskon terbatas atau penawaran bundling untuk mempercepat penjualan.",
                categoryAdvice = getCategorySpecificAdvice(batch.category, UrgencyLevel.URGENT),
                promoSimulation = promoSim,
                isExpiredGuardrail = false
            )
        }

        if (daysLeft in 4..7) {
            val promoSim = PromoCalculator.calculate(
                normalSellingPricePerUnit = batch.normalSellingPricePerUnit,
                purchasePricePerUnit = batch.purchasePricePerUnit,
                discountRate = discountRate
            )

            return BatchRecommendation(
                batchId = batch.id,
                productName = batch.productName,
                category = batch.category,
                quantity = batch.quantity,
                unit = batch.unit,
                daysLeft = daysLeft,
                urgencyLevel = UrgencyLevel.NEAR,
                actionDirection = "Promosi Terencana",
                detailedAdvice = "🟠 Siapkan strategi promosi terbatas atau bundling hemat. Pastikan rotasi stok FEFO dipatuhi oleh staf.",
                categoryAdvice = getCategorySpecificAdvice(batch.category, UrgencyLevel.NEAR),
                promoSimulation = promoSim,
                isExpiredGuardrail = false
            )
        }

        if (daysLeft in 8..14) {
            return BatchRecommendation(
                batchId = batch.id,
                productName = batch.productName,
                category = batch.category,
                quantity = batch.quantity,
                unit = batch.unit,
                daysLeft = daysLeft,
                urgencyLevel = UrgencyLevel.ATTENTION,
                actionDirection = "Perhatian",
                detailedAdvice = "🟡 Pindahkan posisi batch ini ke bagian depan rak (rotasi FEFO) dan evaluasi jumlah pembelian berikutnya.",
                categoryAdvice = getCategorySpecificAdvice(batch.category, UrgencyLevel.ATTENTION),
                promoSimulation = null,
                isExpiredGuardrail = false
            )
        }

        return BatchRecommendation(
            batchId = batch.id,
            productName = batch.productName,
            category = batch.category,
            quantity = batch.quantity,
            unit = batch.unit,
            daysLeft = daysLeft,
            urgencyLevel = UrgencyLevel.NORMAL,
            actionDirection = "Rotasi Normal",
            detailedAdvice = "🟢 Stok aman. Lakukan rotasi standar FEFO saat melakukan penataan ulang produk.",
            categoryAdvice = getCategorySpecificAdvice(batch.category, UrgencyLevel.NORMAL),
            promoSimulation = null,
            isExpiredGuardrail = false
        )
    }

    private fun getCategorySpecificAdvice(category: ProductCategory, urgencyLevel: UrgencyLevel): String {
        return when (category) {
            ProductCategory.FROZEN_FOOD -> {
                if (urgencyLevel == UrgencyLevel.URGENT || urgencyLevel == UrgencyLevel.NEAR || urgencyLevel == UrgencyLevel.DUE_TODAY) {
                    "Frozen Food: Terapkan rotasi FEFO & pertimbangkan bundling multi-unit. Dilarang mencairkan (thaw) lalu membekukan ulang (refreeze)."
                } else {
                    "Frozen Food: Terapkan rotasi FEFO & pantau suhu freezer secara konsisten."
                }
            }
            ProductCategory.BAKERY -> {
                if (urgencyLevel == UrgencyLevel.URGENT || urgencyLevel == UrgencyLevel.DUE_TODAY) {
                    "Bakery: Pertimbangkan paket promo hari berjalan / bundling. Evaluasi jumlah produksi batch berikutnya."
                } else {
                    "Bakery: Jaga kelembapan tempat penyimpanan & terapkan rotasi FEFO."
                }
            }
            ProductCategory.PACKAGED_BEVERAGE -> {
                "Minuman Kemasan: Utamakan penawaran promo bundling / multi-pack. Dilarang membuka atau mengemas ulang kemasan pabrik."
            }
            ProductCategory.PACKAGED_SNACK -> {
                "Makanan Ringan: Tawarkan diskon terbatas / bundling grosir. Pastikan kemasan tertutup rapat dari kelembapan."
            }
            ProductCategory.OTHER -> {
                "Rotasi Umum: Terapkan prinsip FEFO (First-Expired, First-Out) dan patuhi petunjuk penyimpanan label."
            }
        }
    }
}
