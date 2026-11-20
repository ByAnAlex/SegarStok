package com.segarstok.domain.model

enum class BatchStatus(val label: String) {
    AVAILABLE("Tersedia"),
    SOLD_OR_USED("Terjual / Dipakai"),
    DISCARDED("Dibuang")
}
