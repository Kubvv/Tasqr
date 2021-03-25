package com.example.tasqr;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AddTaskActivity extends AppCompatActivity {

    private FloatingActionButton addPeopleButton;
    private TextView taskName;

    private Bundle bundle = new Bundle();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addtask);

        taskName = findViewById(R.id.taskName);
        addPeopleButton = findViewById(R.id.addPeopleButtontsk);

        addPeopleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addPeopleIntent = new Intent(AddTaskActivity.this, AddUsersActivity.class);
                addPeopleIntent.putExtra("projectId", getIntent().getStringExtra("projectId"));
                addPeopleIntent.putExtra("previous_activity", "Task");
                addPeopleIntent.putExtra("taskName", taskName.getText().toString());
                addPeopleIntent.putExtra("logged_mail", getIntent().getStringExtra("logged_mail"));
                startActivity(addPeopleIntent);
            }
        });
    }

    /* Resume previous inputs in textfields */
    protected void onResume() {
        taskName.setText(bundle.getString("taskName"));
        super.onResume();
    }

    /* Saves inputs from textfields */
    protected void onPause() {
        bundle.putString("taskName", taskName.getText().toString());
        super.onPause();
    }

}
