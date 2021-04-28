package com.example.tasqr;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tasqr.classes.Company;
import com.example.tasqr.classes.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ManageCompanyActivity extends AppCompatActivity {

    private static final String TAG = "ManageCompanyActivity";

    private String logged_mail;
    private User user;
    private Bundle bndl;

    private Button createCompanyButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;

    /* Firebase database */
    private FirebaseDatabase database;
    private DatabaseReference usersRef;
    private DatabaseReference companyRef;

    private ArrayList<Company> owned = new ArrayList<>();
    private ArrayList<String> displayArray = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_managecompanies);

        bndl = getIntent().getExtras();
        logged_mail = bndl.getString("logged_mail");

        /* Database fetch */
        database = FirebaseDatabase.getInstance("https://tasqr-android-default-rtdb.europe-west1.firebasedatabase.app/");
        usersRef = database.getReference("Users");
        companyRef = database.getReference("Companies");

        listView = (ListView) findViewById(R.id.userlist);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showPopUp(owned.get(position), displayArray.get(position));
            }
        });

        createCompanyButton = (Button) findViewById(R.id.createCompanyButton);
        createCompanyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCreateCompany();
            }
        });

        Query q = usersRef.orderByChild("mail").equalTo(logged_mail);
        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    user = ds.getValue(User.class);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        createListView();
    }

    private void createListView() {
        Query q = companyRef.orderByChild("owner").equalTo(logged_mail);
        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Company c = ds.getValue(Company.class);
                    owned.add(c);
                    displayArray.add(c.getName());
                }
                /* create adapter for list view */
                adapter = new ArrayAdapter(ManageCompanyActivity.this, R.layout.company_list_item, displayArray);
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utilities.toastMessage("error " + error.toString(), ManageCompanyActivity.this);
            }
        });
    }

    private void showPopUp(Company c, String name) {
        ManageCompanyPopUp popUp = new ManageCompanyPopUp();
        Bundle bundle = new Bundle();
        bundle.putString("logged_mail", logged_mail);
        bundle.putString("company_name", name);
        bundle.putParcelable("company", c);
        popUp.setArguments(bundle);
        popUp.show(getSupportFragmentManager(), "ManageCompanyPopUp");
    }

    private void startCreateCompany() {
        Intent createCompanyIntent = new Intent(this, CreateCompanyActivity.class);
        createCompanyIntent.putExtra("logged_mail", logged_mail);
        startActivity(createCompanyIntent);
    }
}
