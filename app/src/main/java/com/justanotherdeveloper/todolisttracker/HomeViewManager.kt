package com.justanotherdeveloper.todolisttracker

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.size
import kotlinx.android.synthetic.main.fragment_home.*
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("InflateParams, SetTextI18n")
class HomeViewManager(private val fragment: HomeFragment) {

    private val paramsWrapHeight = fragment.tasksScrollView.layoutParams
    private val paramsScrollHeight = fragment.fixedScrollView.layoutParams

    private var fixedScrollHeight = 0

    fun setFixedScrollHeight() {
        fixedScrollHeight = fragment.fixedScrollView.height
    }

    fun toggleTaskOptionsVisibility() {
        val tasksOptionsAreVisible = fragment.tasksOptionsLayout.visibility == View.VISIBLE
        fragment.activity?.highlightClickedView(fragment.tasksOptionsToggle)

        beginTransition(fragment.homeFragmentParent)
        if(tasksOptionsAreVisible) {
            fragment.tasksOptionsToggleText.text = fragment.getString(R.string.showTasksOptionsString)
            fragment.tasksOptionsToggleArrow.setImageResource(R.drawable.ic_keyboard_arrow_down_option)
            fragment.tasksOptionsLayout.visibility = View.GONE
        } else {
            fragment.tasksOptionsToggleText.text = fragment.getString(R.string.hideTasksOptionsString)
            fragment.tasksOptionsToggleArrow.setImageResource(R.drawable.ic_keyboard_arrow_up_option)
            fragment.tasksOptionsLayout.visibility = View.VISIBLE
        }
    }

    fun addCompletedActivityView(task: Task, indexToAdd: Int,
                                 dividerLineVisibility: Int) {
        beginTransition(fragment.homeFragmentParent)
        fragment.noCompletedTasksText.visibility = View.GONE
        val view = inflateCompletedTaskView(task)
        val dividerLine = view.findViewById<LinearLayout>(R.id.dividerLine)
        dividerLine.visibility = dividerLineVisibility

        fragment.addTaskViewToMap(task, view)
        if(indexToAdd == SENTINEL) {
            if(fragment.completedTasksContainer.size == 0)
                dividerLine.visibility = View.GONE
            fragment.completedTasksContainer.addView(view)
        }
        else fragment.completedTasksContainer.addView(view, indexToAdd)
    }

    private fun inflateCompletedTaskView(task: Task): View {
        val view = fragment.layoutInflater.inflate(R.layout.widget_completed_task, null)
        val titleTextLayout = view.findViewById<LinearLayout>(R.id.nameTextLayout)
        val titleText = view.findViewById<TextView>(R.id.titleText)
        val dateText = view.findViewById<TextView>(R.id.dateText)
        val taskText = view.findViewById<TextView>(R.id.taskText)

        val taskDate = task.getDate()
        val year = taskDate.get(Calendar.YEAR)
        val month = taskDate.get(Calendar.MONTH)
        val day = taskDate.get(Calendar.DAY_OF_MONTH)

        titleText.text = fragment.getToDoListTitle(task.getToDoListId())
        dateText.text = "${month + 1}/$day/$year"
        taskText.text = task.getTask()

        taskText.setOnClickListener {
            fragment.activity!!.highlightClickedView(taskText)
            fragment.showEditTaskDialog(taskText, task)
        }

        titleTextLayout.setOnClickListener {
            fragment.activity!!.highlightClickedView(titleText)
            fragment.openContactInfoActivity(task.getToDoListId())
        }

        return view
    }

    fun showNextCompletedTask(completedTasks: ArrayList<Task>, startIndex: Int) {
        val task = completedTasks[startIndex]
        val view = inflateCompletedTaskView(task)

        fragment.addTaskViewToMap(task, view)
        fragment.completedTasksContainer.addView(view)

        var index = startIndex + 1
        if(index == completedTasks.size) index = SENTINEL
        fragment.setIndexOfNextCompletedActivityToShow(index)
    }

    fun showNextBatchOfCompletedTasks(completedTasks: ArrayList<Task>, startIndex: Int) {
        var index = startIndex
        for(i in startIndex until completedTasks.size) {
            val task = completedTasks[i]
            val view = inflateCompletedTaskView(task)
            val dividerLine = view.findViewById<LinearLayout>(R.id.dividerLine)

            if(fragment.completedTasksContainer.size == 0)
                dividerLine.visibility = View.GONE
            fragment.addTaskViewToMap(task, view)
            fragment.completedTasksContainer.addView(view)
            if(++index % N_COMPLETED_ACTIVITIES_PER_BATCH == 0)
                break
        }
        if(index == completedTasks.size) index = SENTINEL
        fragment.setIndexOfNextCompletedActivityToShow(index)
    }

