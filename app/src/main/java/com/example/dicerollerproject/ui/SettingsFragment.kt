package com.example.dicerollerproject.ui

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.dicerollerproject.R
import com.example.dicerollerproject.data.LocalStore

class SettingsFragment : Fragment(R.layout.fragment_settings) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val store = LocalStore(requireContext())

//        view.findViewById<Button>(R.id.btnTextBlack).setOnClickListener { store.saveTextColor(Color.BLACK) }
//        view.findViewById<Button>(R.id.btnTextBlue).setOnClickListener { store.saveTextColor(Color.BLUE) }

        view.findViewById<Button>(R.id.btnElemNone).setOnClickListener { store.saveElementColor(Color.parseColor("#808080")) }
        view.findViewById<Button>(R.id.btnElemPurple).setOnClickListener { store.saveElementColor(Color.parseColor("#6200EE")) }
        view.findViewById<Button>(R.id.btnElemGreen).setOnClickListener { store.saveElementColor(Color.parseColor("#2D5A27")) }
    }
}