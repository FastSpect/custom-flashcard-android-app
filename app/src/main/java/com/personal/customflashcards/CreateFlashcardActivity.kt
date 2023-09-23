package com.personal.customflashcards

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import java.io.Serializable

class CreateFlashcardActivity : AppCompatActivity() {

    private lateinit var setNameEditText: EditText
    private lateinit var questionEditText: EditText
    private lateinit var answerEditText: EditText
    private lateinit var addFlashcardButton: Button
    private lateinit var saveAllFlashcardsButton: Button
    private val temporaryFlashcards = mutableListOf<Flashcard>()
    private var setName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_flashcard)

        setNameEditText = findViewById(R.id.nameFlashCard)
        questionEditText = findViewById(R.id.questionEditText)
        answerEditText = findViewById(R.id.answerEditText)
        addFlashcardButton = findViewById(R.id.addButton)
        saveAllFlashcardsButton = findViewById(R.id.savebutton)
        val flashcardsRecyclerView: RecyclerView = findViewById(R.id.flashcardsRecyclerView)

        addFlashcardButton.setOnClickListener {
            val question = questionEditText.text.toString().trim()
            val answer = answerEditText.text.toString().trim()

            if (question.isNotBlank() && answer.isNotBlank()) {
                temporaryFlashcards.add(Flashcard(question, answer))
                flashcardAdapter.notifyDataSetChanged()  // Update the RecyclerView
                questionEditText.text.clear()
                answerEditText.text.clear()
                Toast.makeText(this, "Flashcard added!", Toast.LENGTH_SHORT).show()
            }
        }

        flashcardAdapter = FlashcardAdapter(temporaryFlashcards)
        flashcardsRecyclerView.layoutManager = LinearLayoutManager(this)
        flashcardsRecyclerView.adapter = flashcardAdapter

        saveAllFlashcardsButton.setOnClickListener {
            setName = setNameEditText.text.toString().trim()
            if (setName.isNotBlank() && temporaryFlashcards.isNotEmpty()) {
                saveFlashcards(setName)
                Toast.makeText(this, "All flashcards saved under $setName!", Toast.LENGTH_SHORT)
                    .show()
                temporaryFlashcards.clear()
                flashcardAdapter.notifyDataSetChanged()
                setNameEditText.text.clear()
            } else {
                Toast.makeText(
                    this,
                    "Please provide a name and add at least one flashcard.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun saveFlashcards(setName: String) {
        // Use 'setName' as a key or part of the key to save 'temporaryFlashcards' as JSON
        // For example, you could prefix all set names with "flashcard_set_" to distinguish in SharedPreferences
        // e.g., "flashcard_set_topic1", "flashcard_set_topic2", etc.
        val gson = Gson()
        val flashcardsJson = gson.toJson(temporaryFlashcards)

        // Save the JSON string in SharedPreferences under a specific key
        val sharedPreferences = getSharedPreferences("flashcards_data", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Using a key based on setName, e.g., "flashcard_set_topic1"
        editor.putString("$setName", flashcardsJson)

        editor.apply()
    }
}

private lateinit var flashcardAdapter: FlashcardAdapter

data class Flashcard(val question: String, val answer: String) : Serializable


class FlashcardAdapter(private val flashcards: List<Flashcard>) :
    RecyclerView.Adapter<FlashcardAdapter.FlashcardViewHolder>() {

    inner class FlashcardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val questionText: TextView = itemView.findViewById(R.id.questionText)
        val answerText: TextView = itemView.findViewById(R.id.answerText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlashcardViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.flashcard_item, parent, false)
        return FlashcardViewHolder(view)
    }

    override fun onBindViewHolder(holder: FlashcardViewHolder, position: Int) {
        val flashcard = flashcards[position]
        holder.questionText.text = flashcard.question
        holder.answerText.text = flashcard.answer
    }

    override fun getItemCount(): Int {
        return flashcards.size
    }
}
