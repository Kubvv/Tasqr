package com.example.tasqr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.example.tasqr.classes.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


/* Login activity is mainly used for logging users into the proper part of application.
 * It has two text fields that user can fill up, login and password.
 * Login activity also has two buttons, one to login user in, if mail and password are
 * corresponding to that stored in database. The other button is solely used to redirect to
 * Register activity. */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    /* View elements */
    private Button goToRegisterButton;
    private Button loginUserButton;
    private EditText etMail;
    private EditText etPass;

    /* bundle used in storing elements */
    Bundle bundle = new Bundle();

    /* Firebase database info */
    private FirebaseDatabase database;
    private DatabaseReference usersRef;

    /* lifecycle functions */

    /* Initialize view objects and firebase database, create button listeners */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        goToRegisterButton = (Button) findViewById(R.id.register_activity_button);
        goToRegisterButton.setOnClickListener(v -> openRegisterActivity());
        loginUserButton = (Button) findViewById(R.id.login_button);
        loginUserButton.setOnClickListener(v -> validateInput());
        etMail = (EditText) findViewById(R.id.email_textfield);
        etPass = (EditText) findViewById(R.id.password_textfield);

        database = FirebaseDatabase.getInstance("https://tasqr-android-default-rtdb.europe-west1.firebasedatabase.app/");
        usersRef = database.getReference("Users");
    }

    /* Resume previous inputs in text fields */
    protected void onResume() {
        etMail.setText(bundle.getString("etMail"));
        etPass.setText(bundle.getString("etPass"));
        super.onResume();
    }

    /* Saves inputs from text fields */
    protected void onPause() {
        bundle.putString("etMail", etMail.getText().toString());
        bundle.putString("etPass", etPass.getText().toString());
        super.onPause();
    }

    /* Button listener functions */

    /* Opens register activity */
    public void openRegisterActivity() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    /* Checks if there is a registered user with given mail and corresponding password. If yes,
    * pass user info to bundle and go to main activity. If no, print appropriate toast message */
    private void validateInput() {
        String mail = etMail.getText().toString();
        String pass = etPass.getText().toString();
        if (mail.length() == 0 || pass.length() == 0) {
            return;
        }

        /* Query the database for user with given mail */
        Query q = usersRef.orderByChild("mail").equalTo(mail);
        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                /* If found, check if password matches */
                if (snapshot.getChildrenCount() == 1) {
                    for (DataSnapshot childSnapshot: snapshot.getChildren()) {
                        User u = childSnapshot.getValue(User.class);
                        if (u.getPassword().equals(pass)) {
                            loginUser(u.getName(), u.getSurname(), u.getMail());
                        }
                        else {
                            Utilities.toastMessage("Wrong password", LoginActivity.this);
                        }
                    }
                }
                else {
                    Utilities.toastMessage("Wrong mail or password", LoginActivity.this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utilities.toastMessage("error" + error.toString(), LoginActivity.this);
            }
        });
    }

    /* Performs basic setup before moving on to Main Activity */
    private void loginUser(String name, String surname, String mail) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("logged_name", name);
        intent.putExtra("logged_surname", surname);
        intent.putExtra("logged_mail", mail);
        startActivity(intent);
        finish();
    }
}