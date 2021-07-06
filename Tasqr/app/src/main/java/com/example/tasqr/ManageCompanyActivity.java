/*
 * MANAGE COMPANY ACTIVITY
 * Contains     ListView list of all companies user is currently in
 */

package com.example.tasqr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.tasqr.PopUps.ManageCompanyPopUp;
import com.example.tasqr.classes.Company;
import com.example.tasqr.classes.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class ManageCompanyActivity extends AppCompatActivity {

    private String logged_mail;
    private User user;

    private ListView listView;
    private ArrayAdapter<String> adapter;

    private DatabaseReference companyRef;

    private final ArrayList<Company> companyArray = new ArrayList<>();
    private final ArrayList<String> nameArray = new ArrayList<>();
    private final ArrayList<String> positionArray = new ArrayList<>();

    /* HELPER INNER ARRAY ADAPTER */
    private static class CompanyList extends ArrayAdapter {

        private final ArrayList<String> nameArray;
        private final ArrayList<String> positionArray;
        private final Activity context;
        private final HashMap<String, Integer> colorMap;

        public CompanyList(Activity context, ArrayList<String> nameArray, ArrayList<String> positionArray) {
            super(context, R.layout.company_list_item, nameArray);
            this.context = context;
            this.nameArray = nameArray;
            this.positionArray = positionArray;
            colorMap = new HashMap<String, Integer>() {{
                put("owner", ContextCompat.getColor(context, R.color.strongest_text_color));
                put("manager", ContextCompat.getColor(context, R.color.stronger_text_color));
                put("employee", ContextCompat.getColor(context, R.color.text_color));
            }};
        }

        /* Creates one row of ListView, consisting of company name and position */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            /* Set up */
            View row = convertView;
            LayoutInflater inflater = context.getLayoutInflater();
            if (convertView == null)
                row = inflater.inflate(R.layout.company_list_item, null, true);

            /* Find and set */
            TextView companyName = (TextView) row.findViewById(R.id.nameTextView);
            TextView companyPosition = (TextView) row.findViewById(R.id.companyPositionTextView);

            String positionName = positionArray.get(position);
            companyName.setText(nameArray.get(position));
            companyPosition.setText(positionName);
            companyName.setTextColor(colorMap.get(positionName));
            companyPosition.setTextColor(colorMap.get(positionName));
            return row;
        }
    }


    /* MAIN ON CREATE METHOD */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_managecompanies);

        Bundle bndl = getIntent().getExtras();
        logged_mail = bndl.getString("logged_mail");

        /* Database fetch */
        /* Firebase database */
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://tasqr-android-default-rtdb.europe-west1.firebasedatabase.app/");
        DatabaseReference usersRef = database.getReference("Users");
        companyRef = database.getReference("Companies");

        listView = (ListView) findViewById(R.id.companylist);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showPopUp(companyArray.get(position), nameArray.get(position), positionArray.get(position));
            }
        });

        Button createCompanyButton = (Button) findViewById(R.id.createCompanyButton);
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
                for (DataSnapshot ds : snapshot.getChildren())
                    user = ds.getValue(User.class);

                createListView();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void createListView() {
        companyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Company c = ds.getValue(Company.class);
                    if (c.getOwner().equals(logged_mail)) {
                        companyArray.add(c);
                        nameArray.add(c.getName());
                        positionArray.add("owner");
                    }
                    else if (user.getCompanies().contains(c.getId())) {
                        companyArray.add(c);
                        nameArray.add(c.getName());
                        if (c.getManagers() != null && c.getManagers().contains(user.getMail()))
                            positionArray.add("manager");
                        else
                            positionArray.add("employee");
                    }
                }
                /* create list view */
                listView = findViewById(R.id.companylist);
                listView.setAdapter(new CompanyList(ManageCompanyActivity.this, nameArray, positionArray));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utilities.toastMessage("error " + error.toString(), ManageCompanyActivity.this);
            }
        });
    }

    private void showPopUp(Company c, String name, String position) {
        Bundle bundle = new Bundle();
        bundle.putString("logged_mail", logged_mail);
        bundle.putString("company_name", name);
        bundle.putString("position", position);
        bundle.putParcelable("company", c);

        if (position.equals("owner"))
            bundle.putBoolean("isOwner", true);
        else if (position.equals("manager")) {
            bundle.putBoolean("isOwner", false);
            bundle.putBoolean("isManager", true);
        }
        else {
            bundle.putBoolean("isOwner", false);
            bundle.putBoolean("isManager", false);
        }

        bundle.putParcelable("user", user);

        ManageCompanyPopUp popUp = new ManageCompanyPopUp();
        popUp.setArguments(bundle);
        popUp.show(getSupportFragmentManager(), "ManageCompanyPopUp");
    }

    private void startCreateCompany() {
        Intent createCompanyIntent = new Intent(this, CreateCompanyActivity.class);
        createCompanyIntent.putExtra("logged_mail", logged_mail);
        startActivity(createCompanyIntent);
    }
}
