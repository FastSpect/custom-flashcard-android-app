package com.personal.customflashcards

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
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
    private lateinit var questionLimitSpinner: Spinner
    private lateinit var itemCounterTextView: TextView
    private lateinit var addFlashcardButton: Button
    private lateinit var saveAllFlashcardsButton: Button
    private lateinit var flashcardSetTitle: TextView
    private var fileName: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flashcard_detail)

        flashcardsRecyclerView = findViewById(R.id.flashcardsRecyclerView)
        questionLimitSpinner = findViewById(R.id.questionLimitSpinner)
        itemCounterTextView = findViewById(R.id.itemCounterTextView)

        setupSpinner()

        // Get the fileName or setName passed from the previous activity
        fileName = intent.getStringExtra("fileName") ?: (intent.getStringExtra("setName")?.let { "$it.txt" })

        if (fileName != null) {
            flashcardSetTitle = findViewById(R.id.flashcardSetTitle)
            val displayName = fileName!!.substringBeforeLast('.')
            "Flashcard Set: $displayName".also { flashcardSetTitle.text = it }
            flashcards.addAll(loadFlashcards(fileName!!))
            updateItemCounter(0)

            val flashcardAdapter = FlashcardAdapter(flashcards)
            flashcardsRecyclerView.layoutManager = LinearLayoutManager(this)
            flashcardsRecyclerView.adapter = flashcardAdapter

            flashcardsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    if (firstVisibleItemPosition != RecyclerView.NO_POSITION) {
                        updateItemCounter(firstVisibleItemPosition)
                    }
                }
            })
        }

        flashcardSetTitle.setOnLongClickListener {
            if (fileName != null) {
                showRenameDialog(fileName!!)
            }
            true
        }

        val testButton: Button = findViewById(R.id.testButton)
        testButton.setOnClickListener {
            val intent = Intent(this, TestActivity::class.java)

            val selectedOption = questionLimitSpinner.selectedItem.toString()
            val questionLimit = if (selectedOption == "All") {
                flashcards.size
            } else {
                selectedOption.toIntOrNull() ?: 60
            }

            val selectedFlashcards = pickRandomFlashcards(flashcards, questionLimit)

            intent.putExtra("flashcards", selectedFlashcards)
            startActivity(intent)
        }

        val deleteButton: Button = findViewById(R.id.deleteButton)
        deleteButton.setOnClickListener {
            deleteFlashcards(fileName ?: "")
        }

        editQuestionEditText = findViewById(R.id.editQuestionEditText)
        editAnswerEditText = findViewById(R.id.editAnswerEditText)
        addFlashcardButton = findViewById(R.id.addFlashcardButton)
        saveAllFlashcardsButton = findViewById(R.id.saveAllFlashcardsButton)

        addFlashcardButton.setOnClickListener {
            val question = editQuestionEditText.text.toString().trim()
            val answer = editAnswerEditText.text.toString().trim()

            if (question.isNotBlank() && answer.isNotBlank()) {
                flashcards.add(Flashcard(question, answer))
                flashcardsRecyclerView.adapter?.notifyItemInserted(flashcards.size - 1)
                updateItemCounter(0) // Refresh count
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

    private fun updateItemCounter(currentPosition: Int) {
        val total = flashcards.size
        val current = if (total > 0) currentPosition + 1 else 0
        itemCounterTextView.text = "$current - $total"
    }

    private fun setupSpinner() {
        val options = arrayOf("10", "20", "60", "80", "100", "All")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        questionLimitSpinner.adapter = adapter
        questionLimitSpinner.setSelection(2)
    }

    private fun loadFlashcards(fileName: String): List<Flashcard> {
        val file =
            File(Environment.getExternalStorageDirectory(), "Documents/Flashcards/$fileName")
        val gson = Gson()
        val type = object : TypeToken<List<Flashcard>>() {}.type
        return try {
            gson.fromJson(file.readText(), type) ?: listOf()
        } catch (e: Exception) {
            listOf()
        }
    }


    private fun deleteFlashcards(fileName: String) {
        Log.i(tag, "Deleting $fileName")
        val file =
            File(Environment.getExternalStorageDirectory().absolutePath + "/Documents/Flashcards/$fileName")
        if (file.exists()) {
            file.delete()
        } else {
            Log.e(tag, "No $fileName file found")
        }
        finish()
    }

    private fun updateFlashcards() {
        Log.i(tag, "Updating the Flashcards")
        val gson = Gson()
        val flashcardsJson = gson.toJson(flashcards)

        val filePath =
            "${Environment.getExternalStorageDirectory().absolutePath}/Documents/Flashcards/$fileName"

        try {
            val file = File(filePath)
            file.parentFile?.mkdirs()  // Ensure the directories exist
            file.writeText(flashcardsJson, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e(tag, "Error updating flashcards to file", e)
        }
    }


    private fun showRenameDialog(currentFileName: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Rename Flashcard Set")

        val currentDisplayName = currentFileName.substringBeforeLast('.')
        val extension = currentFileName.substringAfterLast('.', "txt")

        val input = EditText(this)
        input.setText(currentDisplayName)
        builder.setView(input)

        builder.setPositiveButton("Rename") { dialog, _ ->
            val newDisplayName = input.text.toString().trim()
            if (newDisplayName.isNotEmpty() && newDisplayName != currentDisplayName) {
                val newFileName = "$newDisplayName.$extension"
                if (renameFlashcardSet(currentFileName, newFileName)) {
                    fileName = newFileName
                    "Flashcard Set: $newDisplayName".also { flashcardSetTitle.text = it }
                }
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }


    private fun renameFlashcardSet(oldFileName: String, newFileName: String): Boolean {
        Log.i(tag, "Renaming from $oldFileName to $newFileName")
        val oldFile =
            File(Environment.getExternalStorageDirectory().absolutePath + "/Documents/Flashcards/$oldFileName")
        return if (oldFile.exists()) {
            val newFile =
                File(Environment.getExternalStorageDirectory().absolutePath + "/Documents/Flashcards/$newFileName")
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

    private fun pickRandomFlashcards(all: List<Flashcard>, limit: Int): ArrayList<Flashcard> {
        if (all.size <= limit) return ArrayList(all)
        return ArrayList(all.shuffled().take(limit))
    }


}
