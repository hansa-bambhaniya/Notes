package com.example.notes.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.notes.model.Notes
import com.example.notes.model.NotesRepository
import com.example.notes.model.NotesRoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotesViewModel(application: Application): AndroidViewModel(application) {

    enum class ClickType{
        All,
        Completed,
        Pending
    }
    var allNotes: LiveData<List<Notes>>
    private var repository: NotesRepository
    var postClick = MutableLiveData(ClickType.All)

    init {
        val dao = NotesRoomDatabase.getDatabase(application).getNotesDao()
        repository = NotesRepository(dao)
        allNotes = repository.allNotes
    }

//    fun addNotes(notes: Notes) = viewModelScope.launch(Dispatchers.IO) {
//        repository.insert(notes)
//    }

    fun deleteNotes(notes: Notes) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(notes)
    }

    fun updateNotes(notes: Notes) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(notes)
    }

    fun changeTitleList(clickType: ClickType){
        postClick.postValue(clickType)
    }
}