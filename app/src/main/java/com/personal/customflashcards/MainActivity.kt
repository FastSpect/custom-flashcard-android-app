package com.personal.customflashcards

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.documentfile.provider.DocumentFile

class MainActivity : AppCompatActivity() {

    private val tag = "MainActivity"
    private val defaultRequestCode = 1234

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyThemeFromPreferences()
        setContentView(R.layout.activity_main)

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
        startActivityForResult(intent, defaultRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == defaultRequestCode && resultCode == RESULT_OK) {
            val uriTree: Uri? = data?.data
            var importedCount = 0

            // Persist permissions.
            val takeFlags: Int =
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            contentResolver.takePersistableUriPermission(uriTree!!, takeFlags)

            val documentFile = DocumentFile.fromTreeUri(this, uriTree)
            documentFile?.listFiles()?.forEach { file ->
                if (file.isFile && (file.name?.endsWith(".txt") == true || file.name?.endsWith(".json") == true)) {
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

    private fun applyThemeFromPreferences() {
        val sharedPref = getSharedPreferences("theme_prefs", MODE_PRIVATE)

        when (sharedPref.getInt("theme_mode", 1)) { // Default is system default
            0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    private fun saveToFlashcardsDirectory(content: String?, filename: String): Boolean {
        val mimeType = if (filename.endsWith(".json")) "application/json" else "text/plain"
        val values = ContentValues().apply {
            put(MediaStore.Files.FileColumns.DISPLAY_NAME, filename)
            put(MediaStore.Files.FileColumns.MIME_TYPE, mimeType)
            put(MediaStore.Files.FileColumns.RELATIVE_PATH, "Documents/Flashcards")
        }

        val uri = contentResolver.insert(
            MediaStore.Files.getContentUri("external"), values
        ) ?: return false

        contentResolver.openOutputStream(uri)?.use {
            it.write(content?.toByteArray())
        }

        return true
    }
}
