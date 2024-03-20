package com.example.notes.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Notes::class], version = 2, exportSchema = false)
abstract class NotesRoomDatabase: RoomDatabase() {
    abstract fun getNotesDao(): NotesDao

    companion object{
        @Volatile
        private var INSTANCE: NotesRoomDatabase? = null

        fun getDatabase(context: Context): NotesRoomDatabase {
            return INSTANCE ?: synchronized(this){

                val instance = Room.databaseBuilder(context.applicationContext,
                    NotesRoomDatabase::class.java,"notes_database").fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}