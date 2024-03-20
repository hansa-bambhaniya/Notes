package com.example.notes.model

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface NotesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
     suspend fun insert(item: List<Notes?>)

    @Delete
     suspend fun delete(notes: Notes)

    @Update
    suspend fun update(notes: Notes)

    @Query("SELECT * FROM notes_table order by id ASC ")
    fun getAllItemList(): LiveData<List<Notes>>

}