package com.example.dicerollerproject.domain;

import java.util.List;

/**
 * Represents a single die, which can either be a standard type (e.g. d4, d6, d10)
 * or a custom dice with a specific list of faces
 */
public class Dice {

/** Enumeration of Standard Dice types */
  public enum Standard {
    D3(3), D4(4), D6(6), D8(8), D10(10), D12(12), D20(20);
    public final int sides;
    Standard(int sides) {
      this.sides = sides;
    }
  }

  private final boolean isStandard;
  private final Standard standard;
  private final List<String> faces;

  /** Constructor for standard dice
   * @param standard The standard type of the dice (e.g. d4, d6, d10)
   * */
  private Dice(Standard standard) {
    this.isStandard = true; this.standard = standard; this.faces = null;
  }

  /** Constructor for custom dice
   * @param faces The list of faces for the custom dice (e.g. "a", "b", "c")
   */
  private Dice(List<String> faces) {
    this.isStandard = false; this.standard = null; this.faces = faces;
  }

  /** Factory method to create a new Dice instance.
   * @param s The standard type of the dice (e.g. d4, d6, d10)
   * @return A new Dice instance representing a standard dice of the specified type
   */
  public static Dice standard(Standard s){ return new Dice(s); }

  /**
   * Factory method to create a new Dice instance.
   * @param faces The list of faces for the custom dice (e.g. "a", "b", "c")
   * @return A new Dice instance representing a custom dice with the specified faces
   */
  public static Dice custom(List<String> faces){ return new Dice(faces); }

  // Accessors for standard dice

  /**
   * @return True if the die is a standard type, false otherwise.
   */
  public boolean isStandard(){ return isStandard; }

  /**
   * @return The total number of sides on the die.
   */
  public int sides(){
    if (isStandard) {
      assert standard != null;
      return standard.sides;
    } else {
      assert faces != null;
      return faces.size();
    }
  }

  /**
   * Retrieves the face value for a given roll index.
   * For standard dice, this is the index + 1 (e.g., index 0 is face "1").
   * For custom dice, this is the string at the specified index in the faces list.
   * @param idx The zero-based index of the roll.
   * @return The string representation of the face.
   */
  public String faceAtIndex(int idx){
    if (isStandard) {
      return String.valueOf(idx + 1);
    } else {
      assert faces != null;
      return faces.get(idx);
    }
  }
}

