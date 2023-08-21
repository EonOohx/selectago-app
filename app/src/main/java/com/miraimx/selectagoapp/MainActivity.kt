package com.miraimx.selectagoapp

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.miraimx.selectagoapp.databinding.ActivityMainBinding
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var mAdView : AdView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val actionBar: ActionBar? = supportActionBar
        if (actionBar != null) {
            //Poner el ícono al ActionBar
            actionBar.setIcon(R.drawable.tfl_logo)
            actionBar.setDisplayShowHomeEnabled(true)
            actionBar.setBackgroundDrawable(ColorDrawable(Color.parseColor("#62aa00")))
        }
        if (savedInstanceState == null){
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            val navView: BottomNavigationView = binding.navView

            val navController = findNavController(R.id.nav_host_fragment_activity_main)
            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            val appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.navigation_home, R.id.navigation_detectar, R.id.navigation_estimar
                )
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
            navView.setupWithNavController(navController)

            // ANUNCIO
            MobileAds.initialize(this) {}
            mAdView = findViewById(R.id.adView)
            val adRequest = AdRequest.Builder().build()
            mAdView.loadAd(adRequest)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.ayuda -> {
                val intent = Intent(this, Ayuda::class.java)
                startActivity(intent)
            }
            R.id.about ->{
                val intent = Intent(this, AcercaDe::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}