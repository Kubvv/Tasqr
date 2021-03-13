package com.example.tasqr;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class RegisterActivity extends AppCompatActivity {

    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        loginButton = (Button) findViewById(R.id.login_activity_button);
        loginButton.setOnClickListener(v -> openLoginActivity());
    }

    public void openLoginActivity() {
        RegisterActivity.this.finish();
    }

}