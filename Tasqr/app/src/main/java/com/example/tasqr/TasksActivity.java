/*
 * TASKS ACTIVITY
 * Contains  List of all tasks (target: only those in which the user takes part in) in a given project
 *              which can be clicked on to go to Subtask Activity
 *           Add new task button (target: visible only if user is project leader, but it's easier to debug right now)
 */

package com.example.tasqr;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.provider.ContactsContract;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.tasqr.classes.Project;
import com.example.tasqr.classes.Task;
import com.example.tasqr.classes.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.Query;
import com.google.firebase.firestore.core.QueryListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public class TasksActivity extends AppCompatActivity implements ConfirmationPopUp.ConfirmationListener{

    private static final String TAG = "TasksActivity";

    private boolean isLeader = false;
    private Project currProject;

    /* Class for sorting purposes */
    private static class DisplayArrayElement implements Comparable<DisplayArrayElement>{
        private final String id;
        private final String name;
        private final boolean visible;
        private final int progress;

        private DisplayArrayElement(String id, String name, boolean visible, int progress) {
            this.id = id;
            this.name = name;
            this.visible = visible;
            this.progress = progress;
        }

        @Override
        public int compareTo(DisplayArrayElement o) {
            if (this.visible == o.visible)
                return Integer.compare(o.progress, this.progress);
            if (this.visible)
                return -1;
            return 1;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public boolean isVisible() {
            return visible;
        }

        public int getProgress() {
            return progress;
        }
    }

    /*  Custom Array Adapter */
    private static class TaskList extends ArrayAdapter {

        private final ArrayList<DisplayArrayElement> displayArray;

        private final Activity context;

        public TaskList(Activity context, ArrayList<DisplayArrayElement> displayArray) {
            super(context, R.layout.project_list_item, displayArray);
            this.context = context;
            this.displayArray = displayArray;
        }

        /* Creates one row of ListView, consisting of task name */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            LayoutInflater inflater = context.getLayoutInflater();

            if(convertView == null && displayArray.get(position).isVisible())
                row = inflater.inflate(R.layout.task_list_item_visible, null, true);
            else if (convertView == null)
                row = inflater.inflate(R.layout.task_list_item_invisible, null, true);

            TextView taskName = row.findViewById(R.id.taskNameList);
            ProgressBar progressBar = row.findViewById(R.id.taskProg);

            taskName.setText(displayArray.get(position).getName());
            progressBar.setProgress(displayArray.get(position).getProgress());
            return row;
        }
    }
    /********END OF INNER CLASSES*********/

    private final FirebaseDatabase database = FirebaseDatabase.getInstance("https://tasqr-android-default-rtdb.europe-west1.firebasedatabase.app/");
    private FloatingActionButton addTaskButton;
    private SwipeRefreshLayout refreshLayout;
    private TextView projectName;
    private ListView taskList;
    private FloatingActionButton deleteButton, workerListButton, workerSettingsButton;
    private final ArrayList<DisplayArrayElement> displayArray = new ArrayList<>();
    private boolean deleteMode = false;

    /* Main on create method */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        /* Finding and setting xml elements */
        projectName = findViewById(R.id.projectNametsk);
        taskList = findViewById(R.id.taskList);
        addTaskButton = findViewById(R.id.addTaskButton);
        workerListButton = findViewById(R.id.workerListButton);
        workerSettingsButton = findViewById(R.id.workerSettingsButton);
        deleteButton = findViewById(R.id.trashButton);
        refreshLayout = findViewById(R.id.swipe_refresh);
        setRefresher();

        addTaskButton.setOnClickListener(v -> openAddTaskActivity());
        workerListButton.setOnClickListener(v -> showWorkerListPopUp());
        workerSettingsButton.setOnClickListener(v -> showWorkerSettingsPopup());
        deleteButton.setOnClickListener(v -> deleteAction());

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
    private void fetchActivityData() {
        /* Database fetch */
        DatabaseReference projectRef = database.getReference("Projects/" + getIntent().getStringExtra("projectId"));

        projectRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currProject = snapshot.getValue(Project.class);
                String logged_mail = getIntent().getStringExtra("logged_mail");
                if (currProject.getLeaders().contains(logged_mail))
                    isLeader = true;
                else
                    addTaskButton.setOnClickListener(null);

                projectName.setText(snapshot.getValue(Project.class).getName());
                /* Getting all project tasks */
                if(snapshot.getValue(Project.class).getTasks() != null && snapshot.getValue(Project.class).getTasks().size() != 0) {
                    HashSet<String> taskIds =  new HashSet<>(snapshot.getValue(Project.class).getTasks());
                    /* Nested query to avoid nulls from multithreading */
                    /* We get all the tasks */
                    database.getReference("Tasks").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            ArrayList<Task> tasks = new ArrayList<>(taskIds.size());
                            /* We match the project task ids to ids present in Tasks */
                            for (DataSnapshot task: snapshot.getChildren())
                                if (taskIds.contains(task.getValue(Task.class).getId()))
                                    tasks.add(task.getValue(Task.class));

                            /* Setting up display array */
                            for (Task task : tasks) {
                                boolean found = false;
                                /* Determining visibility for every list element */
                                for (String user: task.getWorkers()){
                                    if (user.equals(getIntent().getStringExtra("logged_mail"))){
                                        found = true;
                                        break;
                                    }
                                }
                                displayArray.add(new DisplayArrayElement(task.getId(), task.getTaskName(), found, task.getProgress()));
                            }
                            Collections.sort(displayArray);
                            taskList.setAdapter(new TaskList(TasksActivity.this, displayArray));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Utilities.toastMessage("error" + error.toString(), TasksActivity.this);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utilities.toastMessage("error" + error.toString(), TasksActivity.this);
            }
        });
    }

    /* Opens activity for adding new task */
    private void openAddTaskActivity() {
        Intent addTaskIntent = new Intent(TasksActivity.this, AddTaskActivity.class);
        addTaskIntent.putExtra("projectId", getIntent().getStringExtra("projectId"));
        addTaskIntent.putExtra("logged_mail", getIntent().getStringExtra("logged_mail"));
        addTaskIntent.putExtra("logged_name", getIntent().getStringExtra("logged_name"));
        addTaskIntent.putExtra("logged_surname", getIntent().getStringExtra("logged_surname"));
        startActivity(addTaskIntent);
    }

    /* Opens sub task activity */
    private void openSubTaskActivity(int position){
        if (!displayArray.get(position).isVisible())
            return;

        Intent subTaskIntent = new Intent(TasksActivity.this, SubTasksActivity.class);
        subTaskIntent.putExtra("taskId", displayArray.get(position).getId());
        subTaskIntent.putExtra("taskName", displayArray.get(position).getName());
        subTaskIntent.putExtra("logged_mail", getIntent().getStringExtra("logged_mail"));
        startActivity(subTaskIntent);
    }

    /* Refreshes activity with recent data */
    private void setRefresher() {
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                displayArray.clear();

                fetchActivityData();
                refreshLayout.setRefreshing(false);
            }
        });
    }

    /* Shows popup with list of workers */
    private void showWorkerListPopUp() {
        WorkerListPopUp popUp = new WorkerListPopUp(database.getReference("Projects/" + getIntent().getStringExtra("projectId") + "/workers"));
        popUp.show(getSupportFragmentManager(), "worker list");
    }

    /* Shows popup with team leader activities */
    private void showWorkerSettingsPopup() {
        Bundle bundle = new Bundle();
        bundle.putString("logged_mail", getIntent().getStringExtra("logged_mail"));
        bundle.putString("logged_name", getIntent().getStringExtra("logged_name"));
        bundle.putString("logged_surname", getIntent().getStringExtra("logged_surname"));
        bundle.putString("company_name", getIntent().getStringExtra("company_name"));
        bundle.putParcelable("project", currProject);
        bundle.putString("project_id", getIntent().getStringExtra("projectId"));
        bundle.putBoolean("isLeader", isLeader);

        ManageProjectPopUp popUp = new ManageProjectPopUp();
        popUp.setArguments(bundle);
        popUp.show(getSupportFragmentManager(), "ManageProjectPopUp");
    }


    /* Sets state of activity to delete mode */
    private void deleteAction(){
        if (!isLeader)
            return;

        if (deleteMode)
            unsetDelete();
        else
            setDelete();
    }

    /* Sets delete mode of activity */
    private void setDelete(){
        deleteMode = true;
        deleteButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.delete_red)));
        taskList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bndl = new Bundle();
                bndl.putString("text", "ARE YOU SURE YOU WANT TO DELETE " + displayArray.get(position).getName());
                ConfirmationPopUp confirmationPopUp = new ConfirmationPopUp(displayArray.get(position).getName(), position);
                confirmationPopUp.setArguments(bndl);
                confirmationPopUp.show(getSupportFragmentManager(), "ConfirmationPopUp");
            }
        });
    }

    /* Unsets delete mode of activity */
    private void unsetDelete(){
        deleteMode = false;
        deleteButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.text_color)));
        taskList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openSubTaskActivity(position);
            }
        });
    }

    /* After deletion confirmation we delete given item from database */
    @Override
    public void confirmation(int position){
        unsetDelete();
        DatabaseReference taskRef = database.getReference("Tasks/" + displayArray.get(position).getId());
        DatabaseReference projectRef = database.getReference("Projects/" + getIntent().getStringExtra("projectId"));

        projectRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                snapshot.getValue(Project.class).deleteTask(projectRef, displayArray.get(position).getId());
                taskRef.removeValue();
                displayArray.remove(position);
                Collections.sort(displayArray);
                taskList.setAdapter(new TaskList(TasksActivity.this, displayArray));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utilities.toastMessage("error" + error.toString(), TasksActivity.this);
            }
        });
    }
}
