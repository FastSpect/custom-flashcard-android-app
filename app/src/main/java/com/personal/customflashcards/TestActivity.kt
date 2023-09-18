package com.personal.customflashcards

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TestActivity : AppCompatActivity() {


    private val tag = "TestActivity"

    private lateinit var questionTextView: TextView
    private lateinit var optionsRadioGroup: RadioGroup
    private lateinit var nextButton: Button
    private lateinit var correctOption: RadioButton


    private var flashcards: List<Flashcard> = listOf()
    private var questionIndices: MutableList<Int> = mutableListOf()
    private var isAnswerCorrect = false
    private var totalQuestions = 0
    private var correctAnswers = 0
    private var incorrectTries = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        questionTextView = findViewById(R.id.questionTextView)
        optionsRadioGroup = findViewById(R.id.optionsRadioGroup)
        nextButton = findViewById(R.id.nextButton)

        // Load flashcards - This should ideally be loaded from SharedPreferences or passed from the previous activity
        // ...
        flashcards = intent.getSerializableExtra("flashcards") as? List<Flashcard> ?: listOf()
        totalQuestions = flashcards.size

        // Initialize and shuffle the question indices
        questionIndices = flashcards.indices.toMutableList().shuffled().toMutableList()



        displayQuestion()

        nextButton.setOnClickListener {
            val selectedOptionId = optionsRadioGroup.checkedRadioButtonId

            // Check if an option was selected
            if (selectedOptionId == -1) {
                Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedOption: RadioButton = findViewById(selectedOptionId)

            // If you shuffle options in a real test, then you'd need a mechanism to track the correct answer's position
            isAnswerCorrect =
                if (selectedOption.text == flashcards[questionIndices.first()].answer) {
                    selectedOption.setBackgroundColor(Color.GREEN)  // Correct Answer
                    true
                } else {
                    selectedOption.setBackgroundColor(Color.RED)  // Wrong Answer
                    correctOption.setBackgroundColor(Color.GREEN)  // Indicate the correct answer
                    false
                }


            // Delay for a brief moment to show the correct/wrong color feedback
            Handler(Looper.getMainLooper()).postDelayed({
                optionsRadioGroup.clearCheck() // Clear previous selection
                selectedOption.setBackgroundColor(Color.WHITE)
                correctOption.setBackgroundColor(Color.WHITE)

                if (isAnswerCorrect) {
                    correctAnswers++
                    questionIndices.removeAt(0)
                } else {
                    incorrectTries++
                    questionIndices.add(questionIndices.removeAt(0))
                }

                if (questionIndices.isNotEmpty()) displayQuestion()
                else {
                    val efficiency =
                        (totalQuestions.toFloat() / (totalQuestions + incorrectTries).toFloat()) * 100
                    AlertDialog.Builder(this).setTitle("Test Report")
                        .setMessage("Efficiency: $efficiency%")
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                            finish()
                        }
                        .setCancelable(false)  // This ensures the dialog can't be dismissed without pressing "OK"
                        .show()
                }
            }, 1000)  // Delay for 1 second
        }
    }

    private fun displayQuestion() {
        Log.i(tag, flashcards.toString())
        if (questionIndices.isEmpty()) return
        val currentFlashcard = flashcards[questionIndices.first()]
        questionTextView.text = currentFlashcard.question

        val options = generateOptions(currentFlashcard.answer)
        if (options.size < 4) {
            // Handle error: Not enough options generated
            return
        }
        // Array of RadioButton IDs
        val radioButtonIds = arrayOf(R.id.option1, R.id.option2, R.id.option3, R.id.option4)

        // Set options to RadioButtons and find correct option
        var correctRadioButton: RadioButton? = null
        for (i in options.indices) {
            val radioButton = findViewById<RadioButton>(radioButtonIds[i])
            radioButton.text = options[i]
            if (options[i] == currentFlashcard.answer) {
                correctRadioButton = radioButton
            }
        }

        // If none matched (though it shouldn't happen), you can default to any RadioButton, or handle the error differently
        correctOption = correctRadioButton ?: findViewById(R.id.option4)
    }

    private fun generateOptions(correctAnswer: String): List<String> {
        val wrongAnswers =
            flashcards.filter { it.answer != correctAnswer }.shuffled().take(3).map { it.answer }

        return (wrongAnswers + correctAnswer).shuffled() // Mix the correct answer with the wrong ones
    }
}
