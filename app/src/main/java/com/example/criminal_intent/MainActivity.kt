package com.example.criminal_intent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class MainActivity : AppCompatActivity() {





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MAIN", "Activity: onCreate()")
        setContentView(R.layout.activity_main)

        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        if (currentFragment == null) {
            val fragment = CrimeFragment()
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit()
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("MAIN", "Activity: onStart()")
    }

    override fun onResume() {
        super.onResume()
        Log.d("MAIN", "Activity: onResume()")
    }

    override fun onPause() {
        super.onPause()
        Log.d("MAIN", "Activity: onPause()")
    }

    override fun onStop() {
        super.onStop()
        Log.d("MAIN", "Activity: onStop()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MAIN", "Activity: onDestroy()")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d("MAIN", "Activity: onSaveInstanceState()")
    }
}