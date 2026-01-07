package com.example.dicerollerproject.domain;

import java.util.*;

/**
 * Logic engine for rolling dice, taking specifications and modifiers to produce a result
  */

public class DiceEngine {
  private final Random rng;

  /**
   * Constructs a new DiceEngine
   * @param seed A seed for random number generation for deterministic testing
   *             If null, a new random object is created without a fixed seed.
   */
  public DiceEngine(Long seed) { this.rng = (seed == null) ? new Random() : new Random(seed); }

  /**
   * Generates random zero-based index for die roll
   * @param sides Number of sides on the die
   * @return Random index between 0 and sides-1
   */
  private int rollIndex(int sides) { return rng.nextInt(sides); } // 0..sides-1

  /**
   * Parses a string face into a integer. Non-numeric faces are pased as 0.
   * @param face The string face to parse
   * @return The numeric value of the face, or 0 if parsing fails
   */
  private int parseNumeric(String face) {
    try { return Integer.parseInt(face.trim()); } catch (Exception e) { return 0; }
  }

  /**
   * Executes a dice roll based on the given specifications and modifier
   * @param specs A list of RollSpec objects, each defines a set of dice to roll
   * @param mod Modifier applied to the entire roll (e.g. flat bonuses, keep/drop).
   * @return A RollResult object containing the total and individual face results
   */
  public RollResult roll(java.util.List<RollSpec> specs, Modifier mod) {
    List<String> faces = new ArrayList<>();
    List<Integer> contribs = new ArrayList<>();

    // Roll each die specific in RollSpec list
    for (RollSpec spec : specs) {
      for (int i = 0; i < spec.count; i++) {
        String face = rollOneFace(spec.die, mod);
        faces.add(face);
        contribs.add(parseNumeric(face));
      }
    }

    // Apply keep/drop logic
    List<Integer> indices = new ArrayList<>();
    for (int i = 0; i < contribs.size(); i++) indices.add(i);

    if (mod.keepHighest != null) {
      indices.sort((a,b) -> Integer.compare(contribs.get(b), contribs.get(a))); // desc
      indices = indices.subList(0, Math.min(mod.keepHighest, indices.size()));
    } else if (mod.keepLowest != null) {
      indices.sort((a,b) -> Integer.compare(contribs.get(a), contribs.get(b))); // asc
      indices = indices.subList(0, Math.min(mod.keepLowest, indices.size()));
    }

    // Calculates sum based on dice kept.
    int sum = 0;
    if (mod.keepHighest != null || mod.keepLowest != null) {
      Set<Integer> keep = new HashSet<>(indices);
      for (int i = 0; i < contribs.size(); i++) if (keep.contains(i)) sum += contribs.get(i);
    } else {
      for (int v : contribs) sum += v;
    }
    sum += mod.flat; // Flat modifier applied

    return new RollResult(sum, faces, contribs);
  }

  /**
   * Rolls a single die, applying any relevant per-die modifiers like rerolling.
   * @param die The die to roll.
   * @param mod The modifier containing rules for the roll.
   * @return The final face string after the roll and any rerolls.
   */
  private String rollOneFace(Dice die, Modifier mod) {
    // Performs base roll
    int idx = rollIndex(die.sides());
    String face = die.faceAtIndex(idx);

    // Handle the "reroll 1s once" modifier
    if (mod.rerollOnesOnce) {
      int val = parseNumeric(face);
      if (val == 1) {
        int idx2 = rollIndex(die.sides());
        face = die.faceAtIndex(idx2);
      }
    }
    return face;
  }
}

