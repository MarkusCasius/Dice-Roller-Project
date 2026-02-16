package com.example.dicerollerproject.domain

/**
 * Represents a single die, which can either be a standard type (e.g. d4, d6, d10)
 * or a custom dice with a specific list of faces
 */
class Dice {
    /** Enumeration of Standard Dice types  */
    enum class Standard(val sides: Int) {
        D3(3), D4(4), D6(6), D8(8), D10(10), D12(12), D20(20)
    }

    /**
     * @return True if the die is a standard type, false otherwise.
     */
    val isStandard: Boolean
    private val standard: Standard?
    private val faces: MutableList<String?>?

    /** Constructor for standard dice
     * @param standard The standard type of the dice (e.g. d4, d6, d10)
     */
    private constructor(standard: Standard?) {
        this.isStandard = true
        this.standard = standard
        this.faces = null
    }

    /** Constructor for custom dice
     * @param faces The list of faces for the custom dice (e.g. "a", "b", "c")
     */
    private constructor(faces: MutableList<String?>?) {
        this.isStandard = false
        this.standard = null
        this.faces = faces
    }

    // Accessors for standard dice

    /**
     * @return The total number of sides on the die.
     */
    fun sides(): Int {
        if (isStandard) {
            checkNotNull(standard)
            return standard.sides
        } else {
            checkNotNull(faces)
            return faces.size
        }
    }

    /**
     * Retrieves the face value for a given roll index.
     * For standard dice, this is the index + 1 (e.g., index 0 is face "1").
     * For custom dice, this is the string at the specified index in the faces list.
     * @param idx The zero-based index of the roll.
     * @return The string representation of the face.
     */
    fun faceAtIndex(idx: Int): String? {
        if (isStandard) {
            return (idx + 1).toString()
        } else {
            checkNotNull(faces)
            return faces.get(idx)
        }
    }

    companion object {
        /** Factory method to create a new Dice instance.
         * @param s The standard type of the dice (e.g. d4, d6, d10)
         * @return A new Dice instance representing a standard dice of the specified type
         */
        @JvmStatic
        fun standard(s: Standard?): Dice {
            return Dice(s)
        }

        /**
         * Factory method to create a new Dice instance.
         * @param faces The list of faces for the custom dice (e.g. "a", "b", "c")
         * @return A new Dice instance representing a custom dice with the specified faces
         */
        fun custom(faces: MutableList<String?>?): Dice {
            return Dice(faces)
        }
    }
}

