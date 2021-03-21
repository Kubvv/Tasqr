package com.example.tasqr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.LogDescriptor;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    
    private Button goToRegisterButton;
    private Button loginUserButton;
    private EditText etMail;
    private EditText etPass;
    Bundle bundle = new Bundle();
    private FirebaseDatabase database;
//    private DatabaseReference userdb;

    /* lifecycle functions */

    /* Initialize view objects */
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
//        userdb = database.getReference("Users");
    }

    /* Resume previous inputs in textfields */
    protected void onResume() {
        etMail.setText(bundle.getString("etMail"));
        etPass.setText(bundle.getString("etPass"));
        super.onResume();
    }

    /* Saves inputs from textfields */
    protected void onPause() {
        bundle.putString("etMail", etMail.getText().toString());
        bundle.putString("etPass", etPass.getText().toString());
        super.onPause();
    }

    /* Button listener function */

    /* Opens register activity */
    public void openRegisterActivity() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    /* Checks if there is a registered user with given mail and corresponding password */
    private void validateInput() {
        String mail = etMail.getText().toString();
        String pass = etPass.getText().toString();
        if (mail.length() == 0 || pass.length() == 0) {
            return;
        }

        Query q = database.getReference().child("Users").orderByChild("mail").equalTo(mail);
        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange: " + snapshot.getChildrenCount());
                if (snapshot.getChildrenCount() == 1) {
                    for (DataSnapshot childSnapshot: snapshot.getChildren()) {
                        User u = childSnapshot.getValue(User.class);
                        if (u.getPassword().equals(pass)) {
                            Log.d(TAG, "onDataChange: " + u.getPassword() + " " + u.getMail() + " " + pass);
                            loginUser(u.getName(), u.getSurname(), u.getMail());
                        }
                        else {
                            toastMessage("Wrong password");
                        }
                    }
                }
                else {
                    toastMessage("Wrong mail or password");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                toastMessage("error" + error.toString());
            }
        });

        /*{
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists() && doc.get("password").toString().equals(pass)) {
                        loginUser(doc.get("name").toString(), doc.get("surname").toString());
                    }
                    else {
                        toastMessage("Wrong mail or password");
                    }
                }
            }
        }); */
    }

    /* Performs basic setup before moving on to mainActivity */
    private void loginUser(String name, String surname, String mail) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("name", name);
        intent.putExtra("surname", surname);
        intent.putExtra("mail", mail);
        startActivity(intent);
        finish();
    }

    /* Messages user with long toast message */
    private void toastMessage(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
    }
}