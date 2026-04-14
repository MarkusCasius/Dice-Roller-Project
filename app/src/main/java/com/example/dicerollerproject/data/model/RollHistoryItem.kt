package com.example.dicerollerproject.data.model

/**
 * Represents an item in the roll history.
 * @param id Unique identifier for the history item.
 * @param timestamp Timestamp when the roll occurred.
 * @param diceDescription Description of the dice used in the roll.
 * @param results Results of the roll.
 * @param total Total value of the roll.
 * @param modifierLabel Description of the modifier applied to the roll.
 */
data class RollHistoryItem(
    val id: String,
    val timestamp: Long,
    val diceDescription: String, // e.g., "2x D20, 1x D6"
    val results: String,         // e.g., "[18, 5, 4]"
    val total: Int,
    val modifierLabel: String    // e.g., "+5 (Drop Lowest)"
)