package com.justanotherdeveloper.todolisttracker

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.get
import androidx.core.view.iterator
import androidx.core.view.size
import kotlinx.android.synthetic.main.activity_to_do_list.*
import java.util.*

@SuppressLint("InflateParams, SetTextI18n")
class ToDoListViewManager(private val activity: ToDoListActivity) {

    fun showSavedToDoListState() {
        activity.newToDoListHeaderLayout.visibility = View.GONE
        activity.toDoListTitleSpacer.visibility = View.GONE
        activity.deleteButton.visibility = View.VISIBLE
    }

    fun addNewTaskView(task: Task, month: Int, day: Int, year: Int,
                       runningOnCreated: Boolean = false) {
        if(activity.noContentsText.visibility == View.VISIBLE)
            activity.noContentsText.visibility = View.GONE
        val newContactActivityView =
            activity.layoutInflater.inflate(R.layout.widget_to_do_list_task, null)
        val dividerLine =
            newContactActivityView.findViewById<LinearLayout>(R.id.dividerLine)
        val checkboxLayout =
            newContactActivityView.findViewById<LinearLayout>(R.id.checkboxLayout)
        val checkbox = newContactActivityView.findViewById<ImageView>(R.id.checkbox)
        val taskText = newContactActivityView.findViewById<TextView>(R.id.taskText)
        val taskTextLayout = newContactActivityView.findViewById<LinearLayout>(R.id.taskTextLayout)
        val dateText = newContactActivityView.findViewById<TextView>(R.id.dateText)
        val dateTextLayout = newContactActivityView.findViewById<LinearLayout>(R.id.dateTextLayout)
        val moreButton = newContactActivityView.findViewById<ImageView>(R.id.moreButton)

        var isChecked = task.isCompleted()
        var checkboxImageCode =
            if(!isChecked) R.drawable.ic_check_box_unchecked
            else R.drawable.ic_check_box_checked
        checkbox.setImageResource(checkboxImageCode)

        if(activity.tasksContainer.size == 0) dividerLine.visibility = View.GONE
        checkboxLayout.setOnClickListener {
            checkboxImageCode =
                if(isChecked) R.drawable.ic_check_box_unchecked
                else R.drawable.ic_check_box_checked
            isChecked = !isChecked
            checkbox.setImageResource(checkboxImageCode)
            task.markCompleted(isChecked)
            activity.updateToDoList()
        }

        taskTextLayout.setOnClickListener {
            activity.highlightClickedView(taskText)
            activity.showEditTaskDialog(task, taskText)
        }

        moreButton.setOnClickListener {
            activity.circleHighlightClickedView(moreButton)
            activity.showContactActivityOptionsDialog(task,
                taskText, dateText, newContactActivityView)
        }

        dateTextLayout.setOnClickListener {
            activity.highlightClickedView(dateText)
            activity.setWidgetDateValues(dateText, task,
                null, newContactActivityView)
            activity.showDatePickerDialog()
        }

        taskText.text = task.getTask()
        dateText.text = "${month + 1}/$day/$year"

        addNewContactActivityInOrder(year, month, day, dividerLine,
            newContactActivityView, runningOnCreated)
    }

    private fun addNewContactActivityInOrder(year: Int, month: Int, day: Int,
                                             dividerLine: LinearLayout,
                                             newContactActivityView: View,
                                             runningOnCreated: Boolean) {
        if(!runningOnCreated) beginTransition(activity.toDoListParent)
        val newContactActivityDate = getTodaysDate()
        newContactActivityDate.set(Calendar.YEAR, year)
        newContactActivityDate.set(Calendar.MONTH, month)
        newContactActivityDate.set(Calendar.DAY_OF_MONTH, day)

        var viewAdded = false
        for((index, view) in activity.tasksContainer.iterator().withIndex()) {
            val viewDateText = view.findViewById<TextView>(R.id.dateText)
            val viewDateTextContents = viewDateText.text.toString().split("/")
            val viewMonth = viewDateTextContents[0].toInt()
            val viewDay = viewDateTextContents[1].toInt()
            val viewYear = viewDateTextContents[2].toInt()
            val viewDate: Calendar = Calendar.getInstance(TimeZone.getDefault())
            val viewDividerLine = view.findViewById<LinearLayout>(R.id.dividerLine)
            viewDate.set(Calendar.YEAR, viewYear)
            viewDate.set(Calendar.MONTH, viewMonth-1)
            viewDate.set(Calendar.DAY_OF_MONTH, viewDay)
            val viewTime = viewDate.timeInMillis
            val selectedTime = newContactActivityDate.timeInMillis
            if(datesAreTheSame(newContactActivityDate, viewDate) || viewTime <= selectedTime) {
                viewAdded = true
                if(index == 0) {
                    viewDividerLine.visibility = View.VISIBLE
                    dividerLine.visibility = View.GONE
                } else dividerLine.visibility = View.VISIBLE
                activity.tasksContainer.addView(newContactActivityView, index)
                break
            }
        }
        if(!viewAdded) {
            activity.tasksContainer.addView(newContactActivityView)
            dividerLine.visibility = View.VISIBLE
            activity.tasksContainer[0]
                .findViewById<LinearLayout>(R.id.dividerLine).visibility = View.GONE
        }
    }
}