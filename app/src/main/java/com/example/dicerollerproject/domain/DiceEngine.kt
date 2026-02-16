package com.example.dicerollerproject.domain

import java.util.Random
import kotlin.math.min

/**
 * Logic engine for rolling dice, taking specifications and modifiers to produce a result
 */
class DiceEngine(seed: Long?) {
    private val rng: Random

    /**
     * Constructs a new DiceEngine
     * @param seed A seed for random number generation for deterministic testing
     * If null, a new random object is created without a fixed seed.
     */
    init {
        this.rng = if (seed == null) Random() else Random(seed)
    }

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
    private fun parseNumeric(face: String): Int {
        try {
            return face.trim { it <= ' ' }.toInt()
        } catch (e: Exception) {
            return 0
        }
    }

    /**
     * Executes a dice roll based on the given specifications and modifier
     * @param specs A list of RollSpec objects, each defines a set of dice to roll
     * @param mod Modifier applied to the entire roll (e.g. flat bonuses, keep/drop).
     * @return A RollResult object containing the total and individual face results
     */
    fun roll(specs: MutableList<RollSpec>, mod: Modifier): RollResult {
        val faces: MutableList<String?> = ArrayList<String?>()
        val contribs: MutableList<Int?> = ArrayList<Int?>()

        // Roll each die specific in RollSpec list
        for (spec in specs) {
            for (i in 0..<spec.count) {
                val face = rollOneFace(spec.die, mod)
                faces.add(face)
                contribs.add(parseNumeric(face))
            }
        }

        // Apply keep/drop logic
        var indices: MutableList<Int?> = ArrayList<Int?>()
        for (i in contribs.indices) indices.add(i)

        if (mod.keepHighest != null) {
            indices.sort(Comparator { a: Int?, b: Int? ->
                Integer.compare(
                    contribs.get(b!!)!!,
                    contribs.get(a!!)!!
                )
            }) // desc
            indices = indices.subList(0, min(mod.keepHighest, indices.size))
        } else if (mod.keepLowest != null) {
            indices.sort(Comparator { a: Int?, b: Int? ->
                Integer.compare(
                    contribs.get(a!!)!!,
                    contribs.get(b!!)!!
                )
            }) // asc
            indices = indices.subList(0, min(mod.keepLowest, indices.size))
        }

        // Calculates sum based on dice kept.
        var sum = 0
        if (mod.keepHighest != null || mod.keepLowest != null) {
            val keep: MutableSet<Int?> = HashSet<Int?>(indices)
            for (i in contribs.indices) if (keep.contains(i)) sum += contribs.get(i)!!
        } else {
            for (v in contribs) sum += v!!
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
    private fun rollOneFace(die: Dice, mod: Modifier): String {
        // Performs base roll
        val idx = rollIndex(die.sides())
        var face = die.faceAtIndex(idx)

        // Handle the "reroll 1s once" modifier
        if (mod.rerollOnesOnce) {
            val `val` = parseNumeric(face)
            if (`val` == 1) {
                val idx2 = rollIndex(die.sides())
                face = die.faceAtIndex(idx2)
            }
        }
        return face
    }
}

