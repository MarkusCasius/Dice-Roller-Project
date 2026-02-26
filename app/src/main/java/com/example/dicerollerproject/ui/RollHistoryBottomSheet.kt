package com.example.dicerollerproject.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dicerollerproject.R
import com.example.dicerollerproject.data.LocalStore
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class RollHistoryBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_sheet_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val store = LocalStore(requireContext())
        val history = store.listHistory()

        val rv = view.findViewById<RecyclerView>(R.id.rvHistory)
        rv.layoutManager = LinearLayoutManager(context)
        rv.adapter = RollHistoryAdapter(history)

        view.findViewById<Button>(R.id.btnClearHistory).setOnClickListener {
            store.saveHistory(emptyList())
            rv.adapter = RollHistoryAdapter(emptyList())
        }
    }
}