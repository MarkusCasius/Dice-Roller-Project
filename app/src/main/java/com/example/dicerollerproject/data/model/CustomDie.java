package com.example.dicerollerproject.data.model;

import java.util.List;

public class CustomDie {
  public String id;    // UUID string
  public String name;
  public List<String> faces;

  public CustomDie(String id, String name, List<String> faces){
    this.id = id; this.name = name; this.faces = faces;
  }
}
