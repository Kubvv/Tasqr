/*
 * SUBTASKS ACTIVITY
 * Contains  List of all subtasks in a given task
 *           Add new subtask button (target: visible only if user is project leader, but it's easier to debug right now)
 */

package com.example.tasqr;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.tasqr.PopUps.AddSubTaskPopUp;
import com.example.tasqr.PopUps.ConfirmationPopUp;
import com.example.tasqr.PopUps.WorkerListPopUp;
import com.example.tasqr.Styling.CheckBoxTriState;
import com.example.tasqr.classes.SubTask;
import com.example.tasqr.classes.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SubTasksActivity extends AppCompatActivity implements AddSubTaskPopUp.AddSubTaskListener, ConfirmationPopUp.ConfirmationListener {

    private FirebaseDatabase database;

    private ListView subTaskList;
    private SubTaskList subTaskAdapter;
    private SwipeRefreshLayout refreshLayout;
    private FloatingActionButton addSubTaskButton;
    private FloatingActionButton deleteButton;
    private FloatingActionButton saveChangesButton;

    private boolean deleteMode;

    /* Custom SubTask array adapter */
    private class SubTaskList extends ArrayAdapter{

        private final ArrayList<String> displayArray;
        private final ArrayList<CheckBoxTriState> checkBoxes;
        private final ArrayList<SubTask.SubTaskState> initStates;
        private final Activity context;

        public SubTaskList(Activity context, ArrayList<String> displayArray, ArrayList<SubTask.SubTaskState> states) {
            super(context, R.layout.subtask_list_item, displayArray);
            this.context = context;
            this.displayArray = displayArray;
            this.initStates = states;
            checkBoxes = new ArrayList<>(states.size());
        }

        /* Creates one row of ListView, consisting of subtask name and checkbox */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            /* Set up */
            View row = convertView;
            LayoutInflater inflater = context.getLayoutInflater();
            if(convertView == null)
                row = inflater.inflate(R.layout.subtask_list_item, null, true);

            /* Find and set */
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bndl = new Bundle();
                    bndl.putString("text", "ARE YOU SURE YOU WANT TO DELETE " + subTaskAdapter.getArray().get(position));
                    ConfirmationPopUp confirmationPopUp = new ConfirmationPopUp(subTaskAdapter.getArray().get(position), position);
                    confirmationPopUp.setArguments(bndl);
                    confirmationPopUp.show(getSupportFragmentManager(), "ConfirmationPopUp");
                }
            });

            TextView subTaskName = row.findViewById(R.id.subTaskName);
            checkBoxes.add(row.findViewById(R.id.subTaskCheckbox));

            subTaskName.setText(displayArray.get(position));
            /* Setting checkbox state and listener */
            checkBoxes.get(position).setState(initStates.get(position).getValue());
            checkBoxes.get(position).setOnClickListener(v -> setSaveVisible());
            return row;
        }

        /* GET ALL STATES INTO AN ARRAY */
        public ArrayList<Integer> getCheckBoxStates(){
            ArrayList<Integer> returnArray = new ArrayList<>(checkBoxes.size());
            for (CheckBoxTriState checkbox: checkBoxes)
                returnArray.add(checkbox.getState());
            return returnArray;
        }

        /* SHOW SAVE BUTTON */
        private void setSaveVisible(){
            saveChangesButton.setOnClickListener(v -> saveStateChanges());
            saveChangesButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.save));
        }

        public ArrayList<String> getArray() {
            return displayArray;
        }
    }
    /*********END OF INNER CLASS*********/

    /* Main On Create Method */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subtasks);

        database = FirebaseDatabase.getInstance("https://tasqr-android-default-rtdb.europe-west1.firebasedatabase.app/");

        /* Find and set xml items */
        subTaskList = findViewById(R.id.subTaskList);
        addSubTaskButton = findViewById(R.id.addSubTaskButton);
        FloatingActionButton workerListButton = findViewById(R.id.workerListButton);
        FloatingActionButton workerSettingsButton = findViewById(R.id.workerSettingsButton);
        deleteButton = findViewById(R.id.trashButton);
        saveChangesButton = findViewById(R.id.saveSubTaskChangesButton);
        TextView projectName = findViewById(R.id.taskName);
        refreshLayout = findViewById(R.id.swipe_refresh);
        setRefresher();

        projectName.setText(getIntent().getStringExtra("taskName"));

        addSubTaskButton.setOnClickListener(v -> showAddPopUp());
        workerListButton.setOnClickListener(v -> showWorkerListPopUp());
        workerSettingsButton.setOnClickListener(v -> showWorkerSettingsPopup());
        deleteButton.setOnClickListener(v -> deleteAction());

        fetchSubTaskData();
    }

    /* Saves subtask states into the database */
    private void saveStateChanges() {
        DatabaseReference taskRef = database.getReference("Tasks/" + getIntent().getStringExtra("taskId"));

        taskRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                snapshot.getValue(Task.class)
                        .setSubTasksState(SubTasksActivity.this, taskRef, subTaskAdapter.getCheckBoxStates());
                saveChangesButton.setOnClickListener(null);
                saveChangesButton.setImageDrawable(ContextCompat.getDrawable(SubTasksActivity.this, R.drawable.check));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utilities.toastMessage("error" + error.toString(), SubTasksActivity.this);
            }
        });
    }

    private void noDataChangeFetch(Task task)
    {
        if (!task.getLeader().contains(getIntent().getStringExtra("logged_mail")))
            addSubTaskButton.setOnClickListener(null);

        if (task != null && task.getSubTasks() != null) {
            ArrayList<SubTask.SubTaskState> states = new ArrayList<>();
            for (int i = 0; i < task.getSubTasks().size(); i++)
                states.add(task.getSubTasks().get(i).getState());

            subTaskAdapter = new SubTaskList(SubTasksActivity.this, task.getSubTasksString(), states);
            subTaskList.setAdapter(subTaskAdapter);
        }
    }

    /* Data fetcher for subtask list inside subtask activity */
    private void fetchSubTaskData() {
        DatabaseReference taskRef = database.getReference("Tasks/" + getIntent().getStringExtra("taskId"));

        taskRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Task task = snapshot.getValue(Task.class);
                noDataChangeFetch(task);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utilities.toastMessage("error" + error.toString(), SubTasksActivity.this);
            }
        });
    }

    /* Shows pop-up for adding new subtask */
    private void showAddPopUp() {
        AddSubTaskPopUp popUp = new AddSubTaskPopUp();
        popUp.show(getSupportFragmentManager(), "addSubTaskPopUp");
    }

    /* List refresher after adding new subtask */
    @Override
    public void sendSubTaskName(String subTaskName) {
        DatabaseReference taskRef = database.getReference("Tasks/" + getIntent().getStringExtra("taskId"));

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

    /* Refreshes activity with recent data */
    private void setRefresher() {
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchSubTaskData();
                refreshLayout.setRefreshing(false);
            }
        });
    }

    /* Shows popup with list of workers */
    private void showWorkerListPopUp() {
        WorkerListPopUp popUp = new WorkerListPopUp(database.getReference("Tasks/" + getIntent().getStringExtra("taskId") + "/workers/"));
        popUp.show(getSupportFragmentManager(), "worker list");
    }

    /* Shows popup with team leader activities */
    private void showWorkerSettingsPopup(){
    }

    /* Sets state of activity to delete mode */
    private void deleteAction(){
        if (deleteMode)
            unsetDelete();
        else
            setDelete();
    }

    private void setDelete(){
        deleteMode = true;
        deleteButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.delete_red)));
    }

    private void unsetDelete(){
        deleteMode = false;
        deleteButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.text_color)));
        subTaskList.setOnItemClickListener(null);
    }

    @Override
    public void confirmation(int position){
        unsetDelete();
        DatabaseReference taskRef = database.getReference("Tasks/" + getIntent().getStringExtra("taskId"));
        taskRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Task task = snapshot.getValue(Task.class);
                task.deleteSubTask(SubTasksActivity.this, taskRef, position);
                noDataChangeFetch(task);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utilities.toastMessage("error " + error.toString(), SubTasksActivity.this);
            }
        });

    }

}
