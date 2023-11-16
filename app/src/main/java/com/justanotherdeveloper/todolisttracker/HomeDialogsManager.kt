package com.justanotherdeveloper.todolisttracker

import android.annotation.SuppressLint
import android.view.View
import android.widget.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.DateFormat
import java.util.*

@SuppressLint("InflateParams")
class HomeDialogsManager(private val fragment: HomeFragment) {

    fun showDatePickerDialog(date: Calendar) {
        val year = date.get(Calendar.YEAR)
        val month = date.get(Calendar.MONTH)
        val day = date.get(Calendar.DAY_OF_MONTH)
        val datePicker = DatePickerFragment(year, month, day, fragment)
        datePicker.show(fragment.activity!!.supportFragmentManager, "date picker")
    }

    fun showEditTaskDialog(taskText: TextView, task: Task) {
        val editTaskDialog = BottomSheetDialog(fragment.activity!!)
        val view = fragment.layoutInflater.inflate(R.layout.bottomsheet_edit_task, null)
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
                fragment.updateTask(editedTask, task)
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

    fun showAddTaskOrToDoListDialog() {
        val addTaskOrToDoListDialog = BottomSheetDialog(fragment.activity!!)
        val view = fragment.layoutInflater.inflate(R.layout.bottomsheet_add_task_or_to_do_list, null)
        addTaskOrToDoListDialog.setContentView(view)

        val addTaskOption = view.findViewById<LinearLayout>(R.id.addTaskOption)
        val addToDoListOption = view.findViewById<LinearLayout>(R.id.addToDoListOption)

        addTaskOption.setOnClickListener {
            fragment.activity!!.highlightClickedView(addTaskOption)
            addTaskOrToDoListDialog.dismiss()
            showAddTaskDialog()
        }

        addToDoListOption.setOnClickListener {
            fragment.activity!!.highlightClickedView(addToDoListOption)
            addTaskOrToDoListDialog.dismiss()
            fragment.openNewToDoListActivity()
        }

        addTaskOrToDoListDialog.show()
    }

    private fun showAddTaskDialog() {
        val addNewTaskDialog = BottomSheetDialog(fragment.activity!!)
        val view = fragment.layoutInflater.inflate(R.layout.bottomsheet_add_task, null)
        addNewTaskDialog.setContentView(view)

        val dialogParent = view.findViewById<LinearLayout>(R.id.dialogParent)
        val errorMessage = view.findViewById<TextView>(R.id.errorMessage)
        val newTaskField = view.findViewById<EditText>(R.id.newTaskField)
        val saveButton = view.findViewById<ImageView>(R.id.saveButton)
        val dateTextView = view.findViewById<TextView>(R.id.dateText)
        val calendarButton = view.findViewById<LinearLayout>(R.id.calendarButton)
        val toDoListsOptionsErrorMessage = view.findViewById<TextView>(R.id.toDoListsOptionsErrorMessage)
        val toDoListsSpinnerLayout = view.findViewById<LinearLayout>(R.id.toDoListsSpinnerLayout)
        val toDoListsSpinnerText = view.findViewById<TextView>(R.id.toDoListsSpinnerText)
        val toDoListsSpinner = view.findViewById<Spinner>(R.id.toDoListsSpinner)

        val toDoListsOptions = initToDoListsOptions()
        val spinnerAdapter = ArrayAdapter<String>(fragment.activity!!, R.layout.spinner_item_regular, toDoListsOptions)
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_item_regular)
        toDoListsSpinner.adapter = spinnerAdapter
        toDoListsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parentView: AdapterView<*>) { }
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View,
                                        position: Int, id: Long) {
                toDoListsSpinnerText.text = toDoListsSpinner.selectedItem.toString()
            }
        }

        toDoListsSpinnerLayout.visibility = View.VISIBLE
        toDoListsSpinnerLayout.setOnClickListener {
            toDoListsSpinner.performClick()
        }

        fragment.setDialogDateValues(dateTextView)
        val c = fragment.getDialogCalendar()
        dateTextView.text = DateFormat.getDateInstance(DateFormat.FULL).format(c.time)

        dateTextView.setOnClickListener {
            fragment.activity!!.highlightClickedView(dateTextView)
            showDatePickerDialog(fragment.getDialogCalendar())
        }

        calendarButton.setOnClickListener {
            fragment.activity!!.circleHighlightClickedView(calendarButton)
            showDatePickerDialog(fragment.getDialogCalendar())
        }

        addNewTaskDialog.setOnDismissListener {
            fragment.setDialogDateValues(null)
        }

        saveButton.setOnClickListener {
            fragment.activity!!.circleHighlightClickedView(saveButton)
            val newTask = newTaskField.text.toString()
            val toDoListIndex = toDoListsSpinner.selectedItemPosition
            var transitionStarted = false
            if(newTask.isEmpty()) {
                transitionStarted = true
                beginTransition(dialogParent)
                errorMessage.visibility = View.VISIBLE
            }
            if(toDoListIndex == 0) {
                if(!transitionStarted)
                    beginTransition(dialogParent)
                toDoListsOptionsErrorMessage.visibility = View.VISIBLE
            }
            if(newTask.isNotEmpty() && toDoListIndex != 0) {
                fragment.saveNewTask(newTask, toDoListIndex - 1)
                addNewTaskDialog.dismiss()
            }
        }

        addNewTaskDialog.show()
    }

    private fun initToDoListsOptions(): ArrayList<String> {
        val toDoListsOptions = ArrayList<String>()
        toDoListsOptions.add(fragment.getString(R.string.selectToDoListOption))
        val toDoLists = fragment.getToDoListsOptions()
        for(list in toDoLists) toDoListsOptions.add(list.getListTitle())
        return toDoListsOptions
    }
}