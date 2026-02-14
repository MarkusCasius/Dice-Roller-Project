package com.example.dicerollerproject.data.model

class Rule(
    var id: String?, var name: String?, // die ref + count
    var components: MutableList<RuleComponent?>?, var modifier: RuleModifier?, var flat: Int
) {
    class RuleComponent(
        var isStandard: Boolean, // "D4"..."D20" if standard
        var standard: String?, // if custom
        var customDieId: String?, var count: Int
    )

    class RuleModifier(var keepHighest: Int?, var keepLowest: Int?, var rerollOnesOnce: Boolean)
}
