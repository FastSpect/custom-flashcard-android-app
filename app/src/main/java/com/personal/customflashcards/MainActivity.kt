package com.personal.customflashcards

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    private val tag = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val createFlashCardButton: Button = findViewById(R.id.createFlashcardButton)
        val viewFlashcardsButton: Button = findViewById(R.id.viewFlashcardsButton)

        createFlashCardButton.setOnClickListener {
            val createIntent = Intent(this, CreateFlashcardActivity::class.java)
            startActivity(createIntent)
        }

        viewFlashcardsButton.setOnClickListener {
            val viewIntent = Intent(this, ViewFlashcardsActivity::class.java)
            startActivity(viewIntent)
        }
    }
}


