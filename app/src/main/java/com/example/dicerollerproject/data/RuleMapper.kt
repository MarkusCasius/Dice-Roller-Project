package com.example.dicerollerproject.data

import com.example.dicerollerproject.data.model.CustomDie
import com.example.dicerollerproject.data.model.Rule
import com.example.dicerollerproject.domain.Dice
import com.example.dicerollerproject.domain.Modifier
import com.example.dicerollerproject.domain.RollSpec

object RuleMapper {
    fun prepare(rule: Rule?, allDice: MutableList<CustomDie?>?): Prepared {
        val specs = mutableListOf<RollSpec>()

        rule?.components?.filterNotNull()?.forEach { c ->
            val die: Dice = if (c.isStandard) {
                // c.standard is a String? (e.g. "D6"), valueOf converts string to Enum
                Dice.standard(Dice.Standard.valueOf(c.standard!!))
            } else {
                val cd = findById(allDice.orEmpty(), c.customDieId)
                Dice.custom(cd.faces)
            }
            specs.add(RollSpec(die, c.count))
        }

        val m = Modifier.none().apply {
            flat = rule?.flat ?: 0
            rule?.modifier?.let { mod ->
                keepHighest = mod.keepHighest
                keepLowest = mod.keepLowest
                rerollOnesOnce = mod.rerollOnesOnce
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

private fun Unit.valueOf(value: String?): Dice.Standard? {
    return TODO("Provide the return value")
}

