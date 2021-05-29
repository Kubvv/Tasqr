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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
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

import com.example.tasqr.classes.Task;
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
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.ArrayList;

/* Profile activity has two functionalities, depending if you are inspecting your own profile or someone else's */
public class ProfileActivity extends AppCompatActivity implements View.OnClickListener, RecyclerViewAdapter.OnSkillListener {

    private static final String TAG = "ProfileActivity";

    private User user;
    /* Stores logged user mail */
    private String logged_mail;
    /* Stores clicked user mail, in order to fetch his data from db */
    private String clicked_mail;

    private Bundle bndl = new Bundle();

    private RecyclerView recyclerView;

    /* Firebase database */
    private FirebaseDatabase database = FirebaseDatabase.getInstance("https://tasqr-android-default-rtdb.europe-west1.firebasedatabase.app/");
    private DatabaseReference rootRef = database.getReference();
    private DatabaseReference usersRef = rootRef.child("Users");

    private StorageReference avatarRef;

    /* View items */
    private ImageView avatarImageView;
    private TextView emailTextView, nameTextView, surnameTextView;
    private Button buttonSettings, buttonLogout, buttonCreateCompany, buttonUpdateProfile, skillButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Uri avatarUri;

    private final int LAUNCH_UPDATE_PROFILE_ACTIVITY = 1;
    private final int LAUNCH_SKILLS_ACTIVITY = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        avatarImageView = findViewById(R.id.user_avatar);
        emailTextView = findViewById(R.id.user_email);
        nameTextView = findViewById(R.id.user_name);
        surnameTextView = findViewById(R.id.user_surname);
        buttonSettings = findViewById(R.id.button_settings);
        buttonLogout = findViewById(R.id.button_logout);
        buttonCreateCompany = findViewById(R.id.button_manage_company);
        buttonUpdateProfile = findViewById(R.id.button_update_profile);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        skillButton = findViewById(R.id.skill_button);

        buttonLogout.setOnClickListener(this);
        buttonSettings.setOnClickListener(this);
        buttonCreateCompany.setOnClickListener(this);
        buttonUpdateProfile.setOnClickListener(this);
        skillButton.setOnClickListener(this);

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
                for (DataSnapshot childSnapshot: snapshot.getChildren()) {
                    user = childSnapshot.getValue(User.class);
                }

                /* need to call this function here to overcome multithreading */
                setTextViews(user);
                initRecyclerView(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error);
            }
        });

        /* hide buttons if clicked on profile was not equal to logged user */
        if (!clicked_mail.equals(logged_mail)) {
            hideButtons();
        }

        setRefresher();
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
            case R.id.skill_button:
                Intent updateSkillIntent = new Intent(this, SkillsActivity.class);
                Bundle skillbundle = new Bundle();
                skillbundle.putParcelable("user", user);
                updateSkillIntent.putExtras(skillbundle);
                startActivityForResult(updateSkillIntent, LAUNCH_SKILLS_ACTIVITY);
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

            if (new_name != null)
                user.setName(new_name);
            if (new_surname != null)
                user.setSurname(new_surname);

            setTextViews(user);
        }
        else if (requestCode == LAUNCH_SKILLS_ACTIVITY && resultCode == Activity.RESULT_OK) {
            ArrayList<String> skills = data.getStringArrayListExtra("skills");
            user.setSkills(skills);
            usersRef.child(user.getId()).child("skills").setValue(skills);
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
        buttonSettings.setVisibility(View.INVISIBLE);
        buttonLogout.setVisibility(View.INVISIBLE);
        buttonCreateCompany.setVisibility(View.INVISIBLE);
        buttonUpdateProfile.setVisibility(View.INVISIBLE);
    }

    private void initRecyclerView(User user) {
        ArrayList<String> userSkills = new ArrayList<>();
        if (user.getSkills() == null || user.getSkills().size() == 0) {
            skillButton.setText("No skills yet :(");
        } else {
            skillButton.setText("My skills:");
            for (int i = 0; i < 3 && i < user.getSkills().size(); i++) {
                userSkills.add(user.getSkills().get(i));
            }
        }

        recyclerView = findViewById(R.id.skillsList);
        boolean[] selected = new boolean[] {true, true, true};
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(userSkills, this, this, selected, false);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
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

    private void deleteAccout() {
        //TODO
    }

    @Override
    public void onSkillClick(int position) {
        //pass
    }
}