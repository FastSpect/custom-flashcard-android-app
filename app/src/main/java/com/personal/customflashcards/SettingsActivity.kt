package com.personal.customflashcards

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class SettingsActivity : AppCompatActivity() {

    private lateinit var currentMode: TextView
    private lateinit var modeSeekBar: SeekBar
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        currentMode = findViewById(R.id.current_mode)
        modeSeekBar = findViewById(R.id.mode_seekbar)
        sharedPref = getSharedPreferences("theme_prefs", MODE_PRIVATE)

        // Load saved preference and set SeekBar and text accordingly
        when (sharedPref.getInt("theme_mode", 1)) { // Default is system default
            0 -> {
                modeSeekBar.progress = 0
                "Light Mode".also { currentMode.text = it }
            }

            1 -> {
                modeSeekBar.progress = 1
                "System Default".also { currentMode.text = it }
            }

            2 -> {
                modeSeekBar.progress = 2
                "Dark Mode".also { currentMode.text = it }
            }
        }

        modeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                when (progress) {
                    0 -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        "Light Mode".also { currentMode.text = it }
                    }

                    1 -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                        "System Default".also { currentMode.text = it }
                    }

                    2 -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        "Dark Mode".also { currentMode.text = it }
                    }
                }

                // Save the selected theme mode in SharedPreferences
                val editor = sharedPref.edit()
                editor.putInt(
                    "theme_mode", progress
                ) // 0 for light, 1 for system default, 2 for dark
                editor.apply()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
}
