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
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.example.dicerollerproject.R
import com.example.dicerollerproject.data.LocalStore
import com.example.dicerollerproject.data.model.CustomDie
import com.example.dicerollerproject.data.model.Rule
import com.example.dicerollerproject.data.model.Rule.RuleComponent
import com.example.dicerollerproject.domain.Dice
import java.util.Arrays
import java.util.UUID

/**
 * Fragment for creating custom dice and rules.
 */
class CreateFragment : Fragment() {
    private var store: LocalStore? = null
    private var editDieName: EditText? = null
    private var editDieFaces: EditText? = null
    private var editRuleName: EditText? = null
    private var spinnerRuleDice: Spinner? = null
    private var currentDice: MutableList<CustomDie?> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        store = LocalStore(requireContext())
        applyStoreStyles(requireView())

        // Help Guide
        val helpOverlay = view.findViewById<View>(R.id.helpOverlay)
        val btnHelp = view.findViewById<View>(R.id.btnHelp)
        val btnCloseHelp = view.findViewById<Button>(R.id.btnCloseHelp)
        btnHelp?.setOnClickListener { helpOverlay.visibility = View.VISIBLE }
        btnCloseHelp?.setOnClickListener { helpOverlay.visibility = View.GONE }

        // Dice Spinner elements
        editDieName = view.findViewById<EditText>(R.id.editDieName)
        editDieFaces = view.findViewById<EditText>(R.id.editDieFaces)
        editRuleName = view.findViewById<EditText>(R.id.editRuleName)
        view.findViewById<View?>(R.id.btnSaveDie)
            ?.setOnClickListener(View.OnClickListener { v: View? -> saveDie() })
        view.findViewById<View?>(R.id.btnAddDiceToRule)
            ?.setOnClickListener(View.OnClickListener { v: View? -> addDiceRowToRule() })
        view.findViewById<View?>(R.id.btnSaveRule)
            ?.setOnClickListener(View.OnClickListener { v: View? -> saveRule() })

        refreshDiceSpinner()
    }

    /**
     * Refreshes the dice spinner options.
     */
    private fun refreshDiceSpinner() {
        currentDice = store!!.listCustomDice()
        val options: MutableList<String?> = ArrayList<String?>()
        for (s in Dice.Standard.entries) options.add(s.name)
        for (d in currentDice!!) options.add("Custom: " + d?.name)

        val adapter =
            ArrayAdapter<String?>(requireContext(), android.R.layout.simple_spinner_item, options)
        spinnerRuleDice?.setAdapter(adapter)
    }

    /**
     * Saves a custom die to the store.
     */
    private fun saveDie() {
        val name = editDieName!!.getText().toString()
        val facesArr: Array<String?> =
            editDieFaces!!.getText().toString().split(",".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
        if (name.isEmpty() || facesArr.size == 0) return

        val dice = store!!.listCustomDice()
        dice?.add(CustomDie(UUID.randomUUID().toString(), name, Arrays.asList<String?>(*facesArr)))
        store!!.saveCustomDice(dice)
        editDieName?.setText("")
        editDieFaces?.setText("")
        Toast.makeText(requireContext(), "Die Added to Factory!", Toast.LENGTH_SHORT).show()
        refreshDiceSpinner()
    }

    /**
     * Adds a new row to the rule dice container.
     */
    private fun addDiceRowToRule() {
        val container = view?.findViewById<LinearLayout>(R.id.ruleDiceContainer) ?: return
        val row = layoutInflater.inflate(R.layout.item_dice_row, container, false)
        val spinner = row.findViewById<Spinner>(R.id.spinnerDiceRow)

        val options = mutableListOf<String>()
        Dice.Standard.entries.forEach { options.add(it.name) }
        store?.listCustomDice()?.filterNotNull()?.forEach { options.add("Custom: ${it.name}") }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        row.findViewById<ImageButton>(R.id.btnRemoveRow).setOnClickListener {
            container.removeView(row)
        }
        container.addView(row)
    }

    /**
     * Saves a rule to the store.
     */
    private fun saveRule() {
        val name = editRuleName?.text.toString()
        if (name.isEmpty()) return

        val components = mutableListOf<RuleComponent?>()
        val container = view?.findViewById<LinearLayout>(R.id.ruleDiceContainer)

        container?.children?.forEach { row ->
            val spinner = row.findViewById<Spinner>(R.id.spinnerDiceRow)
            val count = row.findViewById<EditText>(R.id.editDiceCountRow).text.toString().toIntOrNull() ?: 1
            val selected = spinner.selectedItem.toString()

            if (selected.startsWith("Custom: ")) {
                val dieName = selected.replace("Custom: ", "")
                val found = currentDice?.find { it?.name == dieName }
                components.add(RuleComponent(false, null, found?.id, count))
            } else {
                components.add(RuleComponent(true, selected, null, count))
            }
        }

        val flatMod = view?.findViewById<EditText>(R.id.editRuleFlatMod)?.text.toString().toIntOrNull() ?: 0
        val rerollStr = view?.findViewById<EditText>(R.id.editRuleReroll)?.text.toString()

        val keepHigh = if (view?.findViewById<CheckBox>(R.id.checkRuleKeepHigh)?.isChecked == true)
            view?.findViewById<EditText>(R.id.editRuleKeepHighQty)?.text.toString().toIntOrNull() ?: 1 else null

        val keepLow = if (view?.findViewById<CheckBox>(R.id.checkRuleKeepLow)?.isChecked == true)
            view?.findViewById<EditText>(R.id.editRuleKeepLowQty)?.text.toString().toIntOrNull() ?: 1 else null

        val modifier = Rule.RuleModifier(keepHigh, keepLow, rerollStr)

        val newRule = Rule(UUID.randomUUID().toString(), name, components, modifier, flatMod)
        val allRules = store?.listRules() ?: mutableListOf()
        allRules.add(newRule)
        store?.saveRules(allRules)

        Toast.makeText(getContext(), "Rule Saved!", Toast.LENGTH_SHORT).show()
    }

    /**
     * Changes the button, text and background colours of the fragment
     */
    private fun applyStoreStyles(rootView: View) {
        val store = LocalStore(requireContext())
        val bgColour = store.getBackgroundColour()
        val txtColour = store.getTextColour()
        val btnColour = store.getButtonColour()

        // Apply colors to the root view
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
                    // Handle the nested EditText inside TextInputLayout
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