package com.example.dicerollerproject.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import com.example.dicerollerproject.R
import com.example.dicerollerproject.data.LocalStore
import com.example.dicerollerproject.data.RuleMapper
import com.example.dicerollerproject.data.model.Rule
import com.example.dicerollerproject.domain.Dice
import com.example.dicerollerproject.domain.DiceEngine
import com.example.dicerollerproject.domain.Modifier
import com.example.dicerollerproject.domain.RollResult
import com.example.dicerollerproject.domain.RollSpec
import com.google.android.material.textfield.TextInputEditText

class RollFragment : Fragment() {
    private lateinit var diceEngine: DiceEngine
    private lateinit var store: LocalStore

    // UI Components
    private var spinnerDice: Spinner? = null
    private var textInputEditText: TextInputEditText? = null
    private var checkBoxRerollOne: CheckBox? = null
    private var radioKeepHighest: RadioButton? = null
    private var radioKeepLowest: RadioButton? = null
    private var editTextFlat: EditText? = null
    private var buttonRoll: Button? = null
    private var buttonClear: Button? = null
    private var textView: TextView? = null
    private var spinnerSavedRules: Spinner? = null
    private var savedRules: MutableList<Rule?> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        diceEngine = DiceEngine(null)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_roll, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find all the UI components by their ID
        checkBoxRerollOne = view.findViewById<CheckBox>(R.id.checkBoxRerollOne)
        radioKeepHighest = view.findViewById<RadioButton>(R.id.RadioKeepHighest)
        radioKeepLowest = view.findViewById<RadioButton>(R.id.RadioKeepLowest)
        editTextFlat = view.findViewById<EditText>(R.id.editTextFlat)
        buttonRoll = view.findViewById<Button>(R.id.buttonRoll)
        buttonClear = view.findViewById<Button>(R.id.buttonClear)
        textView = view.findViewById<TextView>(R.id.textView)
        spinnerSavedRules = view.findViewById<Spinner>(R.id.spinnerSavedRules)
        store = LocalStore(requireContext())

        val btnAdd = view.findViewById<Button>(R.id.btnAddDiceRow)
        btnAdd.setOnClickListener {
            addDiceRow()
        }

        // Set up the Spinners with standard dice types
        // An ArrayAdapter is used to adapt the Dice.Standard enum values to be displayed in the Spinner
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            Dice.Standard.entries.toTypedArray()
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDice?.setAdapter(adapter)

        // Populate the spinner with saved rules
        refreshRules()

        // Set an OnClickListener for the roll button
        buttonRoll?.setOnClickListener { rollDice() }
        buttonClear?.setOnClickListener { clearInputs() }
    }

    private fun refreshRules() {
        savedRules = store.listRules() ?: mutableListOf()

        val ruleNames = mutableListOf("Manual Roll (No Rule)")
        ruleNames.addAll(savedRules.map { it?.name ?: "Unnamed Rule" })

//        for (r in savedRules!!) {
//            ruleNames.add(r.name)
//        }

        val adapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item, ruleNames
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSavedRules?.adapter = adapter
    }

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

    private fun rollDice() {
        val selectedRuleIndex = spinnerSavedRules?.selectedItemPosition ?: 0

        val result: RollResult = if (selectedRuleIndex > 0) {
            // --- SAVED RULE SELECTED ---
            val selectedRule = savedRules.get(selectedRuleIndex - 1)
            val allCustomDice = store.listCustomDice()

            // Use the Mapper to convert the Persistence Rule into Domain Specs/Modifiers
            val prepared = RuleMapper.prepare(selectedRule, allCustomDice)
            diceEngine!!.roll(prepared.specs, prepared.mod)
        } else {
            // --- MANUAL ROLL SELECTED ---
            // Get the selected die type from the spinner
            val specs = mutableListOf<RollSpec>()
            val container = view?.findViewById<LinearLayout>(R.id.layoutDiceContainer)

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
            }


            // Get the number of dice to roll, defaulting to 1 if empty
            val numDiceStr = textInputEditText!!.getText().toString()
            val numDice = if (numDiceStr.isEmpty()) 1 else numDiceStr.toInt()

            // Create the RollSpec based on the die type and count
            // val spec = RollSpec(Dice.Companion.standard(selectedDie), numDice)

            // Get the flat modifier, defaulting to 0 if empty
            val flatModStr = editTextFlat!!.getText().toString()
            val flatMod = if (flatModStr.isEmpty()) 0 else flatModStr.toInt()

            // Check the reroll and keep/drop options
            val rerollOnes = checkBoxRerollOne!!.isChecked()
            var keepHigh: Int? = null
            var keepLow: Int? = null
            if (radioKeepHighest!!.isChecked()) {
                keepHigh = if (numDice > 1) numDice - 1 else 1 // Example: Keep all but one
            } else if (radioKeepLowest!!.isChecked()) {
                keepLow = if (numDice > 1) numDice - 1 else 1
            }


            // Create the Modifier object
            val modifier: Modifier = Modifier.Companion.none()
            modifier.flat = flatMod
            modifier.rerollOnesOnce = rerollOnes
            modifier.keepHighest = keepHigh
            modifier.keepLowest = keepLow

            // Call the DiceEngine to get the result
            diceEngine.roll(specs, modifier)
        }

        // Display result in the TextView
        textView!!.setText(
            String.format(
                "Result: %d\nRolls: %s",
                result.total,
                result.facesRolled.toString()
            )
        )
    }

    private fun clearInputs() {
        textInputEditText!!.setText("")
        editTextFlat!!.setText("")
        checkBoxRerollOne!!.setChecked(false)
        radioKeepHighest!!.setChecked(false)
        radioKeepLowest!!.setChecked(false)
        spinnerDice!!.setSelection(0) // Reset spinner to the first item
        spinnerSavedRules!!.setSelection(0)
        textView!!.setText("Roll Output") // Reset the output text
    }
}

private fun MutableList<String>.add(element: String?) {}
