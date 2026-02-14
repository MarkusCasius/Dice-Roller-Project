package com.example.dicerollerproject.domain;

import java.util.*;

/**
 * Data class holding complete result of a dice roll operation.
 * Immutable.
 */
public class RollResult {
  /** Final total after all modifiers */
  public final int total;
  /** Lists all face results as strings, before keep/drop logic. */
  public final java.util.List<String> facesRolled;
  /** Numeric value of each die roll. List corresponds to facesRolled and is used for calculation. */
  public final java.util.List<Integer> numericContributions; // each face mapped to numeric value used in sum

  /**
   * Constructs a new RollResult.
   * @param total The final sum.
   * @param facesRolled The list of string faces that were rolled.
   * @param numericContributions The numeric value of each roll.
   */
  public RollResult(int total, List<String> facesRolled, List<Integer> numericContributions) {
    this.total = total; this.facesRolled = facesRolled; this.numericContributions = numericContributions;
  }
}

