package com.example.mqtt_demo_app.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.mqtt_demo_app.R
import com.example.mqtt_demo_app.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity.kt-------- MainActivity of App that contains the Fragment Container
 * ----------------- developed by Theologos Batsioulas 21/01/2022 for MQTT Demo App
 */

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    ////NavController OBJ that manages APP NAV within a NavHost
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        //Get the root view of the layout file with ViewBinding
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //Retrieve NavController from the NavHost Fragment with unsafe cast
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        setupActionBarWithNavController(navController)

    }

    //Support Up Navigation
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}