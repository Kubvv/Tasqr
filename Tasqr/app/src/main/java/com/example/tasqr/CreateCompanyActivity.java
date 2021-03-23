package com.example.tasqr;

import android.content.Intent;
import android.os.Bundle;

import com.example.tasqr.classes.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.EditText;

public class CreateCompanyActivity extends AppCompatActivity {

    private String logged_mail;
    private User projectOwner;

    private Bundle bundle;

    private FloatingActionButton addPeopleButton;
    private EditText companyName;
    private EditText description;

    /* Firebase database */
    private FirebaseDatabase database = FirebaseDatabase.getInstance("https://tasqr-android-default-rtdb.europe-west1.firebasedatabase.app/");
    private DatabaseReference rootRef = database.getReference();
    private DatabaseReference usersRef = rootRef.child("Users");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_company);

        bundle = getIntent().getExtras();
        logged_mail = bundle.getString("logged_mail");

        Query q = usersRef.orderByChild("mail").equalTo(logged_mail);
        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    projectOwner = ds.getValue(User.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utilities.toastMessage("error" + error.toString(), CreateCompanyActivity.this);
            }
        });

        companyName = findViewById(R.id.addedCompanyName);
        description = findViewById(R.id.desc);

        addPeopleButton = findViewById(R.id.addPeopleButton);
        addPeopleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAddUsersActivity();
            }
        });
    }

    /* Resume previous inputs in textfields */
    protected void onResume() {
        companyName.setText(bundle.getString("companyName"));
        description.setText(bundle.getString("desc"));
        super.onResume();
    }

    /* Saves inputs from textfields */
    protected void onPause() {
        bundle.putString("companyName", companyName.getText().toString());
        bundle.putString("desc", description.getText().toString());
        super.onPause();
    }

    public void openAddUsersActivity() {
        String name = companyName.getText().toString();
        String desc = companyName.getText().toString();
        if (name.length() == 0) {
            Utilities.toastMessage("Company name cannot be empty", CreateCompanyActivity.this);
            return;
        }

        Intent addPeopleIntent = new Intent(CreateCompanyActivity.this, AddUsersActivity.class);
        addPeopleIntent.putExtra("previous_activity", "Company");
        addPeopleIntent.putExtra("logged_name", projectOwner.getName());
        addPeopleIntent.putExtra("logged_name", projectOwner.getSurname());
        addPeopleIntent.putExtra("logged_mail", logged_mail);
        addPeopleIntent.putExtra("company_name", name);
        addPeopleIntent.putExtra("description", desc);
        startActivity(addPeopleIntent);
    }
}