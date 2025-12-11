package com.athaya.post_8_354

import Task
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.athaya.post_8_354.databinding.ActivityMainBinding
import com.athaya.post_8_354.databinding.DialogTaskBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: DatabaseReference
    private val taskList = mutableListOf<Task>()
    private lateinit var adapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Firebase
        database = FirebaseDatabase.getInstance().getReference("tasks")

        setupRecyclerView()
        setupFab()
        fetchData()
    }

    private fun setupRecyclerView() {
        adapter = TaskAdapter(taskList,
            onEdit = { task -> showTaskDialog(task) },
            onDelete = { task -> deleteTask(task) },
            onStatusChange = { task, isChecked -> toggleTaskStatus(task, isChecked) }
        )
        binding.rvTasks.layoutManager = LinearLayoutManager(this)
        binding.rvTasks.adapter = adapter
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            showTaskDialog(null) // null artinya Tambah Baru
        }
    }

    // Fungsi Fetch Data Realtime
    private fun fetchData() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                taskList.clear()
                for (data in snapshot.children) {
                    val task = data.getValue(Task::class.java)
                    task?.let { taskList.add(it) }
                }

                taskList.sortWith(compareBy<Task> { it.completed }.thenBy { it.deadline })

                adapter.notifyDataSetChanged()
                updateEmptyState()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Logika Poin 2: Tampilkan gambar jika list kosong
    private fun updateEmptyState() {
        if (taskList.isEmpty()) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.rvTasks.visibility = View.GONE
        } else {
            binding.layoutEmptyState.visibility = View.GONE
            binding.rvTasks.visibility = View.VISIBLE
        }
    }

    // Fungsi Dialog untuk Tambah (Create) dan Edit (Update) - Poin 4
    private fun showTaskDialog(task: Task?) {
        val dialogBinding = DialogTaskBinding.inflate(LayoutInflater.from(this))
        val isEdit = task != null

        // Set judul dialog
        dialogBinding.tvDialogTitle.text = if (isEdit) "Edit Tugas" else "Tambah Tugas Baru"

        // Jika Edit, isi form dengan data lama
        if (isEdit) {
            dialogBinding.etTitle.setText(task?.title)
            dialogBinding.etDesc.setText(task?.description)
            dialogBinding.etDate.setText(task?.deadline)
        }

        // Setup DatePicker
        dialogBinding.etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(this,
                { _, year, month, dayOfMonth ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(year, month, dayOfMonth)
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    dialogBinding.etDate.setText(dateFormat.format(selectedDate.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            // Logika Poin 5: Tidak bisa pilih hari sebelum hari ini
            datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
            datePicker.show()
        }

        MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setPositiveButton("Simpan") { _, _ ->
                val title = dialogBinding.etTitle.text.toString().trim()
                val desc = dialogBinding.etDesc.text.toString().trim()
                val date = dialogBinding.etDate.text.toString().trim()

                // Logika Poin 3: Validasi Judul & Deadline
                if (title.isEmpty() || date.isEmpty()) {
                    Toast.makeText(this, "Judul dan Deadline harus diisi!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (isEdit) {
                    // Update Data
                    val updatedTask = task!!.copy(title = title, description = desc, deadline = date)
                    database.child(task.id!!).setValue(updatedTask)
                        .addOnSuccessListener { Toast.makeText(this, "Tugas diperbarui", Toast.LENGTH_SHORT).show() } // Toast Update [cite: 77]
                } else {
                    // Create Data
                    val id = database.push().key
                    val newTask = Task(id, title, desc, date, false)
                    id?.let { database.child(it).setValue(newTask) }
                    // Toast default Firebase success akan mentrigger onDataChange
                }
            }
            .setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    // Logika Hapus (Poin 4: CRUD & Toast)
    private fun deleteTask(task: Task) {
        task.id?.let {
            database.child(it).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Tugas dihapus", Toast.LENGTH_SHORT).show() // [cite: 71]
                }
        }
    }

    // Logika Checkbox (Poin 6)
    private fun toggleTaskStatus(task: Task, isChecked: Boolean) {
        task.id?.let {
            database.child(it).child("completed").setValue(isChecked)
        }
    }
}