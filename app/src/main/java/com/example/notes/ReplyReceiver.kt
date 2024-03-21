package com.example.notes

import android.annotation.SuppressLint
import android.app.Notification
import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat

class ReplyReceiver: BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context?, intent: Intent?) {

        getMessageText(intent)

//        val remoteInput = RemoteInput.getResultsFromIntent(intent)
//        if (remoteInput != null) {
//            val replyText = remoteInput.getCharSequence("key_text_reply")
//            Toast.makeText(context, "Reply :$replyText", Toast.LENGTH_SHORT).show()
//        }
    }

    private fun getMessageText(intent: Intent?): CharSequence {
        Log.d("TAG","reply input")
        return RemoteInput.getResultsFromIntent(intent).getCharSequence("key_text_reply").toString().trim()
    }
}
