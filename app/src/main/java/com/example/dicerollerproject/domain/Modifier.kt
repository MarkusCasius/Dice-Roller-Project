package com.example.dicerollerproject.domain

/**Defines modifications to apply to dice rolls.
 * Includes flat bonuses, keeping highest/lowest rolls, and rules for rerolling  */
class Modifier {
    // Constant value added or subtracted from total sum
    var flatBonus: Int = 0 // +/- constant


    /** Highest Dice to keep. If null, rule isn't applied.  */
    @JvmField
    var keepHighest: Int? = null // null if not set
    /** Lowest dice is kept. if null, rule isn't applied. */
    var keepLowest: Int? = null // null if not set

    /** Defines specific numeric values that should be rerolled once */
    val rerollValues = mutableSetOf<Int>()

    /** Defines specific string faces that should be rerolled once */
    val rerollFaces = mutableSetOf<String>()

    companion object {
        /**
         * Factory method to create a default Modifier with no rules applied.
         * @return A new Modifier instance with default (zero/false/null) values.
         */
        @JvmStatic
        fun none(): Modifier {
            return Modifier()
        }
    }
}

