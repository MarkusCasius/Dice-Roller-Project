package com.example.dicerollerproject.data.model;

import java.util.List;

public class Rule {
  public String id;
  public String name;
  public List<RuleComponent> components; // die ref + count
  public RuleModifier modifier;
  public int flat;

  public Rule(String id, String name, List<RuleComponent> components, RuleModifier modifier, int flat){
    this.id = id; this.name = name; this.components = components; this.modifier = modifier; this.flat = flat;
  }

  public static class RuleComponent {
    public boolean isStandard;
    public String standard; // "D4"..."D20" if standard
    public String customDieId; // if custom
    public int count;

    public RuleComponent(boolean isStandard, String standard, String customDieId, int count){
      this.isStandard = isStandard; this.standard = standard; this.customDieId = customDieId; this.count = count;
    }
  }

  public static class RuleModifier {
    public Integer keepHighest;
    public Integer keepLowest;
    public boolean rerollOnesOnce;

    public RuleModifier(Integer keepHighest, Integer keepLowest, boolean rerollOnesOnce){
      this.keepHighest = keepHighest; this.keepLowest = keepLowest; this.rerollOnesOnce = rerollOnesOnce;
    }
  }
}
