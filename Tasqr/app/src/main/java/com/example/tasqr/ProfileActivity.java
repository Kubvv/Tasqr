/*
 * PROFILE ACTIVITY
 * Contains  User's profile with it's data stored in firebase realtime db
 *           User's profile picture stored from firebase storage
 *           Button for user's settings
 *           Button for logging out of an account
 *           Button for viewing the list of all users in database
 *           Button for changing user's avatar
 *           Button for deleting user's account
 */

package com.example.tasqr;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.tasqr.Styling.RecyclerViewAdapter;
import com.example.tasqr.classes.User;
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

import java.util.ArrayList;
import java.util.Arrays;

/* Profile activity has two functionalities, depending if you are inspecting your own profile or someone else's */
public class ProfileActivity extends AppCompatActivity implements View.OnClickListener, RecyclerViewAdapter.OnSkillListener {

    private User user;
    /* Stores logged user mail */
    private String logged_mail;
    /* Stores clicked user mail, in order to fetch his data from db */
    private String clicked_mail;

    private RecyclerView recyclerView;

    /* Firebase database */
    private final FirebaseDatabase database = FirebaseDatabase.getInstance("https://tasqr-android-default-rtdb.europe-west1.firebasedatabase.app/");
    private final DatabaseReference rootRef = database.getReference();
    private final DatabaseReference usersRef = rootRef.child("Users");

    private StorageReference avatarRef;

    /* View items */
    private ImageView avatarImageView;
    private TextView emailTextView, nameTextView, surnameTextView, skillText;
    private Button buttonSettings, buttonLogout, buttonCreateCompany, buttonUpdateProfile;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Uri avatarUri;

    private final int LAUNCH_UPDATE_PROFILE_ACTIVITY = 1;

    /* MAIN ON CREATE METHOD */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        setBehavior();

        Bundle bndl = getIntent().getExtras();
        logged_mail = bndl.getString("logged_mail");
        clicked_mail = bndl.getString("clicked_mail");

        if (clicked_mail == null)
            clicked_mail = logged_mail;

        fetchData();

        /* hide buttons if clicked on profile was not equal to logged user */
        if (!clicked_mail.equals(logged_mail)) {
            hideButtons();
            swipeRefreshLayout.setEnabled(false);
        }
        else
            setRefresher();

    }

    /* FIND VIEWS AND SET LISTENERS */
    private void setBehavior(){
        avatarImageView = findViewById(R.id.user_avatar);
        emailTextView = findViewById(R.id.user_email);
        nameTextView = findViewById(R.id.user_name);
        surnameTextView = findViewById(R.id.user_surname);
        buttonSettings = findViewById(R.id.button_settings);
        buttonLogout = findViewById(R.id.button_logout);
        buttonCreateCompany = findViewById(R.id.button_manage_company);
        buttonUpdateProfile = findViewById(R.id.button_update_profile);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        skillText = findViewById(R.id.skill_text);
        recyclerView = findViewById(R.id.skillsList);

        buttonLogout.setOnClickListener(this);
        buttonSettings.setOnClickListener(this);
        buttonCreateCompany.setOnClickListener(this);
        buttonUpdateProfile.setOnClickListener(this);
    }

    /* FETCH DATA FROM DATABASE */
    private void fetchData(){
        /* reference to user's avatar */
        avatarRef = FirebaseStorage.getInstance().getReference("Images/" + clicked_mail);

        /* fetch user's avatar from database and set it */
        avatarRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                avatarUri = uri;
                Picasso.with(ProfileActivity.this).load(uri).into(avatarImageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                avatarImageView.setImageResource(R.drawable.avatar);
            }
        });

        /* get user of clicked mail from database */
        Query query = usersRef.orderByChild("mail").equalTo(clicked_mail);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot: snapshot.getChildren())
                    user = childSnapshot.getValue(User.class);

                /* need to call this function here to overcome multithreading */
                setTextViews(user);
                initRecyclerView(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    /* Basic method for determining clicked button */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.button_logout:
                logout();
                break;
            case R.id.button_manage_company:
                Intent manageCompanyIntent = new Intent(this, ManageCompanyActivity.class);
                manageCompanyIntent.putExtra("logged_mail", logged_mail);
                startActivity(manageCompanyIntent);
                break;
            case R.id.button_update_profile:
                Intent updateProfileIntent = new Intent(this, UpdateProfileActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("user", user);
                if (avatarUri != null)
                    bundle.putString("uri", avatarUri.toString());
                updateProfileIntent.putExtras(bundle);
                startActivityForResult(updateProfileIntent, LAUNCH_UPDATE_PROFILE_ACTIVITY);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LAUNCH_UPDATE_PROFILE_ACTIVITY && resultCode == Activity.RESULT_OK) {
            String new_avatar = data.getStringExtra("new_avatar_uri");
            if (new_avatar != null) {
                avatarUri = Uri.parse(new_avatar);
                Picasso.with(ProfileActivity.this).load(avatarUri).into(avatarImageView);
            }

            String new_name = data.getStringExtra("new_name");
            String new_surname = data.getStringExtra("new_surname");
            ArrayList<String> new_skills = data.getStringArrayListExtra("new_skills");

            if (new_name != null)
                user.setName(new_name);
            if (new_surname != null)
                user.setSurname(new_surname);
            if (new_skills != null)
                user.setSkills(new_skills);

            setTextViews(user);
            initRecyclerView(user);
        }
    }

    /* Sets textviews accordingly to found user usr*/
    private void setTextViews(User usr) {
        nameTextView.setText(usr.getName());
        surnameTextView.setText(usr.getSurname());
        emailTextView.setText(usr.getMail());
    }

    /* Sets up refresh layout */
    private void setRefresher() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                /* refresh user info */
                DatabaseReference userRef = database.getReference("Users/" + user.getId());
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        user = snapshot.getValue(User.class);
                        if (user != null)
                            setTextViews(user);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

                /* refresh avatar */
                avatarRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.with(ProfileActivity.this).load(uri).into(avatarImageView);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        avatarImageView.setImageResource(R.drawable.avatar);
                    }
                });

                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    /* used for setting buttons' visibility for when the profile we are on are not our's */
    private void hideButtons() {
        buttonSettings.setVisibility(View.GONE);
        buttonLogout.setVisibility(View.GONE);
        buttonCreateCompany.setVisibility(View.GONE);
        buttonUpdateProfile.setVisibility(View.GONE);


    }

    private void initRecyclerView(User user) {
        ArrayList<String> userSkills = new ArrayList<>();
        int recyclerSize = !clicked_mail.equals(logged_mail) ? 16 : 8;

        if (user.getSkills() == null || user.getSkills().size() == 0)
            skillText.setText("No skills yet :(");
        else {
            skillText.setText("My skills:");
            for (int i = 0; i < recyclerSize && i < user.getSkills().size(); i++)
                userSkills.add(user.getSkills().get(i));
        }

        boolean[] selected = new boolean[recyclerSize];
        Arrays.fill(selected, true);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(userSkills, this, this, selected, false);
        recyclerView.setAdapter(adapter);
    }

    /* logs user out of an account and opens login activity */
    private void logout() {
        SharedPreferences preferences = getSharedPreferences("autoLogin", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("isLogged", "false");
        editor.apply();

        Intent logoutIntent = new Intent(this, LoginActivity.class);
        logoutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(logoutIntent);
    }


    @Override
    public void onSkillClick(int position) {
        //pass
    }
}