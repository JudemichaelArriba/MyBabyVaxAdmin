package com.example.mybabyvaxadmin.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mybabyvaxadmin.databinding.ItemVaccinesBinding
import com.example.mybabyvaxadmin.models.Vaccine
import com.example.mybabyvaxadmin.pages.VaccineInfoPage

class VaccineAdapter(private val vaccines: List<Vaccine>) :
    RecyclerView.Adapter<VaccineAdapter.VaccineViewHolder>() {

    inner class VaccineViewHolder(val binding: ItemVaccinesBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VaccineViewHolder {
        val binding = ItemVaccinesBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VaccineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VaccineViewHolder, position: Int) {
        val vaccine = vaccines[position]

        holder.binding.tvVaccineName.text = vaccine.name
        holder.binding.routeTv.text = vaccine.route ?: "-"
        holder.binding.typeTv.text = vaccine.type ?: "-"

        holder.binding.root.setOnClickListener {
            val intent = Intent(holder.itemView.context, VaccineInfoPage::class.java)
            intent.putExtra("vaccineId", vaccine.id)
            intent.putExtra("vaccineName", vaccine.name)
            intent.putExtra("vaccineRoute", vaccine.route)
            intent.putExtra("vaccineType", vaccine.type)
            intent.putExtra("vaccineDescription", vaccine.description)
            intent.putExtra("vaccineSideEffects", vaccine.sideEffects)
            intent.putExtra("vaccineEligibleAge", vaccine.eligibleAge)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = vaccines.size
}
