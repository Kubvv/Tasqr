package com.example.tasqr;

import android.content.Intent;
import android.os.Bundle;

import com.example.tasqr.classes.Company;
import com.example.tasqr.classes.Project;
import com.example.tasqr.classes.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ChangeOwnershipActivity extends AppCompatActivity implements ConfirmationPopUp.ConfirmationListener {

    private Bundle bndl;

    private String previous_activity;
    private String logged_mail;

    private TextView changeOwnerTitle;
    private EditText searchUsers;

    /* Arraylists used for creating user listView */
    private ListView listView;
    private ArrayList<User> userArray = new ArrayList<>();
    private ArrayList<String> displayArray = new ArrayList<>();
    ArrayAdapter<String> adapter;

    /* Firebase database */
    private FirebaseDatabase database = FirebaseDatabase.getInstance("https://tasqr-android-default-rtdb.europe-west1.firebasedatabase.app/");
    private DatabaseReference rootRef = database.getReference();
    private DatabaseReference usersRef = rootRef.child("Users");
    private DatabaseReference projectsRef = rootRef.child("Projects");
    private DatabaseReference companiesRef = rootRef.child("Companies");

    /* Company info */
    private Company currCompany;

    /* Project info */
    private Project currProject;

    private User oldOwner;
    private User clickedUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_ownership);

        bndl = getIntent().getExtras();
        previous_activity = bndl.getString("previous_activity");
        logged_mail = bndl.getString("logged_mail");
        currCompany = bndl.getParcelable("company");

        changeOwnerTitle = findViewById(R.id.changeOwnershipTitle);
        searchUsers = findViewById(R.id.editTextOwnership);

        searchUsers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    (ChangeOwnershipActivity.this).adapter.getFilter().filter(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        listView = (ListView) findViewById(R.id.companylist);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /* get user's mail from userArray by index since we know displayArray index that was clicked on
                * and both arrays share indices */
                showPopUp(position);
            }
        });

        switch(previous_activity) {
            case "manageCompany":
                changeOwnerTitle.setText(String.format("Change %s's owner", currCompany.getName()));
                fetchCompanyWorkers();
                break;
            case "manageProject":
                changeOwnerTitle.setText(String.format("Change %s's manager", currProject.getName()));

                break;
        }
    }

    private void fetchCompanyWorkers() {
        /* fetching company users in order to show them in a listview */
        ArrayList<String> alreadyWorking = currCompany.getWorkers();
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                /* iterate through users and add each that works in company to array */
                int i = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (i == alreadyWorking.size()) {
                        break; /* end of company users array */
                    }
                    User user = ds.getValue(User.class);
                    /* If user is not the owner show him on the list of users that can be added */
                    if (user.getMail().equals(alreadyWorking.get(i))) {
                        userArray.add(user);
                        displayArray.add(user.getName() + " " + user.getSurname());
                        i++;
                    }
                }

                setUserListAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utilities.toastMessage("error " + error, ChangeOwnershipActivity.this);
            }
        });
    }

    /* create adapter for list view */
    private void setUserListAdapter() {
        adapter = new ArrayAdapter(ChangeOwnershipActivity.this, R.layout.single_user_list_item, displayArray);
        listView.setAdapter(adapter);
    }

    private void showPopUp(int position) {
        ConfirmationPopUp popUp = new ConfirmationPopUp("changeOwner", position);
        User clickedUser = userArray.get(position);
        Bundle bundle = new Bundle();
        bundle.putString("logged_mail", logged_mail);
        bundle.putParcelable("company", currCompany);
        String text = String.format("Are you sure you want to pass ownership to %s? You'll be degraded to regular worker.",
                clickedUser.getName() + " " + clickedUser.getSurname());
        bundle.putString("text", text);
        popUp.setArguments(bundle);
        popUp.show(getSupportFragmentManager(), "ConfirmationPopUp");
    }

    public void changeOwner(User newOwner) {
        /* company changes */
        String oldOwnerMail = currCompany.getOwner();
        currCompany.setOwner(newOwner.getMail());
        ArrayList<String> tmp = currCompany.getWorkers();
        tmp.remove(newOwner.getMail());
        currCompany.setWorkers(tmp);
        tmp = currCompany.getManagers();
        if (tmp != null) {
            tmp.remove(newOwner.getMail());
            currCompany.setManagers(tmp);
        }
        companiesRef.child(currCompany.getId()).setValue(currCompany);

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User u;
                int i = 0;
                ArrayList<String> workersTmp = currCompany.getWorkers();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (i == workersTmp.size()) {
                        workersTmp.add(oldOwnerMail);
                        break;
                    }
                    u = ds.getValue(User.class);
                    if (u.getMail().equals(oldOwnerMail)) {
                        workersTmp.add(i, oldOwnerMail);
                        break;
                    }
                    if (u.getMail().equals(workersTmp.get(i))) {
                        i++;
                    }
                }
                companiesRef.child(currCompany.getId()).child("workers").setValue(workersTmp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utilities.toastMessage("error " + error, ChangeOwnershipActivity.this);
            }
        });

        /* user changes */
        Query q = usersRef.orderByChild("mail").equalTo(oldOwnerMail);
        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> ownerTmp;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    oldOwner = ds.getValue(User.class);
                }
                ownerTmp = oldOwner.getManagedCompanies();
                ownerTmp.remove(currCompany.getName());
                usersRef.child(oldOwner.getId()).child("managedCompanies").setValue(ownerTmp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utilities.toastMessage("error " + error, ChangeOwnershipActivity.this);
            }
        });

        tmp = newOwner.getManagedCompanies();
        if (!tmp.contains(currCompany.getName())) {
            tmp.add(currCompany.getName());
        }
        usersRef.child(newOwner.getId()).child("managedCompanies").setValue(tmp);

        openManageCompaniesActivity();
    }

    private void openManageCompaniesActivity() {
        Intent intent = new Intent(ChangeOwnershipActivity.this, ManageCompanyActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("logged_mail", logged_mail);
        startActivity(intent);
    }

    @Override
    public void confirmation(int position) {
        changeOwner(userArray.get(position));
    }
}