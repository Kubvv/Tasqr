package com.example.tasqr;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    private Button registerButton;
    private EditText etMail;
    private EditText etPass;
    Bundle bundle = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        registerButton = (Button) findViewById(R.id.register_activity_button);
        registerButton.setOnClickListener(v -> openRegisterActivity());
        etMail = (EditText) findViewById(R.id.email_textfield);
        etPass = (EditText) findViewById(R.id.password_textfield);
    }

    public void openRegisterActivity() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    protected void onResume() {
        etMail.setText(bundle.getString("etMail"));
        etPass.setText(bundle.getString("etPass"));
        super.onResume();
    }

    protected void onPause() {
        bundle.putString("etMail", etMail.getText().toString());
        bundle.putString("etPass", etPass.getText().toString());
        super.onPause();
    }
}