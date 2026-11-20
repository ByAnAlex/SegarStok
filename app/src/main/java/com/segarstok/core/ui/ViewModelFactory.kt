package com.segarstok.core.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.segarstok.SegarStokApp
import com.segarstok.feature.addbatch.AddBatchViewModel
import com.segarstok.feature.batchdetail.BatchDetailViewModel
import com.segarstok.feature.dashboard.DashboardViewModel
import com.segarstok.feature.stocklist.StockListViewModel

class ViewModelFactory(
    private val app: SegarStokApp,
    private val batchId: Long = 0L
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AddBatchViewModel::class.java) -> {
                AddBatchViewModel(
                    addBatchUseCase = app.addBatchUseCase,
                    ocrService = app.ocrService
                ) as T
            }
            modelClass.isAssignableFrom(StockListViewModel::class.java) -> {
                StockListViewModel(app.getBatchesUseCase) as T
            }
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                DashboardViewModel(app.getBatchesUseCase, app.repository) as T
            }
            modelClass.isAssignableFrom(BatchDetailViewModel::class.java) -> {
                BatchDetailViewModel(
                    batchId = batchId,
                    getBatchByIdUseCase = app.getBatchByIdUseCase,
                    updateBatchStatusUseCase = app.updateBatchStatusUseCase,
                    deleteBatchUseCase = app.deleteBatchUseCase
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        fun getInstance(context: Context, batchId: Long = 0L): ViewModelFactory {
            val app = context.applicationContext as SegarStokApp
            return ViewModelFactory(app, batchId)
        }
    }
}
