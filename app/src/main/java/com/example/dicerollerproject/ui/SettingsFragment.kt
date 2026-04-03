package com.example.dicerollerproject.ui

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.dicerollerproject.R
import com.example.dicerollerproject.data.LocalStore
import androidx.core.graphics.toColorInt

class SettingsFragment : Fragment(R.layout.fragment_settings) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val store = LocalStore(requireContext())

        view.findViewById<Button>(R.id.btnElemNone).setOnClickListener { store.saveElementColor("#CCCCCC".toColorInt()) }
        view.findViewById<Button>(R.id.btnElemPurple).setOnClickListener { store.saveElementColor("#6200EE".toColorInt()) }
        view.findViewById<Button>(R.id.btnElemGreen).setOnClickListener { store.saveElementColor("#2D5A27".toColorInt()) }
    }
}