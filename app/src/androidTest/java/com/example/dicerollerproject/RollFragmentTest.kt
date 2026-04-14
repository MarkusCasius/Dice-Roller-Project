package com.example.dicerollerproject

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.containsString
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
    class RollFragmentTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testManualRollLogic() {
        // Roll Fragment is start destination

        // Add a dice row
        onView(withId(R.id.btnAddDiceRow)).perform(click())

        // Type quantity 5
        onView(withId(R.id.editDiceCountRow)).perform(replaceText("5"), closeSoftKeyboard())

        // Set a flat modifier
        onView(withId(R.id.editTextFlat)).perform(typeText("10"), closeSoftKeyboard())

        // Perform Roll
        onView(withId(R.id.buttonRoll)).perform(click())

        // Check if output text changed from default
        onView(withId(R.id.textView)).check(matches(withText(containsString("Result:"))))
    }

    @Test
    fun testKeepHighestToggle() {
        // Click Keep Highest Checkbox
        onView(withId(R.id.checkBoxKeepHighest)).perform(click())

        // Verify EditText is enabled
        onView(withId(R.id.editTextKeepHighest)).check(matches(isEnabled()))

        // Click Keep Lowest
        onView(withId(R.id.checkBoxKeepLowest)).perform(click())

        // Verify Keep Highest was automatically unchecked (Mutual Exclusion)
        onView(withId(R.id.checkBoxKeepHighest)).check(matches(isNotChecked()))
        onView(withId(R.id.editTextKeepHighest)).check(matches(isNotEnabled()))
    }
}