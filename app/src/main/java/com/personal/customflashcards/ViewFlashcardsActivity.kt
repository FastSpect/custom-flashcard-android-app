package com.personal.customflashcards

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File


class ViewFlashcardsActivity : AppCompatActivity() {

    private val tag = "ViewFlashcardsActivity"

    private lateinit var setsRecyclerView: RecyclerView
    private val setFiles = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_flashcards)

        setsRecyclerView = findViewById(R.id.setsRecyclerView)

        setFiles.addAll(loadFlashcards())

        val setNameAdapter = SetNameAdapter(setFiles) { fileName ->
            val intent = Intent(this@ViewFlashcardsActivity, FlashcardDetailActivity::class.java)
            intent.putExtra("fileName", fileName)
            // For backward compatibility if needed, or just use fileName
            intent.putExtra("setName", fileName.substringBeforeLast('.'))
            startActivity(intent)
        }

        setsRecyclerView.layoutManager = LinearLayoutManager(this)
        setsRecyclerView.adapter = setNameAdapter
    }

    override fun onResume() {
        super.onResume()
        setFiles.clear()
        setFiles.addAll(loadFlashcards())
        setsRecyclerView.adapter?.notifyDataSetChanged()
    }

    private fun loadFlashcards(): List<String> {
        val filenames = mutableListOf<String>()
        val directory = File(Environment.getExternalStorageDirectory(), "Documents/Flashcards")
        directory.listFiles()?.forEach { f ->
            if (f.isFile && (f.name.endsWith(".txt") || f.name.endsWith(".json"))) {
                filenames.add(f.name)
            }
        }
        Log.d(tag, "${filenames.size} Flashcard files found: $filenames")
        return filenames
    }

}

class SetNameAdapter(
    private val setFiles: List<String>, private val onClick: (String) -> Unit
) : RecyclerView.Adapter<SetNameAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val setNameTextView: TextView = itemView.findViewById(R.id.setNameTextView)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val fileName = setFiles[position]
                    onClick(fileName)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.setname_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fileName = setFiles[position]
        holder.setNameTextView.text = fileName.substringBeforeLast('.')
    }

    override fun getItemCount() = setFiles.size
}
