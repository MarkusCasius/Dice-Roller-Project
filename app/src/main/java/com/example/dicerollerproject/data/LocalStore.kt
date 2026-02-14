package com.example.dicerollerproject.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.dicerollerproject.data.model.CustomDie;
import com.example.dicerollerproject.data.model.Rule;
import com.example.dicerollerproject.domain.Modifier;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles local persistence of custom dice and rules using SharedPreferences and GSON.
 */
public class LocalStore {
  private static final String PREF_NAME = "dice_store";
  private static final String KEY_CUSTOM_DICE = "customDiceJson";
  private static final String KEY_CUSTOM_RULES = "customRulesJson";

  private final SharedPreferences prefs;
  private final Gson gson;

  public LocalStore(Context context) {
    // Initialize SharedPreferences in Private mode (only accessible by this app)
    this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    this.gson = new Gson();
  }

  /**
   * Saves the list of custom dice to local storage.
   */
  public void saveCustomDice(List<CustomDie> dice) {
    String json = gson.toJson(dice);
    prefs.edit().putString(KEY_CUSTOM_DICE, json).apply();
  }

  /**
   * Retrieves the list of custom dice from local storage.
   * Returns an empty list if nothing is saved.
   */
  public List<CustomDie> listCustomDice() {
    String json = prefs.getString(KEY_CUSTOM_DICE, null);
    if (json == null || json.isEmpty()) return new ArrayList<>();
    Type type = new TypeToken<List<CustomDie>>() {}.getType();
    return gson.fromJson(json, type);
  }

  /**
   * Saves custom rules (Modifiers) to local storage.
   */
  public void saveRules(List<Rule> rules) {
    String json = gson.toJson(rules);
    prefs.edit().putString(KEY_CUSTOM_RULES, json).apply();
  }

  /**
   * Retrieves the list of custom rules from local storage.
   */
  public List<Rule> listRules() {
    String json = prefs.getString(KEY_CUSTOM_RULES, null);
    if (json == null || json.isEmpty()) return new ArrayList<>();
    Type type = new TypeToken<List<Rule>>() {}.getType();
    return gson.fromJson(json, type);
  }

  /**
   * Wipes all data in the store.
   */
  public void clearAll() {
    prefs.edit().clear().apply();
  }
}
