package com.example.dicerollerproject.ui

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.dicerollerproject.MainActivity
import com.example.dicerollerproject.R
import com.example.dicerollerproject.data.LocalStore
import com.example.dicerollerproject.data.model.CustomDie
import com.example.dicerollerproject.data.model.Rule
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dataconnect.generated.DicerollerConnector
import com.google.firebase.dataconnect.generated.execute
import com.google.firebase.dataconnect.generated.instance
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID
import kotlinx.coroutines.launch

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var store: LocalStore
    private var itemToExport: Any? = null
    private val auth = FirebaseAuth.getInstance()
    private val gson = Gson()

    private var btnNavLogin: Button? = null
    private var layoutLoggedIn: LinearLayout? = null
    private var txtUserStatus: TextView? = null
    private var btnLogout: Button? = null
    private var btnSyncData: Button? = null

    private val authListener = FirebaseAuth.AuthStateListener {
        updateAuthUI()
    }

    override fun onStart() {
        super.onStart()
        // Start listening for auth changes when the fragment is visible
        auth.addAuthStateListener(authListener)
    }

    override fun onStop() {
        super.onStop()
        // Stop listening to prevent memory leaks when the fragment is hidden
        auth.removeAuthStateListener(authListener)
    }

    private val exportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let { writeRulesToUri(it) }
    }

    private val exportItemLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let { writeItemToUri(it) }
    }

    private val importLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { readRulesFromUri(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        store = LocalStore(requireContext())
        applyStoreStyles(requireView())

        view.findViewById<Button>(R.id.btnPickBg).setOnClickListener { showColourPicker("background") }
        view.findViewById<Button>(R.id.btnPickBtn).setOnClickListener { showColourPicker("button") }
        view.findViewById<Button>(R.id.btnPickTxt).setOnClickListener { showColourPicker("text") }

        view.findViewById<Button>(R.id.btnExportRules).setOnClickListener {
            exportLauncher.launch("dice_rules_export.json")
        }

        view.findViewById<Button>(R.id.btnImportRules).setOnClickListener {
            importLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
        }

        view.findViewById<Button>(R.id.btnAnimNormal).setOnClickListener {
            store.saveAnimSpeed(1.0f)
            Toast.makeText(context, "Animation: Normal", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<Button>(R.id.btnAnimFast).setOnClickListener {
            store.saveAnimSpeed(0.4f)
            Toast.makeText(context, "Animation: Fast", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<Button>(R.id.btnAnimInstant).setOnClickListener {
            store.saveAnimSpeed(0.0f)
            Toast.makeText(context, "Animation: Instant", Toast.LENGTH_SHORT).show()
        }

        btnNavLogin = view.findViewById(R.id.btnNavLogin)
        layoutLoggedIn = view.findViewById(R.id.layoutLoggedIn)
        txtUserStatus = view.findViewById(R.id.txtUserStatus)
        btnLogout = view.findViewById(R.id.btnLogout)
        btnSyncData = view.findViewById(R.id.btnSyncData)

        btnNavLogin?.setOnClickListener {
            findNavController().navigate(R.id.loginFragment) // Navigates to LoginFragment
        }

        btnLogout?.setOnClickListener {
            auth.signOut()
            updateAuthUI()
            Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
        }

        btnSyncData?.setOnClickListener {
            syncDataToCloud()
        }

        refreshManagementLists()

        val helpOverlay = view.findViewById<View>(R.id.helpOverlay)
        view.findViewById<View>(R.id.btnHelp).setOnClickListener { helpOverlay.visibility = View.VISIBLE }
        view.findViewById<View>(R.id.btnCloseHelp).setOnClickListener { helpOverlay.visibility = View.GONE }

    }

    /**
     * Updates the UI based on the current auth state
     */
    private fun updateAuthUI() {
        val user = auth.currentUser
        if (view == null) return

        if (user != null) {
            btnNavLogin?.visibility = View.GONE
            layoutLoggedIn?.visibility = View.VISIBLE
            txtUserStatus?.text = "Logged in as: ${user.email}"

            // Refresh management lists now that we are logged in
            refreshManagementLists()
        } else {
            btnNavLogin?.visibility = View.VISIBLE
            layoutLoggedIn?.visibility = View.GONE
            txtUserStatus?.text = ""
        }
    }

    /**
     * Firebase Data Connect Integration
     * Currently WIP, will throw an exception when run.
     */
    private fun syncDataToCloud() {
        val user = auth.currentUser ?: return
        val connector = DicerollerConnector.instance

        val localDice = store.listCustomDice().filterNotNull()
        val localRules = store.listRules().filterNotNull()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                throw Exception("Sync Error")
//                // 1. Sync User Profile
//                connector.upsertUser.execute(user.displayName ?: "Anonymous") {
//                    email = user.email
//                    photoUrl = user.photoUrl?.toString()
//                }
//
//                // 2. Sync Dice
//                localDice.forEach { die ->
//                    val dieId = die.id ?: return@forEach
//                    connector.upsertDie.execute(UUID.fromString(dieId), die.name ?: "Unnamed") {
//                        faces = die.faces?.filterNotNull() ?: emptyList()
//                    }
//                }
//
//                // 3. Sync Rules
//                localRules.forEach { rule ->
//                    val ruleId = rule.id ?: return@forEach
//                    val (nums, faces) = parseRerollForCloud(rule.modifier?.rerollString)
//                    val ruleUuid = UUID.fromString(ruleId)
//
//                    // Create a deterministic UUID for the modifier based on the Rule ID
//                    val modifierUuid = UUID.nameUUIDFromBytes(ruleId.toByteArray())
//
//                    // A. Sync Modifier
//                    // id and name are mandatory, others are optional
//                    connector.upsertModifier.execute(modifierUuid, "${rule.name} Modifier") {
//                        flat = rule.modifier?.flatBonus // If 'flatBonus' is an unresolved reference, your local 'Modifier' data class is missing this property. Please update your local data model.
//                        keepH = rule.modifier?.keepHighest
//                        keepL = rule.modifier?.keepLowest
//                        rerollV = nums
//                        rerollF = faces
//                    }
//
//                    // B. Sync Rule
//                    // id and name are mandatory, desc and modifierId are optional
//                    connector.upsertRule.execute(ruleUuid, rule.name ?: "Unnamed Rule") {
//                        desc = rule.description // If 'description' is an unresolved reference, your local 'Rule' data class is missing this property. Please update your local data model.
//                        modifierId = modifierUuid
//                    }
//
//                    // C. Sync Components (Dice links)
//                    // Delete existing components first to avoid duplicates
//                    connector.deleteRuleComponents.execute(ruleUuid)
//
//                    rule.components?.filterNotNull()?.forEach { comp ->
//                        comp.customDieId?.let { customId ->
//                            // All parameters in UpsertComponent are mandatory (!)
//                            // therefore NO builder block is used.
//                            connector.upsertComponent.execute(
//                                UUID.randomUUID(),
//                                ruleUuid,
//                                UUID.fromString(customId),
//                                comp.count
//                            )
//                        }
//                    }
//                }
//
//                Toast.makeText(context, "Cloud Sync Complete!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("SyncError", "Sync failed: ${e.message}", e)
                Toast.makeText(context, "Sync Failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }


    /**
     * Helper to convert the local "1-2, Fire" string into structured data for Postgres
     */
    private fun parseRerollForCloud(input: String?): Pair<List<Int>, List<String>> {
        val values = mutableListOf<Int>()
        val faces = mutableListOf<String>()

        input?.split(",")?.map { it.trim() }?.forEach { part ->
            if (part.contains("-")) {
                val range = part.split("-")
                val start = range.getOrNull(0)?.toIntOrNull()
                val end = range.getOrNull(1)?.toIntOrNull()
                if (start != null && end != null) {
                    for (v in start..end) values.add(v)
                }
            } else {
                val num = part.toIntOrNull()
                if (num != null) values.add(num)
                else if (part.isNotEmpty()) faces.add(part)
            }
        }
        return Pair(values, faces)
    }

    /**
     * Shows a colour picker dialog
     */
    private fun showColourPicker(type: String) {
        val colors = intArrayOf(
            0xFFFF0000.toInt(), 0xFF00FF00.toInt(), 0xFF0000FF.toInt(), // R, G, B
            0xFFFFFF00.toInt(), 0xFF00FFFF.toInt(), 0xFFFF00FF.toInt(), // Y, C, M
            0xFF000000.toInt(), 0xFFFFFFFF.toInt(), 0xFF888888.toInt(), // B, W, G
            0xFF6200EE.toInt(), 0xFF3700B3.toInt(), 0xFF03DAC5.toInt()  // Material palette
        )
        val colorNames = arrayOf("Red", "Green", "Blue", "Yellow", "Cyan", "Magenta", "Black", "White", "Gray", "Deep Purple", "Dark Blue", "Teal")

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select Global ${type.replaceFirstChar { it.uppercase() }}")

        builder.setItems(colorNames) { _, which ->
            val color = colors[which]
            when (type) {
                "background" -> store.saveBackgroundColour(color)
                "button" -> store.saveButtonColour(color)
                "text" -> store.saveTextColour(color)
            }

            // Apply styles to the current view
            applyStoreStyles(requireView())
            // Apply to BottomNav/Activity
            (activity as? MainActivity)?.applyGlobalStyles()

            Toast.makeText(context, "Global Theme Updated", Toast.LENGTH_SHORT).show()
        }
        builder.show()
    }

    /**
     * Changes the button, text and background colours of the fragment
     */
    private fun applyStoreStyles(rootView: View) {
        val store = LocalStore(requireContext())
        val bgColour = store.getBackgroundColour()
        val txtColour = store.getTextColour()
        val btnColour = store.getButtonColour()

        // Set the background of the fragment root itself
        rootView.setBackgroundColor(bgColour)

        // Recursively apply colors to children
        if (rootView is ViewGroup) {
            applyRecursiveStyles(rootView, txtColour, btnColour)
        }
    }

    /**
     * Recursively applies colours to children
     */
    private fun applyRecursiveStyles(viewGroup: ViewGroup, txtCol: Int, btnCol: Int) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)

            when (child) {
                is Button -> {
                    child.setBackgroundColor(btnCol)
                    child.setTextColor(Color.WHITE) // High contrast for buttons
                }
                is TextView -> {
                    child.setTextColor(txtCol)
                }
                is TextInputLayout -> {
                    child.defaultHintTextColor = ColorStateList.valueOf(txtCol)
                    // Handle the nested EditText inside TextInputLayout
                    val editText = child.editText
                    editText?.setTextColor(txtCol)
                    editText?.setHintTextColor(txtCol)
                }
                is ViewGroup -> {
                    // Gestalt: Enclosure - Keep Cards white or a slightly lighter/darker shade
                    // than BG to maintain grouping, or let them take the BG color.
                    // For now, we continue recursion
                    applyRecursiveStyles(child, txtCol, btnCol)
                }
            }
        }
    }

    /**
     * Refreshes the management lists
     */
    private fun refreshManagementLists() {
        val diceContainer = view?.findViewById<LinearLayout>(R.id.listManageDice)
        val rulesContainer = view?.findViewById<LinearLayout>(R.id.listManageRules)

        diceContainer?.removeAllViews()
        rulesContainer?.removeAllViews()

        // Populate Dice
        store.listCustomDice().filterNotNull().forEach { die ->
            val row = layoutInflater.inflate(R.layout.item_manage_resource, diceContainer, false)
            row.findViewById<TextView>(R.id.txtName).text = die.name
            row.findViewById<View>(R.id.btnDeleteItem).setOnClickListener {
                store.deleteCustomDie(die.id)
                refreshManagementLists()
            }
            row.findViewById<View>(R.id.btnExportItem).setOnClickListener {
                itemToExport = die
                exportItemLauncher.launch("die_${die.name}.json")
            }
            diceContainer?.addView(row)
        }

        // Populate Rules
        store.listRules().filterNotNull().forEach { rule ->
            val row = layoutInflater.inflate(R.layout.item_manage_resource, rulesContainer, false)
            row.findViewById<TextView>(R.id.txtName).text = rule.name
            row.findViewById<View>(R.id.btnDeleteItem).setOnClickListener {
                store.deleteRule(rule.id)
                refreshManagementLists()
            }
            row.findViewById<View>(R.id.btnExportItem).setOnClickListener {
                itemToExport = rule
                exportItemLauncher.launch("rule_${rule.name}.json")
            }
            rulesContainer?.addView(row)
        }
    }

    private fun writeItemToUri(uri: Uri) {
        try {
            val jsonString = gson.toJson(itemToExport)
            requireContext().contentResolver.openOutputStream(uri)?.use { os ->
                os.write(jsonString.toByteArray())
            }
            Toast.makeText(context, "Item exported!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to export item", Toast.LENGTH_SHORT).show()
        } finally {
            itemToExport = null
        }
    }

    private fun writeRulesToUri(uri: Uri) {
        try {
            val rules = store.listRules()
            val jsonString = gson.toJson(rules)

            requireContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonString.toByteArray())
            }
            Toast.makeText(context, "Rules exported successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun readRulesFromUri(uri: Uri) {
        try {
            requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream))
                val jsonString = reader.readText()

                val jsonElement = com.google.gson.JsonParser.parseString(jsonString)
                if (jsonElement.isJsonArray) {
                    // --- CASE 1: ENTIRE SAVE (List of Rules) ---
                    val type = object : TypeToken<MutableList<Rule?>>() {}.type
                    val importedRules: MutableList<Rule?> = gson.fromJson(jsonString, type)
                    importRulesList(importedRules)
                }
                else if (jsonElement.isJsonObject) {
                    val jsonObject = jsonElement.asJsonObject

                    if (jsonObject.has("faces")) {
                        // --- CASE 2: SINGLE CUSTOM DIE ---
                        val importedDie = gson.fromJson(jsonString, CustomDie::class.java)
                        importSingleDie(importedDie)
                    }
                    else if (jsonObject.has("components")) {
                        // --- CASE 3: SINGLE RULE ---
                        val importedRule = gson.fromJson(jsonString, Rule::class.java)
                        importRulesList(mutableListOf(importedRule))
                    }
                    else {
                        throw IllegalArgumentException("Unknown JSON format")
                    }
                }

                refreshManagementLists()
            }
        } catch (e: Exception) {
            Log.e("ImportError", "Failed to import: ${e.message}")
            Toast.makeText(context, "Import failed: Invalid or unsupported JSON", Toast.LENGTH_LONG).show()
        }
    }

    private fun importRulesList(importedRules: MutableList<Rule?>) {
        val currentRules = store.listRules() ?: mutableListOf()
        val existingIds = currentRules.filterNotNull().map { it.id }.toSet()

        val newOnes = importedRules.filterNotNull().filter { it.id !in existingIds }
        currentRules.addAll(newOnes)

        store.saveRules(currentRules)
        Toast.makeText(context, "Imported ${newOnes.size} rules", Toast.LENGTH_SHORT).show()
    }

    private fun importSingleDie(die: CustomDie) {
        val currentDice = store.listCustomDice() ?: mutableListOf()
        // Deduplicate by ID
        if (currentDice.none { it?.id == die.id }) {
            currentDice.add(die)
            store.saveCustomDice(currentDice)
            Toast.makeText(context, "Imported die: ${die.name}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Die '${die.name}' already exists", Toast.LENGTH_SHORT).show()
        }
    }
}