/*
*   ADD TASK ACTIVITY
*   Activity to add task to project
*   CONTAINS        EditText form for task name
*                   Button next step button
*                   Calendar choosing deadline
* */

package com.example.tasqr;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.Date;

public class AddTaskActivity extends AppCompatActivity {

    private TextView taskName;

    private CalendarView calendarView;
    private int selectedYear;
    private int selectedMonth;
    private int selectedDay;

    private final Bundle bundle = new Bundle();

    /* MAIN ON CREATE METHOD */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addtask);

        /* Random values */
        selectedYear = 2021;
        selectedMonth = 5;
        selectedDay = 8;

        /* Finding views and setting initial values */
        taskName = findViewById(R.id.taskName);
        FloatingActionButton addPeopleButton = findViewById(R.id.addPeopleButtontsk);
        calendarView = findViewById(R.id.calendar);
        calendarView.setDate(System.currentTimeMillis());

        /* Changing */
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                selectedYear = year;
                selectedMonth = month;
                selectedDay = dayOfMonth;
            }
        });

        /* Sending all useful data inside intent */
        addPeopleButton.setOnClickListener(v -> startAddPeopleActivity());
    }

    /* ADD PEOPLE INTENT STARTER */
    private void startAddPeopleActivity(){
        Intent addPeopleIntent = new Intent(AddTaskActivity.this, AddUsersActivity.class);
        addPeopleIntent.putExtra("projectId", getIntent().getStringExtra("projectId"));
        addPeopleIntent.putExtra("previous_activity", "Task");
        addPeopleIntent.putExtra("taskName", taskName.getText().toString());
        addPeopleIntent.putExtra("logged_mail", getIntent().getStringExtra("logged_mail"));
        addPeopleIntent.putExtra("logged_name", getIntent().getStringExtra("logged_name"));
        addPeopleIntent.putExtra("logged_surname", getIntent().getStringExtra("logged_surname"));
        addPeopleIntent.putExtra("year", selectedYear);
        addPeopleIntent.putExtra("month", selectedMonth);
        addPeopleIntent.putExtra("day", selectedDay);
        startActivity(addPeopleIntent);
    }

    /* RESUME TEXTFIELDS */
    protected void onResume() {
        taskName.setText(bundle.getString("taskName"));
        super.onResume();
    }

    /* SAVE TEXTFIELDS */
    protected void onPause() {
        bundle.putString("taskName", taskName.getText().toString());
        super.onPause();
    }

}
