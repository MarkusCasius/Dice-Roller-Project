package com.example.dicerollerproject

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
    class CreateFragmentTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testCreateCustomDie() {
        // Navigate to Create Fragment (Assuming it's the 2nd menu item)
        onView(withId(R.id.createFragment)).perform(click())

        // Input Die Name
        onView(withId(R.id.editDieName)).perform(typeText("IceDie"), closeSoftKeyboard())

        // Input Faces
        onView(withId(R.id.editDieFaces)).perform(typeText("1,2,Freeze,3"), closeSoftKeyboard())

        // Click Save
        onView(withId(R.id.btnSaveDie)).perform(click())

        // Verify the fields are still there (or check for a Toast if you have custom matchers)
        onView(withId(R.id.editDieName)).check(matches(isDisplayed()))
    }

    @Test
    fun testAddDiceRowToRule() {
        onView(withId(R.id.createFragment)).perform(click())

        // Click Add Dice to Rule button
        onView(withId(R.id.btnAddDiceToRule)).perform(click())

        // Check if the dynamic row (item_dice_row) was added
        onView(withId(R.id.editDiceCountRow)).check(matches(isDisplayed()))
    }
}