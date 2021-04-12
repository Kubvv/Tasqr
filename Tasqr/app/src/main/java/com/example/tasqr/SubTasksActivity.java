/*
 * SUBTASKS ACTIVITY
 * Contains  List of all subtasks in a given task
 *           Add new subtask button (target: visible only if user is project leader, but it's easier to debug right now)
 */

package com.example.tasqr;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tasqr.classes.SubTask;
import com.example.tasqr.classes.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SubTasksActivity extends AppCompatActivity implements AddSubTaskPopUp.AddSubTaskListener {

    private static final String TAG = "SubTaskActivity";

    private FirebaseDatabase database;

    private ListView subTaskList;
    private Button saveChangesButton;

    /* Main On Create Method */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subtasks);

        database = FirebaseDatabase.getInstance("https://tasqr-android-default-rtdb.europe-west1.firebasedatabase.app/");

        /* Find and set xml items */
        subTaskList = findViewById(R.id.subTaskList);
        FloatingActionButton addSubTaskButton = findViewById(R.id.addSubTaskButton);
        saveChangesButton = findViewById(R.id.saveSubTaskChangesButton);

        addSubTaskButton.setOnClickListener(v -> showPopUp());
        saveChangesButton.setOnClickListener(v -> saveStateChanges());
        subTaskList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                saveChangesButton.setVisibility(View.VISIBLE);
            }
        });

        fetchSubTaskData();
    }

    /* Saves subtask states into the database */
    private void saveStateChanges() {
        DatabaseReference taskRef = database.getReference("Projects/" + getIntent().getStringExtra("projectId") + "/tasks/" + getIntent().getStringExtra("taskPosition"));

        taskRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                snapshot.getValue(Task.class).setSubTasksState(SubTasksActivity.this, taskRef, subTaskList.getCheckedItemPositions());
                saveChangesButton.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utilities.toastMessage("error" + error.toString(), SubTasksActivity.this);
            }
        });
    }

    /* Data fetcher for subtask list inside subtask activity */
    private void fetchSubTaskData() {
        DatabaseReference taskRef = database.getReference("Projects/" + getIntent().getStringExtra("projectId") + "/tasks/" + getIntent().getStringExtra("taskPosition"));

        taskRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Task task = snapshot.getValue(Task.class);
                if (task != null) {
                    subTaskList.setAdapter(new ArrayAdapter<>(SubTasksActivity.this, android.R.layout.simple_list_item_multiple_choice, task.getSubTasksString()));

                    for (int i = 0; i < task.getSubTasks().size(); i++) {
                        boolean state = task.getSubTasks().get(i).getState() == SubTask.SubTaskState.done;
                        subTaskList.setItemChecked(i, state);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utilities.toastMessage("error" + error.toString(), SubTasksActivity.this);
            }
        });
    }

    /* Shows pop-up for adding new subtask */
    private void showPopUp() {
        AddSubTaskPopUp popUp = new AddSubTaskPopUp();
        popUp.show(getSupportFragmentManager(), "addSubTaskPopUp");
    }

    /* List refresher after adding new subtask */
    @Override
    public void sendSubTaskName(String subTaskName) {
        DatabaseReference taskRef = database.getReference("Projects/" + getIntent().getStringExtra("projectId") + "/tasks/" + getIntent().getStringExtra("taskPosition"));

        taskRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                snapshot.getValue(Task.class).addSubTask(SubTasksActivity.this, taskRef, new SubTask(subTaskName, SubTask.SubTaskState.pending));
                fetchSubTaskData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utilities.toastMessage("error" + error.toString(), SubTasksActivity.this);
            }
        });
    }
}
