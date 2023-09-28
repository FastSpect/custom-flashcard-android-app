package com.personal.customflashcards

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class FlashcardDetailActivity : AppCompatActivity() {

    private val tag = "FlashcardDetailActivity"

    private lateinit var flashcardsRecyclerView: RecyclerView
    private val flashcards = mutableListOf<Flashcard>()
    private lateinit var editQuestionEditText: EditText
    private lateinit var editAnswerEditText: EditText
    private lateinit var addFlashcardButton: Button
    private lateinit var saveAllFlashcardsButton: Button
    private lateinit var flashcardSetTitle: TextView
    private var setName: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flashcard_detail)

        flashcardsRecyclerView = findViewById(R.id.flashcardsRecyclerView)

        // Get the setName passed from the previous activity
        setName = intent.getStringExtra("setName")

        if (setName != null) {
            flashcardSetTitle = findViewById(R.id.flashcardSetTitle)
            "Flashcard Set: $setName".also { flashcardSetTitle.text = it }
            flashcards.addAll(loadFlashcards(setName!!))
            val flashcardAdapter = FlashcardAdapter(flashcards)
            flashcardsRecyclerView.layoutManager = LinearLayoutManager(this)
            flashcardsRecyclerView.adapter = flashcardAdapter
        }

        flashcardSetTitle.setOnLongClickListener {
            if (setName != null) {
                showRenameDialog(setName!!)
            }
            true
        }

        val testButton: Button = findViewById(R.id.testButton)
        testButton.setOnClickListener {
            // Start a new Activity or show a dialog, etc., to begin the test
            val intent = Intent(this, TestActivity::class.java)
            intent.putExtra(
                "flashcards", ArrayList(flashcards)
            ) // Convert the flashcards list to an ArrayList before passing
            startActivity(intent)
        }

        val deleteButton: Button = findViewById(R.id.deleteButton)
        deleteButton.setOnClickListener {
            deleteFlashcards(setName ?: "")
        }

        editQuestionEditText = findViewById(R.id.editQuestionEditText)
        editAnswerEditText = findViewById(R.id.editAnswerEditText)
        addFlashcardButton = findViewById(R.id.addFlashcardButton)
        saveAllFlashcardsButton = findViewById(R.id.saveAllFlashcardsButton)

        addFlashcardButton.setOnClickListener {
            val question = editQuestionEditText.text.toString().trim()
            val answer = editAnswerEditText.text.toString().trim()

            if (question.isNotBlank() && answer.isNotBlank()) {
                // Add to your list of flashcards
                flashcards.add(Flashcard(question, answer))

                // Update the RecyclerView to reflect the new addition
                flashcardsRecyclerView.adapter?.notifyItemInserted(flashcards.size - 1)

                // Clear the input fields
                editQuestionEditText.text.clear()
                editAnswerEditText.text.clear()
            } else {
                Toast.makeText(this, "Both fields are required!", Toast.LENGTH_SHORT).show()
            }
        }

        saveAllFlashcardsButton.setOnClickListener {
            updateFlashcards()
            Toast.makeText(this, "All flashcards saved!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadFlashcards(setName: String): List<Flashcard> {
        val file =
            File(Environment.getExternalStorageDirectory(), "Documents/Flashcards/$setName.txt")
        val gson = Gson()
        val type = object : TypeToken<List<Flashcard>>() {}.type
        return gson.fromJson(file.readText(), type)
    }


    private fun deleteFlashcards(setName: String) {
        Log.i(tag, "Deleting $setName")
        val file =
            File(Environment.getExternalStorageDirectory().absolutePath + "/Documents/Flashcards/$setName.txt")
        if (file.exists()) {
            file.delete()
        } else {
            Log.e(tag, "No $setName file found")
        }
        finish()
    }

    private fun updateFlashcards() {
        Log.i(tag, "Updating the Flashcards")
        val gson = Gson()
        val flashcardsJson = gson.toJson(flashcards)

        val setName = intent.getStringExtra("setName") ?: "defaultSet"
        val filePath =
            "${Environment.getExternalStorageDirectory().absolutePath}/Documents/Flashcards/$setName.txt"

        try {
            val file = File(filePath)
            file.parentFile?.mkdirs()  // Ensure the directories exist
            file.writeText(flashcardsJson, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e(tag, "Error updating flashcards to file", e)
        }
    }


    private fun showRenameDialog(currentName: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Rename Flashcard Set")

        val input = EditText(this)
        input.setText(currentName)
        builder.setView(input)

        builder.setPositiveButton("Rename") { dialog, _ ->
            val newName = input.text.toString().trim()
            if (newName.isNotEmpty() && newName != currentName) {
                if (renameFlashcardSet(currentName, newName)) {
                    setName = newName
                    "Flashcard Set: $setName".also { flashcardSetTitle.text = it }
                }
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }


    private fun renameFlashcardSet(oldName: String, newName: String): Boolean {
        Log.i(tag, "Renaming from $oldName to $newName")
        val oldFile =
            File(Environment.getExternalStorageDirectory().absolutePath + "/Documents/Flashcards/$oldName.txt")
        return if (oldFile.exists()) {
            val newFile =
                File(Environment.getExternalStorageDirectory().absolutePath + "/Documents/Flashcards/$newName.txt")
            if (!newFile.exists()) {
                oldFile.renameTo(newFile)
            } else {
                Log.e("RenameFile", "Target filename already exists.")
                false
            }
        } else {
            Log.e("RenameFile", "Source file does not exist.")
            false
        }
    }

}
