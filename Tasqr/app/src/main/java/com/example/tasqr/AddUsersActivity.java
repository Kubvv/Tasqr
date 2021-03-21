package com.example.tasqr;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AddUsersActivity extends AppCompatActivity {

    private static final String TAG = "AddUsersActivity";

    private Bundle bndl = new Bundle();
    private ListView listView;
    private FirebaseDatabase database;
    private DatabaseReference rootRef;
    private DatabaseReference usersRef;
    private ArrayList<User> userArray = new ArrayList<>();
    private ArrayList<String> displayArray = new ArrayList<>();

    private ImageButton nigga;
    private Integer[] avatars = {R.drawable.avatarcircle, R.drawable.white, R.drawable.asian};
    private int currentPhoto = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addusers);

        nigga = findViewById(R.id.snickers);
        nigga.setImageResource(avatars[currentPhoto]);

        nigga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPhoto = (currentPhoto + 1) % 3;
                nigga.setImageResource(avatars[currentPhoto]);
            }
        });

        bndl = getIntent().getExtras();


        listView = (ListView)findViewById(R.id.userlist);

        /* establish connection to database and some references */

        database = FirebaseDatabase.getInstance("https://tasqr-android-default-rtdb.europe-west1.firebasedatabase.app/");
        rootRef = database.getReference();
        usersRef = rootRef.child("Users");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                /* iterate through users and add each to array */

                for (DataSnapshot ds : snapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    userArray.add(user);
                    displayArray.add(user.getNameSurname());
                }

                /* create some weird ass adapter for list view */
                ArrayAdapter<String> adapter = new ArrayAdapter(AddUsersActivity.this, android.R.layout.simple_list_item_multiple_choice, displayArray);

                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error);
            }
        });

    }
}
