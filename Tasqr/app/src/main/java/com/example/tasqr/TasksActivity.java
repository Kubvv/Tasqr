/*
 * TASKS ACTIVITY
 * Contains  List of all tasks (target: only those in which the user takes part in) in a given project
 *              which can be clicked on to go to Subtask Activity
 *           Add new task button (target: visible only if user is project leader, but it's easier to debug right now)
 */

package com.example.tasqr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tasqr.classes.Project;
import com.example.tasqr.classes.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class TasksActivity extends AppCompatActivity {

    private static final String TAG = "TasksActivity";

    /*  Custom Array Adapter */
    private static class TaskList extends ArrayAdapter {

        private final ArrayList<String> taskString;
        private final ArrayList<Integer> progress;
        private final ArrayList<Boolean> visibility;

        private final Activity context;

        public TaskList(Activity context, ArrayList<String> taskString, ArrayList<Integer> progress, ArrayList<Boolean> visibility) {
            super(context, R.layout.project_list_item, taskString);
            this.context = context;
            this.taskString = taskString;
            this.progress = progress;
            this.visibility = visibility;
        }

        /* Creates one row of ListView, consisting of task name */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            LayoutInflater inflater = context.getLayoutInflater();

            if(convertView == null && visibility.get(position))
                row = inflater.inflate(R.layout.task_list_item_visible, null, true);
            else if (convertView == null)
                row = inflater.inflate(R.layout.task_list_item_invisible, null, true);

            TextView taskName = row.findViewById(R.id.taskNameList);
            ProgressBar progressBar = row.findViewById(R.id.taskProg);

            taskName.setText(taskString.get(position));
            progressBar.setProgress(progress.get(position));
            return row;
        }
    }

    private TextView projectName;
    private ListView taskList;
    ArrayList<String> displayStringArray = new ArrayList<>();
    ArrayList<Boolean> displayVisibilityArray = new ArrayList<>();
    ArrayList<Integer> displayProgressArray = new ArrayList<>();

    /* Main on create method */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        /* Finding and setting xml elements */
        projectName = findViewById(R.id.projectNametsk);
        taskList = findViewById(R.id.taskList);
        FloatingActionButton addTaskButton = findViewById(R.id.addTaskButton);

        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddSubTaskActivity();
            }
        });

        taskList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openSubTaskActivity(position);
            }
        });

        /* Fetching tasks */
        fetchActivityData();
    }

    /* Fetches task list into displayArray for a given project */
    private void fetchActivityData()
    {
        /* Database fetch */
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://tasqr-android-default-rtdb.europe-west1.firebasedatabase.app/");
        DatabaseReference projectRef = database.getReference("Projects/" + getIntent().getStringExtra("projectId"));
        Log.e(TAG, "projectId is: " + getIntent().getStringExtra("projectId"));

        projectRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                projectName.setText(snapshot.getValue(Project.class).getName());
                ArrayList<Task> tasks = snapshot.getValue(Project.class).getTasks();

                if(tasks != null && tasks.size() != 0) {
                    for (Task task : tasks) {
                        boolean found = false;
                        /* Determining visibility for every list element */
                        for (String user: task.getWorkers()){
                            if (user.equals(getIntent().getStringExtra("logged_mail"))){
                                found = true;
                                break;
                            }
                        }

                        displayStringArray.add(task.getTaskName());
                        displayProgressArray.add(task.getProgress());
                        displayVisibilityArray.add(found);
                    }
                    taskList.setAdapter(new TaskList(TasksActivity.this, displayStringArray, displayProgressArray, displayVisibilityArray));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utilities.toastMessage("error" + error.toString(), TasksActivity.this);
            }
        });
    }

    /* Opens activity for adding new task */
    private void openAddSubTaskActivity() {
        Intent addTaskIntent = new Intent(TasksActivity.this, AddTaskActivity.class);
        addTaskIntent.putExtra("projectId", getIntent().getStringExtra("projectId"));
        addTaskIntent.putExtra("logged_mail", getIntent().getStringExtra("logged_mail"));
        startActivity(addTaskIntent);
    }

    /* Opens sub task activity */
    private void openSubTaskActivity(int position){
        if (!displayVisibilityArray.get(position))
            return;

        Intent subTaskIntent = new Intent(TasksActivity.this, SubTasksActivity.class);
        subTaskIntent.putExtra("taskPosition", Integer.toString(position));
        subTaskIntent.putExtra("taskName", displayStringArray.get(position));
        subTaskIntent.putExtra("projectId", getIntent().getStringExtra("projectId"));
        startActivity(subTaskIntent);
    }
}
