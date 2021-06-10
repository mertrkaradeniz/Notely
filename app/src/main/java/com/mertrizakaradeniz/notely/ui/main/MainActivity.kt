package com.mertrizakaradeniz.notely.ui.main

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.mertrizakaradeniz.notely.R
import com.mertrizakaradeniz.notely.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupNavigation()
        supportActionBar?.elevation = 0f
    }

    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.ListFragment
            )
        )

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.signInFragment -> {
                    supportActionBar?.hide()
                    makeFullScreen()
                }
                R.id.signUpFragment -> {
                    supportActionBar?.hide()
                    makeFullScreen()
                }
                else -> {
                    supportActionBar?.show()
                }
            }
        }
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onNavigateUp(): Boolean {
        return navController.navigateUp()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun hideProgressBar() {
        binding.progressBar.visibility = View.INVISIBLE
    }

    fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun makeFullScreen() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }
}