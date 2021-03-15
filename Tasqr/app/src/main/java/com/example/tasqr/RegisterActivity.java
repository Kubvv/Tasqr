package com.example.tasqr;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class RegisterActivity extends AppCompatActivity {

    private Button goToLoginButton;
    private Button registerUserButton;
    private final EditText[] ets = new EditText[4];
    private final Bundle bundle = new Bundle();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private boolean mailUniqueness = false;

    /* Lifecycle functions */

    /* Initialize view objects */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        goToLoginButton = (Button) findViewById(R.id.login_activity_button);
        goToLoginButton.setOnClickListener(v -> openLoginActivity());
        registerUserButton = (Button) findViewById(R.id.register_button);
        registerUserButton.setOnClickListener(v -> registerUser());
        ets[0] = findViewById(R.id.name_textfield);
        ets[1] = findViewById(R.id.surname_textfield);
        ets[2] = findViewById(R.id.email_textfield);
        ets[3] = findViewById(R.id.password_textfield);
    }

    /* Resume previous inputs in textfields */
    protected void onResume() {
        ets[0].setText(bundle.getString("etName"));
        ets[1].setText(bundle.getString("etSurname"));
        ets[2].setText(bundle.getString("etMail"));
        ets[3].setText(bundle.getString("etPass"));
        super.onResume();
    }

    /* Save inputs from textfields */
    protected void onPause() {
        bundle.putString("etName", ets[0].getText().toString());
        bundle.putString("etSurname", ets[1].getText().toString());
        bundle.putString("etMail", ets[2].getText().toString());
        bundle.putString("etPass", ets[3].getText().toString());
        super.onPause();
    }

    /* Button listener function */

    /* Closes register activity */
    public void openLoginActivity() {
        RegisterActivity.this.finish();
    }

    /* Adds user to firestore and closes register activity */
    public void registerUser() {
        String[] data = new String[4];
        data[0] = ets[0].getText().toString();
        data[1] = ets[1].getText().toString();
        data[2] = ets[2].getText().toString();
        data[3] = ets[3].getText().toString();

        if (validateInput(data)) {
            User user = new User(data[0], data[1], data[2], data[3]);
            db.collection("Users").document(data[2])
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        toastMessage("Successfully registered");
                        openLoginActivity();
                    }
                });
        }
    }

    /* Validates if given account is unique and correct */
    private boolean validateInput(String[] data) {
        String item;
        HashMap<Integer, String> dic = new HashMap<>();
        dic.put(0, "Name");
        dic.put(1, "Surname");
        dic.put(2, "Mail");
        dic.put(3, "Password");

        for (int i = 0; i < data.length; i++) {
            if (data[i].isEmpty()) {
                item = dic.get(i);
                toastMessage(item + " cannot be empty");
                return false;
            }
            if (data[i].length() > 40 && i != 2) {
                item = dic.get(i);
                toastMessage(item + "must be at most 40 letters long");
                return false;
            }
        }

        Pattern p = Pattern.compile("[A-Za-z]{1,40}");
        Matcher m = p.matcher(data[0]);
        if (!m.matches()) {
            toastMessage("Name can only contain characters between A-Z and a-z");
            return false;
        }
        m = p.matcher(data[1]);
        if (!m.matches()) {
            toastMessage("Surname can only contain characters between A-z and a-z");
            return false;
        }
        p = Pattern.compile("@");
        m = p.matcher(data[2]);
        if (!m.find()) {
            toastMessage("Not a valid mail");
            return false;
        }

        /* TO DO clean this shit */
        /*DocumentReference checkMail = db.collection("Users").document(data[2]);
        checkMail.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        mailUniqueness = false;
                        toastMessage("User already exists");
                    }
                    else {
                        mailUniqueness = true;
                    }
                }
            }
        });*/

        return true;
    }

    /* Messages user with long toast message */
    private void toastMessage(String message) {
        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
    }
}