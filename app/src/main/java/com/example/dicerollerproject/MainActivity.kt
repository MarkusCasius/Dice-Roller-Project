package com.example.dicerollerproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.example.dicerollerproject.data.LocalStore
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val root = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.MainActivity)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        val navHostFragment = getSupportFragmentManager()
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment?
        val navController = navHostFragment!!.navController

        setupWithNavController(bottomNav, navController)
    }

    /**
     * Applies the global styles to the activity
     */
    fun applyGlobalStyles() {
        val store = LocalStore(this)
        val root = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.MainActivity)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        root.setBackgroundColor(store.getBackgroundColour())
        bottomNav.setBackgroundColor(store.getBackgroundColour())

        val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
        val colors = intArrayOf(store.getButtonColour(), store.getTextColour())
        bottomNav.itemIconTintList = android.content.res.ColorStateList(states, colors)
        bottomNav.itemTextColor = android.content.res.ColorStateList(states, colors)
    }
}