package com.example.notes.model

import androidx.lifecycle.LiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class NotesRepository(private val notesDao: NotesDao) {
    val allNotes: LiveData<List<Notes>> = notesDao.getAllItemList()

//    suspend fun insert(notes: Notes){
//        notesDao.insert(notes)
//    }

    suspend fun delete(notes: Notes){
        notesDao.delete(notes)
    }

    suspend fun update(notes: Notes){
        notesDao.update(notes)
    }

}
