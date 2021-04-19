package com.example.tasqr;
//
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AddProjectActivity extends AppCompatActivity {

    private String logged_name;
    private String logged_surname;
    private String logged_mail;

    private Bundle bundle = new Bundle();
    private FloatingActionButton addPeopleButton;
    private EditText projectName;
    private EditText desc;
    private Spinner companySpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addproject);

        bundle = getIntent().getExtras();
        logged_name = bundle.getString("logged_name");
        logged_surname = bundle.getString("logged_surname");
        logged_mail = bundle.getString("logged_mail");

        addPeopleButton = findViewById(R.id.addPeopleButton);
        projectName = findViewById(R.id.addedCompanyName);
        companySpinner = findViewById(R.id.chooseCompanySpinner);
        desc = findViewById(R.id.desc);

        ArrayAdapter<String> adapter = new ArrayAdapter(this, R.layout.spinner_item, bundle.getStringArrayList("owned_companies"));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        companySpinner.setAdapter(adapter);

        addPeopleButton.setOnClickListener(v -> {
            startAddUsersActivity();
        });
    }

    private void startAddUsersActivity() {

        String project_name = projectName.getText().toString();
        String company_name = companySpinner.getSelectedItem().toString();
        String description = desc.getText().toString();

        /* Do not allow creating project if these fields are empty */
        if (project_name.length() == 0) {
            Utilities.toastMessage("Project name cannot be empty", AddProjectActivity.this);
            return;
        }

        Intent addPeopleIntent = new Intent(AddProjectActivity.this, AddUsersActivity.class);
        addPeopleIntent.putExtra("previous_activity", "Project");
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
        desc.setText(bundle.getString("desc"));
        companySpinner.setSelection(bundle.getInt("spinnerPosition"));
        super.onResume();
    }

    /* Saves inputs from textfields */
    protected void onPause() {
        bundle.putString("projectName", projectName.getText().toString());
        bundle.putString("desc", desc.getText().toString());
        bundle.putInt("spinnerPosition", companySpinner.getSelectedItemPosition());
        super.onPause();
    }
}
