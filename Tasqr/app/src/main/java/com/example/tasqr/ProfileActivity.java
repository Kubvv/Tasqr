package com.example.tasqr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ProfileActivity";

    private ImageView avatarImageView;
    private TextView emailTextView, passwordTextView, nameTextView, surnameTextView;
    private String logged_mail;
    private String clicked_mail;
    private User user;
    private Button buttonOptions, buttonLogout, buttonUserList, buttonCreateCompany;

    private Bundle bndl = new Bundle();

    private FirebaseDatabase database = FirebaseDatabase.getInstance("https://tasqr-android-default-rtdb.europe-west1.firebasedatabase.app/");
    private DatabaseReference rootRef = database.getReference();
    private DatabaseReference usersRef = rootRef.child("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        avatarImageView = findViewById(R.id.user_avatar);
        emailTextView = findViewById(R.id.user_email);
        passwordTextView = findViewById(R.id.user_password);
        nameTextView = findViewById(R.id.user_name);
        surnameTextView = findViewById(R.id.user_surname);
        buttonOptions = findViewById(R.id.button_profile_options);
        buttonLogout = findViewById(R.id.button_logout);
        buttonUserList = findViewById(R.id.button_user_list);
        buttonCreateCompany = findViewById(R.id.button_create_company);

        buttonLogout.setOnClickListener(this);
        buttonOptions.setOnClickListener(this);
        buttonUserList.setOnClickListener(this);
        buttonCreateCompany.setOnClickListener(this);

        bndl = getIntent().getExtras();

        logged_mail = bndl.getString("logged_mail");
        clicked_mail = bndl.getString("clicked_mail");

        if (clicked_mail == null) {
            clicked_mail = logged_mail;
        }

        /* get user of clicked mail from database */

        Query query = usersRef.orderByChild("mail").equalTo(clicked_mail);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange: children count " + snapshot.getChildrenCount());
                for (DataSnapshot childSnapshot: snapshot.getChildren()) {
                    user = childSnapshot.getValue(User.class);
                }

                /* need to call this function here to overcome multithreading */
                setTextViews(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error);
            }
        });

        /* if clicked on profile was equal to logged user */
        if (clicked_mail.equals(logged_mail)) {

            /* show buttons for logged user */
            buttonOptions.setVisibility(View.VISIBLE);
            buttonLogout.setVisibility(View.VISIBLE);
            buttonUserList.setVisibility(View.VISIBLE);
            buttonCreateCompany.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_profile_options:
                break;
            case R.id.button_logout:
                Intent logoutIntent = new Intent(this, LoginActivity.class);
                logoutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(logoutIntent);
                break;
            case R.id.button_user_list:
                Intent userListIntent = new Intent(this, UserListActivity.class);

                userListIntent.putExtra("logged_mail", logged_mail);

                startActivity(userListIntent);
                break;
            case R.id.button_create_company:
                break;
        }
    }

    private void setTextViews(User usr) {
        nameTextView.setText(usr.getName());
        surnameTextView.setText(usr.getSurname());
        emailTextView.setText(usr.getMail());
    }
}