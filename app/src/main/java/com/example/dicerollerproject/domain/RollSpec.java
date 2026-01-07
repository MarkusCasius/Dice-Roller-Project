package com.example.dicerollerproject.domain;

/**
 * Data class representing a specification for a roll, e.g. "roll 3 D6".
 * Immutable
 */
public class RollSpec {
  /** Type of die roll to roll */
  public final Dice die;
  /** Number of times to roll the dice */
  public final int count;

  /**
   * Constructs a new RollSpec.
   * @param die Type of die to roll
   * @param count Number of times to roll the dice
   */
  public RollSpec(Dice die, int count){ this.die = die; this.count = count; }
}

