package com.example.dicerollerproject.data.model

/**
 * Represents a custom die with an optional name and a list of faces.
 * @param id Unique identifier for the die.
 * @param name Optional name for the die.
 * @param faces List of faces for the die.
 */
class CustomDie(
    var id: String?, var name: String?, var faces: MutableList<String?>?
)
