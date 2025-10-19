package com.example.mybabyvaxadmin.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mybabyvaxadmin.R
import com.example.mybabyvaxadmin.databinding.ItemSchedulesBinding
import com.example.mybabyvaxadmin.models.MergedSchedule

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


            binding.statusIndicator.setBackgroundResource(R.color.mainColor)

            binding.root.setOnClickListener {
                onItemClick(schedule)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = ItemSchedulesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScheduleViewHolder(view)
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
