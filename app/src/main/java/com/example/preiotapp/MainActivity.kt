package com.example.preiotapp

import MapsFragment
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.MapView
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var frameLayout :FrameLayout
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        replaceFragment(MapsFragment())
        initEvenHandlers()
    }

    private fun initEvenHandlers() {
        bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.map -> replaceFragment(MapsFragment())
                R.id.status -> replaceFragment(StatusFragment())
                R.id.update  -> replaceFragment(UpdateFragment())
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frameLayout, fragment)
        transaction.commit()
    }


    private fun initView() {
        frameLayout = findViewById(R.id.frameLayout)
        bottomNavigationView = findViewById(R.id.bottomNavigationview)

    }

}