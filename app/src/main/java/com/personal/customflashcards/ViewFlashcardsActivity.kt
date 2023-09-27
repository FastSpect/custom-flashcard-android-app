package com.personal.customflashcards

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class ViewFlashcardsActivity : AppCompatActivity() {

    private val tag = "ViewFlashcardsActivity"

    private lateinit var setsRecyclerView: RecyclerView
    private val setNames = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_flashcards)

        setsRecyclerView = findViewById(R.id.setsRecyclerView)

        setNames.addAll(loadFlashcards())

        val setNameAdapter = SetNameAdapter(setNames) { setName ->
            val intent = Intent(this@ViewFlashcardsActivity, FlashcardDetailActivity::class.java)
            intent.putExtra("setName", setName)
            startActivity(intent)
        }

        setsRecyclerView.layoutManager = LinearLayoutManager(this)
        setsRecyclerView.adapter = setNameAdapter
    }

    override fun onResume() {
        super.onResume()
        setNames.clear()  // Clear the previous list
        setNames.addAll(loadFlashcards())  // Reload the list from SharedPreferences
        setsRecyclerView.adapter?.notifyDataSetChanged()  // Notify the adapter to refresh the list
    }

    private fun loadFlashcards(): List<String> {
        val filenames = mutableListOf<String>()

        val projection = arrayOf(MediaStore.Files.FileColumns.DISPLAY_NAME)

        // Filter results to show only .txt files in "Documents/Flashcards"
        val selection =
            "${MediaStore.Files.FileColumns.RELATIVE_PATH} LIKE ? AND ${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%Documents/Flashcards%", "%.txt")

        contentResolver.query(
            MediaStore.Files.getContentUri("external"), projection, selection, selectionArgs, null
        )?.use { cursor ->
            val displayNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val filename = cursor.getString(displayNameColumn)
                filenames.add(filename.substringBefore('.'))
            }
        }

        return filenames
    }

}

class SetNameAdapter(
    private val setNames: List<String>, private val onClick: (String) -> Unit
) : RecyclerView.Adapter<SetNameAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val setNameTextView: TextView = itemView.findViewById(R.id.setNameTextView)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val setName = setNames[position]
                    onClick(setName)
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
        val currentSetName = setNames[position]
        holder.setNameTextView.text = currentSetName
    }

    override fun getItemCount() = setNames.size
}


