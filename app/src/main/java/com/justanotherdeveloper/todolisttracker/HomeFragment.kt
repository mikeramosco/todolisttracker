package com.justanotherdeveloper.todolisttracker

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.get
import androidx.core.view.iterator
import androidx.core.view.size
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_home.*
import java.text.DateFormat
import java.util.*

class HomeFragment : Fragment(), DatePickerDialog.OnDateSetListener {

    private lateinit var view: HomeViewManager
    private lateinit var toDoListsMgr: ToDoListsManager
    private lateinit var completedTasks: ArrayList<Task>
    private lateinit var dialogsMgr: HomeDialogsManager
    private lateinit var settingsMgr: SettingsManager

    private val requestCode = 0
    private var taskViewsMap = HashMap<Task, View>()
    private var dialogDateTextView: TextView? = null
    private var dialogCalendar = getTodaysDate()
    private var selectedDateOfDisplayedActivities = getTodaysDate()
    private var indexOfNextCompletedTaskToShow = SENTINEL
    private var toDoListEdited = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        toDoListsMgr = ToDoListsManager(context!!)
        settingsMgr = SettingsManager(context!!)
        dialogsMgr = HomeDialogsManager(this)
        completedTasks =
            toDoListsMgr.getCompletedTasks()
        view = HomeViewManager(this)
        fixedScrollView.post {
            view.setFixedScrollHeight()
            fixedScrollView.visibility = View.GONE
            fixedScrollView.post {
                view.displayTasks(toDoListsMgr.getTasks(selectedDateOfDisplayedActivities,
                    overdueTasksSwitch.isChecked), getDateStringOfDisplayedTasks())
            }
        }
        setSwitchBooleans()
        view.displayCompletedTasks(completedTasks)
        initListeners()
    }

    private fun setSwitchBooleans() {
        overdueTasksSwitch.isChecked = settingsMgr.getShowOverdueTasksSwitch()
        updateDateWhenCompletedSwitch.isChecked = settingsMgr.getUpdateDateWhenCompleted()
    }

    private fun initListeners() {
        initScrollViewListener()
        initArrowListeners()
        initSwitchListeners()
        tasksOptionsToggleLayout.setOnClickListener { view.toggleTaskOptionsVisibility() }
        dateOfActivitiesText.setOnClickListener { changeDateOfActivities(false) }
        calendarButton.setOnClickListener { changeDateOfActivities(true) }
        addNewToDoListButton.setOnClickListener {
            activity!!.circleHighlightClickedView(addNewToDoListButton)
            dialogsMgr.showAddTaskOrToDoListDialog()
        }
    }

    private fun initSwitchListeners() {
        overdueTasksSwitch.setOnCheckedChangeListener { _, isChecked ->
            settingsMgr.setShowOverdueTasksSwitch(isChecked)
            refreshActivitiesContainer()
        }

        overdueTasksTextLayout.setOnClickListener {
            activity?.highlightClickedView(overdueTasksText)
            overdueTasksSwitch.isChecked = !overdueTasksSwitch.isChecked
        }

        updateDateWhenCompletedSwitch.setOnCheckedChangeListener { _, isChecked ->
            settingsMgr.setUpdateDateWhenCompleted(isChecked)
        }

        updateDateWhenCompletedTextLayout.setOnClickListener {
            activity?.highlightClickedView(updateDateWhenCompletedText)
            updateDateWhenCompletedSwitch.isChecked = !updateDateWhenCompletedSwitch.isChecked
        }
    }

    fun getToDoListsOptions(): ArrayList<ToDoList> {
        return toDoListsMgr.getToDoListsAToZ()
    }

    fun setDialogDateValues(dialogDateTextView: TextView?) {
        this.dialogDateTextView = dialogDateTextView
        if(dialogDateTextView != null) dialogCalendar = getTodaysDate()
    }

    fun getDialogCalendar(): Calendar {
        return dialogCalendar
    }

    fun saveNewTask(taskString: String, toDoListIndex: Int) {
        val toDoList = getToDoListsOptions()[toDoListIndex]
        val taskIds = toDoList.getTaskIds()
        val id = generateId(taskIds)
        taskIds.add(id)
        val task = Task(taskString, dialogCalendar, toDoList.getToDoListId(), id)
        toDoList.getTasks().add(task)
        toDoListsMgr.updateToDoList(toDoList.getToDoListId(), toDoList)
        if(datesAreTheSame(dialogCalendar, selectedDateOfDisplayedActivities))
            refreshActivitiesContainer()
        activity!!.showToastMessage(getString(R.string.taskAddedSuccessfullyPrompt))
    }

    private fun initArrowListeners() {
        leftArrow.setOnClickListener {
            activity!!.circleHighlightClickedView(leftArrow)
            selectedDateOfDisplayedActivities.add(Calendar.DATE, -1)
//            beginTransition(homeFragmentParent)
            refreshActivitiesContainer()
        }
        rightArrow.setOnClickListener {
            activity!!.circleHighlightClickedView(rightArrow)
            selectedDateOfDisplayedActivities.add(Calendar.DATE, 1)
//            beginTransition(homeFragmentParent)
            refreshActivitiesContainer()
        }
    }

    private fun getDateStringOfDisplayedTasks(): String {
        if(datesAreTheSame(selectedDateOfDisplayedActivities, getTodaysDate()))
            return getString(R.string.todayHeader)
        if(isTomorrowsDate(selectedDateOfDisplayedActivities))
            return getString(R.string.tomorrowHeader)
        val year = selectedDateOfDisplayedActivities.get(Calendar.YEAR)
        val month = selectedDateOfDisplayedActivities.get(Calendar.MONTH)
        val day = selectedDateOfDisplayedActivities.get(Calendar.DAY_OF_MONTH)
        return "${month + 1}/$day/$year"
    }

    private fun updateCompletedTasks(isCompleted: Boolean, contactId: Int,
                                     taskId: Int, task: Task) {
        if(isCompleted) {
            val viewAdded = addViewIfBeforeLastViewInContainer(task)
            if (!viewAdded) addActivityToCompletedList(task)
        } else {
            val viewRemoved = removeViewIfItExists(contactId, taskId)
            if(!viewRemoved) removeTaskFromCompletedList(contactId, taskId)
        }
    }

    private fun addViewIfBeforeLastViewInContainer(task: Task): Boolean {
        var dividerLineVisibility = View.VISIBLE
        var indexToAdd = SENTINEL
        for((index, view) in completedTasksContainer.iterator().withIndex()) {
            val dateText = view.findViewById<TextView>(R.id.dateText)
            val dateTextContents = dateText.text.toString().split("/")
            val viewMonth = dateTextContents[0].toInt()
            val viewDay = dateTextContents[1].toInt()
            val viewYear = dateTextContents[2].toInt()
            val viewDate: Calendar = Calendar.getInstance(TimeZone.getDefault())
            val viewDividerLine = view.findViewById<LinearLayout>(R.id.dividerLine)
            viewDate.set(Calendar.YEAR, viewYear)
            viewDate.set(Calendar.MONTH, viewMonth - 1)
            viewDate.set(Calendar.DAY_OF_MONTH, viewDay)
            val viewTime = viewDate.timeInMillis
            val selectedTime = task.getDate().timeInMillis
            if(datesAreTheSame(task.getDate(), viewDate) || viewTime <= selectedTime) {
                if(index == 0) {
                    viewDividerLine.visibility = View.VISIBLE
                    dividerLineVisibility = View.GONE
                } else dividerLineVisibility = View.VISIBLE
                indexToAdd = index
                break
            }
        }
        val viewAdded = indexOfNextCompletedTaskToShow == SENTINEL
        if(viewAdded) view.addCompletedActivityView(task, indexToAdd, dividerLineVisibility)
        return viewAdded
    }

    private fun addActivityToCompletedList(task: Task) {
        var activityAdded = false
        for(i in indexOfNextCompletedTaskToShow until completedTasks.size) {
            val completedActivity = completedTasks[i]
            if(completedActivity.getDate().timeInMillis <= task.getDate().timeInMillis) {
                completedTasks.add(i, task)
                activityAdded = true
                break
            }
        }
        if(!activityAdded) completedTasks.add(task)
    }

    private fun removeTaskFromCompletedList(toDoListId: Int, taskId: Int) {
        for(i in indexOfNextCompletedTaskToShow until completedTasks.size) {
            val task = completedTasks[i]
            if(task.getToDoListId() == toDoListId && task.getTaskId() == taskId) {
                completedTasks.removeAt(i)
                break
            }
        }
        if(indexOfNextCompletedTaskToShow == completedTasks.size)
            indexOfNextCompletedTaskToShow = SENTINEL
    }

    private fun removeViewIfItExists(toDoListId: Int, taskId: Int): Boolean {
        var viewRemoved = false
        for(task in taskViewsMap.keys.iterator()) {
            if(task.getToDoListId() == toDoListId
                && task.getTaskId() == taskId) {
                beginTransition(homeFragmentParent)
                completedTasksContainer.removeView(taskViewsMap[task])
                taskViewsMap.remove(task)
                if(indexOfNextCompletedTaskToShow != SENTINEL)
                    view.showNextCompletedTask(completedTasks,
                        indexOfNextCompletedTaskToShow)
                viewRemoved = true
                break
            }
        }
        if(completedTasksContainer.size == 0)
            noCompletedTasksText.visibility = View.VISIBLE
        else completedTasksContainer[0]
            .findViewById<LinearLayout>(R.id.dividerLine).visibility = View.GONE
        return viewRemoved
    }

    fun refreshHomePage() {
        toDoListEdited = true
        refreshActivitiesContainer()
        refreshCompletedActivitiesContainer()
    }

    private fun refreshCompletedActivitiesContainer() {
        taskViewsMap.clear()
        completedTasksContainer.removeAllViews()
        completedTasks = toDoListsMgr.getCompletedTasks()
        view.displayCompletedTasks(completedTasks)
    }

    private fun refreshActivitiesContainer() {
        view.displayTasks(toDoListsMgr.getTasks(selectedDateOfDisplayedActivities,
            overdueTasksSwitch.isChecked), getDateStringOfDisplayedTasks())
    }

    private fun initScrollViewListener(){
        scrollView.viewTreeObserver.addOnScrollChangedListener {
            if (scrollView.getChildAt(0).bottom <= scrollView.height + scrollView.scrollY
                && indexOfNextCompletedTaskToShow != SENTINEL)
                view.showNextBatchOfCompletedTasks(completedTasks,
                    indexOfNextCompletedTaskToShow)
        }
    }

    private fun changeDateOfActivities(iconClicked: Boolean) {
        if(iconClicked) activity!!.circleHighlightClickedView(calendarButton)
        else activity!!.highlightClickedView(dateOfActivitiesText)
        dialogsMgr.showDatePickerDialog(selectedDateOfDisplayedActivities)
    }

    fun getSelectedDateOfDisplayedActivities(): Calendar {
        return selectedDateOfDisplayedActivities
    }

    fun openContactInfoActivity(contactId: Int) {
        val toDoListPage = Intent(context, ToDoListActivity::class.java)
        toDoListPage.putExtra(TO_DO_LIST_ID_REF, contactId)
        startActivityForResult(toDoListPage, requestCode)
    }

    fun getToDoListTitle(toDoListId: Int): String {
        return toDoListsMgr.getToDoList(toDoListId).getListTitle()
    }

    fun showEditTaskDialog(taskText: TextView, task: Task) {
        dialogsMgr.showEditTaskDialog(taskText, task)
    }

    fun updateTask(editedTask: String, updatedTask: Task) {
        val toDoList = toDoListsMgr.getToDoList(updatedTask.getToDoListId())
        val tasks = toDoList.getTasks()
        for(task in tasks) {
            if(task.getTaskId() == updatedTask.getTaskId()) {
                task.setTask(editedTask)
                break
            }
        }
        toDoListsMgr.updateToDoList(toDoList.getToDoListId(), toDoList)
    }

    fun markActivityCompleted(isCompleted: Boolean, toDoListId: Int, taskId: Int) {
        val toDoList = toDoListsMgr.getToDoList(toDoListId)
        val tasks = toDoList.getTasks()
        for(task in tasks) {
            if(task.getTaskId() == taskId) {
                if(updateDateWhenCompletedSwitch.isChecked)
                    task.setDate(getTodaysDate())
                task.markCompleted(isCompleted)
                updateCompletedTasks(isCompleted, toDoListId, taskId, task)
                break
            }
        }
        toDoListsMgr.updateToDoList(toDoListId, toDoList)
    }

    fun addTaskViewToMap(task: Task, view: View) {
        taskViewsMap[task] = view
    }

    fun setIndexOfNextCompletedActivityToShow(index: Int) {
        indexOfNextCompletedTaskToShow = index
    }

    fun checkIfToDoListsEdited(): Boolean {
        return toDoListEdited
    }

    fun setToDoListsEdited(contactsEdited: Boolean) {
        this.toDoListEdited = contactsEdited
    }

    fun openNewToDoListActivity() {
        val newToDoListPage = Intent(context, ToDoListActivity::class.java)
        startActivityForResult(newToDoListPage, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(data == null) return
        if(this.requestCode == requestCode) {
            val toDoListEdited = data.getBooleanExtra(TO_DO_LIST_EDITED_REF, true)
            if(toDoListEdited) refreshHomePage()
        }
    }

    override fun onDateSet(p0: DatePicker?, year: Int, month: Int, day: Int) {
        if(dialogDateTextView != null) {
            dialogCalendar.set(Calendar.YEAR, year)
            dialogCalendar.set(Calendar.MONTH, month)
            dialogCalendar.set(Calendar.DAY_OF_MONTH, day)
            dialogDateTextView?.text = DateFormat.getDateInstance(DateFormat.FULL).format(dialogCalendar.time)
        } else {
            selectedDateOfDisplayedActivities.set(Calendar.YEAR, year)
            selectedDateOfDisplayedActivities.set(Calendar.MONTH, month)
            selectedDateOfDisplayedActivities.set(Calendar.DAY_OF_MONTH, day)
//            beginTransition(homeFragmentParent)
            refreshActivitiesContainer()
        }
    }
}