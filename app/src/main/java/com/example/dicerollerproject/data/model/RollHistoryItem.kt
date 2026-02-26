package com.example.dicerollerproject.data.model

data class RollHistoryItem(
    val id: String,
    val timestamp: Long,
    val diceDescription: String, // e.g., "2x D20, 1x D6"
    val results: String,         // e.g., "[18, 5, 4]"
    val total: Int,
    val modifierLabel: String    // e.g., "+5 (Drop Lowest)"
)