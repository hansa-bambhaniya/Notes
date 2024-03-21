package com.example.notes.adapter

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.recyclerview.widget.RecyclerView
import com.example.notes.BubbleActivity
import com.example.notes.MainActivity
import com.example.notes.model.Notes
import com.example.notes.R
import com.example.notes.ReplyReceiver
import com.example.notes.fragment.AllFragment

class AllAdapter(
    private var notesList: MutableList<Notes>,
    private val notesDeleteInterface: NotesDeleteInterface,
    private var itemClickInterface: ItemClickInterface,
    private var notesEditInterface: NotesEditInterface,
):RecyclerView.Adapter<RecyclerView.ViewHolder>(),Filterable {

    private var isAll: Boolean = true
    private var filteredItems: List<Notes> = notesList

    val CHANNEL_ID = "channelID"
    val CHANNEL_NAME = "channelName"
    var NOTIF_ID = 1

    companion object {
        const val VIEW_TYPE_ONE = 1 // All
        const val VIEW_TYPE_TWO = 2 // Completed
        const val VIEW_TYPE_THREE = 3 // Pending
    }

    class AllTypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val check: CheckBox = itemView.findViewById(R.id.check)
        val task: TextView = itemView.findViewById(R.id.text)
        val delete: ImageView = itemView.findViewById(R.id.delete)
        val edit: ImageView = itemView.findViewById(R.id.edit_task)
    }
    class CompletedTypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val check: CheckBox = itemView.findViewById(R.id.check)
        val task:TextView = itemView.findViewById(R.id.txt_completed)
    }
    class PendingTypeViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val task: TextView = itemView.findViewById(R.id.txt_pending)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType != VIEW_TYPE_ONE) {
            if (viewType == VIEW_TYPE_TWO) {
                CompletedTypeViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.row_completed_list, parent, false)
                )
            } else {
                PendingTypeViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.row_pending_list, parent, false)
                )
            }
        } else {
            AllTypeViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.row_all_list, parent, false)
            )
        }
    }
    override fun getItemCount(): Int {
        return filteredItems.size
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = filteredItems[position]
        if (getItemViewType(position) == VIEW_TYPE_ONE) {
            val listItem = holder as AllTypeViewHolder
            // Hide the keyboard
            listItem.itemView.setOnClickListener {
                hideKeyboard(listItem.itemView)
            }

            listItem.delete.setOnClickListener {
                notesDeleteInterface.onDeleteIcon(notesList[position])
            }

            listItem.edit.setOnClickListener {
                notesEditInterface.onEditIcon(notesList[position])
            }
            listItem.task.text = item.notesName

            listItem.check.setOnCheckedChangeListener { _, isChecked ->
                item.isCompleted = isChecked
               if (isChecked){
                   createNotificationChannel(listItem.check.context)
                   showReplyMessage(listItem.check.context,"Item is completed : ${item.notesName}")

               }
                itemClickInterface.onItemChecked(notesList[position])
            }
            listItem.check.isChecked = item.isCompleted
            if (item.isCompleted){
                listItem.task.paintFlags = listItem.task.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                listItem.task.setTextColor(Color.BLACK)
            }else{
                listItem.task.paintFlags = listItem.task.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                listItem.task.setTextColor(Color.BLACK)
            }
            listItem.check.setOnClickListener {
                itemClickInterface.checkClick(isChecked = false)
            }
        }else if (getItemViewType(position)== VIEW_TYPE_TWO){
            val checkItem = holder as CompletedTypeViewHolder
            // Hide the keyboard
            checkItem.itemView.setOnClickListener {
                hideKeyboard(checkItem.itemView)
            }
            checkItem.task.text = item.notesName
                if (item.isCompleted){
                        checkItem.task.paintFlags = checkItem.task.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                        checkItem.task.setTextColor(Color.BLACK)
                }
                else {
                        checkItem.task.paintFlags = checkItem.task.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG
                        checkItem.task.setTextColor(Color.BLACK )
                }
                    checkItem.check.isChecked = true
                    checkItem.check.isClickable = false
        }else{
                val pendingItem = holder as PendingTypeViewHolder
                // Hide the keyboard
                pendingItem.itemView.setOnClickListener {
                    hideKeyboard(pendingItem.itemView)
                }
                pendingItem.task.text = item.notesName
        }
    }
    override fun getItemViewType(position: Int): Int {
        return if (isAll){
            1
        } else if (notesList[position].isCompleted){
            2
        } else{
            3
        }
    }

    fun setAllClick(i: Boolean) {
            isAll = i
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateList(list:List<Notes>){
        this.notesList.clear()
        this.notesList.addAll(list)
        notifyDataSetChanged()
    }

    // Create notification channel
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    // Create reply notification
    @SuppressLint("MissingPermission")
    fun showReplyMessage(context: Context, Text: String) {

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0,intent, PendingIntent.FLAG_MUTABLE)

        // Direct reply message notification
        val directReplyIntent = Intent(context,ReplyReceiver::class.java)

        val directReplyPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            directReplyIntent,
            PendingIntent.FLAG_MUTABLE)

        // create a RemoteInput for handling the text input
        val remoteInput = androidx.core.app.RemoteInput.Builder("key_text_reply")
            .setLabel("types...")
            .build()

        // create notification action with ReplyInput
        val replyAction = NotificationCompat.Action.Builder(
            R.drawable.ic_reply,
            "Reply",
            directReplyPendingIntent
        ).addRemoteInput(remoteInput)
            .build()

        // create notification messageStyle
        val person = Person.Builder().setName("Notification").build()
        val notificationStyle = NotificationCompat.MessagingStyle(person)
            .addMessage("hyy", System.currentTimeMillis(),person)

        val build = NotificationCompat.Builder(context,CHANNEL_ID)
            .setSmallIcon(R.drawable.notifications_icon)
            .setContentTitle("Notification")
            .setContentText(Text)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(replyAction)
            .setAutoCancel(true)
            .build()

        // show the reply notification
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(NOTIF_ID,build)
    }
    
    // Search filter list
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSearch: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                if (charSearch.isNullOrEmpty()) {
                    filterResults.values = notesList
                    Log.d("TAG","isEmpty: $notesList")
                } else {
                    val filteredList = notesList.filter { it.notesName!!.startsWith(charSearch, true) }
                    filterResults.values = filteredList
                    Log.d("TAG","filterList of all:$filteredList")
                }
                return filterResults
            }
            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(charSearch: CharSequence?, results: FilterResults?) {
                filteredItems = results?.values as List<Notes>
                notifyDataSetChanged()
            }
        }
    }
     private fun hideKeyboard(view: View){
         val i = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
         i.hideSoftInputFromWindow(view.windowToken,0)
         Log.d("TAG","keyboard close")
    }
}


interface NotesDeleteInterface{
    fun onDeleteIcon(notes: Notes)
}
interface ItemClickInterface {
    fun onItemChecked(notes: Notes)
    fun checkClick(isChecked: Boolean)
}
interface NotesEditInterface{
    fun onEditIcon(items: Notes)
}
