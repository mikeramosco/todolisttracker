package com.justanotherdeveloper.todolisttracker

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.view.get
import androidx.core.view.iterator
import androidx.core.view.size
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_to_do_list.*
import java.text.DateFormat
import java.util.*

class ToDoListActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {

    private lateinit var view: ToDoListViewManager
    private lateinit var toDoListsMgr: ToDoListsManager
    private lateinit var dialogsMgr: ToDoListDialogsManager
    private lateinit var toDoList: ToDoList

    private var dialogDateTextView: TextView? = null
    private var widgetDateTextView: TextView? = null
    private var task: Task? = null
    private var taskOptionsDialog: BottomSheetDialog? = null
    private var selectedTaskView: View? = null
    private var year = SENTINEL
    private var month = SENTINEL
    private var day = SENTINEL
    private var toDoListEdited = false

    private var toDoListId = SENTINEL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_to_do_list)
        view = ToDoListViewManager(this)
        toDoListsMgr = ToDoListsManager(this)
        dialogsMgr = ToDoListDialogsManager(this)
        toDoListId = intent.getIntExtra(TO_DO_LIST_ID_REF, SENTINEL)
        toDoList = ToDoList()
        initListeners()
        if(toDoListId != SENTINEL) displaySavedToDoList()
    }

    private fun displaySavedToDoList() {
        toDoList = toDoListsMgr.getToDoList(toDoListId)
        view.showSavedToDoListState()
        titleField.setText(toDoList.getListTitle())
        if(toDoList.isStarred()) starredIcon.setImageResource(R.drawable.ic_star_checked)
        for(task in toDoList.getTasks()) {
            val taskDate = task.getDate()
            val year = taskDate.get(Calendar.YEAR)
            val month = taskDate.get(Calendar.MONTH)
            val day = taskDate.get(Calendar.DAY_OF_MONTH)
            view.addNewTaskView(task, month, day, year, true)
        }
    }

    private fun clearFocus() {
        toDoListParent.requestFocus()
    }

    private fun initListeners() {
        starredIcon.setOnClickListener { starredIconClicked() }
        saveButton.setOnClickListener { saveButtonClicked() }
        addButton.setOnClickListener {
            clearFocus()
            circleHighlightClickedView(addButton)
            dialogsMgr.showAddNewTaskDialog() }
        deleteButton.setOnClickListener {
            clearFocus()
            circleHighlightClickedView(deleteButton)
            dialogsMgr.showConfirmDeleteToDoListDialog()
        }
        notesButton.setOnClickListener {
            clearFocus()
            circleHighlightClickedView(notesButton)
            dialogsMgr.showEditNotesDialog(toDoList.getNotes()) }
    }

    private fun starredIconClicked() {
        clearFocus()
        if(toDoList.isStarred()) {
            toDoList.setStarred(false)
            starredIcon.setImageResource(R.drawable.ic_star_unchecked)
        } else {
            toDoList.setStarred(true)
            starredIcon.setImageResource(R.drawable.ic_star_checked)
        }
        updateToDoList()
    }

    fun deleteToDoList() {
        toDoListsMgr.deleteToDoList(toDoListId)
        toDoListEdited = true
        initFinishProcess()
    }

    fun saveButtonClicked() {
        if(toDoListId != SENTINEL) return
        highlightClickedView(saveButton)
        val title = titleField.text.toString()
        if(title.isEmpty()) {
            beginTransition(toDoListParent)
            titleErrorMessage.visibility = View.VISIBLE
        } else {
            val dateCreated: Calendar = getTodaysDate()
            toDoList.saveToDoList(title, dateCreated)
            toDoListsMgr.saveToDoList(toDoList)
            showToastMessage(getString(R.string.toDoListSavedPrompt, title))
            toDoListEdited = true
            initFinishProcess()
        }
    }

    fun setDialogDateValues(dialogDateTextView: TextView?) {
        this.dialogDateTextView = dialogDateTextView
        if(dialogDateTextView != null) {
            val tz = TimeZone.getDefault()
            val c: Calendar = Calendar.getInstance(tz)
            year = c.get(Calendar.YEAR)
            month = c.get(Calendar.MONTH)
            day = c.get(Calendar.DAY_OF_MONTH)
        } else {
            year = SENTINEL
            month = SENTINEL
            day = SENTINEL
        }
    }

    fun setWidgetDateValues(widgetDateTextView: TextView?, task: Task?,
                            taskOptionsDialog: BottomSheetDialog?,
                            newTaskView: View?) {
        this.widgetDateTextView = widgetDateTextView
        this.task = task
        this.taskOptionsDialog = taskOptionsDialog
        this.selectedTaskView = newTaskView
        if(widgetDateTextView != null) {
            val c = task!!.getDate()
            year = c.get(Calendar.YEAR)
            month = c.get(Calendar.MONTH)
            day = c.get(Calendar.DAY_OF_MONTH)
        } else {
            year = SENTINEL
            month = SENTINEL
            day = SENTINEL
        }
    }

    fun updateToDoList() {
        clearFocus()
        toDoListEdited = true
        if(toDoListId == SENTINEL) return
        toDoListsMgr.updateToDoList(toDoListId, toDoList)
    }

    fun saveNewTask(newTaskText: String) {
        val tz = TimeZone.getDefault()
        val c: Calendar = Calendar.getInstance(tz)
        c.set(Calendar.YEAR, year)
        c.set(Calendar.MONTH, month)
        c.set(Calendar.DAY_OF_MONTH, day)

        val taskId = if(toDoListId != SENTINEL) {
            val taskIds = toDoList.getTaskIds()
            val id = generateId(taskIds)
            taskIds.add(id)
            id
        } else SENTINEL
        val task = Task(newTaskText, c, toDoListId, taskId)
        toDoList.getTasks().add(task)
        view.addNewTaskView(task, month, day, year)
        updateToDoList()
        month = SENTINEL
        day = SENTINEL
        year = SENTINEL
    }

    fun getYear(): Int {
        return year
    }

    fun getMonth(): Int {
        return month
    }

    fun getDay(): Int {
        return day
    }

    fun showContactActivityOptionsDialog(task: Task, taskText: TextView,
                                         dateText: TextView, newTaskView: View) {
        dialogsMgr.showTaskOptionsDialog(task, taskText, dateText, newTaskView)
    }

    fun showEditTaskDialog(task: Task, taskText: TextView) {
        dialogsMgr.showEditTaskDialog(task, taskText, null)
    }

    fun showDatePickerDialog() {
        dialogsMgr.showDatePickerDialog()
    }

    fun deleteTask(task: Task, newTaskView: View) {
        toDoList.getTasks().remove(task)
        updateToDoList()
        beginTransition(toDoListParent)
        tasksContainer.removeView(newTaskView)
        if(tasksContainer.size != 0)
            tasksContainer[0]
                .findViewById<LinearLayout>(R.id.dividerLine).visibility = View.GONE
        else noContentsText.visibility = View.VISIBLE
    }

    private fun newContactStarted(): Boolean {
        if(toDoListEdited) return true
        return titleField.text.toString().isNotEmpty()
    }

    private fun saveEditedContactInfo() {
        var title = titleField.text.toString()
        if(title.isEmpty()) title = toDoList.getListTitle()
        if(title != toDoList.getListTitle()) {
            toDoListEdited = true
            toDoList.setListTitle(title)
        }
        if(toDoListEdited) updateToDoList()
    }

    fun newToDoListDiscarded() {
        toDoListEdited = false
    }

    fun initFinishProcess(backPressed: Boolean = false) {
        val intentData = Intent()
        intentData.putExtra(TO_DO_LIST_EDITED_REF, toDoListEdited)
        setResult(RESULT_OK, intentData)
        if(!backPressed) finish()
    }

    fun datePickerDialogCancelClicked() {
        if(widgetDateTextView != null && taskOptionsDialog == null)
            setWidgetDateValues(null, null,
                null, null)
    }

    fun updateNotes(notes: String) {
        toDoList.setNotes(notes)
        updateToDoList()
    }

    private fun changeNewTaskDate(year: Int, month: Int, day: Int) {
        this.year = year
        this.month = month
        this.day = day
        val tz = TimeZone.getDefault()
        val c: Calendar = Calendar.getInstance(tz)
        c.set(Calendar.YEAR, year)
        c.set(Calendar.MONTH, month)
        c.set(Calendar.DAY_OF_MONTH, day)
        dialogDateTextView!!.text = DateFormat.getDateInstance(DateFormat.FULL).format(c.time)
    }

    @SuppressLint("SetTextI18n")
    private fun changeTaskDate(year: Int, month: Int, day: Int) {
        widgetDateTextView!!.text = "${month + 1}/$day/$year"
        val tz = TimeZone.getDefault()
        val c: Calendar = Calendar.getInstance(tz)
        c.set(Calendar.YEAR, year)
        c.set(Calendar.MONTH, month)
        c.set(Calendar.DAY_OF_MONTH, day)
        task!!.setDate(c)
        updateToDoList()
        if(taskOptionsDialog != null)
            taskOptionsDialog!!.dismiss()
        reorderTasksWithChangedDate(c)
        setWidgetDateValues(null, null,
            null, null)
    }

    private fun reorderTasksWithChangedDate(selectedViewDate: Calendar) {
        beginTransition(toDoListParent)
        tasksContainer.removeView(selectedTaskView)
        var viewAdded = false
        val selectedViewDividerLine =
            selectedTaskView!!.findViewById<LinearLayout>(R.id.dividerLine)
        for((index, view) in tasksContainer.iterator().withIndex()) {
            val dateText = view.findViewById<TextView>(R.id.dateText)
            val dateTextContents = dateText.text.toString().split("/")
            val viewMonth = dateTextContents[0].toInt()
            val viewDay = dateTextContents[1].toInt()
            val viewYear = dateTextContents[2].toInt()
            val viewDate: Calendar = Calendar.getInstance(TimeZone.getDefault())
            val viewDividerLine = view.findViewById<LinearLayout>(R.id.dividerLine)
            viewDate.set(Calendar.YEAR, viewYear)
            viewDate.set(Calendar.MONTH, viewMonth-1)
            viewDate.set(Calendar.DAY_OF_MONTH, viewDay)
            val viewTime = viewDate.timeInMillis
            val selectedTime = selectedViewDate.timeInMillis
            if(datesAreTheSame(selectedViewDate, viewDate) || viewTime <= selectedTime) {
                viewAdded = true
                if(index == 0) {
                    viewDividerLine.visibility = View.VISIBLE
                    selectedViewDividerLine.visibility = View.GONE
                } else selectedViewDividerLine.visibility = View.VISIBLE
                tasksContainer.addView(selectedTaskView, index)
                break
            }
        }
        if(!viewAdded) {
            tasksContainer.addView(selectedTaskView)
            selectedViewDividerLine.visibility = View.VISIBLE
        }
        tasksContainer[0].findViewById<LinearLayout>(R.id.dividerLine).visibility = View.GONE
    }

    override fun onDateSet(p0: DatePicker?, year: Int, month: Int, day: Int) {
        when {
            dialogDateTextView != null -> changeNewTaskDate(year, month, day)
            widgetDateTextView != null -> changeTaskDate(year, month, day)
        }
    }

    override fun onBackPressed() {
        when {
            toDoListId != SENTINEL -> {
                saveEditedContactInfo()
                initFinishProcess(true)
                super.onBackPressed()
            }
            newContactStarted() -> dialogsMgr.showSaveOrDiscardToDoListDialog()
            else -> {
                initFinishProcess(true)
                super.onBackPressed()
            }
        }
    }
}
