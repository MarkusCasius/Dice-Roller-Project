package com.example.dicerollerproject.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import com.example.dicerollerproject.R
import com.example.dicerollerproject.data.LocalStore
import com.example.dicerollerproject.data.model.CustomDie
import com.example.dicerollerproject.data.model.Rule
import com.example.dicerollerproject.data.model.Rule.RuleComponent
import com.example.dicerollerproject.data.model.Rule.RuleModifier
import com.example.dicerollerproject.domain.Dice
import java.util.Arrays
import java.util.UUID

class CreateFragment : Fragment() {
    private var store: LocalStore? = null
    private var editDieName: EditText? = null
    private var editDieFaces: EditText? = null
    private var editRuleName: EditText? = null
    private var editRuleDiceCount: EditText? = null
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
        store = LocalStore(requireContext())
        editDieName = view.findViewById<EditText>(R.id.editDieName)
        editDieFaces = view.findViewById<EditText>(R.id.editDieFaces)
        editRuleName = view.findViewById<EditText>(R.id.editRuleName)

        view.findViewById<View?>(R.id.btnSaveDie)
            .setOnClickListener(View.OnClickListener { v: View? -> saveDie() })
        view.findViewById<View?>(R.id.btnAddDiceToRule)
            .setOnClickListener(View.OnClickListener { v: View? -> saveRule() })

        refreshDiceSpinner()
    }

    private fun refreshDiceSpinner() {
        currentDice = store!!.listCustomDice()
        val options: MutableList<String?> = ArrayList<String?>()
        for (s in Dice.Standard.entries) options.add(s.name)
        for (d in currentDice!!) options.add("Custom: " + d?.name)

        val adapter =
            ArrayAdapter<String?>(requireContext(), android.R.layout.simple_spinner_item, options)
        spinnerRuleDice!!.setAdapter(adapter)
    }

    private fun saveDie() {
        val name = editDieName!!.getText().toString()
        val facesArr: Array<String?> =
            editDieFaces!!.getText().toString().split(",".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
        if (name.isEmpty() || facesArr.size == 0) return

        val dice = store!!.listCustomDice()
        dice?.add(CustomDie(UUID.randomUUID().toString(), name, Arrays.asList<String?>(*facesArr)))
        store!!.saveCustomDice(dice)
        Toast.makeText(getContext(), "Die Saved", Toast.LENGTH_SHORT).show()
        refreshDiceSpinner()
    }

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

        val newRule = Rule(UUID.randomUUID().toString(), name, components, RuleModifier(null, null, false), 0)
        val allRules = store?.listRules() ?: mutableListOf()
        allRules.add(newRule)
        store?.saveRules(allRules)

        Toast.makeText(getContext(), "Rule Saved!", Toast.LENGTH_SHORT).show()
    }
}