    fun displayTasks(tasks: ArrayList<Task>, dateString: String) {
        for(task in tasks) {
            val newTaskView =
                fragment.layoutInflater.inflate(R.layout.widget_task, null)
            val checkboxLayout =
                newTaskView.findViewById<LinearLayout>(R.id.checkboxLayout)
            val checkbox = newTaskView.findViewById<ImageView>(R.id.checkbox)
            val taskText = newTaskView.findViewById<TextView>(R.id.taskText)
            val taskTextLayout = newTaskView.findViewById<LinearLayout>(R.id.taskTextLayout)
            val titleText = newTaskView.findViewById<TextView>(R.id.titleText)
            val titleTextLayout = newTaskView.findViewById<LinearLayout>(R.id.titleTextLayout)
            taskText.text = task.getTask()
            titleText.text = fragment.getToDoListTitle(task.getToDoListId())

            if(!datesAreTheSame(fragment.getSelectedDateOfDisplayedActivities(),
                    task.getDate())) {
                val backgroundLayout = newTaskView.findViewById<LinearLayout>(R.id.backgroundLayout)
                backgroundLayout.setBackgroundColor(ContextCompat.getColor(fragment.activity!!, R.color.lightRed))
            }

            var isChecked = task.isCompleted()
            var checkboxImageCode =
                if(!isChecked) R.drawable.ic_check_box_unchecked
                else R.drawable.ic_check_box_checked
            checkbox.setImageResource(checkboxImageCode)

            checkboxLayout.setOnClickListener {
                checkboxImageCode =
                    if(isChecked) R.drawable.ic_check_box_unchecked
                    else R.drawable.ic_check_box_checked
                isChecked = !isChecked
                checkbox.setImageResource(checkboxImageCode)
                task.markCompleted(isChecked)
                fragment.markActivityCompleted(isChecked,
                    task.getToDoListId(), task.getTaskId())
            }

            taskTextLayout.setOnClickListener {
                fragment.activity!!.highlightClickedView(taskText,
                    ContextCompat.getColor(fragment.activity!!, R.color.mediumGray))
                fragment.showEditTaskDialog(taskText, task)
            }

            titleTextLayout.setOnClickListener {
                fragment.activity!!.highlightClickedView(titleText,
                    ContextCompat.getColor(fragment.activity!!, R.color.mediumGray))
                fragment.openContactInfoActivity(task.getToDoListId())
            }

            fragment.tasksTestContainer.addView(newTaskView)
        }

        displayTasksWithAdjustedHeight(dateString)
    }

    private fun displayTasksWithAdjustedHeight(dateString: String) {
        fragment.tasksTestScrollView.post {
            fragment.tasksContainer.removeAllViews()
            val tasksScrollViewHeight = fragment.tasksTestScrollView.height
            moveTasksFromTestContainer()
            fragment.dateOfActivitiesText.text = fragment.getString(R.string.containerCountHeader,
                dateString, fragment.tasksContainer.size.toString())
            fragment.tasksScrollView.layoutParams =
                if(tasksScrollViewHeight > fixedScrollHeight)
                    paramsScrollHeight else paramsWrapHeight
            if(fragment.tasksContainer.size == 0)
                showNoContentsText()

            if(fragment.tasksContainer.visibility != View.VISIBLE) {
                beginTransition(fragment.homeFragmentParent)
                fragment.tasksContainer.visibility = View.VISIBLE
            }
        }
    }

    private fun moveTasksFromTestContainer() {
        val size = fragment.tasksTestContainer.size
        for(i in 0 until size) {
            val view = fragment.tasksTestContainer[0]
            fragment.tasksTestContainer.removeViewAt(0)
            fragment.tasksContainer.addView(view)
        }
    }

    private fun showNoContentsText() {
        val noContentsView =
            fragment.layoutInflater.inflate(R.layout.widget_no_contents_text, null)
        val noContentsText = noContentsView.findViewById<TextView>(R.id.noContentsText)
        noContentsText.text = fragment.getString(R.string.noTasksToShowPrompt)
        fragment.tasksContainer.addView(noContentsView)
    }

    fun displayCompletedTasks(completedTasks: ArrayList<Task>) {
        showNextBatchOfCompletedTasks(completedTasks, 0)
        fragment.noCompletedTasksText.visibility =
            if(fragment.completedTasksContainer.size == 0)
                View.VISIBLE else View.GONE
    }
}