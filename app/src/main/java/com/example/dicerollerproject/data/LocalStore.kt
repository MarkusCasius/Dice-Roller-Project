package com.example.dicerollerproject.data

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.dicerollerproject.data.model.CustomDie
import com.example.dicerollerproject.data.model.RollHistoryItem
import com.example.dicerollerproject.data.model.Rule
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dice_store")


/**
 * Handles local persistence of custom dice and rules using Jetpack Datastore and GSON.
 */
class LocalStore(private val context: Context) {
    private val gson = Gson()
    companion object {
        private val KEY_CUSTOM_DICE = stringPreferencesKey("customDiceJson")
        private val KEY_CUSTOM_RULES = stringPreferencesKey("customRulesJson")
        private val KEY_BG_COLOR = intPreferencesKey("bg_color")
        private val KEY_BTN_COLOR = intPreferencesKey("btn_color")
        private val KEY_TXT_COLOR = intPreferencesKey("text_color")
        private val KEY_HISTORY = stringPreferencesKey("roll_history")
        private val KEY_ANIM_SPEED = floatPreferencesKey("anim_speed")
    }


    init {
        runBlocking {
            if (listRules().isEmpty()) {
                seedInitialData()
            }
        }
    }

    private suspend fun seedInitialData() {
        val initialDice = mutableListOf<CustomDie?>()
        val initialRules = mutableListOf<Rule?>()

        val confusionDieId = UUID.randomUUID().toString()
        val confusionDie = CustomDie(
            confusionDieId,
            "Confusion Dice",
            mutableListOf("Confused", "Confused", "Fizzle", "Fail", "Fail")
        )
        initialDice.add(confusionDie)

        initialRules.add(Rule(UUID.randomUUID().toString(), "Advantage",
            mutableListOf(Rule.RuleComponent(true, "D20", null, 2)),
            Rule.RuleModifier(1, null, null, null), 0, null))

        initialRules.add(Rule(UUID.randomUUID().toString(), "Fireball",
            mutableListOf(Rule.RuleComponent(true, "D6", null, 5)),
            Rule.RuleModifier(null, null, null, null), 0, null))

        initialRules.add(Rule(UUID.randomUUID().toString(), "Confusion",
            mutableListOf(Rule.RuleComponent(false, null, confusionDieId, 1)),
            Rule.RuleModifier(null, null, null, null), 0, null))

        saveCustomDice(initialDice)
        saveRules(initialRules)
    }

    // --- Colour Themes ---

    fun saveBackgroundColour(color: Int) = runBlocking {
        context.dataStore.edit { it[KEY_BG_COLOR] = color }
    }
    fun getBackgroundColour(): Int = runBlocking {
        context.dataStore.data.map { it[KEY_BG_COLOR] ?: android.graphics.Color.parseColor("#F5F5F5") }.first()
    }

    fun saveButtonColour(color: Int) = runBlocking {
        context.dataStore.edit { it[KEY_BTN_COLOR] = color }
    }
    fun getButtonColour(): Int = runBlocking {
        context.dataStore.data.map { it[KEY_BTN_COLOR] ?: android.graphics.Color.parseColor("#6200EE") }.first()
    }

    fun saveTextColour(color: Int) = runBlocking {
        context.dataStore.edit { it[KEY_TXT_COLOR] = color }
    }
    fun getTextColour(): Int = runBlocking {
        context.dataStore.data.map { it[KEY_TXT_COLOR] ?: android.graphics.Color.BLACK }.first()
    }

    // --- Dice ---

    fun saveCustomDice(dice: MutableList<CustomDie?>) = runBlocking {
        val json = gson.toJson(dice)
        context.dataStore.edit { it[KEY_CUSTOM_DICE] = json }
    }

    fun listCustomDice(): MutableList<CustomDie?> = runBlocking {
        val json = context.dataStore.data.map { it[KEY_CUSTOM_DICE] }.first()
        if (json.isNullOrEmpty()) return@runBlocking mutableListOf<CustomDie?>()
        val type = object : TypeToken<MutableList<CustomDie?>>() {}.type
        gson.fromJson(json, type)
    }

    // --- Rules ---

    fun saveRules(rules: MutableList<Rule?>) = runBlocking {
        val json = gson.toJson(rules)
        context.dataStore.edit { it[KEY_CUSTOM_RULES] = json }
    }

    fun listRules(): MutableList<Rule?> = runBlocking {
        val json = context.dataStore.data.map { it[KEY_CUSTOM_RULES] }.first()
        if (json.isNullOrEmpty()) return@runBlocking mutableListOf<Rule?>()
        val type = object : TypeToken<MutableList<Rule?>>() {}.type
        gson.fromJson(json, type)
    }

    fun deleteCustomDie(id: String?) {
        val dice = listCustomDice()
        dice.removeAll { it?.id == id }
        saveCustomDice(dice)
    }

    fun deleteRule(id: String?) {
        val rules = listRules()
        rules.removeAll { it?.id == id }
        saveRules(rules)
    }

    fun clearAll() = runBlocking {
        context.dataStore.edit { it.clear() }
    }

    // --- Settings ---

    fun saveAnimSpeed(speed: Float) = runBlocking {
        context.dataStore.edit { it[KEY_ANIM_SPEED] = speed }
    }
    fun getAnimSpeed(): Float = runBlocking {
        context.dataStore.data.map { it[KEY_ANIM_SPEED] ?: 1.0f }.first()
    }

    fun saveHistory(history: List<RollHistoryItem>) = runBlocking {
        val json = gson.toJson(history)
        context.dataStore.edit { it[KEY_HISTORY] = json }
    }

    fun listHistory(): MutableList<RollHistoryItem> = runBlocking {
        val json = context.dataStore.data.map { it[KEY_HISTORY] }.first()
        if (json.isNullOrEmpty()) return@runBlocking mutableListOf<RollHistoryItem>()
        val type = object : TypeToken<List<RollHistoryItem>>() {}.type
        gson.fromJson(json, type)
    }
}
