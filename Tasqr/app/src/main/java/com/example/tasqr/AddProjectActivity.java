package com.example.tasqr;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AddProjectActivity extends AppCompatActivity {

    private Bundle bundle = new Bundle();
    private FloatingActionButton addPeopleButton;
    private EditText projectName;
    private EditText companyName;
    private EditText desc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addproject);

        addPeopleButton = findViewById(R.id.addPeopleButton);
        projectName = findViewById(R.id.addedProjectName);
        companyName = findViewById(R.id.addedProjectOwner);
        desc = findViewById(R.id.desc);

        addPeopleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addPeopleIntent = new Intent(AddProjectActivity.this, AddUsersActivity.class);
                startActivity(addPeopleIntent);
            }
        });
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


}
