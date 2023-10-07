package com.personal.customflashcards


import android.content.res.Configuration
import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class SettingsActivity : AppCompatActivity() {

    private lateinit var currentMode: TextView
    private lateinit var modeSeekBar: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        currentMode = findViewById(R.id.current_mode)
        modeSeekBar = findViewById(R.id.mode_seekbar)

        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                modeSeekBar.progress = 2
                "Dark Mode".also { currentMode.text = it }
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                modeSeekBar.progress = 0
                "Light Mode".also { currentMode.text = it }
            }

            else -> {
                modeSeekBar.progress = 1
                "System Default".also { currentMode.text = it }
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
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
}
