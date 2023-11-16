package com.justanotherdeveloper.todolisttracker

import android.annotation.SuppressLint
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.DateFormat
import java.util.*

@SuppressLint("InflateParams")
class ToDoListDialogsManager(private val activity: ToDoListActivity) {

    fun showDatePickerDialog() {
        val datePicker = DatePickerFragment(activity.getYear(),
            activity.getMonth(), activity.getDay(), activity)
        datePicker.show(activity.supportFragmentManager, "date picker")
    }

    fun showEditNotesDialog(notes: String) {
        val editContactNotesDialog = BottomSheetDialog(activity)
        val view = activity.layoutInflater.inflate(R.layout.bottomsheet_edit_notes, null)
        editContactNotesDialog.setContentView(view)

        val notesField = view.findViewById<EditText>(R.id.notesField)
        val saveButton = view.findViewById<TextView>(R.id.saveButton)

        notesField.setText(notes)

        editContactNotesDialog.setOnDismissListener {
            val newNotes = notesField.text.toString()
            if(newNotes != notes) activity.updateNotes(newNotes)
        }

        saveButton.setOnClickListener {
            activity.highlightClickedView(saveButton)
            editContactNotesDialog.dismiss()
        }

        editContactNotesDialog.show()
    }

    fun showConfirmDeleteToDoListDialog() {
        val confirmDeleteToDoListDialog = BottomSheetDialog(activity)
        val view = activity.layoutInflater.inflate(R.layout.bottomsheet_confirm_delete, null)
        confirmDeleteToDoListDialog.setContentView(view)

        val cancelButton = view.findViewById<TextView>(R.id.cancelButton)
        val deleteButton = view.findViewById<TextView>(R.id.deleteButton)

        cancelButton.setOnClickListener { confirmDeleteToDoListDialog.dismiss() }
        deleteButton.setOnClickListener {
            confirmDeleteToDoListDialog.dismiss()
            activity.deleteToDoList()
        }

        confirmDeleteToDoListDialog.show()
    }

    fun showSaveOrDiscardToDoListDialog() {
        val saveOrDiscardToDoListDialog = BottomSheetDialog(activity)
        val view = activity.layoutInflater.inflate(R.layout.bottomsheet_save_or_discard_to_do_list, null)
        saveOrDiscardToDoListDialog.setContentView(view)

        val cancelButton = view.findViewById<TextView>(R.id.cancelButton)
        val discardButton = view.findViewById<TextView>(R.id.discardButton)
        val saveButton = view.findViewById<TextView>(R.id.saveButton)

        cancelButton.setOnClickListener { saveOrDiscardToDoListDialog.dismiss() }
        discardButton.setOnClickListener {
            saveOrDiscardToDoListDialog.dismiss()
            activity.newToDoListDiscarded()
            activity.initFinishProcess()
        }

        saveButton.setOnClickListener {
            saveOrDiscardToDoListDialog.dismiss()
            activity.saveButtonClicked()
        }

        saveOrDiscardToDoListDialog.show()
    }

    fun showAddNewTaskDialog() {
        val addNewTaskDialog = BottomSheetDialog(activity)
        val view = activity.layoutInflater.inflate(R.layout.bottomsheet_add_task, null)
        addNewTaskDialog.setContentView(view)

        val dialogParent = view.findViewById<LinearLayout>(R.id.dialogParent)
        val errorMessage = view.findViewById<TextView>(R.id.errorMessage)
        val newContactActivityField = view.findViewById<EditText>(R.id.newTaskField)
        val saveButton = view.findViewById<ImageView>(R.id.saveButton)
        val dateTextView = view.findViewById<TextView>(R.id.dateText)
        val calendarButton = view.findViewById<LinearLayout>(R.id.calendarButton)

        activity.setDialogDateValues(dateTextView)
        val tz = TimeZone.getDefault()
        val c: Calendar = Calendar.getInstance(tz)
        c.set(Calendar.YEAR, activity.getYear())
        c.set(Calendar.MONTH, activity.getMonth())
        c.set(Calendar.DAY_OF_MONTH, activity.getDay())
        dateTextView.text = DateFormat.getDateInstance(DateFormat.FULL).format(c.time)

        dateTextView.setOnClickListener {
            activity.highlightClickedView(dateTextView)
            showDatePickerDialog()
        }

        calendarButton.setOnClickListener {
            activity.circleHighlightClickedView(calendarButton)
            showDatePickerDialog()
        }

        addNewTaskDialog.setOnDismissListener {
            activity.setDialogDateValues(null)
        }

        saveButton.setOnClickListener {
            activity.circleHighlightClickedView(saveButton)
            val newContactActivity = newContactActivityField.text.toString()
            if(newContactActivity.isEmpty()) {
                beginTransition(dialogParent)
                errorMessage.visibility = View.VISIBLE
            } else {
                activity.saveNewTask(newContactActivity)
                addNewTaskDialog.dismiss()
            }
        }

        addNewTaskDialog.show()
    }

