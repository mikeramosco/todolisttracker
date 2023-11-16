package com.justanotherdeveloper.todolisttracker

import android.annotation.SuppressLint
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog

@SuppressLint("InflateParams")
class ToDoListsDialogsManager(private val fragment: ToDoListsFragment) {

    fun showConfirmDeleteContactDialog(toDoList: ToDoList, toDoListView: View, removeButtonView: LinearLayout) {
        val confirmDeleteContactDialog = BottomSheetDialog(fragment.context!!)
        val view = fragment.layoutInflater.inflate(R.layout.bottomsheet_confirm_delete, null)
        confirmDeleteContactDialog.setContentView(view)

        val cancelButton = view.findViewById<TextView>(R.id.cancelButton)
        val deleteButton = view.findViewById<TextView>(R.id.deleteButton)

        cancelButton.setOnClickListener { confirmDeleteContactDialog.dismiss() }
        deleteButton.setOnClickListener {
            confirmDeleteContactDialog.dismiss()
            fragment.deleteContact(toDoList, toDoListView, removeButtonView)
        }

        confirmDeleteContactDialog.show()
    }
}