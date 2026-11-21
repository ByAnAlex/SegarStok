package com.segarstok.domain.model

enum class SortOption(val label: String) {
    EXPIRY_ASC("Kedaluwarsa Terdekat"),
    EXPIRY_DESC("Kedaluwarsa Terjauh"),
    NAME_ASC("Nama Produk (A-Z)"),
    QUANTITY_DESC("Jumlah Stok Terbanyak")
}
