package com.example.dicerollerproject.data;

import com.example.dicerollerproject.data.*;
import com.example.dicerollerproject.data.model.*;
import com.example.dicerollerproject.domain.Dice;
import com.example.dicerollerproject.domain.Modifier;
import com.example.dicerollerproject.domain.RollSpec;

import java.util.*;

public class RuleMapper {

  public static class Prepared {
    public final List<RollSpec> specs;
    public final Modifier mod;
    public Prepared(List<RollSpec> specs, Modifier mod){ this.specs = specs; this.mod = mod; }
  }

  public static Prepared prepare(Rule rule, List<CustomDie> allDice) {
    List<RollSpec> specs = new ArrayList<>();
    for (Rule.RuleComponent c : rule.components) {
      Dice die;
      if (c.isStandard) {
        die = Dice.standard(Dice.Standard.valueOf(c.standard)); // "D6" etc.
      } else {
        CustomDie cd = findById(allDice, c.customDieId);
        die = Dice.custom(cd.faces);
      }
      specs.add(new RollSpec(die, c.count));
    }
    Modifier m = new Modifier();
    m.flat = rule.flat;
    if (rule.modifier != null) {
      m.keepHighest = rule.modifier.keepHighest;
      m.keepLowest  = rule.modifier.keepLowest;
      m.rerollOnesOnce = rule.modifier.rerollOnesOnce;
    }
    return new Prepared(specs, m);
  }

  private static CustomDie findById(List<CustomDie> dice, String id) {
    for (CustomDie d : dice) if (d.id.equals(id)) return d;
    throw new IllegalArgumentException("Custom die not found: " + id);
  }
}

