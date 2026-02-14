package com.example.dicerollerproject.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
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
    private var currentDice: MutableList<CustomDie>? = null

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
        editRuleDiceCount = view.findViewById<EditText>(R.id.editRuleDiceCount)
        spinnerRuleDice = view.findViewById<Spinner>(R.id.spinnerRuleDice)

        view.findViewById<View?>(R.id.btnSaveDie)
            .setOnClickListener(View.OnClickListener { v: View? -> saveDie() })
        view.findViewById<View?>(R.id.btnSaveRule)
            .setOnClickListener(View.OnClickListener { v: View? -> saveRule() })

        refreshDiceSpinner()
    }

    private fun refreshDiceSpinner() {
        currentDice = store!!.listCustomDice()
        val options: MutableList<String?> = ArrayList<String?>()
        for (s in Dice.Standard.entries) options.add(s.name)
        for (d in currentDice!!) options.add("Custom: " + d.name)

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
        dice.add(CustomDie(UUID.randomUUID().toString(), name, Arrays.asList<String?>(*facesArr)))
        store!!.saveCustomDice(dice)
        Toast.makeText(getContext(), "Die Saved", Toast.LENGTH_SHORT).show()
        refreshDiceSpinner()
    }

    private fun saveRule() {
        val name = editRuleName!!.getText().toString()
        val countStr = editRuleDiceCount!!.getText().toString()
        if (name.isEmpty() || countStr.isEmpty()) {
            Toast.makeText(getContext(), "Please enter name and count", Toast.LENGTH_SHORT).show()
            return
        }

        val count = countStr.toInt()
        val selected = spinnerRuleDice!!.getSelectedItem().toString()

        val comp: RuleComponent?
        if (selected.startsWith("Custom: ")) {
            val dieName = selected.replace("Custom: ", "")
            var found: CustomDie? = null
            for (d in currentDice!!) {
                if (d.name == dieName) {
                    found = d
                    break
                }
            }
            comp = RuleComponent(false, null, found!!.id, count)
        } else {
            comp = RuleComponent(true, selected, null, count)
        }

        val newRule = Rule(
            UUID.randomUUID().toString(),
            name,
            mutableListOf<RuleComponent?>(comp),
            RuleModifier(null, null, false),  // Default modifiers
            0 // Default flat bonus
        )

        val allRules = store!!.listRules()
        allRules.add(newRule)
        store!!.saveRules(allRules)

        Toast.makeText(getContext(), "Rule Saved!", Toast.LENGTH_SHORT).show()
    }
}