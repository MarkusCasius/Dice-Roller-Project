package com.example.dicerollerproject.domain

/**
 * Data class representing a specification for a roll, e.g. "roll 3 D6".
 * Immutable
 */
class RollSpec(die: Dice?, count: Int) {
    /** Type of die roll to roll  */
    val die: Dice?

    /** Number of times to roll the dice  */
    val count: Int

    /**
     * Constructs a new RollSpec.
     * @param die Type of die to roll
     * @param count Number of times to roll the dice
     */
    init {
        this.die = die
        this.count = count
    }
}

