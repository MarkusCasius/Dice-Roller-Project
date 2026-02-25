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
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.TextView
import androidx.core.view.children
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

class RollFragment : Fragment() {
    private lateinit var diceEngine: DiceEngine
    private lateinit var store: LocalStore

    // UI Components
    private var spinnerDice: Spinner? = null
    private var checkBoxRerollOne: CheckBox? = null
    private var radioKeepHighest: RadioButton? = null
    private var radioKeepLowest: RadioButton? = null
    private var editTextFlat: EditText? = null
    private var buttonRoll: Button? = null
    private var buttonClear: Button? = null
    private var textView: TextView? = null
    private var spinnerSavedRules: Spinner? = null
    private var tray: ViewGroup? = null
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
        store = LocalStore(requireContext())
        val textColor = store.getTextColor()
        val elementColor = store.getElementColor()

        // Apply to Text
        view.findViewById<TextView>(R.id.textView).setTextColor(textColor)

        // Apply to Buttons
        val rollBtn = view.findViewById<Button>(R.id.buttonRoll)
        rollBtn.setBackgroundColor(elementColor)

        val clearBtn = view.findViewById<Button>(R.id.buttonClear)
        clearBtn.setBackgroundColor(elementColor)

        applyGlobalColors(view as ViewGroup, textColor, elementColor)


        // Find all the UI components by their ID
        checkBoxRerollOne = view.findViewById<CheckBox>(R.id.checkBoxRerollOne)
        radioKeepHighest = view.findViewById<RadioButton>(R.id.RadioKeepHighest)
        radioKeepLowest = view.findViewById<RadioButton>(R.id.RadioKeepLowest)
        editTextFlat = view.findViewById<EditText>(R.id.editTextFlat)
        buttonRoll = view.findViewById<Button>(R.id.buttonRoll)
        buttonClear = view.findViewById<Button>(R.id.buttonClear)
        textView = view.findViewById<TextView>(R.id.textView)
        spinnerSavedRules = view.findViewById<Spinner>(R.id.spinnerSavedRules)
        tray = view.findViewById<ViewGroup>(R.id.diceTrayContainer)

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
            // val spec = RollSpec(Dice.Companion.standard(selectedDie), numDice)

            // Get the flat modifier, defaulting to 0 if empty
            val flatModStr = editTextFlat!!.getText().toString()
            val flatMod = if (flatModStr.isEmpty()) 0 else flatModStr.toInt()

            // Check the reroll and keep/drop options
            val rerollOnes = checkBoxRerollOne!!.isChecked()
            var keepHigh: Int? = null
            var keepLow: Int? = null
            if (radioKeepHighest!!.isChecked()) {
                keepHigh = if (totalNumDice > 1) totalNumDice - 1 else 1 // Example: Keep all but one
            } else if (radioKeepLowest!!.isChecked()) {
                keepLow = if (totalNumDice > 1) totalNumDice - 1 else 1
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

        triggerVisualRoll(result)

        // Display result in the TextView
        textView?.postDelayed({
            textView?.text = String.format("Result: %d\nRolls: %s", result.total, result.facesRolled.toString())
        }, 1000)
    }

    private fun applyGlobalColors(viewGroup: ViewGroup, txtCol: Int, elemCol: Int) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is TextView && child !is Button) {
                child.setTextColor(txtCol)
            } else if (child is Button) {
                child.setBackgroundColor(elemCol)
            } else if (child is ViewGroup) {
                applyGlobalColors(child, txtCol, elemCol)
            }
        }
    }

    private fun triggerVisualRoll(result: RollResult) {
        val tray = view?.findViewById<ViewGroup>(R.id.diceTrayContainer) ?: return
        val rollId = System.currentTimeMillis()
        val viewsFromThisRoll = mutableListOf<View>()

        // Filter for standard dice only and limit to 10
        val totalRolls = result.numericContributions.size
        val displayLimit = 10

        for (i in 0 until minOf(totalRolls, displayLimit)) {
            val value = result.numericContributions[i]
            val faceText = result.facesRolled[i] ?: ""
            val diceContainer = FrameLayout(requireContext())
            diceContainer.tag = rollId

            val diceView = ImageView(requireContext())
            diceView.setImageResource(R.drawable.ic_dice_rolling_placeholder)
            val imgParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            diceContainer.addView(diceView, imgParams)

            val resultLabel = TextView(requireContext()).apply {
                text = faceText
                setTextColor(android.graphics.Color.BLACK) // Or use your store color
                textSize = 14f
                gravity = android.view.Gravity.CENTER
                visibility = View.INVISIBLE // Hide during animation
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            diceContainer.addView(resultLabel)

            // Randomize position
            val containerParams = FrameLayout.LayoutParams(120, 120)
            containerParams.leftMargin = (50..maxOf(50, tray.width - 150)).random()
            containerParams.topMargin = (50..maxOf(50, tray.height - 150)).random()
            diceContainer.layoutParams = containerParams
            tray.addView(diceContainer)
            viewsFromThisRoll.add(diceContainer)


            // Animate
            diceContainer.animate()
                .rotation(1080f) // 3 full spins
                .scaleX(1.2f).scaleY(1.2f)
                .setDuration(600 + (i * 150).toLong())
                .withEndAction {
                    // Change the image to the final result
                    diceView.setImageResource(getDiceDrawable(value))

                    // Show text ONLY if it's a generic die or a custom string
                    if (value > 6 || value == 0) {
                        resultLabel.visibility = View.VISIBLE
                    }

                    diceContainer.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start()
                }
                .start()

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

    // Helper to map numeric result to a drawable resource
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

    private fun clearInputs() {
        editTextFlat?.setText("")
        checkBoxRerollOne?.isChecked = false
        radioKeepHighest?.isChecked = false
        radioKeepLowest?.isChecked = false
        spinnerSavedRules?.setSelection(0)
        textView?.setText("Roll Output") // Reset the output text

        // Remove all dynamic dice rows and dice icons
        val container = view?.findViewById<LinearLayout>(R.id.layoutDiceContainer)
        container?.removeAllViews()
        val tray = view?.findViewById<ViewGroup>(R.id.diceTrayContainer) ?: return
        tray?.removeAllViews()
    }
}

private fun MutableList<String>.add(element: String?) {}
