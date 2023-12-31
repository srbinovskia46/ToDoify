package com.example.todolist

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class ToDoAdapter(
    private val context: Context,
    private val todos: MutableList<Todo>,
    private val onEditClickListener: ((Todo) -> Unit)? = null
) : RecyclerView.Adapter<ToDoAdapter.TodoViewHolder>() {

    class TodoViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val tvTodoTitle: TextView = itemView.findViewById(R.id.tvTodoTitle)
        val cbDone: CheckBox = itemView.findViewById(R.id.cbDone)
        val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        return TodoViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_todo,
                parent,
                false
            )
        )
    }

    fun getTodoItems(): MutableList<Todo> {
        return todos
    }

    fun addTodo(todo: Todo) {
        todos.add(todo)
        notifyItemInserted(todos.size - 1)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun deleteDoneTodos() {
        todos.removeAll { todo ->
            todo.isChecked
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return todos.size
    }

    private fun updateTextViewStyle(tvTodoTitle: TextView, isChecked: Boolean) {
        if (isChecked) {
            tvTodoTitle.paintFlags = tvTodoTitle.paintFlags or STRIKE_THRU_TEXT_FLAG
        } else {
            tvTodoTitle.paintFlags = tvTodoTitle.paintFlags and STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    private fun toggleStrikeThrough(todo: Todo, tvTodoTitle: TextView){
        todo.isChecked = !todo.isChecked
        updateTextViewStyle(tvTodoTitle, todo.isChecked)
        saveListToInternalStorage(context)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val curTodo = todos[position]
        holder.itemView.apply {
            holder.tvTodoTitle.text = curTodo.title
            holder.cbDone.isChecked = curTodo.isChecked
            updateTextViewStyle(holder.tvTodoTitle, curTodo.isChecked)

            holder.cbDone.setOnCheckedChangeListener { _, isChecked ->
                toggleStrikeThrough(curTodo, holder.tvTodoTitle)
                curTodo.isChecked = isChecked
                saveListToInternalStorage(context)
            }

            holder.btnEdit.setOnClickListener {
                onEditClickListener?.invoke(curTodo)
            }
        }
    }


    fun saveListToInternalStorage(context: Context) {
        try {
            val fileOutputStream = context.openFileOutput("todolist.txt", Context.MODE_PRIVATE)
            val objectOutputStream = ObjectOutputStream(fileOutputStream)
            objectOutputStream.writeObject(todos)
            objectOutputStream.close()
            fileOutputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun loadListToInternalStorage(context: Context) {
        try {
            val fileInputStream = context.openFileInput("todolist.txt")
            val objectInputStream = ObjectInputStream(fileInputStream)
            val tmpList = objectInputStream.readObject()
            if (tmpList is List<*>) {
                todos.clear()
                for (item in tmpList) {
                    if (item is Todo) {
                        todos.add(item)
                    }
                }
            }
            fileInputStream.close()
            objectInputStream.close()
            notifyDataSetChanged()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
    }

    fun addFirst(todo: Todo){
        todos.add(0, todo)
        notifyItemInserted(0)
    }

    fun reverseOrderOfItems(){
        todos.reverse()
    }
}