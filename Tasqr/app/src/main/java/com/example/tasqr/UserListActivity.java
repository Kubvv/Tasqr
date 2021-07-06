/*
 * USER LIST ACTIVITY
 *
 * Used for displaying list of users in project's database
 *
 */

package com.example.tasqr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.tasqr.classes.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserListActivity extends AppCompatActivity {

    private static final String TAG = "UserListActivity";

    private Bundle bndl = new Bundle();
    private ListView listView;
    private final ArrayList<User> userArray = new ArrayList<>();
    private final ArrayList<String> displayArray = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        bndl = getIntent().getExtras();
        Log.d(TAG, "onCreate: " + bndl.getString("logged_mail"));

        listView = (ListView)findViewById(R.id.listview);

        /* establish connection to database and some references */

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://tasqr-android-default-rtdb.europe-west1.firebasedatabase.app/");
        DatabaseReference rootRef = database.getReference();
        DatabaseReference usersRef = rootRef.child("Users");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                /* iterate through users and add each to array */

                for (DataSnapshot ds : snapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    userArray.add(user);
                    displayArray.add(user.getName() + " " + user.getSurname());
                }

                /* create some weird adapter for list view */
                ArrayAdapter<String> adapter = new ArrayAdapter(UserListActivity.this, android.R.layout.simple_list_item_1, displayArray);

                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /* get user's mail from userArray by index since we know displayArray index that was clicked on and both arrays share indices */
                Intent profileIntent = new Intent(UserListActivity.this, ProfileActivity.class);

                profileIntent.putExtra("clicked_mail", userArray.get(position).getMail());
                profileIntent.putExtra("logged_mail", bndl.getString("logged_mail"));

                startActivity(profileIntent);
            }
        });
    }
}