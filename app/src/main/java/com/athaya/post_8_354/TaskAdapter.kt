package com.athaya.post_8_354

import Task
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.athaya.post_8_354.databinding.ItemTaskBinding

class TaskAdapter(
    private val tasks: List<Task>,
    private val onEdit: (Task) -> Unit,
    private val onDelete: (Task) -> Unit,
    private val onStatusChange: (Task, Boolean) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task) {
            binding.tvTitle.text = task.title
            binding.tvDesc.text = task.description
            binding.tvDate.text = task.deadline
            binding.cbTask.setOnCheckedChangeListener(null)
            binding.cbTask.isChecked = task.completed

            if (task.completed) {
                binding.tvTitle.paintFlags = binding.tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.tvTitle.alpha = 0.5f // Opsional: bikin agak transparan biar kelihatan selesai
            } else {
                binding.tvTitle.paintFlags = binding.tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                binding.tvTitle.alpha = 1.0f
            }

            itemView.setOnClickListener { onEdit(task) }
            binding.btnDelete.setOnClickListener { onDelete(task) }
            binding.cbTask.setOnClickListener {
                val newState = binding.cbTask.isChecked // Ambil status baru setelah diklik
                onStatusChange(task, newState)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount() = tasks.size
}