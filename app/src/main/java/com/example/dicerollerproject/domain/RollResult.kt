package com.example.dicerollerproject.domain

/**
 * Data class holding complete result of a dice roll operation.
 * Immutable.
 * @param total The final sum.
 * @param facesRolled The list of string faces that were rolled.
 * @param numericContributions The numeric value of each roll.
 */
class RollResult(
    /** Final total after all modifiers  */
    @JvmField val total: Int,
    /** Lists all face results as strings, before keep/drop logic.  */
    val facesRolled: List<String?>,
    /** Numeric value of each die roll. List corresponds to facesRolled and is used for calculation.  */
    @JvmField val numericContributions: List<Int> // each face mapped to numeric value used in sum
)

