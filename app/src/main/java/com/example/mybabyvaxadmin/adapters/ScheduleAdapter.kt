package com.example.mybabyvaxadmin.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mybabyvaxadmin.databinding.ItemSchedulesBinding
import com.example.mybabyvaxadmin.models.MergedSchedule
import com.example.mybabyvaxadmin.pages.ScheduleInfoPage

class ScheduleAdapter(
    private var scheduleList: List<MergedSchedule>,
    private val onItemClick: (MergedSchedule) -> Unit
) : RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    inner class ScheduleViewHolder(private val binding: ItemSchedulesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(schedule: MergedSchedule) {
            binding.tvVaccineName.text = schedule.vaccineName
            binding.tvDoseName.text = schedule.doseName
            binding.tvScheduleDate.text = schedule.date
            binding.statusIndicator.setBackgroundResource(com.example.mybabyvaxadmin.R.color.mainColor)


            binding.root.setOnClickListener {
                onItemClick(schedule)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val binding =
            ItemSchedulesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScheduleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        holder.bind(scheduleList[position])
    }

    override fun getItemCount(): Int = scheduleList.size

    fun updateList(newList: List<MergedSchedule>) {
        scheduleList = newList
        notifyDataSetChanged()
    }
}