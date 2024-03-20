package com.example.notes

import com.example.notes.model.Notes
import com.example.notes.model.NotesRoomDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DataSyncManager(private val firebaseDatabase: FirebaseDatabase,private val roomDatabase: NotesRoomDatabase) {

    fun syncData(){
        val myRef = firebaseDatabase.getReference("Notes")

        myRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val newDataList = snapshot.children.map { it.getValue(Notes::class.java) }
                GlobalScope.launch {
                    roomDatabase.clearAllTables()
                    roomDatabase.getNotesDao().insert(newDataList)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}