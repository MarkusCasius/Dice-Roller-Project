package com.example.dicerollerproject.ui

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import com.example.dicerollerproject.R
import com.example.dicerollerproject.data.LocalStore
import com.example.dicerollerproject.data.model.Rule
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var store: LocalStore
    private val gson = Gson()

    private val exportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let { writeRulesToUri(it) }
    }

    private val importLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { readRulesFromUri(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        store = LocalStore(requireContext())

        view.findViewById<Button>(R.id.btnElemNone).setOnClickListener { store.saveElementColor("#CCCCCC".toColorInt()) }
        view.findViewById<Button>(R.id.btnElemPurple).setOnClickListener { store.saveElementColor("#6200EE".toColorInt()) }
        view.findViewById<Button>(R.id.btnElemGreen).setOnClickListener { store.saveElementColor("#2D5A27".toColorInt()) }

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

                val type = object : TypeToken<MutableList<Rule>>() {}.type
                val importedRules: MutableList<Rule> = gson.fromJson(jsonString, type)

                val currentRules: MutableList<Rule?> = store.listRules()
                val existingIds = currentRules.filterNotNull().map { it.id }.toSet()

                val newRules = importedRules.filter { it.id !in existingIds }
                val newRulesCount = newRules.size

                currentRules.addAll(importedRules.filter { it.id !in existingIds })

                store.saveRules(currentRules)
                Toast.makeText(context, "Imported $newRulesCount new rules!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Import failed: Invalid JSON format", Toast.LENGTH_LONG).show()
        }
    }
}