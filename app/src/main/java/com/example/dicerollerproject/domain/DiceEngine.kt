package com.example.dicerollerproject.domain

import java.util.Random
import kotlin.math.min

/**
 * Logic engine for rolling dice, taking specifications and modifiers to produce a result
 */
class DiceEngine(seed: Long?) {
    /**
     * Constructs a new DiceEngine
     * @param seed A seed for random number generation for deterministic testing
     * If null, a new random object is created without a fixed seed.
     */
    private val rng: Random = if (seed == null) Random() else Random(seed)

    /**
     * Generates random zero-based index for die roll
     * @param sides Number of sides on the die
     * @return Random index between 0 and sides-1
     */
    private fun rollIndex(sides: Int): Int {
        return rng.nextInt(sides)
    } // 0..sides-1

    /**
     * Parses a string face into a integer. Non-numeric faces are pased as 0.
     * @param face The string face to parse
     * @return The numeric value of the face, or 0 if parsing fails
     */
    private fun parseNumeric(face: String?): Int {
        if (face == null) return 0
        return face.trim().toIntOrNull() ?: 0
    }

    /**
     * Executes a dice roll based on the given specifications and modifier
     * @param specs A list of RollSpec objects, each defines a set of dice to roll
     * @param mod Modifier applied to the entire roll (e.g. flat bonuses, keep/drop).
     * @return A RollResult object containing the total and individual face results
     */
    fun roll(specs: MutableList<RollSpec>, mod: Modifier): RollResult {
        val faces = mutableListOf<String?>()
        val contribs = mutableListOf<Int>()

        // Roll each die specific in RollSpec list
        for (spec in specs) {
            for (i in 0 until spec.count) {
                val face = rollOneFace(spec.die, mod)
                faces.add(face)
                contribs.add(parseNumeric(face))
            }
        }

        // Apply keep/drop logic
        var indices = contribs.indices.toMutableList()
        for (i in contribs.indices) indices.add(i)

        if (mod.keepHighest != null) {
            // Sort indices based on values in contribs (Descending)
            indices.sortByDescending { contribs[it] }
            indices = indices.take(mod.keepHighest!!).toMutableList()
        } else if (mod.keepLowest != null) {
            // Sort indices based on values in contribs (Ascending)
            indices.sortBy { contribs[it] }
            indices = indices.take(mod.keepLowest!!).toMutableList()
        }

        // Calculates sum based on dice kept.
        var sum = 0
        if (mod.keepHighest != null || mod.keepLowest != null) {
            val keepSet = indices.toSet()
            for (i in contribs.indices) {
                if (keepSet.contains(i)) sum += contribs[i]
            }
        } else {
            sum = contribs.sum()
        }
        sum += mod.flat // Flat modifier applied

        return RollResult(sum, faces, contribs)
    }

    /**
     * Rolls a single die, applying any relevant per-die modifiers like rerolling.
     * @param die The die to roll.
     * @param mod The modifier containing rules for the roll.
     * @return The final face string after the roll and any rerolls.
     */
    private fun rollOneFace(die: Dice, mod: Modifier): String? {
        // Performs base roll
        val idx = rollIndex(die.sides())
        var face = die.faceAtIndex(idx)

        // Handle the "reroll 1s once" modifier

        if (mod.rerollOnesOnce && parseNumeric(face) == 1) {
            face = die.faceAtIndex(rollIndex(die.sides()))
        }
        return face
    }
}

