package com.personal.customflashcards

import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import java.io.IOException
import java.io.Serializable

class CreateFlashcardActivity : AppCompatActivity() {

    private val tag = "CreateFlashcardActivity"

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
                flashcardAdapter.notifyItemInserted(temporaryFlashcards.size - 1)  // Update the RecyclerView
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

        val gson = Gson()
        val flashcardsJson = gson.toJson(temporaryFlashcards)

        val contentValues = ContentValues().apply {
            put(MediaStore.Files.FileColumns.DISPLAY_NAME, "$setName.txt")
            put(MediaStore.Files.FileColumns.MIME_TYPE, "text/plain")
            put(MediaStore.Files.FileColumns.RELATIVE_PATH, "Documents/Flashcards")
        }

        val fileUri =
            contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

        fileUri?.let { uri ->
            try {
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    // Write actual JSON data to the file
                    outputStream.write(flashcardsJson.toByteArray(Charsets.UTF_8))
                }
            } catch (e: IOException) {
                // Handle the exception. This can be logging or showing an error to the user.
                e.printStackTrace()
            }
        }
    }

}

private lateinit var flashcardAdapter: FlashcardAdapter

data class Flashcard(val question: String, val answer: String) : Serializable

class FlashcardAdapter(private val flashcards: MutableList<Flashcard>) :
    RecyclerView.Adapter<FlashcardAdapter.FlashcardViewHolder>() {

    inner class FlashcardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val questionText: TextView = itemView.findViewById(R.id.questionText)
        val answerText: TextView = itemView.findViewById(R.id.answerText)
        private val deleteImageView: ImageView = itemView.findViewById(R.id.deleteImageView)

        init {
            deleteImageView.setOnClickListener {
                val position = absoluteAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    flashcards.removeAt(position)
                    notifyItemRemoved(position)
                }
            }

            itemView.setOnLongClickListener {
                val position = absoluteAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    showEditDialog(position, itemView.context)
                    notifyItemChanged(position)
                }
                true
            }
        }
    }

    private fun showEditDialog(position: Int, context: Context) {
        val flashcard = flashcards[position]
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle("Edit Flashcard")

        val dialogView = LayoutInflater.from(context).inflate(R.layout.edit_flashcard_dialog, null)
        val editQuestionEditText = dialogView.findViewById<EditText>(R.id.editQuestionEditText)
        val editAnswerEditText = dialogView.findViewById<EditText>(R.id.editAnswerEditText)

        editQuestionEditText.setText(flashcard.question)
        editAnswerEditText.setText(flashcard.answer)

        alertDialogBuilder.setView(dialogView)

        alertDialogBuilder.setPositiveButton("Save") { _, _ ->
            val newQuestion = editQuestionEditText.text.toString()
            val newAnswer = editAnswerEditText.text.toString()

            if (newQuestion.isEmpty() || newAnswer.isEmpty()) {
                Toast.makeText(context, "Question or answer cannot be empty!", Toast.LENGTH_SHORT)
                    .show()
            } else {
                flashcards[position] = Flashcard(newQuestion, newAnswer)
                notifyItemChanged(position)
            }
        }

        alertDialogBuilder.setNegativeButton("Cancel") { _, _ ->
        }

        alertDialogBuilder.create().show()
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
