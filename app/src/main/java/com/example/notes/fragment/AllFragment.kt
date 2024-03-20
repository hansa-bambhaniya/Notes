package com.example.notes.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.notes.DataSyncManager
import com.example.notes.adapter.ItemClickInterface
import com.example.notes.model.Notes
import com.example.notes.adapter.AllAdapter
import com.example.notes.adapter.NotesDeleteInterface
import com.example.notes.viewmodel.NotesViewModel
import com.example.notes.R
import com.example.notes.adapter.NotesEditInterface
import com.example.notes.model.NotesRoomDatabase
import com.google.firebase.FirebaseApp
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class AllFragment : Fragment(), NotesDeleteInterface, ItemClickInterface,NotesEditInterface {
    private lateinit var viewModel: NotesViewModel
    private lateinit var adapter: AllAdapter
    private lateinit var list: RecyclerView
    private lateinit var txt1: TextView
    private lateinit var txt2: TextView
    private lateinit var txt3: TextView
    private lateinit var txtName: TextView
    private lateinit var searchView: SearchView
    private lateinit var notesList: ArrayList<Notes>
    private lateinit var roomDatabase: NotesRoomDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view: View = inflater.inflate(R.layout.fragment_all_, container, false)

        list = view.findViewById(R.id.list)
        txt1 = view.findViewById(R.id.txt1)
        txt2 = view.findViewById(R.id.txt2)
        txt3 = view.findViewById(R.id.txt3)
        txtName = view.findViewById(R.id.task)
        searchView = view.findViewById(R.id.search_view)
        viewModel = ViewModelProvider(requireActivity()).get(NotesViewModel::class.java)

        // Initialize Firebase
        FirebaseApp.initializeApp(requireContext())

        val firebaseDatabase = FirebaseDatabase.getInstance()
        //Initialize Room Database
        roomDatabase = Room.databaseBuilder(requireContext(),NotesRoomDatabase::class.java,"notes_database").build()
        // sync firebase realtime database with room database
        val dataSyncManager = DataSyncManager(firebaseDatabase,roomDatabase)
        dataSyncManager.syncData()

        notesList = arrayListOf()
        list.layoutManager = LinearLayoutManager(context)
        adapter = AllAdapter(arrayListOf(), this, this,this)
        list.adapter = adapter
        searchView.setBackgroundResource(R.drawable.search_icon_background)
        viewModel.postClick.observe(viewLifecycleOwner, Observer{
            when(it.name){
                NotesViewModel.ClickType.All.name->{
                    // To close search view
                    if (searchView.hasFocus()){
                        searchView.clearFocus()
                    }
                    txtName.text = "All tasks"
                    adapter.setAllClick(true)
                    showData()
                    setupChildEventListener()

                    searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?): Boolean {
                            return false
                        }
                        override fun onQueryTextChange(newText: String?): Boolean {
                            adapter.filter.filter(newText)
                            return true
                        }
                    })
                }
                NotesViewModel.ClickType.Completed.name->{
                    // To close search view
                    if (searchView.hasFocus()){
                        searchView.clearFocus()
                    }

                    txtName.text = "Completed task"
                    adapter.setAllClick(false)

                    val firebaseDatabase = FirebaseDatabase.getInstance()
                    val myRef = firebaseDatabase.getReference("Notes")
                    var postListener: ValueEventListener?=null
                    postListener = object : ValueEventListener {
                        @SuppressLint("NotifyDataSetChanged")
                        override fun onDataChange(snapshot: DataSnapshot) {
                            notesList.clear()
                            for (dataSnapshot in snapshot.children) {
                                val item = dataSnapshot.getValue(Notes::class.java)
                                if (item != null) {
                                    notesList.add(item)
                                }
                            }
                            val completedList = notesList.filter { it.isCompleted }
                            adapter.updateList(completedList)
                            // Remove the previous listener
                            myRef.removeEventListener(postListener!!)
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Log.d("TAG", "onCancel....")
                        }
                    }
                    myRef.addValueEventListener(postListener)
                    searchView.setOnQueryTextListener(object :SearchView.OnQueryTextListener{
                        override fun onQueryTextSubmit(query: String?): Boolean {
                            return false
                        }
                        override fun onQueryTextChange(newText: String?): Boolean {
                            adapter.filter.filter(newText)
                            return true
                        }
                    })
                }
                NotesViewModel.ClickType.Pending.name-> {
                    // To close search view
                    if (searchView.hasFocus()){
                        searchView.clearFocus()
                    }
                    txtName.text = "Pending task"
                    adapter.setAllClick(false)

                    val firebaseDatabase = FirebaseDatabase.getInstance()
                    val myRef = firebaseDatabase.getReference("Notes")
                    var postListener: ValueEventListener?=null
                    postListener = object : ValueEventListener {
                        @SuppressLint("NotifyDataSetChanged")
                        override fun onDataChange(snapshot: DataSnapshot) {
                            notesList.clear()
                            for (dataSnapshot in snapshot.children) {
                                val item = dataSnapshot.getValue(Notes::class.java)
                                if (item != null) {
                                    notesList.add(item)
                                }
                            }
                            val pendingList = notesList.filterNot { it.isCompleted }
                            adapter.updateList(pendingList)
                            // Remove the previous listener
                            myRef.removeEventListener(postListener!!)
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Log.d("TAG", "onCancel....")
                        }
                    }
                    myRef.addValueEventListener(postListener)
                   searchView.setOnQueryTextListener(object :SearchView.OnQueryTextListener{
                       override fun onQueryTextSubmit(query: String?): Boolean {
                           return false
                       }
                       override fun onQueryTextChange(newText: String?): Boolean {
                           adapter.filter.filter(newText)
                           return true
                       }
                   })
                }
            }
        })
        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if(!hasFocus){
                searchView.isIconified = true
            }
        }
        list.addOnScrollListener(object :RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                val inputMethodManager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(view.windowToken,0)
            }
        })
        return view
    }


    @SuppressLint("SuspiciousIndentation")
    override fun onDeleteIcon(notes: Notes) {
        if (searchView.hasFocus()){
            searchView.clearFocus()
        }

        val builder = AlertDialog.Builder(context)
            builder.setView(R.layout.dialog_delete)
            val dialog = builder.create()
            dialog.window?.decorView?.setBackgroundResource(R.drawable.alert_dialog_corner)
            dialog.show()

            val cancel = dialog.findViewById<TextView>(R.id.d_cancel)
            val delete = dialog.findViewById<TextView>(R.id.d_delete)

            cancel?.setOnClickListener {
                dialog.dismiss()
            }
            delete?.setOnClickListener {
                // Delete into firebase realtime database
                FirebaseDatabase.getInstance().getReference("Notes").child(notes.userId!!).removeValue()
                Log.d("TAG","Delete item:$notes")
                Toast.makeText(context, "delete", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
    }

    private fun showData(){
        val firebaseDatabase = FirebaseDatabase.getInstance()
        val myRef = firebaseDatabase.getReference("Notes")

        var postListener: ValueEventListener?=null
        postListener = object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                notesList.clear()
                for (dataSnapshot in snapshot.children) {
                    val item = dataSnapshot.getValue(Notes::class.java)
                    if (item != null) {
                        notesList.add(item)
                    }
                }
                adapter.updateList(notesList)

                var allList = 0
                for (i in notesList){
                    allList++
                }
                var completed = 0
                var pending = 0
                for (i in notesList){
                    if (i.isCompleted){
                        completed++
                    }else{
                        pending++
                    }
                }
                txt1.text = "$allList task total"
                txt2.text = "$completed task completed"
                txt3.text = "$pending task pending"
                // Remove the previous listener
                myRef.removeEventListener(postListener!!)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("TAG", "onCancel....")
            }
        }
        myRef.addValueEventListener(postListener)
        myRef.keepSynced(true)
    }
    private fun setupChildEventListener(){
        val firebaseDatabase = FirebaseDatabase.getInstance()
        val myRef = firebaseDatabase.getReference("Notes")

        myRef.addChildEventListener(object :ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                showData()
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                showData()
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {
                showData()
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }


    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onItemChecked(notes: Notes) {
        val database = FirebaseDatabase.getInstance().getReference("Notes")
        database.child(notes.userId!!).setValue(notes)
        //viewModel.updateNotes(notes)
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun checkClick(isChecked: Boolean) {
        Log.d("TAG","isChecked item...")
        if (searchView.hasFocus()){
            searchView.clearFocus()
        }
    }
    override fun onEditIcon(items: Notes) {
        if (searchView.hasFocus()){
            searchView.clearFocus()
        }
        val builder = AlertDialog.Builder(context)
        builder.setView(R.layout.dialog_edit)
        val dialog = builder.create()
        dialog.window?.decorView?.setBackgroundResource(R.drawable.alert_dialog_corner)
        dialog.show()

        val update = dialog.findViewById<Button>(R.id.update)
        val edit = dialog.findViewById<EditText>(R.id.edit_task)
        val delete = dialog.findViewById<ImageView>(R.id.delete)

        edit.setText(items.notesName)
        update.setOnClickListener {
            val notesName = edit.text.toString()
            // Edit into firebase realtime database
            val database = FirebaseDatabase.getInstance().getReference("Notes").child(items.userId!!)
            database.child("notesName").setValue(notesName)

            Log.d("TAG","Update item: $notesName")
            Toast.makeText(context,"update",Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        delete.setOnClickListener {
            dialog.dismiss()
        }
    }
}

