package com.example.dicerollerproject.ui

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
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
import com.example.dicerollerproject.domain.DiceEngine
import java.util.Locale
import java.util.TreeMap
import kotlin.math.max
import kotlin.math.min

/**
 * A simple [Fragment] subclass.
 * create an instance of this fragment.
 */
class SimulateFragment : Fragment() {
    private var store: LocalStore? = null
    private var engine: DiceEngine? = null
    private var spinnerRules: Spinner? = null
    private var editTrials: EditText? = null
    private var progressBar: ProgressBar? = null
    private var textResults: TextView? = null
    private var layoutHistogram: LinearLayout? = null
    private var savedRules: MutableList<Rule>? = null

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

        spinnerRules = view.findViewById<Spinner>(R.id.spinnerSimulateRule)
        editTrials = view.findViewById<EditText>(R.id.editTrials)
        progressBar = view.findViewById<ProgressBar>(R.id.progressSimulation)
        textResults = view.findViewById<TextView>(R.id.textSimResults)
        layoutHistogram = view.findViewById<LinearLayout>(R.id.layoutHistogram)

        refreshRuleSpinner()

        view.findViewById<View?>(R.id.btnRunSimulation)
            .setOnClickListener(View.OnClickListener { v: View? -> runSimulation() })
    }

    private fun refreshRuleSpinner() {
        savedRules = store!!.listRules()
        val names: MutableList<String?> = ArrayList<String?>()
        for (r in savedRules!!) names.add(r.name)

        val adapter = ArrayAdapter<String?>(
            requireContext(),
            android.R.layout.simple_spinner_item, names
        )
        spinnerRules!!.setAdapter(adapter)
    }

    private fun runSimulation() {
        if (savedRules!!.isEmpty()) return

        val selectedRule = savedRules!!.get(spinnerRules!!.getSelectedItemPosition())
        val allDice = store!!.listCustomDice()
        val prepared = RuleMapper.prepare(selectedRule, allDice)

        var trials: Int
        try {
            trials = editTrials!!.getText().toString().toInt()
        } catch (e: NumberFormatException) {
            trials = 1000
        }

        progressBar!!.setVisibility(View.VISIBLE)
        progressBar!!.setMax(trials)
        progressBar!!.setProgress(0)
        layoutHistogram!!.removeAllViews()

        val finalTrials = trials

        // Run on a background thread to keep UI smooth
        Thread(Runnable {
            val frequencyMap: MutableMap<Int?, Int?> = TreeMap<Int?, Int?>()
            var totalSum: Long = 0
            var min = Int.Companion.MAX_VALUE
            var max = Int.Companion.MIN_VALUE

            for (i in 0..<finalTrials) {
                val res = engine!!.roll(prepared.specs, prepared.mod)
                val `val` = res.total

                totalSum += `val`.toLong()
                min = min(min, `val`)
                max = max(max, `val`)
                frequencyMap.put(`val`, frequencyMap.getOrDefault(`val`, 0)!! + 1)

                if (i % 100 == 0) { // Update progress periodically
                    val currentI = i
                    if (getActivity() != null) getActivity()!!.runOnUiThread(Runnable {
                        progressBar!!.setProgress(
                            currentI
                        )
                    })
                }
            }

            // Calculations
            val mean = totalSum.toDouble() / finalTrials
            var mode = -1
            var maxFreq = -1
            for (entry in frequencyMap.entries) {
                if (entry.value!! > maxFreq) {
                    maxFreq = entry.value!!
                    mode = entry.key!!
                }
            }

            // Display Results
            val stats = String.format(
                Locale.getDefault(),
                "Mean: %.2f\nMode: %d\nMin: %d\nMax: %d\nTrials: %d",
                mean, mode, min, max, finalTrials
            )

            val finalMin = min
            val finalMax = max
            val finalMaxFreq = maxFreq
            if (getActivity() != null) {
                getActivity()!!.runOnUiThread(Runnable {
                    progressBar!!.setVisibility(View.GONE)
                    textResults!!.setText(stats)
                    drawHistogram(frequencyMap, finalMaxFreq)
                })
            }
        }).start()
    }

    private fun drawHistogram(data: MutableMap<Int?, Int?>, maxFreq: Int) {
        for (entry in data.entries) {
            // Create a bar (View)
            val barContainer = LinearLayout(requireContext())
            barContainer.setOrientation(LinearLayout.VERTICAL)
            barContainer.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL)

            val bar = View(requireContext())
            val heightPx = ((entry.value!!.toDouble() / maxFreq) * 400).toInt() // 400px max height
            // Calculate height as percentage of max frequency
            val barParams = LinearLayout.LayoutParams(50, heightPx)
            barParams.setMargins(6, 0, 6, 0)
            bar.setLayoutParams(barParams)
            bar.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark))

            val valLabel = TextView(requireContext())
            valLabel.setText(entry.key.toString())
            valLabel.setTextSize(10f)
            valLabel.setGravity(Gravity.CENTER)

            barContainer.addView(bar)
            barContainer.addView(valLabel)

            bar.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark))

            // Tooltip or label could be added here
            layoutHistogram!!.addView(barContainer)
        }
    }
}