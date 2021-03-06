package com.mertrizakaradeniz.notely.ui.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.mertrizakaradeniz.notely.R
import com.mertrizakaradeniz.notely.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController
    }

    fun progressBarVisibility() {
        binding.progressBar.apply {
            when (visibility) {
                View.VISIBLE -> visibility = View.GONE
                View.GONE -> visibility = View.VISIBLE
                View.INVISIBLE -> visibility = View.VISIBLE
            }
        }
    }
}