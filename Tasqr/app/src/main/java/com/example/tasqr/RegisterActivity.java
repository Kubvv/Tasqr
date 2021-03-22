package com.example.tasqr;

import java.util.HashMap;
import java.util.regex.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private Button goToLoginButton;
    private Button registerUserButton;
    private final EditText[] ets = new EditText[4];
    private final Bundle bundle = new Bundle();
    private FirebaseDatabase database;
    private DatabaseReference usersRef;

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

        database = FirebaseDatabase.getInstance("https://tasqr-android-default-rtdb.europe-west1.firebasedatabase.app/");
        usersRef = database.getReference("Users");

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

    /* Button listener functions */

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

        validateInput(data);
    }

    /* Validates if given account is unique and correct */
    private void validateInput(String[] data) {
        String item;
        HashMap<Integer, String> dic = new HashMap<>();
        dic.put(0, "Name");
        dic.put(1, "Surname");
        dic.put(2, "Mail");
        dic.put(3, "Password");

        /* Checking the lengths of inputs, do not empty and lengthy inputs. */
        for (int i = 0; i < data.length; i++) {
            if (data[i].isEmpty()) {
                item = dic.get(i);
                Utilities.toastMessage(item + " cannot be empty", RegisterActivity.this);
                return;
            }
            /* mail is the only excption and does not have upper limit */
            if (data[i].length() > 40 && i != 2) {
                item = dic.get(i);
                Utilities.toastMessage(item + "must be at most 40 letters long", RegisterActivity.this);
                return;
            }
        }

        Pattern p = Pattern.compile("[A-Za-z]{1,40}");
        Matcher m = p.matcher(data[0]);
        if (!m.matches()) {
            Utilities.toastMessage("Name can only contain characters between A-Z and a-z", RegisterActivity.this);
            return;
        }
        m = p.matcher(data[1]);
        if (!m.matches()) {
            Utilities.toastMessage("Surname can only contain characters between A-z and a-z", RegisterActivity.this);
            return;
        }
        p = Pattern.compile("@");
        m = p.matcher(data[2]);
        if (!m.find()) {
            Utilities.toastMessage("Not a valid mail", RegisterActivity.this);
            return;
        }

        /* TO DO change id of database bo znowu sie pruje o . # $ [ i ] */
        Query q = database.getReference().child("Users").orderByChild("mail").equalTo(data[2]);
        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount() > 0) {
                    Utilities.toastMessage("User already exists", RegisterActivity.this);
                }
                else {
                    /* need to call this function here to overcome multithreading */
                    addUser(data);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utilities.toastMessage("error", RegisterActivity.this);
            }
        });

        /*DocumentReference checkMail = db.collection("Users").document(data[2]);
        checkMail.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        Utilities.toastMessage("User already exists");
                    }
                    else {
                        addUser(data);
                    }
                }
            }
        });*/
    }

    /* data is basic user info data[2] references user mail mail  */
    private void addUser(String[] data) {

        User user = new User(data[0], data[1], data[2], data[3]);
        usersRef.child(data[2]).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Utilities.toastMessage("Successfully registered", RegisterActivity.this);
                openLoginActivity();
            }
        });

        /* Old firestore method */
        /*db.collection("Users").document(data[2])
            .set(user)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Utilities.toastMessage("Successfully registered");
                    openLoginActivity();
                }
            }); */
    }
}