package com.example.dicerollerproject.domain

import kotlin.collections.count
import kotlin.sequences.count
import kotlin.text.count

/**
 * Data class representing a specification for a roll, e.g. "roll 3 D6".
 * Immutable
 * @param die Type of die to roll
 * @param count Number of times to roll the dice
 */
class RollSpec(
    /** Type of die roll to roll  */
    val die: Dice,
    /** Number of times to roll the dice  */
    val count: Int)

