package com.justanotherdeveloper.todolisttracker

import java.util.*
import kotlin.collections.ArrayList

class ToDoList {

    private lateinit var listTitle: String
    private lateinit var dateCreated: Calendar
    private var toDoListId = SENTINEL
    private var taskIds = ArrayList<Int>()
    private val tasks = ArrayList<Task>()
    private var starred = false
    private var notes = ""

    fun saveToDoList(listTitle: String, dateCreated: Calendar) {
        this.listTitle = listTitle
        this.dateCreated = dateCreated
    }

    fun getNotes(): String {
        return notes
    }

    fun setNotes(notes: String) {
        this.notes = notes
    }

    fun isStarred(): Boolean {
        return starred
    }

    fun setStarred(starred: Boolean) {
        this.starred = starred
    }

    fun getDateCreated(): Calendar {
        return dateCreated
    }

    fun getListTitle(): String {
        return listTitle
    }

    fun setListTitle(listTitle: String) {
        this.listTitle = listTitle
    }

    fun getToDoListId(): Int {
        return toDoListId
    }

    fun setToDoListId(toDoListId: Int) {
        this.toDoListId = toDoListId
    }

    fun getTaskIds(): ArrayList<Int> {
        return taskIds
    }

    fun getTasks(): ArrayList<Task> {
        return tasks
    }
}