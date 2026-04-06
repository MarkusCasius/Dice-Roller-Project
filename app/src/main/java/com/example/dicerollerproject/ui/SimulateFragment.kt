package com.example.dicerollerproject.ui

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.dicerollerproject.R
import com.example.dicerollerproject.data.LocalStore
import com.example.dicerollerproject.data.RuleMapper
import com.example.dicerollerproject.data.model.Rule
import com.example.dicerollerproject.domain.Dice
import com.example.dicerollerproject.domain.DiceEngine
import com.example.dicerollerproject.domain.Modifier
import com.example.dicerollerproject.domain.RollSpec
import java.util.Locale
import java.util.TreeMap
import kotlin.math.max
import kotlin.math.min

/**
 * A Fragment used to show the user the likelihood of roll outcomes
 */
class SimulateFragment : Fragment() {
    private var store: LocalStore? = null
    private var engine: DiceEngine? = null
    private var spinnerRules: Spinner? = null
    private var editTrials: EditText? = null
    private var progressBar: ProgressBar? = null
    private var textResults: TextView? = null
    private var layoutHistogram: LinearLayout? = null
    private var savedRules: MutableList<Rule?> = mutableListOf()
    private var switchCategoricalMode: com.google.android.material.switchmaterial.SwitchMaterial? = null
    // Modifiers
    private var editReroll: EditText? = null
    private var editFlat: EditText? = null
    private var checkKeepHighest: CheckBox? = null
    private var editKeepHighest: EditText? = null
    private var checkKeepLowest: CheckBox? = null
    private var editKeepLowest: EditText? = null
    private var btnRunSimulation: Button? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_simulate, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        store = LocalStore(requireContext())
        engine = DiceEngine(null)

        spinnerRules?.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val isManual = position == 0
                // Enable or disable manual inputs based on selection
                setManualModifiersEnabled(isManual)

                if (!isManual) {
                    // If a rule is selected, show its modifiers in the UI (read-only)
                    val rule = savedRules[position - 1]
                    applyRuleToUI(rule)
                } else {
                    // If switching back to manual, clear the fields for fresh input
                    clearManualModifiers()
                }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        spinnerRules = view.findViewById<Spinner>(R.id.spinnerSimulateRule)
        editTrials = view.findViewById<EditText>(R.id.editTrials)
        progressBar = view.findViewById<ProgressBar>(R.id.progressSimulation)
        textResults = view.findViewById<TextView>(R.id.textSimResults)
        layoutHistogram = view.findViewById<LinearLayout>(R.id.layoutHistogram)
        switchCategoricalMode = view.findViewById(R.id.switchCategoricalMode)
        btnRunSimulation = view.findViewById<Button>(R.id.btnRunSimulation)

        // Modifiers
        editReroll = view.findViewById(R.id.editRerollSim)
        editFlat = view.findViewById(R.id.editFlatSim)
        checkKeepHighest = view.findViewById(R.id.checkKeepHighestSim)
        editKeepHighest = view.findViewById(R.id.editKeepHighestSim)
        checkKeepLowest = view.findViewById(R.id.checkKeepLowestSim)
        editKeepLowest = view.findViewById(R.id.editKeepLowestSim)

        checkKeepHighest?.setOnCheckedChangeListener { _, isChecked ->
            editKeepHighest?.isEnabled = isChecked
            if (isChecked) {
                checkKeepLowest?.isChecked = false
                if (editKeepHighest?.text.isNullOrEmpty()) editKeepHighest?.setText("1")
            }
        }
        checkKeepLowest?.setOnCheckedChangeListener { _, isChecked ->
            editKeepLowest?.isEnabled = isChecked
            if (isChecked) {
                checkKeepHighest?.isChecked = false
                if (editKeepLowest?.text.isNullOrEmpty()) editKeepLowest?.setText("1")
            }
        }
        btnRunSimulation?.setOnClickListener { runSimulation() }

