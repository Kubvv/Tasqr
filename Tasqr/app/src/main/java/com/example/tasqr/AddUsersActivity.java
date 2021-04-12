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
import com.example.tasqr.classes.Task;
import com.example.tasqr.classes.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;

/* AddUsers Activity is an activity that creates list of users based on context, which is usually
* taken from previous activities. This list contains all users that can be checked. After the button
*  click, checked users are then added to appropriate objects in database, thus adding them to
* projects, companies etc..  */
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
    /* Mail of the task leader */
    private String leader;

    /* Project we are currently in */
    private Project currProject;
    private ArrayList<String> projectUsers;

    /* Arraylists used for creating user listView */
    private ListView listView;
    private ArrayList<User> userArray = new ArrayList<>();
    private ArrayList<String> displayArray = new ArrayList<>();

    /* bundle used in storing items */
    private Bundle bndl;

    /* Firebase database */
    private FirebaseDatabase database = FirebaseDatabase.getInstance("https://tasqr-android-default-rtdb.europe-west1.firebasedatabase.app/");
    private DatabaseReference rootRef = database.getReference();
    private DatabaseReference usersRef = rootRef.child("Users");
    private DatabaseReference projectsRef = rootRef.child("Projects");
    private DatabaseReference projectRef;
    private DatabaseReference companiesRef = rootRef.child("Companies");

    private ImageButton tmpImg;
    private Integer[] avatars = {R.drawable.avatar, R.drawable.avatar2};
    private int currentPhoto = 0;

    /* links appropriate view items, initializes listview, sets some attributes */
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

        /* Fetch some data before moving on with creating listviews */
        preFetch();

        tmpImg = findViewById(R.id.snickers);
        tmpImg.setImageResource(avatars[currentPhoto]);
        tmpImg.setOnClickListener(v -> {

            currentPhoto = (currentPhoto + 1) % 2;
            tmpImg.setImageResource(avatars[currentPhoto]);

            /* Fetch all of the selected users */
            ArrayList<User> checkedUsers = new ArrayList<>();
            ArrayList<String> usersMail = new ArrayList<>();
            /* Also add the owner user object */
            if (previous_activity.equals("Company") || previous_activity.equals("Project")) {
                checkedUsers.add(owner);
            }
            /* Also add the leader's mail, as he is also a worker */
            else if (previous_activity.equals("Task")) {
                usersMail.add(leader);
            }
            SparseBooleanArray checked = listView.getCheckedItemPositions();
            for (int i = 0; i < listView.getCount(); i++) {
                if (checked.get(i)) {
                    checkedUsers.add(userArray.get(i));
                    usersMail.add(userArray.get(i).getMail());
                }
            }

            switch (previous_activity) {
                case "Company":
                    finishAddingCompany(checkedUsers, usersMail);
                    break;
                case "Project":
                    finishAddingProject(checkedUsers, usersMail);
                    break;
                case "Task":
                    finishAddingTask(usersMail);
                    break;
                case "addProjUsers":
                    finishAddingLeaders(usersMail);
            }
        });

        listView = (ListView) findViewById(R.id.userlist);

        /* establish connection to database and some references */
        database = FirebaseDatabase.getInstance("https://tasqr-android-default-rtdb.europe-west1.firebasedatabase.app/");
        rootRef = database.getReference();
        usersRef = rootRef.child("Users");
        projectsRef = rootRef.child("Projects");

        Log.d(TAG, "onCreate: " + previous_activity);

        /* Fetch required data from database based on previous activity */
        switch (previous_activity) {
            case "Company":
                fetchCompany();
                break;
            case "Project":
                fetchProject();
                break;
            case "Task":
                fetchTask();
                break;
            case "addProjUsers":
                /* This listview will consist of previously chosen users, so we have to take it from prev intent */
                ArrayList<User> checked = getIntent().getParcelableArrayListExtra("checked_users");
                if (checked == null) Log.e(TAG, "onCreate: siema ");
                fetchProjUsers(checked);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (previous_activity.equals("addProjUsers")) { /* TO DO Zmienic miejsce dodanie projektu */
            openMainActivity();
        }
    }

    /* Used to fetch some information from database based on previous activity, before creating listview */
    private void preFetch() {
        if (previous_activity.equals("Project")) { /* we need to parse company that creates this project */
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
        /* we nedd to parse leader that created the task / project, and also all the users that take part in the project */
        else if (previous_activity.equals("Task") || previous_activity.equals("addProjUsers")) {
            projectRef = database.getReference("Projects/" + getIntent().getStringExtra("projectId"));
            projectRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    currProject = snapshot.getValue(Project.class);
                    projectUsers = currProject.getWorkers();
                    leader = currProject.getOwner();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "onCancelled: " + error);
                }
            });
        }
    }

    /* All of these functions are used to fetch data from different parts of database and create multiple choice list
    * based on them. Later on checked positions will be used to create new objects. */

    private void fetchCompany() {
        /* fetching all users in order to show them in a listview */
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                /* iterate through users and add each to array */
                for (DataSnapshot ds : snapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    /* If user is not the owner show him on the list of users that can be added */
                    Log.e(TAG, user.getMail());
                    if (!user.getMail().equals(logged_mail)) {
                        userArray.add(user);
                        displayArray.add(user.getName() + " " + user.getSurname());
                    } else {
                        owner = user;
                    }
                }

                /* create adapter for list view */
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
        /* fetching all company users in order to show them in a listview */
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
                    } else if (user.getMail().equals(logged_mail)) {
                        owner = user;
                    }
                }

                /* create adapter for list view */
                ArrayAdapter<String> adapter = new ArrayAdapter(AddUsersActivity.this, android.R.layout.simple_list_item_multiple_choice, displayArray);

                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error);
            }
        });
    }

    private void fetchTask() {
        /* fetching project users in order to show them in a listview */
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (i == projectUsers.size())
                        break;

                    User user = ds.getValue(User.class);
                    Log.d(TAG, "onDataChange: " + projectUsers.get(i) + i);
                    if (user.getMail().equals(projectUsers.get(i))) {
                        if (!user.getMail().equals(logged_mail)) {
                            userArray.add(user);
                            displayArray.add(user.getName() + " " + user.getSurname());
                            Log.d(TAG, "onDataChange: " + user.getMail() + projectUsers.get(i));
                        }
                        i++;
                    }
                }

                /* create adapter for list view */
                ArrayAdapter<String> adapter = new ArrayAdapter(AddUsersActivity.this, android.R.layout.simple_list_item_multiple_choice, displayArray);

                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error);
            }
        });
    }


    private void fetchProjUsers(ArrayList<User> checked) {
        /* fetching project users in order to show them in a listview */
        for (int i = 1; i < checked.size(); i++) {
            userArray.add(checked.get(i));
            displayArray.add(checked.get(i).getName() + " " + checked.get(i).getSurname());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter(AddUsersActivity.this, android.R.layout.simple_list_item_multiple_choice, displayArray);

        listView.setAdapter(adapter);
    }

    /* Adds company and all it's workers to database.
     * Also updates all added user's companies arrays.
     * If succesful, goes to ProfileActivity and closes all previous activities.*/
    private void finishAddingCompany (ArrayList<User> companyUsers, ArrayList<String> usersMail) {
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
            public void onSuccess(Void aVoid) {
                Utilities.toastMessage("Successfully added new Company", AddUsersActivity.this);
            }
        });

        /* add company to owner's projects array, then leave the activity */
        ArrayList<String> tmp = owner.getCompanies();
        tmp.add(id);
        usersRef.child(owner.getId()).child("companies").setValue(tmp).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                openProfileActivity();
            }
        });

        /* meanwhile add companies to all other invited users arrays */
        for (int i = 1; i < companyUsers.size(); i++) {
            tmp = companyUsers.get(i).getCompanies();
            tmp.add(id);
            Log.d(TAG, "finishAddingCompany: " + companyUsers.get(i).getName());
            usersRef.child(companyUsers.get(i).getId()).child("companies").setValue(tmp);
        }
    }

    /* Adds project and all it's workers to database.
     * Also updates all added user's projects arrays.
     * If succesful, goes to MainActivity and closes all previous activities.*/
    private void finishAddingProject (ArrayList<User> projectUsers, ArrayList<String> usersMail) {
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
            public void onSuccess(Void aVoid) {
                Utilities.toastMessage("Successfully added new project", AddUsersActivity.this);
            }
        });

        /* add project to owner's projects array, then leave the activity */
        ArrayList<String> tmp = owner.getProjects();
        Log.e(TAG, "finishAddingProject: " + owner.getMail());
        tmp.add(id);
        Log.e(TAG, tmp.get(tmp.size() - 1));
        usersRef.child(owner.getId()).child("projects").setValue(tmp).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                /* No possible workers to choose as leaders, as no one besides owner is in the project */
                if (projectUsers.size() == 1) {
                    openMainActivity();
                } else {
                    openAddLeaderActivity(id, projectUsers);
                }
            }
        });

        /* meanwhile add project to all other invited users arrays */
        for (int i = 1; i < projectUsers.size(); i++) {
            tmp = projectUsers.get(i).getProjects();
            tmp.add(id);
            usersRef.child(projectUsers.get(i).getId()).child("projects").setValue(tmp);
        }
    }

    /* Creates a new task in current project */
    private void finishAddingTask(ArrayList<String> usersMail) {
        projectsRef = database.getReference("Projects/" + getIntent().getStringExtra("projectId"));
        /* Create new task to be added */
        Task newTask = new Task(getIntent().getStringExtra("taskName"), leader, usersMail, new Date());
        currProject.addTask(AddUsersActivity.this, projectRef, newTask);

        openTasksActivity();
    }

    /* Adds leader to current project */
    private void finishAddingLeaders(ArrayList<String> usersMail) {
        projectsRef = database.getReference("Projects/" + getIntent().getStringExtra("projectId"));
        /* Add leaders to current project */
        currProject.addLeaders(AddUsersActivity.this, projectRef, usersMail);

        openMainActivity();
    }

    /* Activities openers */

    /* Opens main activity and closes activites relating to adding project */
    private void openMainActivity () {
        previous_activity = "addedLeaders";
        Intent intent = new Intent(AddUsersActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("logged_name", logged_name);
        intent.putExtra("logged_surname", logged_surname);
        intent.putExtra("logged_mail", logged_mail);
        startActivity(intent);
    }

    /* Opens profile activity and closes activities relating to adding company */
    private void openProfileActivity () {
        Intent intent = new Intent(AddUsersActivity.this, ProfileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("logged_mail", logged_mail);
        startActivity(intent);
    }

    /* Opens main activity and closes activites relating to adding project */
    private void openTasksActivity() {
        Intent intent = new Intent(AddUsersActivity.this, TasksActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("projectId", getIntent().getStringExtra("projectId"));
        intent.putExtra("logged_mail", getIntent().getStringExtra("logged_mail"));
        startActivity(intent);
    }

    /* Opens addUsersActivity in order to choose leaders among picked users */
    private void openAddLeaderActivity(String projectId, ArrayList<User> checked) {
        Intent intent = new Intent(AddUsersActivity.this, AddUsersActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("logged_name", logged_name);
        intent.putExtra("logged_surname", logged_surname);
        intent.putExtra("logged_mail", logged_mail);
        intent.putExtra("previous_activity", "addProjUsers");
        intent.putExtra("company_name", company);
        intent.putExtra("projectId", projectId);
        intent.putParcelableArrayListExtra("checked_users", checked);
        startActivity(intent);
    }
}
