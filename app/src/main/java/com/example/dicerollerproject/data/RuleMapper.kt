package com.example.dicerollerproject.data

import com.example.dicerollerproject.data.model.CustomDie
import com.example.dicerollerproject.data.model.Rule
import com.example.dicerollerproject.domain.Dice
import com.example.dicerollerproject.domain.Modifier
import com.example.dicerollerproject.domain.RollSpec

/**
 * Mapper used to create a Rules by combining dice and rules.
 */
object RuleMapper {
    fun prepare(rule: Rule?, allDice: MutableList<CustomDie?>?): Prepared {
        val specs = mutableListOf<RollSpec>()

        rule?.components?.filterNotNull()?.forEach { c ->
            val die: Dice = if (c.isStandard) {
                Dice.standard(Dice.Standard.valueOf(c.standard!!))
            } else {
                val cd = findById(allDice.orEmpty(), c.customDieId)
                Dice.custom(cd.faces)
            }
            specs.add(RollSpec(die, c.count))
        }

        val m = Modifier.none().apply {
            flatBonus = rule?.flat ?: 0
            rule?.modifier?.let { mod ->
                keepHighest = mod.keepHighest
                keepLowest = mod.keepLowest

                mod.rerollString?.let { input ->
                    if (input.isNotEmpty()) {
                        val parts = input.split(",").map { it.trim() }
                        for (part in parts) {
                            if (part.contains("-")) {
                                val range = part.split("-")
                                val start = range.getOrNull(0)?.toIntOrNull()
                                val end = range.getOrNull(1)?.toIntOrNull()
                                if (start != null && end != null) {
                                    for (v in start..end) rerollValues.add(v)
                                }
                            } else {
                                val numeric = part.toIntOrNull()
                                if (numeric != null) rerollValues.add(numeric)
                                else rerollFaces.add(part)
                            }
                        }
                    }
                }
            }
        }
        return Prepared(specs, m)
    }

    private fun findById(dice: List<CustomDie?>, id: String?): CustomDie {
        return dice.filterNotNull().find { it.id == id }
            ?: throw IllegalArgumentException("Custom die not found: $id")
    }

    class Prepared(val specs: MutableList<RollSpec>, val mod: Modifier)
}

