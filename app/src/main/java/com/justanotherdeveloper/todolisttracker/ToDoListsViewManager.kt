package com.justanotherdeveloper.todolisttracker

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.get
import kotlinx.android.synthetic.main.fragment_to_do_lists.*

class ToDoListsViewManager(private val fragment: ToDoListsFragment) {

    @SuppressLint("InflateParams")
    fun displayToDoLists(toDoLists: ArrayList<ToDoList>) {
        var showingContacts = false
        for(toDoList in toDoLists) {
            val view = fragment.layoutInflater.inflate(R.layout.widget_to_do_list, null)

            val titleText = view.findViewById<TextView>(R.id.nameText)
            val removeButton = view.findViewById<ImageView>(R.id.removeButton)
            val removeButtonLayout = view.findViewById<LinearLayout>(R.id.removeButtonLayout)

            fragment.addRemoveButtonView(removeButtonLayout)

            val searchText = fragment.searchField.text.toString()

            if(fragment.searchField.visibility == View.VISIBLE && searchText.isNotEmpty()
                && !toDoList.getListTitle().contains(searchText, true))
                view.visibility = View.GONE
            else if(fragment.displaySpinner.selectedItemPosition == 1 && !toDoList.isStarred())
                view.visibility = View.GONE

            if(!showingContacts && view.visibility == View.VISIBLE) {
                showingContacts = true
                val dividerLine = view.findViewById<LinearLayout>(R.id.dividerLine)
                dividerLine.visibility = View.GONE
                fragment.setTopDivider(dividerLine)
            }

            titleText.text = toDoList.getListTitle()

            view.setOnClickListener {
                fragment.activity!!.highlightClickedView(view)
                fragment.openToDoListActivity(toDoList.getToDoListId())
            }

            removeButtonLayout.setOnClickListener {
                fragment.activity!!.circleHighlightClickedView(removeButton)
                fragment.showConfirmDeleteContactDialog(toDoList, view, removeButtonLayout)
            }

            fragment.toDoListsContainer.addView(view)
        }
        fragment.noContactsText.visibility = if(!showingContacts)
            View.VISIBLE else View.GONE
    }

    fun applyFilters(toDoLists: ArrayList<ToDoList>) {
        val searchText = fragment.searchField.text.toString()

        beginTransition(fragment.toDoListsParent)
        val topDivider = fragment.getTopDivider()
        if(topDivider != null) {
            topDivider.visibility = View.VISIBLE
            fragment.setTopDivider(null)
        }

        var showingContacts = false
        for((index, contact) in toDoLists.withIndex()) {
            val view = fragment.toDoListsContainer[index]
            var visibility = View.VISIBLE
            if(fragment.searchField.visibility == View.VISIBLE && searchText.isNotEmpty()
                && !contact.getListTitle().contains(searchText, true))
                visibility = View.GONE
            else if(fragment.displaySpinner.selectedItemPosition == 1 && !contact.isStarred())
                visibility = View.GONE
            view.visibility = visibility

            if(fragment.getTopDivider() == null && view.visibility == View.VISIBLE) {
                val dividerLine = view.findViewById<LinearLayout>(R.id.dividerLine)
                fragment.setTopDivider(dividerLine)
                dividerLine.visibility = View.GONE
                showingContacts = true
            }
        }
        fragment.noContactsText.visibility = if(!showingContacts)
            View.VISIBLE else View.GONE
    }
}