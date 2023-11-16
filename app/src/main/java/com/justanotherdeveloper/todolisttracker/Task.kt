package com.justanotherdeveloper.todolisttracker

import java.util.*

class Task(private var task: String, private var date: Calendar,
           private var toDoListId: Int, private var taskId: Int) {

    private var isCompleted = false

    fun isCompleted(): Boolean {
        return isCompleted
    }

    fun markCompleted(isCompleted: Boolean) {
        this.isCompleted = isCompleted
    }

    fun getTask(): String {
        return task
    }

    fun setTask(task: String) {
        this.task = task
    }

    fun getToDoListId(): Int {
        return toDoListId
    }

    fun setToDoListId(toDoListId: Int) {
        this.toDoListId = toDoListId
    }

    fun getTaskId(): Int {
        return taskId
    }

    fun setTaskId(taskId: Int) {
        this.taskId = taskId
    }

    fun getDate(): Calendar {
        return date
    }

    fun setDate(date: Calendar) {
        this.date = date
    }
}