package com.justanotherdeveloper.todolisttracker;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.Objects;

public class DatePickerFragment extends DialogFragment {

    private ToDoListActivity toDoListActivity;
    private HomeFragment homeFragment;

    private int year, month, day;

    public DatePickerFragment(int year, int month, int day,
                              ToDoListActivity toDoListActivity) {
        this.year = year;
        this.month = month;
        this.day = day;

        this.toDoListActivity = toDoListActivity;
    }

    public DatePickerFragment(int year, int month, int day,
                              HomeFragment homeFragment) {
        this.year = year;
        this.month = month;
        this.day = day;

        this.homeFragment = homeFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        DatePickerDialog dialog;
        if(toDoListActivity != null) {
            dialog = new DatePickerDialog(Objects.requireNonNull(getActivity()),
                    (DatePickerDialog.OnDateSetListener) getActivity(), year, month, day);
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancelString), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_NEGATIVE)
                        toDoListActivity.datePickerDialogCancelClicked();
                }
            });
        } else dialog = new DatePickerDialog(Objects.requireNonNull(homeFragment.getContext()), homeFragment, year, month, day);
        return dialog;
    }
}
