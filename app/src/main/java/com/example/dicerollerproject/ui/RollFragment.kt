package com.example.dicerollerproject.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.example.dicerollerproject.R
import com.example.dicerollerproject.data.LocalStore
import com.example.dicerollerproject.data.RuleMapper
import com.example.dicerollerproject.data.model.RollHistoryItem
import com.example.dicerollerproject.data.model.Rule
import com.example.dicerollerproject.domain.Dice
import com.example.dicerollerproject.domain.DiceEngine
import com.example.dicerollerproject.domain.Modifier
import com.example.dicerollerproject.domain.RollResult
import com.example.dicerollerproject.domain.RollSpec
import java.util.UUID

class RollFragment : Fragment() {
    private lateinit var diceEngine: DiceEngine
    private lateinit var store: LocalStore

    // UI Components
    private var spinnerDice: Spinner? = null

    private var editTextReroll: EditText? = null
    private var checkBoxKeepHighest: CheckBox? = null
    private var editTextKeepHighest: EditText? = null
    private var checkBoxKeepLowest: CheckBox? = null
    private var editTextKeepLowest: EditText? = null
    private var editTextFlat: EditText? = null
    private var buttonRoll: Button? = null
    private var buttonClear: Button? = null
    private var textView: TextView? = null
    private var spinnerSavedRules: Spinner? = null
    private var tray: ViewGroup? = null
    private var buttonHistory: ImageButton? = null

