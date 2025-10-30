package com.example.mybabyvaxadmin.pages

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mybabyvaxadmin.adapters.BabyAdapter
import com.example.mybabyvaxadmin.adapters.ScheduleAdapter
import com.example.mybabyvaxadmin.services.DatabaseService
import com.example.mybabyvaxadmin.databinding.ActivityAllBabiesPageBinding
import com.example.mybabyvaxadmin.models.Baby
import com.example.mybabyvaxadmin.models.Vaccine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AllBabiesPage : AppCompatActivity() {

    private lateinit var binding: ActivityAllBabiesPageBinding
    private var isExpanded = false
    private lateinit var babyAdapter: BabyAdapter
    private val databaseService = DatabaseService()
    private var allBabies: List<Baby> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = getColor(com.example.mybabyvaxadmin.R.color.mainColor)

        binding = ActivityAllBabiesPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadBabies()
        setUpSearchbar()
        babyAdapter = BabyAdapter()
//        binding.recyclerView.layoutManager = LinearLayoutManager(this)
//        binding.recyclerView.adapter = babyAdapter
        binding.scheduleRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.scheduleRecyclerView.adapter = babyAdapter
//        binding.scheduleRecyclerView.adapter = babyAdapter
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


    private fun loadBabies() {
        lifecycleScope.launch(Dispatchers.IO) {

            try {
                launch(Dispatchers.IO) {
                    binding.loading.visibility = View.VISIBLE

                    val babies = fetchBabiesWithCoroutine()
                    launch(Dispatchers.Main) {
                        allBabies = fetchBabiesWithCoroutine()
                        babyAdapter.submitList(babies)
                        binding.loading.visibility = View.GONE
                    }


                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    binding.loading.visibility = View.GONE
                    Log.e("SchedulesPage", "Error fetching schedules: ${e.message}")
                    Toast.makeText(
                        this@AllBabiesPage, e.message ?: "Error loading babies", Toast.LENGTH_LONG
                    ).show()
                }

            }

        }
    }


    private fun setUpSearchbar() {
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
                babyAdapter.updateList(allBabies.toMutableList())
            }
            isExpanded = !isExpanded
        }

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim().lowercase()
                val filteredBaby = allBabies.filter { baby ->
                    baby.fullName?.lowercase()?.contains(query) == true
                }
                babyAdapter.updateList(filteredBaby.toMutableList())
            }
        })
    }


    private suspend fun fetchBabiesWithCoroutine(): List<Baby> {

        return suspendCancellableCoroutine { cont ->
            databaseService.fetchAllBabies(object :
                com.example.mybabyvaxadmin.interfaces.InterfaceClass.AllBabiesCallback {
                override fun onBabiesLoaded(babies: List<Baby>) {
                    cont.resume(babies)
                }

                override fun onError(message: String) {
                    cont.resumeWithException(Exception(message))
                }

            })
        }
    }


}
