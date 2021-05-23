package com.example.tasqr;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.regex.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.example.tasqr.classes.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/* Register activity is used to register users only. After successful registration Register Activity
* closes and redirects to Login activity. Register Activity has 4 text fields that user can fill,
* name, surname, mail, password. It also has 2 buttons used to register user and come back to login
* activity, respectively. Users can't register the same mail twice. Users can register only if their
* name and surname consists of max 40 letters, and their mail is a correct one. */
public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    /* View elements */
    private Button goToLoginButton;
    private Button registerUserButton;
    private final EditText[] ets = new EditText[5];

    /* bundle used in storing elements */
    private final Bundle bundle = new Bundle();

    /* Firebase database info */
    private FirebaseDatabase database;
    private DatabaseReference usersRef;

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

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
        ets[4] = findViewById(R.id.confirmpassword_textfield);

        database = FirebaseDatabase.getInstance("https://tasqr-android-default-rtdb.europe-west1.firebasedatabase.app/");
        usersRef = database.getReference("Users");

    }

    /* Resume previous inputs in textfields */
    protected void onResume() {
        ets[0].setText(bundle.getString("etName"));
        ets[1].setText(bundle.getString("etSurname"));
        ets[2].setText(bundle.getString("etMail"));
        ets[3].setText(bundle.getString("etPass"));
        ets[4].setText(bundle.getString("etPassConf"));
        super.onResume();
    }

    /* Save inputs from textfields */
    protected void onPause() {
        bundle.putString("etName", ets[0].getText().toString());
        bundle.putString("etSurname", ets[1].getText().toString());
        bundle.putString("etMail", ets[2].getText().toString());
        bundle.putString("etPass", ets[3].getText().toString());
        bundle.putString("etPassConf", ets[4].getText().toString());
        super.onPause();
    }

    /* Button listener functions */

    /* Closes register activity */
    public void openLoginActivity() {
        RegisterActivity.this.finish();
    }

    /* Parses texts that user inputted and goes on to validate them */
    public void registerUser() {
        String[] data = new String[5];
        data[0] = ets[0].getText().toString();
        data[1] = ets[1].getText().toString();
        data[2] = ets[2].getText().toString();
        data[3] = ets[3].getText().toString();
        data[4] = ets[4].getText().toString();

        validateInput(data);
    }

    /* Validates if given account is unique and correct, and if given name and surname consists of
    * max 40 letters only and min of 1 letter.  */
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
                Utilities.toastMessage(item + " must be at most 40 letters long", RegisterActivity.this);
                return;
            }
        }

        /* Using regex check if name and surname consists of only letters */
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

        if (!data[3].equals(data[4])) {
            Utilities.toastMessage("Passwords are not equal", RegisterActivity.this);
            return;
        }

        /* Also check mail correctness */

        /* old matching, to delete TODO */
        p = Pattern.compile("@");
        m = p.matcher(data[2]);
        if (!m.find()) {
            Utilities.toastMessage("Not a valid mail", RegisterActivity.this);
            return;
        }

        /* new matching, to uncomment TODO */
//        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(data[2]);
//        if (!matcher.find()) {
//            Utilities.toastMessage("Not a valid mail", RegisterActivity.this);
//            return;
//        }

        /* Check if parsed mail is already in the database. If not, go on to registering user*/
        Query q = usersRef.orderByChild("mail").equalTo(data[2]);
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
    }

    /* data is basic user info, data[2] references user's mail */
    /* addUser is used in adding the user to database. It creates a new instance of User object and
    * puts it in database  */
    private void addUser(String[] data) {

        DatabaseReference pushedUsersRef = usersRef.push();
        String id = pushedUsersRef.getKey();
        String hashedPassword;
        /* TODO uncomment me plis
        byte[] salt = createSalt();

        try {
            hashedPassword = Utilities.generateHash(data[3], salt);
        } catch (NoSuchAlgorithmException e) {
            Utilities.toastMessage("Error " + e, RegisterActivity.this);
            return;
        }
        String strSalt = Utilities.bytesToHex(salt);
        Log.e(TAG, "addUser: " + hashedPassword + " " + strSalt);*/

        /* TODO wyjebongo me plis */
        hashedPassword = data[3];
        String strSalt = "";

        User user = new User(id, data[0], data[1], data[2], hashedPassword, strSalt);
        usersRef.child(id).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Utilities.toastMessage("Successfully registered", RegisterActivity.this);
                /* redirect to login activity */
                openLoginActivity();
            }
        });
    }

    private byte[] createSalt() {
        byte[] bytes = new byte[20];
        SecureRandom random = new SecureRandom();
        random.nextBytes(bytes);
        return bytes;
    }
}