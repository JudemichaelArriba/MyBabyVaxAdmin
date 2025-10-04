package com.example.mybabyvaxadmin.components

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.mybabyvaxadmin.R
import com.example.mybabyvaxadmin.databinding.ActivityBottomNavBinding
import com.example.mybabyvaxadmin.pages.homePage
import com.example.mybabyvaxadmin.pages.profilePage
import com.example.mybabyvaxadmin.pages.vaccinesPage

class bottomNav : AppCompatActivity() {

    private lateinit var binding: ActivityBottomNavBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        showFragment(homePage())
        binding = ActivityBottomNavBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bottomNavigator)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.bottomNav.selectedItemId = R.id.home


        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    showFragment(homePage())
                    true
                }

                R.id.Vaccines -> {
                    showFragment(vaccinesPage())
                    true
                }


                R.id.profile -> {
                    showFragment(profilePage())
                    true
                }

                else -> false
            }
        }


    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }


}