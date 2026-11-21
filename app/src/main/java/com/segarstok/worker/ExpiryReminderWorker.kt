package com.segarstok.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.segarstok.SegarStokApp
import com.segarstok.domain.model.BatchStatus
import com.segarstok.notifications.NotificationHelper
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate

class ExpiryReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? SegarStokApp ?: return Result.failure()
        val repository = app.repository

        try {
            val availableBatches = repository.getAvailableBatches().firstOrNull() ?: emptyList()
            val today = LocalDate.now()

            for (batch in availableBatches) {
                if (batch.status != BatchStatus.AVAILABLE || batch.quantity <= 0) continue

                val daysLeft = batch.daysLeft(today)

                if (daysLeft < 0) {
                    NotificationHelper.showExpiryNotification(
                        context = applicationContext,
                        notificationId = batch.id.toInt(),
                        title = "⛔ Produk Kedaluwarsa: ${batch.productName}",
                        message = "Batch ${batch.productName} (${batch.quantity} ${batch.unit}) telah lewat tanggal (${batch.expiryDate}). Hentikan penjualan segera!"
                    )
                } else if (daysLeft == 0L) {
                    NotificationHelper.showExpiryNotification(
                        context = applicationContext,
                        notificationId = batch.id.toInt(),
                        title = "⚠️ Hari H Kedaluwarsa: ${batch.productName}",
                        message = "Batch ${batch.productName} kedaluwarsa HARI INI. Periksa kemasan & pertimbangkan promo hari berjalan."
                    )
                } else if (daysLeft in 1..3) {
                    NotificationHelper.showExpiryNotification(
                        context = applicationContext,
                        notificationId = batch.id.toInt(),
                        title = "🔴 Mendekati Kedaluwarsa ($daysLeft Hari): ${batch.productName}",
                        message = "Batch ${batch.productName} sisa $daysLeft hari (${batch.expiryDate}). Segera utamakan rotasi FEFO / diskon terbatas."
                    )
                } else if (daysLeft in 4..7) {
                    NotificationHelper.showExpiryNotification(
                        context = applicationContext,
                        notificationId = batch.id.toInt(),
                        title = "🟠 Pengingat Stok ($daysLeft Hari): ${batch.productName}",
                        message = "Batch ${batch.productName} sisa $daysLeft hari (${batch.expiryDate}). Siapkan strategi bundling / promosi."
                    )
                }
            }

            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }
}
