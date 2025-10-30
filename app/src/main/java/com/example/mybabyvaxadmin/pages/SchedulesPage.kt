package com.example.mybabyvaxadmin.pages

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mybabyvaxadmin.adapters.ScheduleAdapter
import com.example.mybabyvaxadmin.databinding.ActivitySchedulesPageBinding
import com.example.mybabyvaxadmin.services.DatabaseService
import com.example.mybabyvaxadmin.models.MergedSchedule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class SchedulesPage : AppCompatActivity() {

    private lateinit var binding: ActivitySchedulesPageBinding
    private lateinit var adapter: ScheduleAdapter
    private val databaseService = DatabaseService()
    private var isExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySchedulesPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(com.example.mybabyvaxadmin.R.color.mainColor)

        setupRecyclerView()
        setupSearchBarAnimation()
        setupBackButton()


        loadSchedules()
    }


    private fun setupRecyclerView() {
        adapter = ScheduleAdapter(emptyList()) { schedule ->

            val intent = Intent(this, ScheduleInfoPage::class.java).apply {
                putExtra("vaccineName", schedule.vaccineName)
                putExtra("doseName", schedule.doseName)
                putExtra("date", schedule.date)
                putStringArrayListExtra("babyIds", ArrayList(schedule.babyIds))
            }
            startActivity(intent)
        }

        binding.scheduleRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.scheduleRecyclerView.adapter = adapter
    }


    private fun loadSchedules() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                launch(Dispatchers.Main) { binding.loading.visibility = View.VISIBLE }

                val schedules = fetchSchedulesSafely()

                launch(Dispatchers.Main) {
                    adapter.updateList(schedules)
                    binding.loading.visibility = View.GONE
                }

            } catch (e: Exception) {
                Log.e("SchedulesPage", "Error fetching schedules: ${e.message}")
                launch(Dispatchers.Main) {
                    binding.loading.visibility = View.GONE
                    Toast.makeText(
                        this@SchedulesPage,
                        e.message ?: "Error loading schedules",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private suspend fun fetchSchedulesSafely(): List<MergedSchedule> =
        suspendCancellableCoroutine { cont ->
            databaseService.fetchAllBabySchedules(object :
                com.example.mybabyvaxadmin.interfaces.InterfaceClass.MergedScheduleCallback {
                override fun onMergedSchedulesLoaded(schedules: List<MergedSchedule>) {
                    cont.resume(schedules)
                }

                override fun onError(error: String) {
                    cont.resumeWithException(Exception(error))
                }
            })
        }


    private fun setupSearchBarAnimation() {
        val searchContainer = binding.searchContainer
        val searchIcon = binding.searchIcon
        val searchInput = binding.searchInput

        searchIcon.setOnClickListener {
            if (!isExpanded) {
                expandSearchBar(searchContainer)
                searchInput.visibility = View.VISIBLE
                searchInput.alpha = 0f
                searchInput.animate().alpha(1f).setDuration(200).start()
            } else {
                collapseSearchBar(searchContainer)
                searchInput.animate().alpha(0f).setDuration(200)
                    .withEndAction { searchInput.visibility = View.GONE }
                    .start()
            }
            isExpanded = !isExpanded
        }
    }

    private fun expandSearchBar(view: View) {
        val startWidth = view.width
        val endWidth = 600
        val animator = ValueAnimator.ofInt(startWidth, endWidth)
        animator.addUpdateListener {
            val value = it.animatedValue as Int
            val params = view.layoutParams
            params.width = value
            view.layoutParams = params
        }
        animator.duration = 300
        animator.start()
    }

    private fun collapseSearchBar(view: View) {
        val startWidth = view.width
        val endWidth = 120
        val animator = ValueAnimator.ofInt(startWidth, endWidth)
        animator.addUpdateListener {
            val value = it.animatedValue as Int
            val params = view.layoutParams
            params.width = value
            view.layoutParams = params
        }
        animator.duration = 300
        animator.start()
    }


    private fun setupBackButton() {
        binding.backButton.setOnClickListener { finish() }
    }
}
