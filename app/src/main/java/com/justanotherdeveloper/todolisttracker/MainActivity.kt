package com.justanotherdeveloper.todolisttracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val homeFragment = HomeFragment()
    private val toDoListsFragment = ToDoListsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottomNavigation.setOnNavigationItemSelectedListener(navListener)
        setupFragments()
    }

    private val navListener = BottomNavigationView.OnNavigationItemSelectedListener {
        var homeVisibility = View.GONE
        var toDoListsVisibility = View.GONE

        when(it.itemId) {
            R.id.nav_home -> homeVisibility = View.VISIBLE
            else -> toDoListsVisibility = View.VISIBLE
        }

        refreshFragmentsIfNeeded(it.itemId)

        homeFragmentContainer.visibility = homeVisibility
        toDoListsFragmentContainer.visibility = toDoListsVisibility
        true
    }

    private fun refreshFragmentsIfNeeded(itemId: Int) {
        if(itemId != R.id.nav_contacts && toDoListsFragmentContainer.visibility == View.VISIBLE)
            toDoListsFragment.resetRemoveButton()
        if(itemId == R.id.nav_home && homeFragmentContainer.visibility == View.GONE) {
            val toDoListsEdited = toDoListsFragment.checkIfToDoListsEdited()
            if(toDoListsEdited) {
                homeFragment.refreshHomePage()
                toDoListsFragment.setToDoListsEdited(false)
            }
        } else if(itemId == R.id.nav_contacts && toDoListsFragmentContainer.visibility == View.GONE) {
            val toDoListsEdited = homeFragment.checkIfToDoListsEdited()
            if(toDoListsEdited) {
                toDoListsFragment.refreshToDoLists()
                homeFragment.setToDoListsEdited(false)
            }
        }
    }

    private fun setupFragments() {
        supportFragmentManager.beginTransaction().replace(
            R.id.homeFragmentContainer, homeFragment).commit()
        supportFragmentManager.beginTransaction().replace(
            R.id.toDoListsFragmentContainer, toDoListsFragment).commit()
    }

    override fun onBackPressed() {
        if(toDoListsFragmentContainer.visibility == View.VISIBLE) {
            if (toDoListsFragment.iconsReset())
                bottomNavigation.selectedItemId = R.id.nav_home
        } else if(homeFragmentContainer.visibility == View.GONE)
            bottomNavigation.selectedItemId = R.id.nav_home
        else super.onBackPressed()
    }
}
