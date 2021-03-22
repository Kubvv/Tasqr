package com.example.tasqr;
//
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AddProjectActivity extends AppCompatActivity {

    private String logged_name;
    private String logged_surname;
    private String logged_mail;

    private Bundle bundle = new Bundle();
    private FloatingActionButton addPeopleButton;
    private EditText projectName;
    private EditText companyName;
    private EditText desc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addproject);

        bundle = getIntent().getExtras();
        logged_name = bundle.getString("logged_name");
        logged_surname = bundle.getString("logged_surname");
        logged_mail = bundle.getString("logged_mail");

        addPeopleButton = findViewById(R.id.addPeopleButton);
        projectName = findViewById(R.id.addedProjectName);
        companyName = findViewById(R.id.addedProjectOwner);
        desc = findViewById(R.id.desc);

        addPeopleButton.setOnClickListener(v -> {
            startAddUsersActivity();
        });
    }

    private void startAddUsersActivity() {

        String project_name = projectName.getText().toString();
        String company_name = companyName.getText().toString();
        String description = desc.getText().toString();

        /* Do not allow creating project if these fields are empty */ /* TO DO changing id of projects in db because sie pruje o $ . # [ ] */
        if (project_name.length() == 0 || company_name.length() == 0) {
            toastMessage("Project name and company name cannot be empty");
            return;
        }

        Intent addPeopleIntent = new Intent(AddProjectActivity.this, AddUsersActivity.class);
        addPeopleIntent.putExtra("logged_name", logged_name);
        addPeopleIntent.putExtra("logged_surname", logged_surname);
        addPeopleIntent.putExtra("logged_mail", logged_mail);
        addPeopleIntent.putExtra("project_name", project_name);
        addPeopleIntent.putExtra("company_name", company_name);
        addPeopleIntent.putExtra("description", description);
        startActivity(addPeopleIntent);
    }

    /* Resume previous inputs in textfields */
    protected void onResume() {
        projectName.setText(bundle.getString("projectName"));
        companyName.setText(bundle.getString("companyName"));
        desc.setText(bundle.getString("desc"));
        super.onResume();
    }

    /* Saves inputs from textfields */
    protected void onPause() {
        bundle.putString("projectName", projectName.getText().toString());
        bundle.putString("companyName", companyName.getText().toString());
        bundle.putString("desc", desc.getText().toString());
        super.onPause();
    }

    /* Messages user with long toast message */
    private void toastMessage(String message) {
        Toast.makeText(AddProjectActivity.this, message, Toast.LENGTH_LONG).show();
    }

}
