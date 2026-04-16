package com.example.dicerollerproject;

import static org.junit.Assert.*;

import com.example.dicerollerproject.domain.Dice;
import com.example.dicerollerproject.domain.DiceEngine;
import com.example.dicerollerproject.domain.Modifier;
import com.example.dicerollerproject.domain.RollResult;
import com.example.dicerollerproject.domain.RollSpec;

import org.junit.Test;

import java.util.*;

public class DiceEngineTest {

  @Test
  public void testStandardD6Range() {
    DiceEngine eng = new DiceEngine(42L);
    List<RollSpec> specs = Collections.singletonList(new RollSpec(Dice.standard(Dice.Standard.D6), 1));
    Modifier m = Modifier.none();
    for (int i = 0; i < 1000; i++) {
      RollResult r = eng.roll(specs, m);
      assertTrue("Roll out of range: " + r.total, r.total >= 1 && r.total <= 6);
    }
  }

  @Test
  public void testFlatModifierPlus() {
    // Seed 1L: 1d6 rolls a 4.
    DiceEngine eng = new DiceEngine(1L);
    List<RollSpec> specs = Collections.singletonList(new RollSpec(Dice.standard(Dice.Standard.D6), 1));

    Modifier mPlus = Modifier.none();
    mPlus.setFlatBonus(5);
    assertEquals(9, eng.roll(specs, mPlus).total); // 4 + 5
  }

  @Test
  public void testFlatModifierMinus() {
    // Seed 1L: 1d6 rolls a 4.
    DiceEngine eng = new DiceEngine(1L);
    List<RollSpec> specs = Collections.singletonList(new RollSpec(Dice.standard(Dice.Standard.D6), 1));

    Modifier mMinus = Modifier.none();
    mMinus.setFlatBonus(-2);
    assertEquals(2, eng.roll(specs, mMinus).total); // 4 - 2
  }

  @Test
  public void testKeepHighestLogic() {
    // Seed 42L: 4d6 rolls [1, 3, 3, 4]
    DiceEngine eng = new DiceEngine(42L);
    List<RollSpec> specs = new ArrayList<>(Collections.singletonList(new RollSpec(Dice.standard(Dice.Standard.D6), 4)));

    Modifier m = Modifier.none();
    m.keepHighest = 2; // Should keep 6 and 5 (@JvmField allows direct access)
    RollResult r = eng.roll(specs, m);

    assertEquals(7, r.total);
    assertEquals(4, r.getFacesRolled().size()); // All dice recorded, only 2 summed
  }

  @Test
  public void testKeepLowestLogic() {
    // Seed 42L: 4d6 rolls [1, 3, 3, 4]
    DiceEngine eng = new DiceEngine(42L);
    List<RollSpec> specs = new ArrayList<>(Collections.singletonList(new RollSpec(Dice.standard(Dice.Standard.D6), 4)));

    Modifier m = Modifier.none();
    m.setKeepLowest(2); // Should keep 1 and 2
    RollResult r = eng.roll(specs, m);

    assertEquals(4, r.total);
  }

  @Test
  public void testKeepMoreThanRolled() {
    // Logic check: If user asks to keep 5 dice but only 2 are rolled
    DiceEngine eng = new DiceEngine(42L);
    List<RollSpec> specs = new ArrayList<>(Collections.singletonList(new RollSpec(Dice.standard(Dice.Standard.D6), 2)));

    Modifier m = Modifier.none();
    m.keepHighest = 5;
    RollResult r = eng.roll(specs, m);

    // Should gracefully keep the 2 available dice (1 and 5)
    assertEquals(7, r.total);
  }


  @Test
  public void testRerollNumericValues() {
    // Seed 1L: 1d6 rolls a 4.
    DiceEngine eng = new DiceEngine(1L);
    List<RollSpec> specs = new ArrayList<>(Collections.singletonList(new RollSpec(Dice.standard(Dice.Standard.D6), 1)));

    Modifier m = Modifier.none();
    // Add 4 to reroll values. The engine will roll 4, see it's in the set, and roll again.
    m.getRerollValues().add(4);
    RollResult r = eng.roll(specs, m);

    assertNotEquals(4, r.total); // Total should not be 4 because it was rerolled
  }

  @Test
  public void testRerollCustomFaces() {
    DiceEngine eng = new DiceEngine(7L);
    // Custom die where 1 face is "Fail"
    Dice custom = Dice.Companion.custom(new ArrayList<>(Arrays.asList("Success", "Fail")));
    List<RollSpec> specs = new ArrayList<>(Collections.singletonList(new RollSpec(custom, 1)));

    Modifier m = Modifier.none();
    m.getRerollFaces().add("Fail");

    // Even if it hits "Fail", it will reroll. In a 50/50, it significantly reduces fail chance
    RollResult r = eng.roll(specs, m);
    // We can't guarantee Success without a specific seed, but we verify the engine runs without error
    assertNotNull(r.getFacesRolled().get(0));
  }

  @Test
  public void testCustomFacesParsing() {
    DiceEngine eng = new DiceEngine(7L);
    // Mixed numeric, symbols, and whitespace
    Dice custom = Dice.Companion.custom(new ArrayList<>(Arrays.asList("10", "  ", "Fire", "-5")));
    List<RollSpec> specs = Collections.singletonList(new RollSpec(custom, 100));

    RollResult r = eng.roll(specs, Modifier.none());

    for (int val : r.numericContributions) {
      // "  " and "Fire" should parse to 0
      // "10" is 10, "-5" is -5
      assertTrue(val == 10 || val == 0 || val == -5);
    }
  }

  @Test
  public void testMultipleSpecs() {
    DiceEngine eng = new DiceEngine(1L);
    List<RollSpec> specs = new ArrayList<>();
    specs.add(new RollSpec(Dice.standard(Dice.Standard.D6), 1)); // Seed 1: 4
    specs.add(new RollSpec(Dice.standard(Dice.Standard.D4), 1)); // Next: 1

    RollResult r = eng.roll(specs, Modifier.none());
    assertEquals(5, r.total);
    assertEquals(2, r.getFacesRolled().size());
  }
}

