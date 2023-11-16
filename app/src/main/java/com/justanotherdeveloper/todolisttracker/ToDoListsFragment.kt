package com.justanotherdeveloper.todolisttracker

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import androidx.core.view.iterator
import androidx.core.view.size
import kotlinx.android.synthetic.main.fragment_to_do_lists.*

class ToDoListsFragment : Fragment() {
    private lateinit var view: ToDoListsViewManager
    private lateinit var dialogsMgr: ToDoListsDialogsManager
    private lateinit var toDoListsMgr: ToDoListsManager
    private lateinit var toDoLists: ArrayList<ToDoList>

    private val requestCode = 0

    private var removeButtonViews = ArrayList<LinearLayout>()
    private var topDivider: LinearLayout? = null
    private var currentSortByPosition = 0
    private var toDoListsEdited = false
    private var showingRemoveViews = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_to_do_lists, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        toDoListsMgr = ToDoListsManager(context!!)
        dialogsMgr = ToDoListsDialogsManager(this)
        view = ToDoListsViewManager(this)
        initListeners()
        toDoLists = getSortedToDoLists()
        view.displayToDoLists(toDoLists)
    }

    fun showConfirmDeleteContactDialog(toDoList: ToDoList, view: View,
                                       removeButtonView: LinearLayout
    ) {
        dialogsMgr.showConfirmDeleteContactDialog(toDoList, view, removeButtonView)
    }

    fun checkIfToDoListsEdited(): Boolean {
        return toDoListsEdited
    }

    fun setToDoListsEdited(contactsEdited: Boolean) {
        this.toDoListsEdited = contactsEdited
    }

    fun getTopDivider(): LinearLayout? {
        return topDivider
    }

    fun setTopDivider(dividerLine: LinearLayout?) {
        topDivider = dividerLine
    }

    fun addRemoveButtonView(removeButtonView: LinearLayout) {
        removeButtonViews.add(removeButtonView)
    }

    private fun openNewContactActivity() {
        resetRemoveButton()
        val newToDoListPage = Intent(context, ToDoListActivity::class.java)
        startActivityForResult(newToDoListPage, requestCode)
    }

    fun openToDoListActivity(contactId: Int) {
        resetRemoveButton()
        val toDoListPage = Intent(context, ToDoListActivity::class.java)
        toDoListPage.putExtra(TO_DO_LIST_ID_REF, contactId)
        startActivityForResult(toDoListPage, requestCode)
    }

    fun deleteContact(toDoList: ToDoList, contactView: View, removeButtonView: LinearLayout) {
        beginTransition(toDoListsParent)
        toDoListsContainer.removeView(contactView)
        toDoListsMgr.deleteToDoList(toDoList.getToDoListId())
        removeButtonViews.remove(removeButtonView)
        checkIfContactsAreShowing()
        toDoLists.remove(toDoList)
        toDoListsEdited = true
    }

    private fun checkIfContactsAreShowing() {
        var showingContacts = false
        for(view in toDoListsContainer.iterator()) {
            if(view.visibility == View.VISIBLE) {
                showingContacts = true
                topDivider = view.findViewById(R.id.dividerLine)
                topDivider!!.visibility = View.GONE
                break
            }
        }
        if(!showingContacts) {
            topDivider = null
            noContactsText.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(data == null) return
        if(this.requestCode == requestCode) {
            val contactEdited = data.getBooleanExtra(TO_DO_LIST_EDITED_REF, true)
            if(contactEdited) {
                toDoListsEdited = true
                refreshToDoLists()
            }
        }
    }

    fun refreshToDoLists() {
        removeButtonViews.clear()
        toDoListsContainer.removeAllViews()
        toDoLists = getSortedToDoLists()
        view.displayToDoLists(toDoLists)
    }

    private fun getSortedToDoLists(): ArrayList<ToDoList> {
        return when(sortBySpinner.selectedItemPosition) {
            0 -> toDoListsMgr.getToDoListsAToZ()
            1 -> toDoListsMgr.getToDoListsZToA()
            2 -> toDoListsMgr.getToDoListsNewestFirst()
            else -> toDoListsMgr.getToDoListsOldestFirst()
        }
    }

    private fun initListeners() {
        initSpinners()
        searchButton.setOnClickListener { toggleSearchViews() }
        removeButton.setOnClickListener { toggleRemoveViews() }
        addButton.setOnClickListener {
            activity!!.circleHighlightClickedView(addButton)
            openNewContactActivity()
        }
        searchField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                if(searchField.visibility == View.VISIBLE) view.applyFilters(toDoLists)
            }
        })
    }

    private fun toggleRemoveViews() {
        beginTransition(toDoListsParent)
        showingRemoveViews = !showingRemoveViews
        if(showingRemoveViews) {
            activity!!.circleSelectClickedView(removeButton)
            for(view in removeButtonViews)
                view.visibility = View.VISIBLE
        } else {
            removeButton.background = null
            for(view in removeButtonViews)
                view.visibility = View.GONE
        }
    }

    private fun toggleSearchViews() {
        beginTransition(toDoListsParent)
        if(searchField.visibility == View.VISIBLE) {
            searchField.visibility = View.GONE
            addButton.visibility = View.VISIBLE
            toDoListsText.visibility = View.VISIBLE
            searchButton.background = null
            view.applyFilters(toDoLists)
            if(toDoListsContainer.size != 0)
                noContactsText.visibility = View.GONE
        } else {
            searchField.setText("")
            searchField.visibility = View.VISIBLE
            addButton.visibility = View.GONE
            toDoListsText.visibility = View.GONE
            activity!!.circleSelectClickedView(searchButton)
        }
    }

    private fun initSpinners() {
        initSortBySpinner()
        initDisplaySpinner()
    }

    private fun initSortBySpinner() {
        val sortByOptions = getSortByOptions()
        val spinnerAdapter = ArrayAdapter<String>(context!!, R.layout.spinner_item_regular, sortByOptions)
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_item_regular)
        sortBySpinner.adapter = spinnerAdapter
        sortBySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parentView: AdapterView<*>) { }
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View,
                                        position: Int, id: Long) {
                if(currentSortByPosition != position) {
                    currentSortByPosition = position
                    if(searchButton.visibility == View.GONE) toggleRemoveViews()
                    else beginTransition(toDoListsParent)
                    toDoListsContainer.removeAllViews()
                    toDoLists = getSortedToDoLists()
                    view.displayToDoLists(toDoLists)
                }
            }
        }

        sortByLayout.setOnClickListener {
            sortBySpinner.performClick()
        }
    }

    private fun getSortByOptions(): ArrayList<String> {
        val sortByList = ArrayList<String>()
        sortByList.add(getString(R.string.aToZOption))
        sortByList.add(getString(R.string.zToAOption))
        sortByList.add(getString(R.string.newestFirstOption))
        sortByList.add(getString(R.string.oldestFirstOption))
        return sortByList
    }

    private fun initDisplaySpinner() {
        val displayOptions = getDisplayOptions()
        val spinnerAdapter = ArrayAdapter<String>(context!!, R.layout.spinner_item_regular, displayOptions)
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_item_regular)
        displaySpinner.adapter = spinnerAdapter
        displaySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parentView: AdapterView<*>) { }
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View,
                                        position: Int, id: Long) {
                view.applyFilters(toDoLists)
            }
        }

        displayLayout.setOnClickListener {
            displaySpinner.performClick()
        }
    }

    private fun getDisplayOptions(): ArrayList<String> {
        val sortByList = ArrayList<String>()
        sortByList.add(getString(R.string.allContactsOption))
        sortByList.add(getString(R.string.starredOnlyOption))
        return sortByList
    }

    fun iconsReset(): Boolean {
        var iconsReset = true
        if(searchField.visibility == View.VISIBLE) {
            toggleSearchViews()
            iconsReset = false
        }
        if(showingRemoveViews) {
            toggleRemoveViews()
            iconsReset = false
        }
        return iconsReset
    }

    fun resetRemoveButton() {
        if(showingRemoveViews) toggleRemoveViews()
        if(searchField.visibility == View.VISIBLE &&
            searchField.text.toString().isEmpty())
            toggleSearchViews()
    }
}
