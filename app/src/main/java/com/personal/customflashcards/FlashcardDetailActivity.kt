package com.personal.customflashcards

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FlashcardDetailActivity : AppCompatActivity() {

    private lateinit var flashcardsRecyclerView: RecyclerView
    private val flashcards = mutableListOf<Flashcard>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flashcard_detail)

        flashcardsRecyclerView = findViewById(R.id.flashcardsRecyclerView)

        // Get the setName passed from the previous activity
        val setName = intent.getStringExtra("setName")

        if (setName != null) {
            flashcards.addAll(loadFlashcards(setName))
            val flashcardAdapter = FlashcardAdapter(flashcards)
            flashcardsRecyclerView.layoutManager = LinearLayoutManager(this)
            flashcardsRecyclerView.adapter = flashcardAdapter
        }

        var testButton: Button = findViewById(R.id.testButton)
        testButton.setOnClickListener {
            // Start a new Activity or show a dialog, etc., to begin the test
            val intent = Intent(this, TestActivity::class.java)
            intent.putExtra("flashcards", ArrayList(flashcards)) // Convert the flashcards list to an ArrayList before passing
            startActivity(intent)
        }
    }

    private fun loadFlashcards(setName: String): List<Flashcard> {
        val sharedPreferences = getSharedPreferences("flashcards_data", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("$setName", null) ?: return emptyList()

        val type = object : TypeToken<List<Flashcard>>() {}.type
        return gson.fromJson(json, type)
    }
}
