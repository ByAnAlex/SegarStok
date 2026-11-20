package com.segarstok.domain.model

enum class UrgencyLevel(
    val label: String,
    val description: String
) {
    NORMAL("Normal", "> 14 Hari"),
    ATTENTION("Perhatian", "8 - 14 Hari"),
    NEAR("Mendekati", "4 - 7 Hari"),
    URGENT("Mendesak", "1 - 3 Hari"),
    DUE_TODAY("Hari H", "Kedaluwarsa Hari Ini"),
    EXPIRED("Lewat Tanggal", "Sudah Kedaluwarsa");

    companion object {
        fun fromDaysLeft(daysLeft: Long): UrgencyLevel {
            return when {
                daysLeft < 0 -> EXPIRED
                daysLeft == 0L -> DUE_TODAY
                daysLeft in 1..3 -> URGENT
                daysLeft in 4..7 -> NEAR
                daysLeft in 8..14 -> ATTENTION
                else -> NORMAL
            }
        }
    }
}
