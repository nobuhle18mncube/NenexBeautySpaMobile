package com.example.nenexbeautyspa

data class InventoryItem(
    var id: String = "",
    var name: String = "",
    var quantity: Int = 0,
    var price: Double = 0.0,
    var category: String = "",
    var description: String = "",
    var lowStock: Boolean = false
)