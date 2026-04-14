package com.example.dicerollerproject.data

import android.content.Context
import android.content.SharedPreferences
import com.example.dicerollerproject.data.model.CustomDie
import com.example.dicerollerproject.data.model.RollHistoryItem
import com.example.dicerollerproject.data.model.Rule
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

/**
 * Handles local persistence of custom dice and rules using SharedPreferences and GSON.
 */
class LocalStore(context: Context) {
    private val prefs: SharedPreferences
    private val gson: Gson
    private val keyElementColour = "bg_color"
    private val keyButtonColour = "btn_color"
    private val keyTextColour = "text_color"
    private val keyHistory = "roll_history"
    private val keyAnimationSpeed = "anim_speed"


    init {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        this.gson = Gson()

        // Seeds initial rules/dice
        if (listRules().isEmpty()) {
            seedInitialData()
        }
    }

    /**
     * Seeds the initial data for first-time users.
     */
    private fun seedInitialData() {
        val initialDice = mutableListOf<CustomDie?>()
        val initialRules = mutableListOf<Rule?>()

        // Create the Confusion Die faces
        val confusionDieId = UUID.randomUUID().toString()
        val confusionDie = CustomDie(
            confusionDieId,
            "Confusion Dice",
            mutableListOf("Confused", "Confused", "Fizzle", "Fail", "Fail")
        )
        initialDice.add(confusionDie)

        // Create "Advantage" Rule: 2d20, Keep Highest 1
        initialRules.add(Rule(
            UUID.randomUUID().toString(),
            "Advantage",
            mutableListOf(Rule.RuleComponent(true, "D20", null, 2)),
            Rule.RuleModifier(1, null, null, null),
            0,
            null

        ))

        // Create "Fireball" Rule: 5d6
        initialRules.add(Rule(
            UUID.randomUUID().toString(),
            "Fireball",
            mutableListOf(Rule.RuleComponent(true, "D6", null, 5)),
            Rule.RuleModifier(null, null, null, null),
            0,
            null
        ))

        // Create "Confusion" Rule: 1x Confusion Die
        initialRules.add(Rule(
            UUID.randomUUID().toString(),
            "Confusion",
            mutableListOf(Rule.RuleComponent(false, null, confusionDieId, 1)),
            Rule.RuleModifier(null, null, null, null),
            0,
            null
        ))

        // Save everything to SharedPreferences
        saveCustomDice(initialDice)
        saveRules(initialRules)
    }

    // For updating the colour options
    fun saveBackgroundColour(color: Int) = prefs.edit().putInt(keyElementColour, color).apply()
    fun getBackgroundColour(): Int = prefs.getInt(keyElementColour, android.graphics.Color.parseColor("#F5F5F5"))

    fun saveButtonColour(color: Int) = prefs.edit().putInt(keyButtonColour, color).apply()
    fun getButtonColour(): Int = prefs.getInt(keyButtonColour, android.graphics.Color.parseColor("#6200EE"))

    fun saveTextColour(color: Int) = prefs.edit().putInt(keyTextColour, color).apply()
    fun getTextColour(): Int = prefs.getInt(keyTextColour, android.graphics.Color.BLACK)


    /**
     * Saves the list of custom dice to local storage.
     */
    fun saveCustomDice(dice: MutableList<CustomDie?>) {
        val json = gson.toJson(dice)
        prefs.edit().putString(KEY_CUSTOM_DICE, json).apply()
    }

    /**
     * Retrieves the list of custom dice from local storage.
     * Returns an empty list if nothing is saved.
     */
    fun listCustomDice(): MutableList<CustomDie?> {
        val json = prefs.getString(KEY_CUSTOM_DICE, null)
        if (json == null || json.isEmpty()) return ArrayList<CustomDie?>()
        val type = object : TypeToken<MutableList<CustomDie?>?>() {}.getType()
        return gson.fromJson<MutableList<CustomDie?>>(json, type)
    }

    /**
     * Saves custom rules (Modifiers) to local storage.
     */
    fun saveRules(rules: MutableList<Rule?>) {
        val json = gson.toJson(rules)
        prefs.edit().putString(KEY_CUSTOM_RULES, json).apply()
    }

    /**
     * Retrieves the list of custom rules from local storage.
     */
    fun listRules(): MutableList<Rule?> {
        val json = prefs.getString(KEY_CUSTOM_RULES, null)
        if (json == null || json.isEmpty()) return ArrayList<Rule?>()
        val type = object : TypeToken<MutableList<Rule?>?>() {}.getType()
        return gson.fromJson<MutableList<Rule?>>(json, type)
    }

    /**
     * Deletes a custom die by its ID.
     */
    fun deleteCustomDie(id: String?) {
        val dice = listCustomDice()
        dice.removeAll { it?.id == id }
        saveCustomDice(dice)
    }

    /**
     * Deletes a rule by its ID.
     */
    fun deleteRule(id: String?) {
        val rules = listRules()
        rules.removeAll { it?.id == id }
        saveRules(rules)
    }

    /**
     * Wipes all data in the store.
     */
    fun clearAll() {
        prefs.edit().clear().apply()
    }

    /**
     * Saves the animation speed to local storage.
     */
    fun saveAnimSpeed(speed: Float) = prefs.edit().putFloat(keyAnimationSpeed, speed).apply()

    /**
     * Returns 1.0f (Normal), 0.4f (Fast), or 0.0f (Instant) that determines the animation's speed
     */
    fun getAnimSpeed(): Float = prefs.getFloat(keyAnimationSpeed, 1.0f)

    /**
     * Saves the roll history to local storage.
     */
    fun saveHistory(history: List<RollHistoryItem>) {
        val json = gson.toJson(history)
        prefs.edit().putString(keyHistory, json).apply()
    }

    /**
     * Retrieves the roll history from local storage.
     */
    fun listHistory(): MutableList<RollHistoryItem> {
        val json = prefs.getString(keyHistory, null)
        if (json.isNullOrEmpty()) return mutableListOf()
        val type = object : TypeToken<List<RollHistoryItem>>() {}.type
        return gson.fromJson(json, type)
    }

    companion object {
        private const val PREF_NAME = "dice_store"
        private const val KEY_CUSTOM_DICE = "customDiceJson"
        private const val KEY_CUSTOM_RULES = "customRulesJson"
    }
}
