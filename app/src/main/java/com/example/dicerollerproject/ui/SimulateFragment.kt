package com.example.dicerollerproject.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.dicerollerproject.R;
import com.example.dicerollerproject.data.LocalStore;
import com.example.dicerollerproject.data.RuleMapper;
import com.example.dicerollerproject.data.model.CustomDie;
import com.example.dicerollerproject.data.model.Rule;
import com.example.dicerollerproject.domain.DiceEngine;
import com.example.dicerollerproject.domain.RollResult;
import java.util.*;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class SimulateFragment extends Fragment {

  private LocalStore store;
  private DiceEngine engine;
  private Spinner spinnerRules;
  private EditText editTrials;
  private ProgressBar progressBar;
  private TextView textResults;
  private LinearLayout layoutHistogram;
  private List<Rule> savedRules;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_simulate, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    store = new LocalStore(requireContext());
    engine = new DiceEngine(null);

    spinnerRules = view.findViewById(R.id.spinnerSimulateRule);
    editTrials = view.findViewById(R.id.editTrials);
    progressBar = view.findViewById(R.id.progressSimulation);
    textResults = view.findViewById(R.id.textSimResults);
    layoutHistogram = view.findViewById(R.id.layoutHistogram);

    refreshRuleSpinner();

    view.findViewById(R.id.btnRunSimulation).setOnClickListener(v -> runSimulation());
  }

  private void refreshRuleSpinner() {
    savedRules = store.listRules();
    List<String> names = new ArrayList<>();
    for (Rule r : savedRules) names.add(r.name);

    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
        android.R.layout.simple_spinner_item, names);
    spinnerRules.setAdapter(adapter);
  }

  private void runSimulation() {
    if (savedRules.isEmpty()) return;

    Rule selectedRule = savedRules.get(spinnerRules.getSelectedItemPosition());
    List<CustomDie> allDice = store.listCustomDice();
    RuleMapper.Prepared prepared = RuleMapper.prepare(selectedRule, allDice);

    int trials;
    try {
      trials = Integer.parseInt(editTrials.getText().toString());
    } catch (NumberFormatException e) {
      trials = 1000;
    }

    progressBar.setVisibility(View.VISIBLE);
    progressBar.setMax(trials);
    progressBar.setProgress(0);
    layoutHistogram.removeAllViews();

    final int finalTrials = trials;

    // Run on a background thread to keep UI smooth
    new Thread(() -> {
      Map<Integer, Integer> frequencyMap = new TreeMap<>();
      long totalSum = 0;
      int min = Integer.MAX_VALUE;
      int max = Integer.MIN_VALUE;

      for (int i = 0; i < finalTrials; i++) {
        RollResult res = engine.roll(prepared.specs, prepared.mod);
        int val = res.total;

        totalSum += val;
        min = Math.min(min, val);
        max = Math.max(max, val);
        frequencyMap.put(val, frequencyMap.getOrDefault(val, 0) + 1);

        if (i % 100 == 0) { // Update progress periodically
          int currentI = i;
          if (getActivity() != null)
            getActivity().runOnUiThread(() -> progressBar.setProgress(currentI));
        }
      }

      // Calculations
      double mean = (double) totalSum / finalTrials;
      int mode = -1;
      int maxFreq = -1;
      for (Map.Entry<Integer, Integer> entry : frequencyMap.entrySet()) {
        if (entry.getValue() > maxFreq) {
          maxFreq = entry.getValue();
          mode = entry.getKey();
        }
      }

      // Display Results
      String stats = String.format(Locale.getDefault(),
          "Mean: %.2f\nMode: %d\nMin: %d\nMax: %d\nTrials: %d",
          mean, mode, min, max, finalTrials);

      int finalMin = min;
      int finalMax = max;
      int finalMaxFreq = maxFreq;

      if (getActivity() != null) {
        getActivity().runOnUiThread(() -> {
          progressBar.setVisibility(View.GONE);
          textResults.setText(stats);
          drawHistogram(frequencyMap, finalMaxFreq);
        });
      }
    }).start();
  }

  private void drawHistogram(Map<Integer, Integer> data, int maxFreq) {
    for (Map.Entry<Integer, Integer> entry : data.entrySet()) {
      // Create a bar (View)
      LinearLayout barContainer = new LinearLayout(requireContext());
      barContainer.setOrientation(LinearLayout.VERTICAL);
      barContainer.setGravity(android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL);

      View bar = new View(requireContext());
      int heightPx = (int) (((double) entry.getValue() / maxFreq) * 400); // 400px max height
      // Calculate height as percentage of max frequency
      LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(50, heightPx);
      barParams.setMargins(6, 0, 6, 0);
      bar.setLayoutParams(barParams);
      bar.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));

      TextView valLabel = new TextView(requireContext());
      valLabel.setText(String.valueOf(entry.getKey()));
      valLabel.setTextSize(10f);
      valLabel.setGravity(android.view.Gravity.CENTER);

      barContainer.addView(bar);
      barContainer.addView(valLabel);

      bar.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));

      // Tooltip or label could be added here
      layoutHistogram.addView(barContainer);
    }
  }
}