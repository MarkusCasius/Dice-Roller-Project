package com.example.dicerollerproject.data.model

/**
 * Represents a sequence of dice with applied modifiers
 * @param id Unique identifier for the rule.
 * @param name Optional name for the rule.
 * @param components List of components (dice) in the rule.
 * @param modifier Modifier applied to the rule.
 * @param flat Flat value added to the total.
 * @param description Optional description for the rule.
 */
class Rule(
    var id: String?, var name: String?, // die ref + count
    var components: MutableList<RuleComponent?>?, var modifier: RuleModifier?, var flat: Int, val description: String? = null
) {

    /**
     * Represents a component (dice) in a rule.
     * @param isStandard Whether the component is a standard die.
     * @param standard The type of standard die (e.g., "D6").
     * @param customDieId The ID of a custom die.
     * @param count The number of dice in the component.
     */
    class RuleComponent(
        var isStandard: Boolean, // "D4"..."D20" if standard
        var standard: String?, // if custom
        var customDieId: String?, var count: Int
    )

    /**
     * Represents the modifier applied to a rule.
     * @param keepHighest Number of highest values to keep.
     * @param keepLowest Number of lowest values to keep.
     * @param rerollString String representation of rerolls.
     * @param flatBonus Flat value added to the total.
     */
    class RuleModifier(var keepHighest: Int?, var keepLowest: Int?, val rerollString: String?, val flatBonus: Int? = null) {
    }
}
