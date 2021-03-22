package com.example.tasqr;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AddUsersActivity extends AppCompatActivity {

    private static final String TAG = "AddUsersActivity";

    /* basic logged user info */
    private String logged_name;
    private String logged_surname;
    private String logged_mail;

    /* Owner of currently creating project [not in userArray!]*/
    private User owner;

    /* Arraylists used for creating user listView */
    private ArrayList<User> userArray = new ArrayList<>();
    private ArrayList<String> displayArray = new ArrayList<>();

    private Bundle bndl;
    private ListView listView;

    /* Firebase database */
    private FirebaseDatabase database;
    private DatabaseReference rootRef;
    private DatabaseReference usersRef;
    private DatabaseReference projectsRef;

    /* button image TO DO zmienic przed prezentacja */
    private ImageButton nigga;
    private Integer[] avatars = {R.drawable.avatarcircle, R.drawable.white, R.drawable.asian};
    private int currentPhoto = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addusers);

        bndl = getIntent().getExtras();
        logged_name = bndl.getString("logged_name");
        logged_surname = bndl.getString("logged_surname");
        logged_mail = bndl.getString("logged_mail");

        nigga = findViewById(R.id.snickers);
        nigga.setImageResource(avatars[currentPhoto]);

        nigga.setOnClickListener(v -> {
            finishAddingProject();
        });

        bndl = getIntent().getExtras();

        listView = (ListView)findViewById(R.id.userlist);

        /* establish connection to database and some references */
        database = FirebaseDatabase.getInstance("https://tasqr-android-default-rtdb.europe-west1.firebasedatabase.app/");
        rootRef = database.getReference();
        usersRef = rootRef.child("Users");
        projectsRef = rootRef.child("Projects");

        /* fetching all users in order to show them in a listviwe */
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                /* iterate through users and add each to array */
                int i = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    /* If user is not the owner show him on the list of users that can be added */
                    if (!user.getMail().equals(logged_mail)) {
                        userArray.add(user);
                        displayArray.add(user.getNameSurname());
                    } else {
                        owner = user;
                    }
                    i++;
                }

                /* create some weird adapter for list view */
                ArrayAdapter<String> adapter = new ArrayAdapter(AddUsersActivity.this, android.R.layout.simple_list_item_multiple_choice, displayArray);

                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error);
            }
        });

    }

    /* Adds project and all it's workers to database.
    * Also updates all added user's projects arrays.
    * If succesful, goes to MainActivity and closes all previous activities.*/
    private void finishAddingProject () {
        currentPhoto = (currentPhoto + 1) % 3;
        nigga.setImageResource(avatars[currentPhoto]);

        ArrayList<User> projectUsers = new ArrayList<>();
        projectUsers.add(owner);
        SparseBooleanArray checked = listView.getCheckedItemPositions();
        for (int i = 0; i < listView.getCount(); i++) {
            if (checked.get(i)) {
                projectUsers.add(userArray.get(i));
            }
        }

        /* Create new project to be added */
        Project project = new Project(
                bndl.getString("project_name"),
                bndl.getString("company_name"),
                bndl.getString("description"),
                owner,
                projectUsers);

        DatabaseReference pushedProjectsRef = projectsRef.push();
        String id = pushedProjectsRef.getKey();
        projectsRef.child(id).setValue(project).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {  Utilities.toastMessage("Successfully added new project", AddUsersActivity.this);
            }
        });

        /* add project to owner's projects array, then leave the activity */
        ArrayList<String> tmp = owner.getProjects();
        tmp.add(id);
        usersRef.child(owner.getMail()).child("projects").setValue(tmp).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                openMainActivity();
            }
        });

        /* meanwhile add project to all other invited users arrays */
        for (int i = 1; i < projectUsers.size(); i++) {
            tmp = projectUsers.get(i).getProjects();
            tmp.add(id);
            usersRef.child(projectUsers.get(i).getMail()).child("projects").setValue(tmp);
        }
    }

    /* Opens main activity and closes activites relating to adding project */
    private void openMainActivity() {
        Intent intent = new Intent(AddUsersActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("logged_name", logged_name);
        intent.putExtra("logged_surname", logged_surname);
        intent.putExtra("logged_mail", logged_mail);
        startActivity(intent);
    }
}
