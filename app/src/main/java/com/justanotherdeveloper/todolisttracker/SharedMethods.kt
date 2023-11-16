package com.justanotherdeveloper.todolisttracker

import android.app.Activity
import android.graphics.drawable.Drawable
import android.os.Handler
import android.transition.TransitionManager
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.util.*

fun beginTransition(layout: LinearLayout) {
    TransitionManager.beginDelayedTransition(layout)
}

fun generateId(ids: ArrayList<Int>): Int {
    val id = Random().nextInt((END_RANDOM_ID + 1) - START_RANDOM_ID) + START_RANDOM_ID
    return if(ids.contains(id)) generateId(ids) else id
}

fun Activity.showToastMessage(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Activity.highlightClickedView(view: View, color: Int =
    ContextCompat.getColor(this, R.color.highlightColor), originalColor: Int =
                                      ContextCompat.getColor(this, R.color.transparent)) {
    view.setBackgroundColor(color)
    Handler().postDelayed({
        view.setBackgroundColor(originalColor)
    }, HIGHLIGHT_DURATION)
}

fun Activity.circleHighlightClickedView(view: View, originalBackground: Drawable? = null) {
    view.background = getDrawable(R.drawable.highlight_circle)
    Handler().postDelayed({
        view.background = originalBackground
    }, HIGHLIGHT_DURATION)
}

fun getTodaysDate(): Calendar {
    return Calendar.getInstance(TimeZone.getDefault())
}

fun datesAreTheSame(date1: Calendar, date2: Calendar): Boolean {
    return date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR) &&
            date1.get(Calendar.MONTH) == date2.get(Calendar.MONTH) &&
            date1.get(Calendar.DAY_OF_MONTH) == date2.get(Calendar.DAY_OF_MONTH)
}

fun isTomorrowsDate(date: Calendar): Boolean {
    val tomorrowsDate = getTodaysDate()
    tomorrowsDate.add(Calendar.DATE, 1)
    return datesAreTheSame(date, tomorrowsDate)
}

fun Activity.circleSelectClickedView(view: View) {
    view.background = getDrawable(R.drawable.highlight_circle)
}

fun String.comesAlphabeticallyBefore(str: String): Boolean {
    return compareTo(str, true) <= 0
}

fun String.comesAlphabeticallyAfter(str: String): Boolean {
    return compareTo(str, true) >= 0
}

fun Calendar.comesBefore(date: Calendar): Boolean  {
    return timeInMillis <= date.timeInMillis
}

fun Calendar.comesAfter(date: Calendar): Boolean {
    return timeInMillis >= date.timeInMillis
}