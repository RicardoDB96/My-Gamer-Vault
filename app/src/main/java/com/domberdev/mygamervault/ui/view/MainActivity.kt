package com.domberdev.mygamervault.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.NavHostFragment
import com.domberdev.mygamervault.R
import com.domberdev.mygamervault.databinding.ActivityMainBinding
import com.google.android.gms.ads.AdRequest

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @SuppressLint("UseSupportActionBar")
    override fun onCreate(savedInstanceState: Bundle?) {
        darkModeActivateStatus()
        setTheme(R.style.Theme_MyGamerVault)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initBanner()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.mainFragment) as NavHostFragment
        val navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id){
                R.id.homeFragment -> showBanner()
                R.id.addEditGameFragment -> showBanner()
                R.id.gameInfoFragment -> showBanner()
                else -> hideBanner()
            }
        }
    }

    private fun initBanner(){
        val adRequest = AdRequest.Builder().build()
        binding.banner.loadAd(adRequest)
    }

    private fun showBanner(){
        binding.banner.visibility = View.VISIBLE
    }

    private fun hideBanner(){
        binding.banner.visibility = View.GONE
    }

    //Hide the keyboard when click outside text field or views like that
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            currentFocus?.clearFocus()
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun darkModeActivateStatus() {
        val status = getSharedPreferences("userdata", MODE_PRIVATE).getInt("modeStatus", -1)
        AppCompatDelegate.setDefaultNightMode(status)
    }
}