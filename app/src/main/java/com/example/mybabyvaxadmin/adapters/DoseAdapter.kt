package com.example.mybabyvaxadmin.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mybabyvaxadmin.R
import com.example.mybabyvaxadmin.models.Dose

class DoseAdapter(private val doses: List<Dose>) :
    RecyclerView.Adapter<DoseAdapter.DoseViewHolder>() {

    inner class DoseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivVaccineIcon: ImageView = itemView.findViewById(R.id.ivVaccineIcon)
        val doseNameTv: TextView = itemView.findViewById(R.id.doseNameTv)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dose, parent, false)
        return DoseViewHolder(view)
    }

    override fun onBindViewHolder(holder: DoseViewHolder, position: Int) {
        val dose = doses[position]
        holder.doseNameTv.text = dose.name ?: "Unknown Dose"
    }

    override fun getItemCount(): Int = doses.size
}
