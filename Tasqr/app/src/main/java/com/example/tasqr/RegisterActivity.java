package com.example.tasqr;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class RegisterActivity extends AppCompatActivity {

    private Button loginButton;
    private EditText ets[] = new EditText[4];
    private Bundle bundle = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        loginButton = (Button) findViewById(R.id.login_activity_button);
        loginButton.setOnClickListener(v -> openLoginActivity());
        ets[0] = findViewById(R.id.name_textfield);
        ets[1] = findViewById(R.id.surname_textfield);
        ets[2] = findViewById(R.id.email_textfield);
        ets[3] = findViewById(R.id.password_textfield);
    }

    public void openLoginActivity() {
        RegisterActivity.this.finish();
    }

    protected void onResume() {
        ets[0].setText(bundle.getString("etName"));
        ets[1].setText(bundle.getString("etSurname"));
        ets[2].setText(bundle.getString("etMail"));
        ets[3].setText(bundle.getString("etPass"));
        super.onResume();
    }

    protected void onPause() {
        bundle.putString("etName", ets[0].getText().toString());
        bundle.putString("etSurname", ets[1].getText().toString());
        bundle.putString("etMail", ets[2].getText().toString());
        bundle.putString("etPass", ets[3].getText().toString());
        super.onPause();
    }
}