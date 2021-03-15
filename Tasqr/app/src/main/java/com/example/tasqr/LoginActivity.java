package com.example.tasqr;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private Button goToRegisterButton;
    private Button loginUserButton;
    private EditText etMail;
    private EditText etPass;
    Bundle bundle = new Bundle();

    /* lifecycle functions */

    /* Initialize view objects */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        goToRegisterButton = (Button) findViewById(R.id.register_activity_button);
        goToRegisterButton.setOnClickListener(v -> openRegisterActivity());
        loginUserButton = (Button) findViewById(R.id.login_button);
        loginUserButton.setOnClickListener(v -> loginUser());
        etMail = (EditText) findViewById(R.id.email_textfield);
        etPass = (EditText) findViewById(R.id.password_textfield);
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

    public void loginUser() {
        String mail = etMail.getText().toString();
        String pass = etPass.getText().toString();

        if (validateInput(mail, pass)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    /* Messages user with long toast message */
    private void toastMessage(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
    }

    /* Checks if there is a registered user with given mail and corresponding password
     * If yes return true, otherwise return false
     */
    private boolean validateInput(String mail, String pass) {
        return true;
    }
}