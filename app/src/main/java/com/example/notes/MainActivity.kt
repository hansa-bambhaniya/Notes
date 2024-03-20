package com.example.notes

import com.example.notes.fragment.AllFragment
import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.notes.databinding.ActivityMainBinding
import com.example.notes.model.Notes
import com.example.notes.model.NotesRoomDatabase
import com.example.notes.viewmodel.NotesViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: NotesViewModel
    private lateinit var roomDatabase: NotesRoomDatabase

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize firebase
        FirebaseApp.initializeApp(this)

        // Initialize Room database
        roomDatabase = Room.databaseBuilder(applicationContext,NotesRoomDatabase::class.java,"notes_database").build()

        supportFragmentManager.beginTransaction()
            .add(R.id.container, AllFragment())
            .commit()

        viewModel = ViewModelProvider(this).get(NotesViewModel::class.java)
        binding.bottomNavigationView.background = null

        binding.fab.setOnClickListener {
            val searchView = findViewById<SearchView>(R.id.search_view)
            if (searchView.hasFocus()){
                searchView.clearFocus()
            }
            val builder = AlertDialog.Builder(this)
            builder.setView(R.layout.dialog_insert)
            val dialog = builder.create()
            dialog.window?.decorView?.setBackgroundResource(R.drawable.alert_dialog_corner)
            dialog.show()

            val cancel = dialog.findViewById<TextView>(R.id.cancel)
            val create = dialog.findViewById<TextView>(R.id.create)

            create?.setOnClickListener {
                val edit = dialog.findViewById<EditText>(R.id.edit_task)
                val notesName = edit?.text.toString()

                if (notesName.isEmpty()) {
                    edit?.error = "please enter notes"
                } else {
                    // Insert data into firebase realtime database
                    writeNewUser(0, notesName,isCompleted = false)
                    dialog.dismiss()
                }
            }
            cancel?.setOnClickListener {
                dialog.dismiss()
            }
        }
        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.all -> {
                    viewModel.changeTitleList(NotesViewModel.ClickType.All)
                    true
                }

                R.id.completed -> {
                    viewModel.changeTitleList(NotesViewModel.ClickType.Completed)
                    true
                }

                R.id.pending -> {
                    viewModel.changeTitleList(NotesViewModel.ClickType.Pending)
                    true
                }
                else -> {
                    false
                }
            }
        }
    }
    private fun writeNewUser(id: Int, notesName: String,isCompleted: Boolean) {
        val firebaseDatabase = FirebaseDatabase.getInstance()
        val myRef = firebaseDatabase.getReference("Notes")

        val userId = myRef.push().key
        val notes = Notes(id,notesName,userId,isCompleted)

        Log.d("TAG","Insert item: $notes")
        myRef.child(userId!!).setValue(notes)
            .addOnCompleteListener {
                Toast.makeText(this,"success",Toast.LENGTH_SHORT).show()
            }
            .addOnCanceledListener {
                Toast.makeText(this,"cancel",Toast.LENGTH_SHORT).show()
            }
    }

    override fun onBackPressed() {
        val searchView = findViewById<SearchView>(R.id.search_view)
        if (searchView.hasFocus()){
            searchView.clearFocus()
        }else {
            super.onBackPressed()
        }
    }
}