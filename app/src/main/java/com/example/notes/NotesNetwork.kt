package com.example.notes

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class NotesNetwork :Application() {

    override fun onCreate() {
        super.onCreate()

        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}