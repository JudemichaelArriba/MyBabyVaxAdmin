package com.example.mybabyvaxadmin.pages

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mybabyvaxadmin.adapters.ScheduleAdapter
import com.example.mybabyvaxadmin.databinding.ActivitySchedulesPageBinding
import com.example.mybabyvaxadmin.models.MergedSchedule
import com.example.mybabyvaxadmin.services.DatabaseService
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
    private var allSched: List<MergedSchedule> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySchedulesPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(com.example.mybabyvaxadmin.R.color.mainColor)
        binding.swipeRefresh.setColorSchemeColors(getColor(com.example.mybabyvaxadmin.R.color.mainColor))
        setupRecyclerView()
        setupSearchBar()
        setupBackButton()
        binding.swipeRefresh.setOnRefreshListener { loadSchedules() }
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
                allSched = schedules
                launch(Dispatchers.Main) {
                    adapter.updateList(allSched)
                    binding.loading.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    binding.loading.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
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

    private fun setupSearchBar() {
        val searchBar = binding.searchContainer
        val searchIcon = binding.searchIcon
        val searchInput = binding.searchInput

        searchIcon.setOnClickListener {
            if (!isExpanded) {
                searchBar.post {
                    expandSearchBar(searchBar)
                    searchInput.visibility = View.VISIBLE
                    searchInput.alpha = 0f
                    searchInput.animate().alpha(1f).setDuration(200).start()
                }
            } else {
                collapseSearchBar(searchBar)
                searchInput.animate().alpha(0f).setDuration(200)
                    .withEndAction { searchInput.visibility = View.GONE }.start()
                searchInput.text?.clear()
                adapter.updateList(allSched)
            }
            isExpanded = !isExpanded
        }

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim().lowercase()
                val filteredList = allSched.filter { schedule ->
                    schedule.vaccineName.lowercase().contains(query)
                }
                adapter.updateList(filteredList)
            }
        })
    }

    private fun expandSearchBar(view: View) {
        val startWidth = view.width
        val animator = ValueAnimator.ofInt(startWidth, 500)
        animator.addUpdateListener {
            val params = view.layoutParams
            params.width = it.animatedValue as Int
            view.layoutParams = params
        }
        animator.duration = 300
        animator.start()
    }

    private fun collapseSearchBar(view: View) {
        val startWidth = view.width
        val animator = ValueAnimator.ofInt(startWidth, 120)
        animator.addUpdateListener {
            val params = view.layoutParams
            params.width = it.animatedValue as Int
            view.layoutParams = params
        }
        animator.duration = 300
        animator.start()
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener { finish() }
    }
}
