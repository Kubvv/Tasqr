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

import com.example.tasqr.classes.Company;
import com.example.tasqr.classes.Project;
import com.example.tasqr.classes.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AddUsersActivity extends AppCompatActivity {

    private static final String TAG = "AddUsersActivity";

    /* basic logged user info */
    private String previous_activity;
    private String logged_name;
    private String logged_surname;
    private String logged_mail;
    private String company;
    private String companyId;

    /* Owner of currently creating project [not in userArray!]*/
    private User owner;

    /* Arraylists used for creating user listView */
    private ArrayList<User> userArray = new ArrayList<>();
    private ArrayList<String> displayArray = new ArrayList<>();

    private Bundle bndl;
    private ListView listView;

    /* Firebase database */
    private FirebaseDatabase database = FirebaseDatabase.getInstance("https://tasqr-android-default-rtdb.europe-west1.firebasedatabase.app/");
    private DatabaseReference rootRef = database.getReference();
    private DatabaseReference usersRef = rootRef.child("Users");
    private DatabaseReference projectsRef = rootRef.child("Projects");
    private DatabaseReference companiesRef = rootRef.child("Companies");

    /* button image TO DO zmienic przed prezentacja */
    private ImageButton nigga;
    private Integer[] avatars = {R.drawable.avatar, R.drawable.white, R.drawable.asian};
    private int currentPhoto = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addusers);

        bndl = getIntent().getExtras();
        previous_activity = bndl.getString("previous_activity");
        logged_name = bndl.getString("logged_name");
        logged_surname = bndl.getString("logged_surname");
        logged_mail = bndl.getString("logged_mail");
        company = bndl.getString("company_name");

        if (previous_activity.equals("Project")) {
            Query q = companiesRef.orderByChild("name").equalTo(company);
            q.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        companyId = ds.getKey();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Utilities.toastMessage("error" + error.toString(), AddUsersActivity.this);
                }
            });

        }

        nigga = findViewById(R.id.snickers);
        nigga.setImageResource(avatars[currentPhoto]);

        nigga.setOnClickListener(v -> {
            if (previous_activity.equals("Company")) {
                finishAddingCompany();
            }
            else if (previous_activity.equals("Project")) {
                finishAddingProject();
            }
        });

        listView = (ListView)findViewById(R.id.userlist);

        /* establish connection to database and some references */
        database = FirebaseDatabase.getInstance("https://tasqr-android-default-rtdb.europe-west1.firebasedatabase.app/");
        rootRef = database.getReference();
        usersRef = rootRef.child("Users");
        projectsRef = rootRef.child("Projects");

        Log.d(TAG, "onCreate: " + previous_activity);

        if (previous_activity.equals("Company")) {
            fetchCompany();
        }
        else if (previous_activity.equals("Project")) {
            fetchProject();
        }
    }

    private void fetchCompany() {
        /* fetching all users in order to show them in a listview */
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                /* iterate through users and add each to array */
                int i = 0;
                Log.d(TAG, "onDataChange: siema");
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: 111");
                    User user = ds.getValue(User.class);
                    Log.d(TAG, "onCreate: " + user.getName());
                    /* If user is not the owner show him on the list of users that can be added */
                    if (!user.getMail().equals(logged_mail)) {
                        userArray.add(user);
                        displayArray.add(user.getName() + " " + user.getSurname());
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

    private void fetchProject() {
        /* fetching all users in order to show them in a listview */
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                /* iterate through users and add each to array */
                for (DataSnapshot ds : snapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    /* If user is not the owner show him on the list of users that can be added */
                    if (!user.getMail().equals(logged_mail) && user.getCompanies().contains(companyId)) {
                        userArray.add(user);
                        displayArray.add(user.getName() + " " + user.getSurname());
                    } else if(user.getMail().equals(logged_mail)) {
                        owner = user;
                    }
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

    /* Adds company and all it's workers to database.
     * Also updates all added user's companies arrays.
     * If succesful, goes to ProfileActivity and closes all previous activities.*/
    private void finishAddingCompany() {
        currentPhoto = (currentPhoto + 1) % 3;
        nigga.setImageResource(avatars[currentPhoto]);

        ArrayList<User> companyUsers = new ArrayList<>();
        ArrayList<String> usersMail = new ArrayList<>();
        companyUsers.add(owner);
        SparseBooleanArray checked = listView.getCheckedItemPositions();
        for (int i = 0; i < listView.getCount(); i++) {
            if (checked.get(i)) {
                companyUsers.add(userArray.get(i));
                usersMail.add(userArray.get(i).getMail());
            }
        }

        /* Create new company to be added */
        Company company = new Company(
                bndl.getString("company_name"),
                bndl.getString("description"),
                owner.getMail(),
                usersMail);

        DatabaseReference pushedCompaniesRef = companiesRef.push();
        String id = pushedCompaniesRef.getKey();
        companiesRef.child(id).setValue(company).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {  Utilities.toastMessage("Successfully added new Company", AddUsersActivity.this);
            }
        });

        /* add company to owner's projects array, then leave the activity */
        ArrayList<String> tmp = owner.getCompanies();
        tmp.add(id);
        usersRef.child(owner.getMail()).child("companies").setValue(tmp).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                openProfileActivity();
            }
        });

        /* meanwhile add companies to all other invited users arrays */
        for (int i = 1; i < companyUsers.size(); i++) {
            tmp = companyUsers.get(i).getCompanies();
            tmp.add(id);
            usersRef.child(companyUsers.get(i).getMail()).child("companies").setValue(tmp); /* TO DO Identyfying users will be wrong after user id change */
        }
    }

    /* Adds project and all it's workers to database.
    * Also updates all added user's projects arrays.
    * If succesful, goes to MainActivity and closes all previous activities.*/
    private void finishAddingProject() {
        currentPhoto = (currentPhoto + 1) % 3;
        nigga.setImageResource(avatars[currentPhoto]);

        ArrayList<User> projectUsers = new ArrayList<>();
        ArrayList<String> usersMail = new ArrayList<>();
        projectUsers.add(owner);
        SparseBooleanArray checked = listView.getCheckedItemPositions();
        for (int i = 0; i < listView.getCount(); i++) {
            if (checked.get(i)) {
                projectUsers.add(userArray.get(i));
                usersMail.add(userArray.get(i).getMail());
            }
        }

        /* Create new project to be added */
        Project project = new Project(
                bndl.getString("project_name"),
                bndl.getString("company_name"),
                bndl.getString("description"),
                owner.getMail(),
                usersMail);

        DatabaseReference pushedProjectsRef = projectsRef.push();
        String id = pushedProjectsRef.getKey();

        projectsRef.child(id).setValue(project).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {  Utilities.toastMessage("Successfully added new project", AddUsersActivity.this);
            }
        });

        /* add project to owner's projects array, then leave the activity */
        ArrayList<String> tmp = owner.getProjects();
        Log.e(TAG, "finishAddingProject: " + owner.getMail());
        tmp.add(id);
        Log.e(TAG, tmp.get(tmp.size() - 1));
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

    private void openProfileActivity() {
        Intent intent = new Intent(AddUsersActivity.this, ProfileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("logged_mail", logged_mail);
        startActivity(intent);
    }
}
