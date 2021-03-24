package com.example.tasqr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
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

import com.example.tasqr.classes.Project;
import com.example.tasqr.classes.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    /* Logged user basic info */
    private String logged_name;
    private String logged_surname;
    private String logged_mail;

    /* Firebase database */
    private FirebaseDatabase database;
    private DatabaseReference usersRef;
    private DatabaseReference projectsRef;
    private DatabaseReference companyRef;
    /* Fetched projects counter */
    private AtomicInteger projectsFetched;

    /* Nested class that helps us in creating listView */
    private class ProjectList extends ArrayAdapter {

        private ArrayList<String> projectNames;
        private ArrayList<String> ownerNames;
        private ArrayList<Integer> projectImages;
        private Activity context;

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
            View row = convertView;
            LayoutInflater inflater = context.getLayoutInflater();
            if(convertView == null)
                row = inflater.inflate(R.layout.project_list_item, null, true);
            TextView projectName = (TextView) row.findViewById(R.id.projectName);
            TextView ownerName = (TextView) row.findViewById(R.id.projectOwner);
            ImageView projectImage = (ImageView) row.findViewById(R.id.projectImage);

            projectName.setText(projectNames.get(position));
            ownerName.setText(ownerNames.get(position));
            projectImage.setImageResource(projectImages.get(position));
            return row;
        }
    }

    /* Sample project list data */
    private ArrayList<String> projectNames = new ArrayList<>();
    private ArrayList<String> companyNames = new ArrayList<>();
    private ArrayList<Integer> projectImages = new ArrayList<>();
    private ListView projectList;

    /* View items */
    private ImageButton addProjectButton;
    private Button profileButton;
    private TextView name;
    private TextView surname;
    private SwipeRefreshLayout swipeRefreshLayout;

    /* Methods */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = FirebaseDatabase.getInstance("https://tasqr-android-default-rtdb.europe-west1.firebasedatabase.app/");
        usersRef = database.getReference("Users");
        projectsRef = database.getReference("Projects");
        companyRef = database.getReference("Companies");

        Bundle bundle = getIntent().getExtras();
        logged_name = bundle.getString("logged_name");
        logged_surname = bundle.getString("logged_surname");
        logged_mail = bundle.getString("logged_mail");

        checkIfManager();

        addProjectButton = findViewById(R.id.addProjectButton);
        profileButton = findViewById(R.id.profileButton);
        name = findViewById(R.id.name);
        surname = findViewById(R.id.surname);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);

        addProjectButton.setOnClickListener(this);
        profileButton.setOnClickListener(this);

        name.setText(logged_name);
        surname.setText(logged_surname);

        fetchProjectData();

//        setRefresher();
    }

    private void checkIfManager() {
        Query q = companyRef.orderByChild("owner").equalTo(logged_mail);
        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount() == 0) {
                    addProjectButton.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utilities.toastMessage("error" + error.toString(), MainActivity.this);
            }
        });
    }

    /* Basic method for determining clicked button */
    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.addProjectButton:
                startAddProjectActivity();
                break;
            case R.id.profileButton:
                startProfileActivity();
                break;
        }
    }

    /* Starts addProject activity */
    private void startAddProjectActivity() {
        Intent addProjectIntent = new Intent(this, AddProjectActivity.class);
        addProjectIntent.putExtra("logged_name", logged_name);
        addProjectIntent.putExtra("logged_surname", logged_surname);
        addProjectIntent.putExtra("logged_mail", logged_mail);
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
                for (DataSnapshot childSnapshot: snapshot.getChildren()) {
                    u = childSnapshot.getValue(User.class);
                }

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
                                projectsFetched.getAndAdd(1);
                            }

                            /* Setting Adapter */
                            if (projectsFetched.get() == tmp.size() - 1) {
                                projectList = findViewById(R.id.projectList);
                                projectList.setAdapter(new ProjectList(MainActivity.this, projectNames, companyNames, projectImages));
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
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
}