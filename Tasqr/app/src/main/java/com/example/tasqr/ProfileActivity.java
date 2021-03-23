package com.example.tasqr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ProfileActivity";

    private ImageView avatarImageView;
    private TextView emailTextView, passwordTextView, nameTextView, surnameTextView;
    private String logged_mail;
    private String clicked_mail;
    private User user;
    private Button buttonSettings, buttonLogout, buttonUserList, buttonCreateCompany, buttonChangeAvatar;

    private Bundle bndl = new Bundle();

    private FirebaseDatabase database = FirebaseDatabase.getInstance("https://tasqr-android-default-rtdb.europe-west1.firebasedatabase.app/");
    private DatabaseReference rootRef = database.getReference();
    private DatabaseReference usersRef = rootRef.child("Users");

    private StorageReference avatarRef;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        avatarImageView = findViewById(R.id.user_avatar);
        emailTextView = findViewById(R.id.user_email);
        passwordTextView = findViewById(R.id.user_password);
        nameTextView = findViewById(R.id.user_name);
        surnameTextView = findViewById(R.id.user_surname);
        buttonSettings = findViewById(R.id.button_settings);
        buttonLogout = findViewById(R.id.button_logout);
        buttonUserList = findViewById(R.id.button_user_list);
        buttonCreateCompany = findViewById(R.id.button_create_company);
        buttonChangeAvatar = findViewById(R.id.button_change_avatar);

        buttonLogout.setOnClickListener(this);
        buttonSettings.setOnClickListener(this);
        buttonUserList.setOnClickListener(this);
        buttonCreateCompany.setOnClickListener(this);
        buttonChangeAvatar.setOnClickListener(this);

        bndl = getIntent().getExtras();

        logged_mail = bndl.getString("logged_mail");
        clicked_mail = bndl.getString("clicked_mail");

        if (clicked_mail == null) {
            clicked_mail = logged_mail;
        }

        /* reference to user's avatar */
        avatarRef = FirebaseStorage.getInstance().getReference("Images/" + clicked_mail);

        /* fetch user's avatar from database and set it */
        avatarRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(ProfileActivity.this).load(uri).into(avatarImageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
//                Toast.makeText(ProfileActivity.this, "Failed to fetch users avatar from database", Toast.LENGTH_LONG).show();
                avatarImageView.setImageResource(R.drawable.avatar);
            }
        });


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
            buttonSettings.setVisibility(View.VISIBLE);
            buttonLogout.setVisibility(View.VISIBLE);
            buttonUserList.setVisibility(View.VISIBLE);
            buttonCreateCompany.setVisibility(View.VISIBLE);
            buttonChangeAvatar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
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
            case R.id.button_change_avatar:
                Intent changeAvatarIntent = new Intent(this, ChangeAvatarActivity.class);

                changeAvatarIntent.putExtra("logged_mail", logged_mail);

                startActivity(changeAvatarIntent);
                break;
        }
    }

    private void setTextViews(User usr) {
        nameTextView.setText(usr.getName());
        surnameTextView.setText(usr.getSurname());
        emailTextView.setText(usr.getMail());
        if (clicked_mail == logged_mail)
            passwordTextView.setText(usr.getPassword());
    }

}