package com.leonchik.android.lab8

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import database.TDLDatabase

class TaskListFragment : Fragment(R.layout.fragment_task_list){

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TaskAdapter

    private val taskListViewModel: TaskListViewModel by viewModels {
        TaskFactory(TDLDatabase.getDatabase(requireContext()).taskDao())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView=view.findViewById(R.id.task_recycler_view)
        recyclerView.layoutManager= LinearLayoutManager(context)
        adapter = TaskAdapter(mutableListOf())
        recyclerView.adapter = adapter
        taskListViewModel.tasks.observe(viewLifecycleOwner){
                tasks->adapter.updateTasks(tasks)
        }
        taskListViewModel.getTasks()

        swipeToDel()
    }

    private inner class TaskHolder(view: View) : RecyclerView.ViewHolder(view) {
        private lateinit var task: Task
        private val taskContent: TextView = itemView.findViewById(R.id.taskContent)
        private val taskCircle: FrameLayout = itemView.findViewById(R.id.priorityCircle)
        private val taskNum: TextView = itemView.findViewById(R.id.priorityNumber)

        fun bind(task: Task) {
            this.task = task
            taskContent.text = task.content
            when (task.priority) {
                1 -> taskCircle.backgroundTintList = ColorStateList.valueOf(Color.RED)
                2 -> taskCircle.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                3 -> taskCircle.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
            }
            taskNum.text = task.priority.toString()
        }
    }
    private inner class TaskAdapter(var tasks: MutableList<Task>) : RecyclerView.Adapter<TaskHolder>() {

        override fun onCreateViewHolder(parent : ViewGroup, viewType: Int) : TaskHolder {
            val view = layoutInflater.inflate(R.layout.list_item_task, parent, false)
            return TaskHolder(view)
        }

        override fun getItemCount() = tasks.size

        override fun onBindViewHolder(holder : TaskHolder, position: Int) {
            val task = tasks[position]
            holder.bind(task)
        }

        @SuppressLint("NotifyDataSetChanged")
        fun updateTasks(newTasks: List<Task>) {
            tasks.clear()
            tasks.addAll(newTasks)
            notifyDataSetChanged()
        }

    }

    private fun swipeToDel()
    {
        val itemTouchHelper = object : ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)
        {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) : Boolean {
                return false
            }
            override  fun  onSwiped(viewHolder: RecyclerView.ViewHolder, direction:Int)
            {
                val position=viewHolder.adapterPosition
                val taskToDel=adapter.tasks[position]
                taskListViewModel.delTask(taskToDel)
            }
        }
        ItemTouchHelper(itemTouchHelper).attachToRecyclerView(recyclerView)
    }

    override fun onResume() {
        super.onResume()
        taskListViewModel.getTasks()
    }

    companion object
    {
        fun newInstance()=TaskListFragment()
    }
}