package com.example.dicerollerproject.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.dicerollerproject.domain.Dice;
import com.example.dicerollerproject.domain.DiceEngine;

import com.example.dicerollerproject.R;
import com.example.dicerollerproject.domain.Modifier;
import com.example.dicerollerproject.domain.RollResult;
import com.example.dicerollerproject.domain.RollSpec;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Collections;

public class RollFragment extends Fragment {

  private DiceEngine diceEngine;
  private Spinner spinnerDice;
  private TextInputEditText textInputEditText;
  private CheckBox checkBoxRerollOne;
  private RadioButton radioKeepHighest;
  private RadioButton radioKeepLowest;
  private EditText editTextFlat;
  private Button buttonRoll;
  private Button buttonClear;
  private TextView textView;

  public RollFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    diceEngine = new DiceEngine(null);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_roll, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    // Find all the UI components by their ID
    spinnerDice = view.findViewById(R.id.spinnerDice);
    textInputEditText = view.findViewById(R.id.textInputEditText);
    checkBoxRerollOne = view.findViewById(R.id.checkBoxRerollOne);
    radioKeepHighest = view.findViewById(R.id.RadioKeepHighest);
    radioKeepLowest = view.findViewById(R.id.RadioKeepLowest);
    editTextFlat = view.findViewById(R.id.editTextFlat);
    buttonRoll = view.findViewById(R.id.buttonRoll);
    buttonClear = view.findViewById(R.id.buttonClear);
    textView = view.findViewById(R.id.textView);

    // Set up the Spinner with standard dice types
    // An ArrayAdapter is used to adapt the Dice.Standard enum values to be displayed in the Spinner
    ArrayAdapter<Dice.Standard> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, Dice.Standard.values());
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinnerDice.setAdapter(adapter);

    // Set an OnClickListener for the roll button
    buttonRoll.setOnClickListener(v -> rollDice());
    buttonClear.setOnClickListener(v -> clearInputs());
  }

  private void rollDice() {

    // Get the selected die type from the spinner
    Dice.Standard selectedDie = (Dice.Standard) spinnerDice.getSelectedItem();

    // Get the number of dice to roll, defaulting to 1 if empty
    String numDiceStr = textInputEditText.getText().toString();
    int numDice = numDiceStr.isEmpty() ? 1 : Integer.parseInt(numDiceStr);

    // Create the RollSpec based on the die type and count
    RollSpec spec = new RollSpec(Dice.standard(selectedDie), numDice);

    // Get the flat modifier, defaulting to 0 if empty
    String flatModStr = editTextFlat.getText().toString();
    int flatMod = flatModStr.isEmpty() ? 0 : Integer.parseInt(flatModStr);

    // Check the reroll and keep/drop options
    boolean rerollOnes = checkBoxRerollOne.isChecked();
    Integer keepHigh = null;
    Integer keepLow = null;
    if (radioKeepHighest.isChecked()) {
      keepHigh = numDice > 1 ? numDice -1 : 1; // Example: Keep all but one
    } else if (radioKeepLowest.isChecked()) {
      keepLow = numDice > 1 ? numDice -1 : 1;
    }


    // Create the Modifier object
    Modifier modifier = Modifier.none(); modifier.flat = flatMod ; modifier.rerollOnesOnce = rerollOnes; modifier.keepHighest = keepHigh; modifier.keepLowest = keepLow;

    // Call the DiceEngine to get the result
    RollResult result = diceEngine.roll(Collections.singletonList(spec), modifier);

    // Display result in the TextView
    textView.setText(String.format("Result: %d\nRolls: %s", result.total, result.facesRolled.toString()));
  }

  private void clearInputs() {
    textInputEditText.setText("");
    editTextFlat.setText("");
    checkBoxRerollOne.setChecked(false);
    radioKeepHighest.setChecked(false);
    radioKeepLowest.setChecked(false);
    spinnerDice.setSelection(0); // Reset spinner to the first item
    textView.setText("Roll Output"); // Reset the output text
  }
}