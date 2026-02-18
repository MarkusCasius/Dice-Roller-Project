package com.example.dicerollerproject.data

import android.content.Context
import android.content.SharedPreferences
import com.example.dicerollerproject.data.model.CustomDie
import com.example.dicerollerproject.data.model.Rule
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Handles local persistence of custom dice and rules using SharedPreferences and GSON.
 */
class LocalStore(context: Context) {
    private val prefs: SharedPreferences
    private val gson: Gson

    init {
        // Initialize SharedPreferences in Private mode (only accessible by this app)
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        this.gson = Gson()
    }

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
     * Wipes all data in the store.
     */
    fun clearAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREF_NAME = "dice_store"
        private const val KEY_CUSTOM_DICE = "customDiceJson"
        private const val KEY_CUSTOM_RULES = "customRulesJson"
    }
}