    fun showTaskOptionsDialog(task: Task, taskText: TextView,
                              dateText: TextView, newTaskView: View) {
        val contactActivityOptionsDialog = BottomSheetDialog(activity)
        val view = activity.layoutInflater.inflate(R.layout.bottomsheet_task_options, null)
        contactActivityOptionsDialog.setContentView(view)

        val changeDateOption = view.findViewById<LinearLayout>(R.id.changeDateOption)
        val editActivityOption = view.findViewById<LinearLayout>(R.id.editTaskOption)
        val deleteTaskOption = view.findViewById<LinearLayout>(R.id.deleteTaskOption)

        activity.setWidgetDateValues(dateText, task,
            contactActivityOptionsDialog, newTaskView)

        changeDateOption.setOnClickListener {
            activity.highlightClickedView(changeDateOption)
            showDatePickerDialog()
        }

        editActivityOption.setOnClickListener {
            activity.highlightClickedView(editActivityOption)
            showEditTaskDialog(task, taskText,
                contactActivityOptionsDialog)
        }

        deleteTaskOption.setOnClickListener {
            activity.highlightClickedView(deleteTaskOption)
            showConfirmDeleteTaskDialog(task, newTaskView,
                contactActivityOptionsDialog)
        }

        contactActivityOptionsDialog.show()
    }

    private fun showConfirmDeleteTaskDialog(task: Task, newTaskView: View,
                                            taskOptionsDialog: BottomSheetDialog) {
        val deleteTaskDialog = BottomSheetDialog(activity)
        val view = activity.layoutInflater.inflate(R.layout.bottomsheet_confirm_delete, null)
        deleteTaskDialog.setContentView(view)

        val confirmDeletePrompt = view.findViewById<TextView>(R.id.confirmDeletePrompt)
        val cancelButton = view.findViewById<TextView>(R.id.cancelButton)
        val deleteButton = view.findViewById<TextView>(R.id.deleteButton)

        confirmDeletePrompt.text = activity.getString(R.string.confirmDeleteTaskHeader)

        cancelButton.setOnClickListener {
            deleteTaskDialog.dismiss()
        }

        deleteButton.setOnClickListener {
            deleteTaskDialog.dismiss()
            taskOptionsDialog.dismiss()
            activity.deleteTask(task, newTaskView)
        }

        deleteTaskDialog.show()
    }

    fun showEditTaskDialog(task: Task, taskText: TextView,
                           taskOptionsDialog: BottomSheetDialog?) {
        val editTaskDialog = BottomSheetDialog(activity)
        val view = activity.layoutInflater.inflate(R.layout.bottomsheet_edit_task, null)
        editTaskDialog.setContentView(view)

        val taskField = view.findViewById<EditText>(R.id.taskField)
        val errorMessage = view.findViewById<TextView>(R.id.errorMessage)
        val dialogParent = view.findViewById<LinearLayout>(R.id.dialogParent)
        val saveButton = view.findViewById<ImageView>(R.id.saveButton)

        taskField.setText(task.getTask())

        editTaskDialog.setOnDismissListener {
            val editedTask = taskField.text.toString()
            if(editedTask.isNotEmpty() && editedTask != task.getTask()) {
                taskText.text = editedTask
                task.setTask(editedTask)
                activity.updateToDoList()
                taskOptionsDialog?.dismiss()
            }
        }

        saveButton.setOnClickListener {
            val editedTask = taskField.text.toString()
            if(editedTask.isEmpty()) {
                beginTransition(dialogParent)
                errorMessage.visibility = View.VISIBLE
            } else editTaskDialog.dismiss()
        }

        editTaskDialog.show()
    }
}