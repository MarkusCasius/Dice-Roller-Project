package com.example.dicerollerproject.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dicerollerproject.R
import com.example.dicerollerproject.data.model.RollHistoryItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.text.format

class RollHistoryAdapter(private val history: List<RollHistoryItem>) :
    RecyclerView.Adapter<RollHistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtDescription: TextView = view.findViewById(R.id.txtHistoryDescription)
        val txtTotal: TextView = view.findViewById(R.id.txtHistoryTotal)
        val txtResults: TextView = view.findViewById(R.id.txtHistoryResults)
        val txtTimestamp: TextView = view.findViewById(R.id.txtHistoryTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_roll_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = history[position]
        holder.txtDescription.text = item.diceDescription
        holder.txtTotal.text = item.total.toString()
        // Clean up the bracket string from the list
        holder.txtResults.text = "${item.modifierLabel} • ${item.results.removeSurrounding("[", "]")}"

        val sdf =
            SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        holder.txtTimestamp.text = sdf.format(Date(item.timestamp))
    }

    override fun getItemCount() = history.size
}