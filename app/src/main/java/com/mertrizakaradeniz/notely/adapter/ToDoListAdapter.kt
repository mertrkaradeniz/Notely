package com.mertrizakaradeniz.notely.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mertrizakaradeniz.notely.R
import com.mertrizakaradeniz.notely.data.model.Priority
import com.mertrizakaradeniz.notely.data.model.ToDo
import com.mertrizakaradeniz.notely.databinding.RowItemBinding

class ToDoListAdapter : RecyclerView.Adapter<ToDoListAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: RowItemBinding) : RecyclerView.ViewHolder(binding.root)

    private val differCallback = object : DiffUtil.ItemCallback<ToDo>() {
        override fun areItemsTheSame(oldItem: ToDo, newItem: ToDo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ToDo, newItem: ToDo): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, differCallback)
    var toDoList: List<ToDo>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RowItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentToDo = toDoList[position]

        holder.binding.apply {
            tvTitle.text = currentToDo.title
            tvDescription.text = currentToDo.description

            when (currentToDo.priority) {
                Priority.HIGH -> {
                    priorityIndicator.setCardBackgroundColor(priorityIndicator.context.getColor(R.color.red))
                }
                Priority.MEDIUM -> {
                    priorityIndicator.setCardBackgroundColor(priorityIndicator.context.getColor(R.color.yellow))
                }
                Priority.LOW -> {
                    priorityIndicator.setCardBackgroundColor(priorityIndicator.context.getColor(R.color.green))
                }
            }
            root.setOnClickListener {
                onItemClickListener?.let { it(currentToDo) }
            }
        }
    }

    override fun getItemCount() = toDoList.size

    private var onItemClickListener: ((ToDo) -> Unit)? = null

    fun setOnItemClickListener(listener: (ToDo) -> Unit) {
        onItemClickListener = listener
    }
}
