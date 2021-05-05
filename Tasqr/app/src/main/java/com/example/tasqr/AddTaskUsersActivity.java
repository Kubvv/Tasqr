/*
 *   ADD TASK USERS ACTIVITY
 *   CONTAINS    list of users tied to a given project
 *               button to add new task to database
 * */
package com.example.tasqr;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tasqr.classes.Project;
import com.example.tasqr.classes.Task;
import com.example.tasqr.classes.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;

public class AddTaskUsersActivity extends AppCompatActivity {

    private static final String TAG = "AddTaskUsersActivity";

    /* Owner of currently creating project [not in userArray!]*/
    private String leader;

    private Project currProject;

    /* Arraylists used for creating user listView */
    private ArrayList<String> userArray = new ArrayList<>();
    private ArrayList<String> displayArray = new ArrayList<>();

    private ListView listView;

    /* Firebase database */
    private FirebaseDatabase database;
    private DatabaseReference projectRef;

    private ImageButton tmpImg;
    private final Integer[] avatars = {R.drawable.avatar, R.drawable.avatar2};
    private int currentPhoto = 0;

    /* Main on create method */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addtaskusersactivity);

        tmpImg = findViewById(R.id.snickerstsk);
        tmpImg.setImageResource(avatars[currentPhoto]);

        tmpImg.setOnClickListener(v -> finishAddingTask());

        listView = (ListView)findViewById(R.id.userlisttsk);

        /* Establish connection to database and some references */
        database = FirebaseDatabase.getInstance("https://tasqr-android-default-rtdb.europe-west1.firebasedatabase.app/");
        projectRef = database.getReference("Projects/" + getIntent().getStringExtra("projectId"));
        DatabaseReference usersRef = database.getReference("Users");

        /* Get current project */
        projectRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currProject = snapshot.getValue(Project.class);
                userArray = currProject.getWorkers();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error);
            }
        });

        /* Get users into list */
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if(i == userArray.size())
                        break;

                    User user = ds.getValue(User.class);

                    if(user.getMail().equals(userArray.get(i))) {
                        if (!user.getMail().equals(getIntent().getStringExtra("logged_mail")))
                            displayArray.add(user.getName() + " " + user.getSurname());
                        else
                            leader = user.getMail();
                        i++;
                    }
                }

                /* create some weird adapter for list view */
                ArrayAdapter<String> adapter = new ArrayAdapter(AddTaskUsersActivity.this, android.R.layout.simple_list_item_multiple_choice, displayArray);

                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error);
            }
        });
    }

    /* Add new task with all checked people tied to it */
    private void finishAddingTask () {
        currentPhoto = (currentPhoto + 1) % 2;
        tmpImg.setImageResource(avatars[currentPhoto]);

        ArrayList<String> taskUsers = new ArrayList<>();
        taskUsers.add(leader);

        SparseBooleanArray checked = listView.getCheckedItemPositions();
        for (int i = 0; i < listView.getCount(); i++)
            if (checked.get(i))
                taskUsers.add(userArray.get(i));

        /* Create new task to be added */
        Task newTask = new Task(getIntent().getStringExtra("taskName"), leader, getIntent().getStringExtra("projectId"), taskUsers, new Date(), 0);
        currProject.addTask(AddTaskUsersActivity.this, projectRef, newTask);

        openTasksActivity();

    }

    /* Opens main activity and closes activites relating to adding project */
    private void openTasksActivity() {
        Intent intent = new Intent(AddTaskUsersActivity.this, TasksActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("projectId", getIntent().getStringExtra("projectId"));
        intent.putExtra("logged_mail", getIntent().getStringExtra("logged_mail"));
        startActivity(intent);
    }
}