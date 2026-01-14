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
import com.example.dicerollerproject.data.model.CustomDie;
import com.example.dicerollerproject.data.model.Rule;
import com.example.dicerollerproject.domain.Dice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class CreateFragment extends Fragment {
  private LocalStore store;
  private EditText editDieName, editDieFaces, editRuleName, editRuleDiceCount;
  private Spinner spinnerRuleDice;
  private List<CustomDie> currentDice;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_create, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    store = new LocalStore(requireContext());
    editDieName = view.findViewById(R.id.editDieName);
    editDieFaces = view.findViewById(R.id.editDieFaces);
    editRuleName = view.findViewById(R.id.editRuleName);
    editRuleDiceCount = view.findViewById(R.id.editRuleDiceCount);
    spinnerRuleDice = view.findViewById(R.id.spinnerRuleDice);

    view.findViewById(R.id.btnSaveDie).setOnClickListener(v -> saveDie());
    view.findViewById(R.id.btnSaveRule).setOnClickListener(v -> saveRule());

    refreshDiceSpinner();
  }

  private void refreshDiceSpinner() {
    currentDice = store.listCustomDice();
    List<String> options = new ArrayList<>();
    for (Dice.Standard s : Dice.Standard.values()) options.add(s.name());
    for (CustomDie d : currentDice) options.add("Custom: " + d.name);

    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, options);
    spinnerRuleDice.setAdapter(adapter);
  }

  private void saveDie() {
    String name = editDieName.getText().toString();
    String[] facesArr = editDieFaces.getText().toString().split(",");
    if (name.isEmpty() || facesArr.length == 0) return;

    List<CustomDie> dice = store.listCustomDice();
    dice.add(new CustomDie(UUID.randomUUID().toString(), name, Arrays.asList(facesArr)));
    store.saveCustomDice(dice);
    Toast.makeText(getContext(), "Die Saved", Toast.LENGTH_SHORT).show();
    refreshDiceSpinner();
  }

  private void saveRule() {
    String name = editRuleName.getText().toString();
    String countStr = editRuleDiceCount.getText().toString();
    if (name.isEmpty() || countStr.isEmpty()) {
      Toast.makeText(getContext(), "Please enter name and count", Toast.LENGTH_SHORT).show();
      return;
    }

    int count = Integer.parseInt(countStr);
    String selected = spinnerRuleDice.getSelectedItem().toString();

    Rule.RuleComponent comp;
    if (selected.startsWith("Custom: ")) {
      String dieName = selected.replace("Custom: ", "");
      CustomDie found = null;
      for (CustomDie d : currentDice) {
        if (d.name.equals(dieName)) {
          found = d;
          break;
        }
      }
      comp = new Rule.RuleComponent(false, null, found.id, count);
    } else {
      comp = new Rule.RuleComponent(true, selected, null, count);
    }

    Rule newRule = new Rule(
        UUID.randomUUID().toString(),
        name,
        Collections.singletonList(comp),
        new Rule.RuleModifier(null, null, false), // Default modifiers
        0 // Default flat bonus
    );

    List<Rule> allRules = store.listRules();
    allRules.add(newRule);
    store.saveRules(allRules);

    Toast.makeText(getContext(), "Rule Saved!", Toast.LENGTH_SHORT).show();
  }

}