    private var savedRules: MutableList<Rule?> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        diceEngine = DiceEngine(null)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_roll, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spinnerSavedRules?.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    // Manual Roll selected: Enable everything and clear
                    setManualModifiersEnabled(true)
                    clearInputs()
                } else {
                    // Saved Rule selected: Disable manual inputs and show the Rule's modifiers
                    val rule = savedRules[position - 1]
                    setManualModifiersEnabled(false)
                    applyRuleToUI(rule)
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        store = LocalStore(requireContext())
        applyStoreStyles(requireView())


        // Help Guide
        val helpOverlay = view.findViewById<View>(R.id.helpOverlay)
        val btnHelp = view.findViewById<View>(R.id.btnHelp) // Ensure you have btnHelp in RollFragment header too
        val btnCloseHelp = view.findViewById<Button>(R.id.btnCloseHelp)
        btnHelp?.setOnClickListener { helpOverlay.visibility = View.VISIBLE }
        btnCloseHelp?.setOnClickListener { helpOverlay.visibility = View.GONE }
        // Rest of UI Components
        editTextReroll = view.findViewById(R.id.editTextReroll)
        editTextFlat = view.findViewById<EditText>(R.id.editTextFlat)
        checkBoxKeepHighest = view.findViewById(R.id.checkBoxKeepHighest)
        editTextKeepHighest = view.findViewById(R.id.editTextKeepHighest)
        checkBoxKeepLowest = view.findViewById(R.id.checkBoxKeepLowest)
        editTextKeepLowest = view.findViewById(R.id.editTextKeepLowest)
        buttonRoll = view.findViewById<Button>(R.id.buttonRoll)
        buttonClear = view.findViewById<Button>(R.id.buttonClear)
        textView = view.findViewById<TextView>(R.id.textView)
        spinnerSavedRules = view.findViewById<Spinner>(R.id.spinnerSavedRules)
        tray = view.findViewById<ViewGroup>(R.id.diceTrayContainer)
        buttonHistory = view.findViewById(R.id.btnHistory)
        buttonHistory?.setOnClickListener { showHistoryBottomSheet() }

        checkBoxKeepHighest?.setOnCheckedChangeListener { _, isChecked ->
            editTextKeepHighest?.isEnabled = isChecked
            if (isChecked) {
                checkBoxKeepLowest?.isChecked = false
                if (editTextKeepHighest?.text.isNullOrEmpty()) editTextKeepHighest?.setText("1")
            }
        }

        checkBoxKeepLowest?.setOnCheckedChangeListener { _, isChecked ->
            editTextKeepLowest?.isEnabled = isChecked
            if (isChecked) {
                checkBoxKeepHighest?.isChecked = false
                if (editTextKeepLowest?.text.isNullOrEmpty()) editTextKeepLowest?.setText("1")
            }
        }

        buttonHistory?.setOnClickListener {
            showHistoryBottomSheet()
        }

        buttonRoll?.setOnClickListener {
            rollDice()
        }

        buttonClear?.setOnClickListener {
            clearInputs()
        }

        val btnAdd = view.findViewById<Button>(R.id.btnAddDiceRow)
        btnAdd.setOnClickListener {
            addDiceRow()
        }

        // Set up the Spinners with standard dice types
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            Dice.Standard.entries.toTypedArray()
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDice?.setAdapter(adapter)

        // Populate the spinner with saved rules
        refreshRules()

    }

    /**
     * Refreshes the saved rules spinner
     */
    private fun refreshRules() {
        savedRules = store.listRules() ?: mutableListOf()

        val ruleNames = mutableListOf("Manual Roll (No Rule)")
        ruleNames.addAll(savedRules.map { it?.name ?: "Unnamed Rule" })

        val adapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item, ruleNames
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSavedRules?.adapter = adapter
    }

    /**
     * Adds a new row to the dice container
     * @param dieType Type of die to add
     * @param count Number of dice to add
     */
    private fun addDiceRow(dieType: String? = null, count: Int = 1) {
        val container = view?.findViewById<LinearLayout>(R.id.layoutDiceContainer) ?: return
        val row = layoutInflater.inflate(R.layout.item_dice_row, container, false)

        val spinner = row.findViewById<Spinner>(R.id.spinnerDiceRow)
        val countEdit = row.findViewById<EditText>(R.id.editDiceCountRow)
        val btnRemove = row.findViewById<ImageButton>(R.id.btnRemoveRow)
        val options = mutableListOf<String>()

        Dice.Standard.entries.forEach { options.add(it.name) }

        val customDice = store.listCustomDice()
        customDice.filterNotNull().forEach { options.add("Custom: ${it.name}") }


        // Set adapter for spinner (Standard Dice)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        btnRemove.setOnClickListener { container.removeView(row) }
        container.addView(row)
    }

    /**
     * Rolls the dice and displays the result
     */
    private fun rollDice() {
        val selectedRuleIndex = spinnerSavedRules?.selectedItemPosition ?: 0
        val specs = mutableListOf<RollSpec>()
        val modifier: Modifier = Modifier.Companion.none()

        val result: RollResult = if (selectedRuleIndex > 0) {
            // Saved Rule
            val selectedRule = savedRules.get(selectedRuleIndex - 1)
            val allCustomDice = store.listCustomDice()

            // Use the Mapper to convert the Persistence Rule into Domain Specs/Modifiers
            val prepared = RuleMapper.prepare(selectedRule, allCustomDice)
            diceEngine!!.roll(prepared.specs, prepared.mod)
        } else {
            // Manual roll
            // Get the selected die type from the spinner
            val container = view?.findViewById<LinearLayout>(R.id.layoutDiceContainer)

            var totalNumDice = 0

            container?.children?.forEach { row ->
                val spinner = row.findViewById<Spinner>(R.id.spinnerDiceRow)
                val qty = row.findViewById<EditText>(R.id.editDiceCountRow).text.toString().toIntOrNull() ?: 1
                val selectedString = spinner.selectedItem.toString()

                val die: Dice = if (selectedString.startsWith("Custom: ")) {
                    val dieName = selectedString.replace("Custom: ", "")
                    val customDie = store.listCustomDice().find { it?.name == dieName }
                    Dice.custom(customDie?.faces?.toMutableList() ?: mutableListOf())
                } else {
                    Dice.standard(Dice.Standard.valueOf(selectedString))
                }

                specs.add(RollSpec(die, qty))
                totalNumDice += qty
            }

            if (specs.isEmpty()) {
                specs.add(RollSpec(Dice.standard(Dice.Standard.D6), 1))
                totalNumDice = 1
            }
            // Create the RollSpec based on the die type and count

            // Get the flat modifier, defaulting to 0 if empty
            val flatModStr = editTextFlat!!.getText().toString()
            val flatMod = if (flatModStr.isEmpty()) 0 else flatModStr.toInt()

            // Check the reroll and keep/drop options
            val rerollInput = editTextReroll?.text?.toString() ?: ""
            modifier.rerollValues.clear()
            modifier.rerollFaces.clear()

            if (rerollInput.isNotEmpty()) {
                val parts = rerollInput.split(",").map { it.trim() }
                for (part in parts) {
                    if (part.contains("-")) {
                        // Handle range: "1-3"
                        val rangeParts = part.split("-")
                        val start = rangeParts.getOrNull(0)?.toIntOrNull()
                        val end = rangeParts.getOrNull(1)?.toIntOrNull()
                        if (start != null && end != null) {
                            for (v in start..end) modifier.rerollValues.add(v)
                        }
                    } else {
                        // Handle single value or face
                        val numeric = part.toIntOrNull()
                        if (numeric != null) {
                            modifier.rerollValues.add(numeric)
                        } else {
                            modifier.rerollFaces.add(part)
                        }
                    }
                }
            }

            var keepHigh: Int? = null
            var keepLow: Int? = null

            if (checkBoxKeepHighest?.isChecked == true) {
                // Read user input, default to 1 if empty
                val qty = editTextKeepHighest?.text.toString().toIntOrNull() ?: 1
                keepHigh = qty.coerceIn(1, totalNumDice)
            } else if (checkBoxKeepLowest?.isChecked == true) {
                val qty = editTextKeepLowest?.text.toString().toIntOrNull() ?: 1
                keepLow = qty.coerceIn(1, totalNumDice)
            }

            // Create the Modifier object
            modifier.flatBonus = flatMod
            modifier.keepHighest = keepHigh
            modifier.keepLowest = keepLow

            // Call the DiceEngine to get the result
            diceEngine.roll(specs, modifier)
        }

        val description = if (selectedRuleIndex > 0) {
            savedRules[selectedRuleIndex - 1]?.name ?: "Unnamed Rule"
        } else {
            getDiceDescription(specs)
        }

        triggerVisualRoll(result)

        view?.findViewById<ScrollView>(R.id.scrollDice)?.post {
            view?.findViewById<ScrollView>(R.id.scrollDice)?.smoothScrollTo(0, 0)
        }

        // Save to history
        val historyItem = RollHistoryItem(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            diceDescription = description,
            results = result.facesRolled.toString(),
            total = result.total,
            modifierLabel = "Mod: ${modifier.flatBonus}"
        )

        val history = store.listHistory()
        history.add(0, historyItem) // Add to top
        store.saveHistory(history.take(50)) // Keep last 50 rolls

        // Display result in the TextView
        textView?.postDelayed({
            textView?.text = String.format("Result: %d\nRolls: %s", result.total, result.facesRolled.toString())
        }, 1000)
    }

    /**
     * Enables or disables manual modifier inputs
     */
    private fun setManualModifiersEnabled(enabled: Boolean) {
        editTextReroll?.isEnabled = enabled
        editTextFlat?.isEnabled = enabled
        checkBoxKeepHighest?.isEnabled = enabled
        checkBoxKeepLowest?.isEnabled = enabled
        view?.findViewById<Button>(R.id.btnAddDiceRow)?.isEnabled = enabled
        val alpha = if (enabled) 1.0f else 0.5f
        view?.findViewById<View>(R.id.modifierGrid)?.alpha = alpha
    }

    /**
     * Applies a rule to the UI
     */
    private fun applyRuleToUI(rule: Rule?) {
        rule?.let {
            editTextFlat?.setText(it.flat.toString())
            editTextReroll?.setText(it.modifier?.rerollString ?: "")

            checkBoxKeepHighest?.isChecked = it.modifier?.keepHighest != null
            editTextKeepHighest?.setText(it.modifier?.keepHighest?.toString() ?: "")

            checkBoxKeepLowest?.isChecked = it.modifier?.keepLowest != null
            editTextKeepLowest?.setText(it.modifier?.keepLowest?.toString() ?: "")

            // Clear the dynamic dice rows and show a placeholder or the rule's dice
            val container = view?.findViewById<LinearLayout>(R.id.layoutDiceContainer)
            container?.removeAllViews()
            val infoText = TextView(requireContext()).apply {
                text = "Using dice defined in Rule: ${it.name}"
                setPadding(16, 16, 16, 16)
            }
            container?.addView(infoText)
        }
    }

    /**
     * Triggers the animation of the dice being rolled.
     */
    private fun triggerVisualRoll(result: RollResult) {
        val tray = view?.findViewById<ViewGroup>(R.id.diceTrayContainer) ?: return
        val rollId = System.currentTimeMillis()
        val viewsFromThisRoll = mutableListOf<View>()
        val animSpeed = store.getAnimSpeed()

        // Filter for standard dice only and limit to 20
        val totalRolls = result.numericContributions.size
        val displayLimit = 20

        for (i in 0 until minOf(totalRolls, displayLimit)) {
            val value = result.numericContributions[i]
            val faceText = result.facesRolled[i] ?: ""
            val diceContainer = FrameLayout(requireContext())
            diceContainer.tag = rollId

            val diceView = ImageView(requireContext())
            diceView.setImageResource(R.drawable.dice_generic)
            val imgParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            diceContainer.addView(diceView, imgParams)

            val resultLabel = TextView(requireContext()).apply {
                text = faceText
                setTextColor(android.graphics.Color.BLACK)
                textSize = 14f
                gravity = android.view.Gravity.CENTER
                visibility = View.INVISIBLE // Hide during animation
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            diceContainer.addView(resultLabel)

            // Randomize position
            val containerParams = FrameLayout.LayoutParams(110, 110)
            // Horizontal: Stay away from edges
            containerParams.leftMargin = (60..maxOf(60, tray.width - 160)).random()
            // Vertical: Avoid the top 60dp (buttons) and bottom 100dp (card area)
            val minY = 70
            val maxY = maxOf(minY + 10, tray.height - 220)
            containerParams.topMargin = (minY..maxY).random()
            diceContainer.layoutParams = containerParams
            tray.addView(diceContainer)
            viewsFromThisRoll.add(diceContainer)

            if (animSpeed == 0.0f) {
                // Instant Mode
                diceView.setImageResource(getDiceDrawable(value))
                if (value > 6 || value == 0) {
                    resultLabel.visibility = View.VISIBLE
                }
            } else {
                // Animated mode
                val baseDuration = (600 + (i * 150)).toLong()
                val scaledDuration = (baseDuration * animSpeed).toLong()

                diceContainer.animate()
                    .rotation(1080f)
                    .scaleX(1.2f).scaleY(1.2f)
                    .setDuration(scaledDuration) // Apply the scaled duration
                    .withEndAction {
                        diceView.setImageResource(getDiceDrawable(value))
                        if (value > 6 || value == 0) {
                            resultLabel.visibility = View.VISIBLE
                        }
                        diceContainer.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start()
                    }
                    .start()
            }

            val clearDelay = if (animSpeed == 0.0f) 2000L else 3000L

            tray.postDelayed({
                viewsFromThisRoll.forEach { view ->
                    view.animate()
                        .alpha(0f)
                        .setDuration(500)
                        .withEndAction {
                            tray.removeView(view)
                        }
                        .start()
                }
            }, 3000)
        }
    }

    /**
     * Helper to map numeric result to a drawable resource
     */
    private fun getDiceDrawable(value: Int): Int {
        return when(value) {
            1 -> R.drawable.dice_1
            2 -> R.drawable.dice_2
            3 -> R.drawable.dice_3
            4 -> R.drawable.dice_4
            5 -> R.drawable.dice_5
            6 -> R.drawable.dice_6
            else -> R.drawable.dice_generic // For D20s etc
        }
    }

    /**
     * Get's the dice description for history
     */
    private fun getDiceDescription(specs: List<RollSpec>): String {
        return specs.joinToString(", ") { spec ->
            val name = if (spec.die.isStandard) "D${spec.die.sides()}" else "Custom"
            "${spec.count}x $name"
        }
    }

    /**
     * Clears all inputs
     */
    private fun clearInputs() {
        editTextFlat?.setText("")
        editTextReroll?.setText("")
        checkBoxKeepHighest?.isChecked = false
        editTextKeepHighest?.setText("")
        checkBoxKeepLowest?.isChecked = false
        editTextKeepLowest?.setText("")
        spinnerSavedRules?.setSelection(0)
        textView?.setText("Roll Output") // Reset the output text

        // Remove all dynamic dice rows and dice icons
        val container = view?.findViewById<LinearLayout>(R.id.layoutDiceContainer)
        container?.removeAllViews()
        val tray = view?.findViewById<ViewGroup>(R.id.diceTrayContainer) ?: return
        tray?.removeAllViews()
    }

    /**
     * Shows the history bottom sheet
     */
    private fun showHistoryBottomSheet() {
        val bottomSheet = RollHistoryBottomSheet()
        bottomSheet.show(parentFragmentManager, "RollHistory")
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
     * Recursively applies colors to children of a ViewGroup
     */
    private fun applyRecursiveStyles(viewGroup: ViewGroup, txtCol: Int, btnCol: Int) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)

            when (child) {
                is Button -> {
                    child.setBackgroundColor(btnCol)
                    child.setTextColor(android.graphics.Color.WHITE) // High contrast for buttons
                }
                is TextView -> {
                    child.setTextColor(txtCol)
                }
                is com.google.android.material.textfield.TextInputLayout -> {
                    child.defaultHintTextColor = android.content.res.ColorStateList.valueOf(txtCol)
                    val editText = child.editText
                    editText?.setTextColor(txtCol)
                    editText?.setHintTextColor(txtCol)
                }
                is ViewGroup -> {
                    applyRecursiveStyles(child, txtCol, btnCol)
                }
            }
        }
    }
}
