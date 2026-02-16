package com.example.dicerollerproject.domain

/**Defines modifications to apply to dice rolls.
 * Includes flat bonuses, keeping highest/lowest rolls, and rules for rerolling  */
class Modifier {
    // Constant value added or subtracted from total sum
    var flat: Int = 0 // +/- constant


    /*TODO: Need to Investigate the drop lowest one logic and fix */
    /** Highest Dice to keep. If null, rule isn't applied. Currently acts as a drop lowest one  */
    @JvmField
    var keepHighest: Int? = null // null if not set
    /*TODO: Need to Investigate the drop highest one logic and fix */
    /** Lowest dice is kept. if null, rule isn't applied. Currently acts as a drop highest one  */
    var keepLowest: Int? = null // null if not set

    /** If true, rerolls dice with a value of "1" exactly one time.  */
    @JvmField
    var rerollOnesOnce: Boolean = false

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

