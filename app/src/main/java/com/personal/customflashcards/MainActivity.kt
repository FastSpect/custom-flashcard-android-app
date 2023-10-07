package com.personal.customflashcards

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import java.io.File

class MainActivity : ComponentActivity() {

    private val tag = "MainActivity"
    private val REQUEST_CODE = 1234

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.READ_MEDIA_IMAGES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES), 1
            )
        }

        val createFlashCardButton: Button = findViewById(R.id.createFlashcardButton)
        val viewFlashcardsButton: Button = findViewById(R.id.viewFlashcardsButton)
        val settingsButton: Button = findViewById(R.id.settingsButton)


        createFlashCardButton.setOnClickListener {
            val createIntent = Intent(this, CreateFlashcardActivity::class.java)
            startActivity(createIntent)
        }

        viewFlashcardsButton.setOnClickListener {
            val viewIntent = Intent(this, ViewFlashcardsActivity::class.java)
            startActivity(viewIntent)
        }

        settingsButton.setOnClickListener {
            val viewIntent = Intent(this, SettingsActivity::class.java)
            startActivity(viewIntent)
        }
    }

    fun onImportClicked(view: View) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val uriTree: Uri? = data?.data
            var importedCount = 0

            // Persist permissions.
            val takeFlags: Int =
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            contentResolver.takePersistableUriPermission(uriTree!!, takeFlags)

            val documentFile = DocumentFile.fromTreeUri(this, uriTree)
            documentFile?.listFiles()?.forEach { file ->
                if (file.isFile) {
                    val content = contentResolver.openInputStream(file.uri)?.bufferedReader()
                        .use { it?.readText() }
                    if (saveToFlashcardsDirectory(
                            content, file.name ?: "default.txt"
                        )
                    ) importedCount++

                }
            }
            Toast.makeText(this, "$importedCount files imported successfully!", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun saveToFlashcardsDirectory(content: String?, filename: String): Boolean {
        if (content == null) return false

        // Specify the directory and filename
        val directory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "Flashcards"
        )
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val file = File(directory, filename)
        if (file.exists()) {
            // Possibly log or show a toast that the file already exists and will be skipped
            Log.d(tag, "File $filename already exists, skipping.")
            return false
        }
        file.writeText(content)
        return true
    }
}


