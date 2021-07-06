/*
* MAIN ACTIVITY
* Contains  List of all projects a user participates in which can be clicked to go to project activity
*           Name and Surname with profile going to profile
*           Button to add new project given logged user is allowed to do that
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.tasqr.classes.Company;
import com.example.tasqr.classes.Project;
import com.example.tasqr.classes.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
public class MainActivity extends AppCompatActivity {

    /* Logged user basic info */
    private String logged_name;
    private String logged_surname;
    private String logged_mail;
    private ArrayList<String> owned_companies = new ArrayList<>();
    private User user;

    private DatabaseReference usersRef;
    private DatabaseReference projectsRef;

    /* Fetched projects counter */
    private AtomicInteger projectsFetched;

    private TextView name;
    private TextView surname;

    private Boolean onResumeFirstTime;

    /* Nested class that helps us in creating listView */
    private static class ProjectList extends ArrayAdapter {

        private final ArrayList<String> projectNames;
        private final ArrayList<String> ownerNames;
        private final ArrayList<Integer> projectImages;
        private final Activity context;

        public ProjectList(Activity context, ArrayList<String> projectNames, ArrayList<String> ownerNames, ArrayList<Integer> projectImages) {
            super(context, R.layout.project_list_item, projectNames);
            this.context = context;
            this.projectNames = projectNames;
            this.ownerNames = ownerNames;
            this.projectImages = projectImages;
        }

        /* Creates one row of ListView, consisting of project name, company name and image */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            /* Set up */
            View row = convertView;
            LayoutInflater inflater = context.getLayoutInflater();
            if(convertView == null)
                row = inflater.inflate(R.layout.project_list_item, null, true);

            /* Find and set */
            TextView projectName = (TextView) row.findViewById(R.id.projectName);
            TextView ownerName = (TextView) row.findViewById(R.id.projectOwner);
            ImageView projectImage = (ImageView) row.findViewById(R.id.projectImage);

            projectName.setText(projectNames.get(position));
            ownerName.setText(ownerNames.get(position));
            projectImage.setImageResource(projectImages.get(position));
            return row;
        }
    }

    /* Fetched project list data */
    private final ArrayList<String> projectNames = new ArrayList<>();
    private final ArrayList<String> companyNames = new ArrayList<>();
    private final ArrayList<Integer> projectImages = new ArrayList<>();
    private final ArrayList<String> projectIds = new ArrayList<>();
    private ListView projectList;

    /* View items */
    private ImageButton addProjectButton;
    private SwipeRefreshLayout swipeRefreshLayout;

    /* Methods */

    /* On Create main method */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        onResumeFirstTime = Boolean.TRUE;

        /* Database fetch */
        /* Firebase database */
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://tasqr-android-default-rtdb.europe-west1.firebasedatabase.app/");
        usersRef = database.getReference("Users");
        projectsRef = database.getReference("Projects");

        /* Intent bundle fetch */
        Bundle bundle = getIntent().getExtras();
        logged_name = bundle.getString("logged_name");
        logged_surname = bundle.getString("logged_surname");
        logged_mail = bundle.getString("logged_mail");

        /* Checks whether we should display add project button */
        checkIfManager();

        /* Xml items find and set */
        addProjectButton = findViewById(R.id.addProjectButton);
        Button profileButton = findViewById(R.id.profileButton);
        name = findViewById(R.id.name);
        surname = findViewById(R.id.surname);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);

        addProjectButton.setOnClickListener(v -> startAddProjectActivity());
        profileButton.setOnClickListener(v -> startProfileActivity());

        name.setText(logged_name);
        surname.setText(logged_surname);

        /* Fetch project data and set on refresh action */
        fetchProjectData();
        setRefresher();
    }

    /* When coming back to main activity there should be no need
     * to refresh the activity to get addProjectButton to show
     */
    @Override
    protected void onResume() {
        super.onResume();

        /* Don't fetch projects if this onResume is after onCreate */
        if (onResumeFirstTime)
            onResumeFirstTime = Boolean.FALSE;
        else {
            /* Fetch user in case he just modified his profile */
            Query q = usersRef.orderByChild("mail").equalTo(logged_mail);
            q.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot childSnapshot: snapshot.getChildren())
                        user = childSnapshot.getValue(User.class);

                    logged_name = user.getName();
                    logged_surname = user.getSurname();

                    name.setText(logged_name);
                    surname.setText(logged_surname);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

            /* Clear project list structures and fetch updated ones */
            projectNames.clear();
            companyNames.clear();
            projectImages.clear();
            projectsFetched.set(0);
            fetchProjectData();
        }

        checkIfManager();
    }

    /* Override back button so it doesn't logout user */
    @Override
    public void onBackPressed() {
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);
    }

    /* Sets visibility depending on the type of logged user */
    private void checkIfManager() {
        Query q = usersRef.orderByChild("mail").equalTo(logged_mail);
        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    user = ds.getValue(User.class);
                    ArrayList<String> managed = user.getManagedCompanies();

                    if (managed.size() == 1)
                        addProjectButton.setVisibility(View.INVISIBLE);
                    else {
                        owned_companies = managed;
                        owned_companies.remove(0);
                        addProjectButton.setVisibility(View.VISIBLE);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utilities.toastMessage("error" + error.toString(), MainActivity.this);
            }
        });
    }

    /* Starts addProject activity */
    private void startAddProjectActivity() {
        Intent addProjectIntent = new Intent(this, AddProjectActivity.class);
        addProjectIntent.putExtra("logged_name", logged_name);
        addProjectIntent.putExtra("logged_surname", logged_surname);
        addProjectIntent.putExtra("logged_mail", logged_mail);
        Bundle arrayBundle = new Bundle();
        addProjectIntent.putStringArrayListExtra("owned_companies", owned_companies);
        startActivity(addProjectIntent);
    }

    /* Starts profile activity */
    private void startProfileActivity() {
        Intent profileIntent = new Intent(this, ProfileActivity.class);
        profileIntent.putExtra("logged_mail", logged_mail);
        startActivity(profileIntent);
    }

    /* Fetches all projects that logged user takes part in.
     * It firsts searches for user in database, in order to get all project identifiers
     * Then, using those identifiers it finds all the projects in projects table, and gets all necessary data
     * for setting up the listView adapter
     */
    private void fetchProjectData() {

        /* Querying user */
        Query qu = usersRef.orderByChild("mail").equalTo(logged_mail);
        qu.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User u = new User();
                for (DataSnapshot childSnapshot: snapshot.getChildren())
                    u = childSnapshot.getValue(User.class);


                projectsFetched = new AtomicInteger(0);
                ArrayList<String> tmp = u.getProjects();
                for (int i = 1; i < tmp.size(); i++) {
                    /* Querying all user projects */
                    Query qp = projectsRef.orderByKey().equalTo(tmp.get(i));
                    qp.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Project p;

                            for (DataSnapshot childSnapshot: snapshot.getChildren()) {
                                p = childSnapshot.getValue(Project.class);

                                projectNames.add(p.getName());
                                companyNames.add(p.getCompany());
                                projectImages.add(R.drawable.templateproject);
                                projectIds.add(childSnapshot.getKey());

                                projectsFetched.getAndAdd(1);
                            }

                            /* Setting list attributes */
                            if (projectsFetched.get() == tmp.size() - 1) {
                                projectList = findViewById(R.id.projectList);
                                projectList.setAdapter(new ProjectList(MainActivity.this, projectNames, companyNames, projectImages));

                                /* Proceeds to tasks activity within a given project*/
                                projectList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        Intent tasksIntent = new Intent(MainActivity.this, TasksActivity.class);
                                        tasksIntent.putExtra("projectId", projectIds.get(position));
                                        tasksIntent.putExtra("logged_mail", logged_mail);
                                        tasksIntent.putExtra("logged_name", logged_name);
                                        tasksIntent.putExtra("logged_surname", logged_surname);
                                        tasksIntent.putExtra("company_name", companyNames.get(position));
                                        startActivity(tasksIntent);
                                    }
                                });
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Utilities.toastMessage("error" + error.toString(), MainActivity.this);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utilities.toastMessage("error" + error.toString(), MainActivity.this);
            }
        });
    }

    /* Sets up refresh layout */
    private void setRefresher() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                projectNames.clear();
                companyNames.clear();
                projectImages.clear();
                projectsFetched.set(0);

                fetchProjectData();
                checkIfManager();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
}