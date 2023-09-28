package com.personal.customflashcards

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
import androidx.appcompat.app.AlertDialog
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
    private var totalTries = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        questionTextView = findViewById(R.id.questionTextView)
        optionsRadioGroup = findViewById(R.id.optionsRadioGroup)
        nextButton = findViewById(R.id.nextButton)

        flashcards =
            intent.getSerializableExtra("flashcards", ArrayList::class.java) as? List<Flashcard>
                ?: listOf()

        questionIndices = flashcards.indices.toMutableList().shuffled().toMutableList()

        displayQuestion()

        nextButton.setOnClickListener {
            totalTries++
            val selectedOptionId = optionsRadioGroup.checkedRadioButtonId

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
                optionsRadioGroup.clearCheck()
                selectedOption.setBackgroundColor(Color.WHITE)
                correctOption.setBackgroundColor(Color.WHITE)

                if (isAnswerCorrect) questionIndices.removeAt(0)
                else questionIndices.add(questionIndices.removeAt(0))

                if (questionIndices.isNotEmpty()) displayQuestion()
                else {
                    // End of questions. Calculate efficiency and show the toast.
                    val efficiency = (flashcards.size.toDouble() / totalTries) * 100
                    val alertDialog = AlertDialog.Builder(this).setTitle("Test Results")
                        .setMessage("Efficiency: ${String.format("%.2f", efficiency)}%")
                        .setPositiveButton("OK") { _, _ ->
                            finish()
                        }.create()

                    alertDialog.show()
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
        val option1: RadioButton = findViewById(R.id.option1)
        val option2: RadioButton = findViewById(R.id.option2)
        val option3: RadioButton = findViewById(R.id.option3)
        val option4: RadioButton = findViewById(R.id.option4)

        option1.text = options[0]
        option2.text = options[1]
        option3.text = options[2]
        option4.text = options[3]

        correctOption = when (currentFlashcard.answer) {
            options[0] -> option1
            options[1] -> option2
            options[2] -> option3
            else -> option4
        }
    }

    private fun generateOptions(correctAnswer: String): List<String> {
        val wrongAnswers =
            flashcards.filter { it.answer != correctAnswer }.shuffled().take(3).map { it.answer }

        return (wrongAnswers + correctAnswer).shuffled() // Mix the correct answer with the wrong ones
    }
}
