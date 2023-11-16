package com.justanotherdeveloper.todolisttracker

import android.content.Context

class SettingsManager(private val context: Context) {

    private val tinyDB = TinyDB(context)

    fun setShowOverdueTasksSwitch(isChecked: Boolean) {
        tinyDB.putBoolean(SHOW_OVERDUE_TASKS_REF, isChecked)
    }

    fun getShowOverdueTasksSwitch(): Boolean {
        return tinyDB.getBoolean(SHOW_OVERDUE_TASKS_REF)
    }

    fun setUpdateDateWhenCompleted(isChecked: Boolean) {
        tinyDB.putBoolean(UPDATE_DATE_WHEN_COMPLETED_REF, isChecked)
    }

    fun getUpdateDateWhenCompleted(): Boolean {
        return tinyDB.getBoolean(UPDATE_DATE_WHEN_COMPLETED_REF)
    }
}