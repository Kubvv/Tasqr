package com.example.tasqr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.LogDescriptor;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    /* Logged user basic info */
    private String logged_name;
    private String logged_surname;
    private String logged_mail;

    /* Firebase database */
    private FirebaseDatabase database;

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

    /* Methods */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addProjectButton = findViewById(R.id.addProjectButton);
        profileButton = findViewById(R.id.profileButton);
        name = findViewById(R.id.name);
        surname = findViewById(R.id.surname);

        addProjectButton.setOnClickListener(this);
        profileButton.setOnClickListener(this);

        logged_name = getIntent().getStringExtra("logged_name");
        logged_surname = getIntent().getStringExtra("logged_surname");
        logged_mail = getIntent().getStringExtra("logged_mail");

        name.setText(logged_name);
        surname.setText(logged_surname);

        fetchProjectData();
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
//      profileIntent.putExtra("name", logged_name);
//      profileIntent.putExtra("surname", logged_surname); TO DO clear?
        profileIntent.putExtra("logged_mail", logged_mail);
        startActivity(profileIntent);
    }

    /* Fetches all projects that logged user takes part in.
     * It firsts searches for user in database, in order to get all project identifiers
     * Then, using those identifiers it finds all the projects in projects table, and gets all necessary data
     * for setting up the listView adapter
     */
    private void fetchProjectData() {
        database = FirebaseDatabase.getInstance("https://tasqr-android-default-rtdb.europe-west1.firebasedatabase.app/");
        DatabaseReference usersRef = database.getReference("Users");
        DatabaseReference projectsRef = database.getReference("Projects");

        /* Querying user */
        Query qu = usersRef.orderByChild("mail").equalTo(logged_mail);
        qu.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User u = new User();
                for (DataSnapshot childSnapshot: snapshot.getChildren()) {
                    u = childSnapshot.getValue(User.class);
                    Log.d(TAG, "onDataChange: " + u.getName());
                }

                ArrayList<String> tmp = u.getProjects();
                for (int i = 1; i < tmp.size(); i++) {
                    Log.d(TAG, "onDataChange: " + tmp.get(i));

                    /* Querying all user projects */
                    Query qp = projectsRef.orderByKey().equalTo(tmp.get(i));
                    qp.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Project p;
                            for (DataSnapshot childSnapshot: snapshot.getChildren()) {
                                p = childSnapshot.getValue(Project.class);
                                Log.d(TAG, "onDataChange: " + p.getName());

                                projectNames.add(p.getName());
                                companyNames.add(p.getCompany());
                                projectImages.add(R.drawable.templateproject);
                            }

                            /* Setting Adapter */
                            projectList = findViewById(R.id.projectList);
                            projectList.setAdapter(new ProjectList(MainActivity.this, projectNames, companyNames, projectImages));

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            toastMessage("error" + error.toString());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                toastMessage("error" + error.toString());
            }
        });
    }

    /* Messages user with long toast message */
    private void toastMessage(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
    }
}