package com.example.mybabyvaxadmin.pages

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.mybabyvaxadmin.databinding.ActivitySchedulesPageBinding

class SchedulesPage : AppCompatActivity() {

    private lateinit var binding: ActivitySchedulesPageBinding
    private var isExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySchedulesPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(com.example.mybabyvaxadmin.R.color.mainColor)
        setupSearchBarAnimation()
        setupBackButton()
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
        val endWidth = 500

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
        binding.backButton.setOnClickListener {
            finish()
        }
    }
}
