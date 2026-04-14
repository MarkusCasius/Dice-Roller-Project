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
    class SimulateFragmentTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testSimulationRun() {
        // Navigate to Simulate Fragment
        onView(withId(R.id.simulateFragment)).perform(click())

        // Enter trials
        onView(withId(R.id.editTrials)).perform(replaceText("500"), closeSoftKeyboard())

        // Click Run
        onView(withId(R.id.btnRunSimulation)).perform(click())

        // Since simulation is on a background thread, we wait a moment or
        // check for the final result string
        // Note: In production, use IdlingResource for background threads.
        // For a simple test, we check if the results text eventually updates.
        Thread.sleep(2000)

        onView(withId(R.id.textSimResults)).check(matches(withText(containsString("Mean:"))))
    }
}