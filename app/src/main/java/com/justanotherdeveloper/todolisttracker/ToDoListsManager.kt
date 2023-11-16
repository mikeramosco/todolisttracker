package com.justanotherdeveloper.todolisttracker

import android.content.Context
import java.util.*
import kotlin.collections.ArrayList

class ToDoListsManager(context: Context) {

    private val tinyDB = TinyDB(context)

    fun saveToDoList(toDoList: ToDoList) {
        val ids = tinyDB.getListInt(IDS_FILENAME)
        val id = generateId(ids)
        val taskIds = toDoList.getTaskIds()

        toDoList.setToDoListId(id)

        for(task in toDoList.getTasks()) {
            val taskId = generateId(taskIds)
            taskIds.add(taskId)
            task.setTaskId(taskId)
            task.setToDoListId(id)
        }

        ids.add(id)

        tinyDB.putListInt(IDS_FILENAME, ids)
        tinyDB.putObject(id.toString(), toDoList)
    }

    fun updateToDoList(id: Int, toDoList: ToDoList) {
        tinyDB.putObject(id.toString(), toDoList)
    }

    fun getToDoList(id: Int): ToDoList {
        return tinyDB.getObject(id.toString(), ToDoList::class.java)
    }

    fun deleteToDoList(id: Int) {
        val ids = tinyDB.getListInt(IDS_FILENAME)
        ids.remove(id)

        tinyDB.putListInt(IDS_FILENAME, ids)
        tinyDB.remove(id.toString())
    }

    fun getTasks(date: Calendar, showOverdueTasks: Boolean): ArrayList<Task> {
        val ids = tinyDB.getListInt(IDS_FILENAME)
        val tasks = ArrayList<Task>()

        if(showOverdueTasks && datesAreTheSame(date, getTodaysDate())) {
            for (id in ids) {
                val toDoList = getToDoList(id)
                val toDoListTasks = toDoList.getTasks()
                for (activity in toDoListTasks) {
                    val activityDate = activity.getDate()
                    if (!datesAreTheSame(activityDate, date) && !activity.isCompleted()
                        && activityDate.timeInMillis < date.timeInMillis)
                        tasks.add(activity)
                }
            }
        }

        for(id in ids) {
            val toDoList = getToDoList(id)
            val toDoListTasks = toDoList.getTasks()
            for(task in toDoListTasks) {
                val activityDate = task.getDate()
                if(datesAreTheSame(activityDate, date) && !task.isCompleted())
                    tasks.add(task)
            }
        }

        for(id in ids) {
            val toDoList = getToDoList(id)
            val toDoListTasks = toDoList.getTasks()
            for(task in toDoListTasks) {
                val activityDate = task.getDate()
                if(datesAreTheSame(activityDate, date) && task.isCompleted())
                    tasks.add(task)
            }
        }
        return tasks
    }

    fun getCompletedTasks(): ArrayList<Task> {
        val ids = tinyDB.getListInt(IDS_FILENAME)
        val completedTasks = ArrayList<Task>()
        for(id in ids) {
            val toDoList = getToDoList(id)
            val tasks = toDoList.getTasks()
            for (task in tasks)
                if(task.isCompleted()) {
                    var taskAdded = false
                    for((index, completedTask) in completedTasks.withIndex()) {
                        if(completedTask.getDate().timeInMillis <= task.getDate().timeInMillis) {
                            completedTasks.add(index, task)
                            taskAdded = true
                            break
                        }
                    }
                    if(!taskAdded) completedTasks.add(task)
                }
        }
        return completedTasks
    }

    fun getToDoListsAToZ(): ArrayList<ToDoList> {
        return getToDoLists(0)
    }

    fun getToDoListsZToA(): ArrayList<ToDoList> {
        return getToDoLists(1)
    }

    fun getToDoListsNewestFirst(): ArrayList<ToDoList> {
        return getToDoLists(2)
    }

    fun getToDoListsOldestFirst(): ArrayList<ToDoList> {
        return getToDoLists(3)
    }

    private fun getToDoLists(sortByIndex: Int): ArrayList<ToDoList> {
        val ids = tinyDB.getListInt(IDS_FILENAME)
        val toDoLists = ArrayList<ToDoList>()

        for(id in ids) {
            val toDo = getToDoList(id)
            when(sortByIndex) {
                0 -> addToDoListAlphabetically(toDo, toDoLists, true)
                1 -> addToDoListAlphabetically(toDo, toDoLists, false)
                2 -> addToDoListByRecency(toDo, toDoLists, true)
                else -> addToDoListByRecency(toDo, toDoLists, false)
            }
        }

        return toDoLists
    }

    private fun addToDoListAlphabetically(toDoListToAdd: ToDoList, toDoLists: ArrayList<ToDoList>, alphabetizeAToZ: Boolean) {
        var contactAdded = false
        for((index, contact) in toDoLists.withIndex()) {
            val toDoListTitle = contact.getListTitle()
            if(alphabetizeAToZ) {
                if (toDoListToAdd.getListTitle().comesAlphabeticallyBefore(toDoListTitle)) {
                    toDoLists.add(index, toDoListToAdd)
                    contactAdded = true
                    break
                }
            } else {
                if (toDoListToAdd.getListTitle().comesAlphabeticallyAfter(toDoListTitle)) {
                    toDoLists.add(index, toDoListToAdd)
                    contactAdded = true
                    break
                }
            }
        }
        if(!contactAdded)
            toDoLists.add(toDoListToAdd)
    }

    private fun addToDoListByRecency(toDoListToAdd: ToDoList, toDoLists: ArrayList<ToDoList>, orderNewestFirst: Boolean) {
        var contactAdded = false
        for((index, toDoList) in toDoLists.withIndex()) {
            val toDoListDate = toDoList.getDateCreated()
            val toDoListToAddDate = toDoListToAdd.getDateCreated()
            if(orderNewestFirst) {
                if (toDoListToAddDate.comesAfter(toDoListDate)) {
                    toDoLists.add(index, toDoListToAdd)
                    contactAdded = true
                    break
                }
            } else {
                if (toDoListToAddDate.comesBefore(toDoListDate)) {
                    toDoLists.add(index, toDoListToAdd)
                    contactAdded = true
                    break
                }
            }
        }
        if(!contactAdded)
            toDoLists.add(toDoListToAdd)
    }
}