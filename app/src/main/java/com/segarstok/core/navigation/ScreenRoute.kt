package com.segarstok.core.navigation

sealed class ScreenRoute(
    val route: String,
    val title: String
) {
    data object Dashboard : ScreenRoute("dashboard", "Dashboard")
    data object StockList : ScreenRoute("stock_list", "Daftar Stok")
    data object AddBatch : ScreenRoute("add_batch", "Tambah Batch")
    data object BatchDetail : ScreenRoute("batch_detail/{batchId}", "Detail Batch") {
        fun createRoute(batchId: Long) = "batch_detail/$batchId"
    }
    data object Recommendation : ScreenRoute("recommendation", "Rekomendasi")
    data object Settings : ScreenRoute("settings", "Pengaturan")
}