        refreshRuleSpinner()

    }

    private fun refreshRuleSpinner() {
        savedRules = store!!.listRules()
        val names = mutableListOf("Manual Simulation")
        names.addAll(savedRules.map { it?.name ?: "Unnamed Rule" })

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRules?.adapter = adapter
    }

    private fun runSimulation() {
        if (savedRules.isEmpty()) return

        val allDice = store!!.listCustomDice()
        val selectedRuleIndex = spinnerRules?.selectedItemPosition ?: 0

        val (specs, modifier) = if (selectedRuleIndex > 0) {
            val selectedRule = savedRules[selectedRuleIndex - 1]
            val prepared = RuleMapper.prepare(selectedRule, allDice)
            // Use specs and modifier defined in the saved rule
            Pair(prepared.specs, prepared.mod)
        } else {
            // Manual Mode: Default to rolling 1D6 if no rule is selected
            val manualSpec = mutableListOf(RollSpec(Dice.standard(Dice.Standard.D6), 1))
            val manualMod = Modifier.none()

            // Parse Manual Reroll Logic
            val rerollInput = editReroll?.text?.toString() ?: ""
            if (rerollInput.isNotEmpty()) {
                val parts = rerollInput.split(",").map { it.trim() }
                for (part in parts) {
                    if (part.contains("-")) {
                        val range = part.split("-")
                        val start = range.getOrNull(0)?.toIntOrNull()
                        val end = range.getOrNull(1)?.toIntOrNull()
                        if (start != null && end != null) {
                            for (v in start..end) manualMod.rerollValues.add(v)
                        }
                    } else {
                        val numeric = part.toIntOrNull()
                        if (numeric != null) manualMod.rerollValues.add(numeric)
                        else manualMod.rerollFaces.add(part)
                    }
                }
            }
            // Parse Manual Flat Mod
            manualMod.flat = editFlat?.text?.toString()?.toIntOrNull() ?: 0

            // Parse Manual Keep High/Low
            if (checkKeepHighest?.isChecked == true) {
                manualMod.keepHighest = editKeepHighest?.text.toString().toIntOrNull() ?: 1
            } else if (checkKeepLowest?.isChecked == true) {
                manualMod.keepLowest = editKeepLowest?.text.toString().toIntOrNull() ?: 1
            }

            Pair(manualSpec, Modifier.none())
        }

        val isCategorical = specs.any { !it.die.isStandard }
        val groupByOutcome = switchCategoricalMode?.isChecked ?: false

        val trials: Int = try {
            editTrials!!.getText().toString().toInt()
        } catch (e: NumberFormatException) {
            1000
        }

        progressBar!!.visibility = View.VISIBLE
        progressBar!!.max = trials
        progressBar!!.progress = 0
        layoutHistogram!!.removeAllViews()

        // Run on a background thread to keep UI smooth
        Thread(Runnable {
            val frequencyMap: TreeMap<String, Int> = TreeMap { a, b ->
                val aInt = a.toIntOrNull()
                val bInt = b.toIntOrNull()
                if (aInt != null && bInt != null) {
                    aInt.compareTo(bInt)
                } else {
                    a.compareTo(b)
                }
            }
            var totalSum: Long = 0
            var min = Int.MAX_VALUE
            var max = Int.MIN_VALUE

            for (i in 0 until trials) {
                val res = engine!!.roll(specs, modifier)

                if (isCategorical) {
                    // Mode: Categorical - Count every face that appeared in this roll
                    if (groupByOutcome) {
                        // MODE: Per Outcome (e.g., "Fire, Ice")
                        // Join all faces from this specific trial into one string
                        val outcomeKey = res.facesRolled.filterNotNull().sorted().joinToString(", ")
                        frequencyMap[outcomeKey] = frequencyMap.getOrDefault(outcomeKey, 0) + 1
                    } else {
                        // MODE: Per Face (Previous behavior)
                        res.facesRolled.forEach { face ->
                            val key = face ?: "null"
                            frequencyMap[key] = frequencyMap.getOrDefault(key, 0) + 1
                        }
                    }
                } else {
                    // Mode: Ordinal - Count the total sum
                    val value = res.total
                    totalSum += value.toLong()
                    min = min(min, value)
                    max = max(max, value)
                    val key = value.toString()
                    frequencyMap[key] = frequencyMap.getOrDefault(key, 0)!! + 1
                }

                if (i % 100 == 0) {
                    val currentI = i
                    requireActivity().runOnUiThread { progressBar!!.progress = currentI }
                }
            }

            // Calculations
            val stats = if (!isCategorical) {
                val mean = totalSum.toDouble() / trials
                var mode = ""
                var maxF = -1
                frequencyMap.forEach { (k, v) ->
                    if (v!! > maxF) {
                        maxF = v
                        mode = k
                    }
                }
                String.format(Locale.getDefault(), "Mean: %.2f\nMode: %s\nMin: %d\nMax: %d\nTrials: %d",
                    mean, mode, min, max, trials)
            } else {
                val label = if (groupByOutcome) "Unique Outcomes" else "Unique Faces"
                "Categorical Simulation\nTotal Results Tracked: ${frequencyMap.values.sum()}\nTrials: $trials"
            }

            val maxFreq = frequencyMap.values.maxOrNull() ?: 1
            val denominator = if (isCategorical) frequencyMap.values.sum().toDouble() else trials.toDouble()

            requireActivity().runOnUiThread {
                progressBar!!.visibility = View.GONE
                textResults!!.text = stats
                drawHistogram(frequencyMap, maxFreq, denominator)
            }
        }).start()
    }

    private fun drawHistogram(data: MutableMap<String, Int>, maxFreq: Int, totalPopulation: Double) {
        layoutHistogram!!.removeAllViews()

        for (entry in data.entries) {
            val count = entry.value ?: 0
            val percentage = (count.toDouble() / totalPopulation) * 100
            val fullLabel = entry.key

            // Create a bar (View)
            val barContainer = LinearLayout(requireContext())
            barContainer.orientation = LinearLayout.VERTICAL
            barContainer.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            barContainer.setPadding(8, 0, 8, 0)

            // Interactive Bar
            barContainer.isClickable = true
            barContainer.setFocusable(true)
            val outValue = android.util.TypedValue()
            requireContext().theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            barContainer.setBackgroundResource(outValue.resourceId) // Add ripple effect

            barContainer.setOnClickListener {
                val message = "Result: $fullLabel\n" +
                        "Count: $count\n" +
                        "Likelihood: ${String.format(Locale.getDefault(), "%.2f%%", percentage)}"

                // Update the results text view or show a Toast
                textResults!!.text = message
            }

            // Tooltip for percentage
            val percLabel = TextView(requireContext())
            percLabel.text = String.format(Locale.getDefault(), "%.1f%%", percentage)
            percLabel.textSize = 8f
            percLabel.gravity = Gravity.CENTER
            barContainer.addView(percLabel)

            // Bar itself
            val bar = View(requireContext())
            val heightPx = ((count.toDouble() / maxFreq) * 400).toInt() // 400px max height
            // Calculate height as percentage of max frequency
            val barParams = LinearLayout.LayoutParams(50, heightPx)
            barParams.setMargins(8, 0, 8, 0)
            bar.setLayoutParams(barParams)
            bar.setBackgroundColor(resources.getColor(android.R.color.holo_blue_dark))
            barContainer.addView(bar)

            // X-Axis Label
            val valLabel = TextView(requireContext())
            valLabel.text = entry.key.toString()
            valLabel.textSize = 10f
            valLabel.setPadding(0, 4, 0, 0)
            valLabel.setGravity(Gravity.CENTER)
            valLabel.maxLines = 1
            valLabel.ellipsize = android.text.TextUtils.TruncateAt.END
            valLabel.layoutParams = LinearLayout.LayoutParams(80, ViewGroup.LayoutParams.WRAP_CONTENT)
            barContainer.addView(valLabel)

            // Tooltip or label could be added here
            layoutHistogram!!.addView(barContainer)
        }
    }

    private fun setManualModifiersEnabled(enabled: Boolean) {
        val alpha = if (enabled) 1.0f else 0.5f

        // Toggle all modifier inputs
        editReroll?.isEnabled = enabled
        editFlat?.isEnabled = enabled
        checkKeepHighest?.isEnabled = enabled
        checkKeepLowest?.isEnabled = enabled

        // Visual feedback: dim the grid when disabled
        view?.findViewById<androidx.gridlayout.widget.GridLayout>(R.id.modifierGridSim)?.alpha = alpha
    }

    private fun applyRuleToUI(rule: Rule?) {
        rule?.let {
            editFlat?.setText(it.flat.toString())
            editReroll?.setText(it.modifier?.rerollString ?: "")

            checkKeepHighest?.isChecked = it.modifier?.keepHighest != null
            editKeepHighest?.setText(it.modifier?.keepHighest?.toString() ?: "")

            checkKeepLowest?.isChecked = it.modifier?.keepLowest != null
            editKeepLowest?.setText(it.modifier?.keepLowest?.toString() ?: "")
        }
    }

    private fun clearManualModifiers() {
        editFlat?.setText("")
        editReroll?.setText("")
        checkKeepHighest?.isChecked = false
        editKeepHighest?.setText("")
        checkKeepLowest?.isChecked = false
        editKeepLowest?.setText("")
    }
}