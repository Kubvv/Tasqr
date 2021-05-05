package com.example.tasqr;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

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
    private String project;
    private String task;

    /* Owner of currently creating project [not in userArray!]*/
    private User owner;
    /* Mail of the task leader */
    private String leader;

    /* Project we are currently in */
    private Project currProject;
    private ArrayList<String> projectUsers;

    /* Company info */
    private Company currCompany;
    private ArrayList<String> alreadyWorking = new ArrayList<>();

    /* Arraylists used for creating user listView */
    private ListView listView;
    private ArrayList<User> userArray = new ArrayList<>();
    private ArrayList<String> displayArray = new ArrayList<>();
    ArrayAdapter<String> adapter;

    private TextView addUsersTitle;
    private EditText searchUsers;

    /* bundle used in storing items */
    private Bundle bndl;
    private Resources res;

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

        res = getResources();
        bndl = getIntent().getExtras();
        previous_activity = bndl.getString("previous_activity");
        logged_name = bndl.getString("logged_name");
        logged_surname = bndl.getString("logged_surname");
        logged_mail = bndl.getString("logged_mail");
        company = bndl.getString("company_name");
        project = bndl.getString("project_name");
        task = bndl.getString("taskName");
        currCompany = bndl.getParcelable("company");

        /* Fetch some data before moving on with creating listviews */
        preFetch();

        addUsersTitle = findViewById(R.id.changeOwnershipTitle);
        searchUsers = findViewById(R.id.editTextOwnership);
        searchUsers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    (AddUsersActivity.this).adapter.getFilter().filter(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

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
                    break;
                case "manageCompanyUsers":
                    finishManageCompany(usersMail, "workers");
                    break;
                case "manageCompanyManagers":
                    finishManageCompany(usersMail, "managers");
                    break;
            }
        });

        listView = (ListView) findViewById(R.id.companylist);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /* get user's mail from userArray by index since we know displayArray index that was clicked on and both arrays share indices */
                /*Intent profileIntent = new Intent(AddUsersActivity.this, ProfileActivity.class);
                //TODO chcialem dac tu przekierowanie do profilu ale nie dziala to za dobrze, do zmiany
                profileIntent.putExtra("clicked_mail", userArray.get(position).getMail());
                profileIntent.putExtra("logged_mail", "logged_mail");

                startActivity(profileIntent);*/
            }
        });

        /* establish connection to database and some references */
        database = FirebaseDatabase.getInstance("https://tasqr-android-default-rtdb.europe-west1.firebasedatabase.app/");
        rootRef = database.getReference();
        usersRef = rootRef.child("Users");
        projectsRef = rootRef.child("Projects");

        Log.d(TAG, "onCreate: " + previous_activity);

        /* Fetch required data from database based on previous activity */
        switch (previous_activity) {
            case "Company":
                addUsersTitle.setText(String.format(res.getString(R.string.adduserscompany), company));
                fetchCompany();
                break;
            case "Project":
                addUsersTitle.setText(String.format(res.getString(R.string.addusersproject), project));
                fetchProject();
                break;
            case "Task":
                addUsersTitle.setText(String.format(res.getString(R.string.adduserstask), task));
                fetchTask();
                break;
            case "addProjUsers":
                /* This listview will consist of previously chosen users, so we have to take it from prev intent */
                ArrayList<User> checked = getIntent().getParcelableArrayListExtra("checked_users");
                fetchProjUsers(checked);
                break;
            case "manageCompanyUsers":
                addUsersTitle.setText(String.format("Add %s's employees", company));
                fetchManageCompany("workers");
                break;
            case "manageCompanyManagers":
                addUsersTitle.setText(String.format("Add %s's managers", company));
                fetchManageCompany("managers");
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
        if (previous_activity.equals("Project")) { /* we need to parse company that created this project */
            Query q = companiesRef.orderByChild("name").equalTo(company);
            q.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        currCompany = ds.getValue(Company.class);
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
                    leader = logged_mail;
                    if (projectUsers == null) { /* If there are no workers, create task with only you enlisted */
                        ArrayList<String> usersMail = new ArrayList<>();
                        usersMail.add(leader);
                        finishAddingTask(usersMail);
                    }
                    if (previous_activity.equals("addProjUsers")) {
                        /* add title to activity */
                        addUsersTitle.setText(String.format(res.getString(R.string.addusersleaders), currProject.getName()));
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Utilities.toastMessage("error " + error, AddUsersActivity.this);
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
                    if (!user.getMail().equals(logged_mail)) {
                        userArray.add(user);
                        displayArray.add(user.getName() + " " + user.getSurname());
                    } else {
                        owner = user;
                    }
                }

                setUserListAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utilities.toastMessage("error " + error, AddUsersActivity.this);
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
                    if (!user.getMail().equals(logged_mail) && user.getCompanies().contains(currCompany.getId())) {
                        userArray.add(user);
                        displayArray.add(user.getName() + " " + user.getSurname());
                    } else if (user.getMail().equals(logged_mail)) {
                        owner = user;
                    }
                }

                if (currCompany.getWorkers() == null) { /* If there are no workers, create project with only you enlisted */
                    ArrayList<String> usersMail = new ArrayList<>();
                    ArrayList<User> projUsers = new ArrayList<>();
                    projUsers.add(owner);
                    finishAddingProject(projUsers, usersMail);
                }

                setUserListAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utilities.toastMessage("error " + error, AddUsersActivity.this);
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
                    if (projectUsers == null || i == projectUsers.size())
                        break;

                    User user = ds.getValue(User.class);
                    if (user.getMail().equals(projectUsers.get(i))) {
                        if (!user.getMail().equals(logged_mail)) {
                            userArray.add(user);
                            displayArray.add(user.getName() + " " + user.getSurname());
                        }
                        i++;
                    }
                }

                setUserListAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utilities.toastMessage("error " + error, AddUsersActivity.this);
            }
        });
    }


    private void fetchProjUsers(ArrayList<User> checked) {
        /* fetching project users in order to show them in a listview */
        for (int i = 1; i < checked.size(); i++) {
            userArray.add(checked.get(i));
            displayArray.add(checked.get(i).getName() + " " + checked.get(i).getSurname());
        }

        setUserListAdapter();
    }

    private void fetchManageCompany(String context) {
        if (currCompany.getWorkers() != null) {
            alreadyWorking = currCompany.getWorkers();
        }
        ArrayList<String> checked;
        if (context.equals("managers")) {
            checked = currCompany.getManagers();
        }
        else {
            checked = alreadyWorking;
        }
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                /* iterate through users and add each to array */
                if (context.equals("workers")) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        User user = ds.getValue(User.class);
                        String mail = user.getMail();
                        if (!mail.equals(currCompany.getOwner())) {
                            userArray.add(user);
                            displayArray.add(user.getName() + " " + user.getSurname());
                        }
                    }
                }
                else {
                    int j = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        if (j == alreadyWorking.size()) { //all workers were found
                            break;
                        }
                        User user = ds.getValue(User.class);
                        String mail = user.getMail();
                        if (mail.equals(alreadyWorking.get(j))) {
                            userArray.add(user);
                            displayArray.add(user.getName() + " " + user.getSurname());
                            j++;
                        }
                    }
                }

                setUserListAdapter();
                if (checked == null) {
                    return;
                }

                int j = 0;
                for (int i = 0; i < listView.getCount() && j < checked.size(); i++) {
                     if (userArray.get(i).getMail().equals(checked.get(j))) {
                         listView.setItemChecked(i, true);
                         j++;
                     }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utilities.toastMessage("error " + error, AddUsersActivity.this);
            }
        });
    }

    /* create adapter for list view */
    private void setUserListAdapter() {
        adapter = new ArrayAdapter(AddUsersActivity.this, R.layout.user_list_item, displayArray);
        listView.setAdapter(adapter);
    }

    /* Adds company and all it's workers to database.
     * Also updates all added user's companies arrays.
     * If succesful, goes to ProfileActivity and closes all previous activities.*/
    private void finishAddingCompany (ArrayList<User> companyUsers, ArrayList<String> usersMail) {
        /* Create new company to be added */
        DatabaseReference pushedCompaniesRef = companiesRef.push();
        String id = pushedCompaniesRef.getKey();

        Company company = new Company(
                id,
                bndl.getString("company_name"),
                bndl.getString("description"),
                owner.getMail(),
                usersMail);


        companiesRef.child(id).setValue(company).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Utilities.toastMessage("Successfully added new Company", AddUsersActivity.this);
            }
        });

        /* add company to owner's companies array and managedCompany array, then leave the activity */
        ArrayList<String> tmp = owner.getCompanies();
        tmp.add(id);
        usersRef.child(owner.getId()).child("companies").setValue(tmp);
        tmp = owner.getManagedCompanies();
        tmp.add(company.getName());
        usersRef.child(owner.getId()).child("managedCompanies").setValue(tmp).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                openManageCompanyActivity();
            }
        });

        /* meanwhile add companies to all other invited users arrays */
        for (int i = 1; i < companyUsers.size(); i++) {
            tmp = companyUsers.get(i).getCompanies();
            tmp.add(id);
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
        tmp.add(id);
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
        Task newTask = new Task(getIntent().getStringExtra("taskName"), leader, getIntent().getStringExtra("projectId"), usersMail, new Date(), 0);
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

    private void finishManageCompany(ArrayList<String> usersMail, String context) {

        /* segregate users that need to be deleted and added */
        ArrayList<String> toDelete = new ArrayList<>();
        ArrayList<String> toAdd;
        companiesRef.child(currCompany.getId()).child(context).setValue(usersMail);
        if (context.equals("managers")) {
            alreadyWorking = currCompany.getManagers();
        }
        if (alreadyWorking == null || alreadyWorking.size() == 0) { //there is no one to delete in empty company
            toAdd = usersMail;
        }
        else if (usersMail.size() == 0) { //there is no one to add to company that is being emptied
            toAdd = new ArrayList<>();
            toDelete = alreadyWorking;
        }
        else {
            HashSet<String> newChecked = new HashSet<>(usersMail);
            for (int i = 0; i < alreadyWorking.size(); i++) {
                if (!newChecked.contains(alreadyWorking.get(i))) {
                    toDelete.add(alreadyWorking.get(i));
                } else {
                    newChecked.remove(alreadyWorking.get(i));
                }
            }
            toAdd = new ArrayList<>(newChecked);
        }

        for (int i = 0; i < toDelete.size(); i++) {
            Query q = usersRef.orderByChild("mail").equalTo(toDelete.get(i));
            q.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user;
                    ArrayList<String> companies;
                    ArrayList<String> companyManagers = new ArrayList<>();
                    if (currCompany.getManagers() != null) {
                        companyManagers = currCompany.getManagers();
                    }
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        user = ds.getValue(User.class);
                        if (context.equals("workers")) {
                            companies = user.getCompanies();
                            companies.remove(currCompany.getId());
                            usersRef.child(user.getId()).child("companies").setValue(companies);
                        }
                        /* if someone is deleted in manageCompany tab, he is always removed from managing position */
                        companies = user.getManagedCompanies();
                        companies.remove(currCompany.getName());
                        companyManagers.remove(user.getMail());
                        usersRef.child(user.getId()).child("managedCompanies").setValue(companies);
                    }
                    if (context.equals("workers")) {
                        /* set new managers after deletion to database */
                        companiesRef.child(currCompany.getId()).child("managers").setValue(companyManagers);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Utilities.toastMessage("error " + error, AddUsersActivity.this);
                }
            });
        }

        for (int j = 0; j < toAdd.size(); j++) {
            Query q = usersRef.orderByChild("mail").equalTo(toAdd.get(j));
            q.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user;
                    ArrayList<String> companies;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        user = ds.getValue(User.class);
                        if (context.equals("workers")) {
                            companies = user.getCompanies();
                            companies.add(currCompany.getId());
                            usersRef.child(user.getId()).child("companies").setValue(companies);
                        }
                        else {
                            companies = user.getManagedCompanies();
                            companies.add(currCompany.getName());
                            usersRef.child(user.getId()).child("managedCompanies").setValue(companies);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Utilities.toastMessage("error " + error, AddUsersActivity.this);
                }
            });
        }

        openManageCompanyActivity();
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

    /* Opens ManageCompany activity and closes activities relating to adding company */
    private void openManageCompanyActivity () {
        Intent intent = new Intent(AddUsersActivity.this, ManageCompanyActivity.class);
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
