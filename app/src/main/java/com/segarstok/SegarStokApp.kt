package com.segarstok

import android.app.Application
import androidx.work.*
import com.segarstok.data.local.database.SegarStokDatabase
import com.segarstok.data.repository.StockRepositoryImpl
import com.segarstok.domain.repository.StockRepository
import com.segarstok.domain.usecase.*
import com.segarstok.ml.ocr.MlKitOcrService
import com.segarstok.ml.ocr.OcrService
import com.segarstok.notifications.NotificationHelper
import com.segarstok.worker.ExpiryReminderWorker
import java.util.concurrent.TimeUnit

class SegarStokApp : Application() {

    lateinit var database: SegarStokDatabase
        private set

    lateinit var repository: StockRepository
        private set

    lateinit var ocrService: OcrService
        private set

    lateinit var addBatchUseCase: AddBatchUseCase
        private set

    lateinit var getBatchesUseCase: GetBatchesUseCase
        private set

    lateinit var getBatchByIdUseCase: GetBatchByIdUseCase
        private set

    lateinit var updateBatchStatusUseCase: UpdateBatchStatusUseCase
        private set

    lateinit var updateBatchUseCase: UpdateBatchUseCase
        private set

    lateinit var reduceStockQuantityUseCase: ReduceStockQuantityUseCase
        private set

    lateinit var deleteBatchUseCase: DeleteBatchUseCase
        private set

    lateinit var getRecommendationsUseCase: GetRecommendationsUseCase
        private set

    override fun onCreate() {
        super.onCreate()
        database = SegarStokDatabase.getDatabase(this)
        repository = StockRepositoryImpl(database.stockBatchDao())
        ocrService = MlKitOcrService()

        addBatchUseCase = AddBatchUseCase(repository)
        getBatchesUseCase = GetBatchesUseCase(repository)
        getBatchByIdUseCase = GetBatchByIdUseCase(repository)
        updateBatchStatusUseCase = UpdateBatchStatusUseCase(repository)
        updateBatchUseCase = UpdateBatchUseCase(repository)
        reduceStockQuantityUseCase = ReduceStockQuantityUseCase(repository)
        deleteBatchUseCase = DeleteBatchUseCase(repository)
        getRecommendationsUseCase = GetRecommendationsUseCase(repository)

        NotificationHelper.createNotificationChannel(this)
        setupDailyWorkManager()
    }

    private fun setupDailyWorkManager() {
        val workRequest = PeriodicWorkRequestBuilder<ExpiryReminderWorker>(24, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "segarstok_daily_expiry_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
