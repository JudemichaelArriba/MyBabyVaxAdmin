package com.example.mybabyvaxadmin.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mybabyvaxadmin.R
import com.example.mybabyvaxadmin.databinding.ItemBabyBinding
import com.example.mybabyvaxadmin.models.Baby
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.util.Base64
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import com.example.mybabyvaxadmin.models.Vaccine
import java.util.Calendar
import kotlin.apply
import kotlin.text.isEmpty
import kotlin.text.isNullOrEmpty
import kotlin.text.lowercase
import kotlin.text.split
import kotlin.text.toIntOrNull
import kotlin.text.trim

class BabyAdapter : RecyclerView.Adapter<BabyAdapter.BabyViewHolder>() {

    private var babies = mutableListOf<Baby>()

    inner class BabyViewHolder(val binding: ItemBabyBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BabyViewHolder {
        val binding = ItemBabyBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BabyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BabyViewHolder, position: Int) {
        val baby = babies[position]
        with(holder.binding) {
            tvBabyName.text = baby.fullName ?: "No Name"
            tvBabyAge.text = calculateAge(baby.dateOfBirth)
            tvBirthWeight.text = "${baby.weightAtBirth ?: 0.0}kg"
            tvBirthHeight.text = "${baby.heightAtBirth ?: 0}cm"


            if (!baby.profileImageUrl.isNullOrEmpty()) {
                try {

                    val imageBytes = Base64.decode(baby.profileImageUrl, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

                    Glide.with(holder.binding.profileImage.context)
                        .load(bitmap)
                        .circleCrop()
                        .into(holder.binding.profileImage)
                } catch (e: Exception) {
                    e.printStackTrace()
                    holder.binding.profileImage.setImageResource(R.color.gray)
                }
            } else {
                holder.binding.profileImage.setImageResource(R.color.gray)
            }

            when (baby.gender?.lowercase()) {
                "male" -> {
                    genderIcon.setImageResource(R.drawable.ic_male_icon)
                    genderIcon.setColorFilter(null)
                }

                "female" -> {
                    genderIcon.setImageResource(R.drawable.ic_female_icon)
                    genderIcon.setColorFilter(
                        ContextCompat.getColor(genderIcon.context, R.color.pink),
                        PorterDuff.Mode.SRC_IN
                    )
                }

                else -> {
                    genderIcon.setImageResource(R.color.whiteTransparent)
                }
            }



            btnMoreOptions.setOnClickListener {

            }
        }
    }

    override fun getItemCount(): Int = babies.size


    fun submitList(newList: List<Baby>) {
        babies.clear()
        babies.addAll(newList)
        notifyDataSetChanged()
    }

    private fun calculateAge(dob: String?): String {
        if (dob.isNullOrEmpty()) return ""

        val dobParts = dob.split("-")
        if (dobParts.size != 3) return ""
        val year = dobParts[0].toIntOrNull() ?: return ""
        val month = dobParts[1].toIntOrNull() ?: return ""
        val day = dobParts[2].toIntOrNull() ?: return ""

        val birthDate = Calendar.getInstance().apply {
            set(year, month - 1, day)
        }

        val today = Calendar.getInstance()

        var ageYears = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR)
        var ageMonths = today.get(Calendar.MONTH) - birthDate.get(Calendar.MONTH)
        var ageDays = today.get(Calendar.DAY_OF_MONTH) - birthDate.get(Calendar.DAY_OF_MONTH)

        if (ageDays < 0) {
            ageMonths--

            val prevMonth = today.clone() as Calendar
            prevMonth.add(Calendar.MONTH, -1)
            ageDays += prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
        }

        if (ageMonths < 0) {
            ageYears--
            ageMonths += 12
        }

        return buildString {
            if (ageYears > 0) append("$ageYears years ")
            if (ageMonths > 0) append("$ageMonths months ")
            if (ageDays > 0) append("$ageDays days")
            if (isEmpty()) append("0 days")
        }.trim()
    }

    fun updateList(newList: MutableList<Baby>) {
        babies = newList
        notifyDataSetChanged()
    }
}
