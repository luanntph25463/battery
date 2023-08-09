package com.example.battery

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    lateinit var bottom_nav :  BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loadfragment(HomeFragment())
        bottom_nav = findViewById(R.id.bottom_nav) as BottomNavigationView
        bottom_nav.setOnItemReselectedListener {
            when(it.itemId){
                R.id.home ->{
                    loadfragment(HomeFragment())
                    true
                }
                R.id.setting ->{
                    loadfragment(settingFragment())
                    true
                }
            }
        }
    }

    private fun loadfragment(fragment: Fragment) {
        var transaction = supportFragmentManager.beginTransaction().replace(R.id.container,fragment).commit()
    }
}