package com.example.tasqr;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import java.lang.Object;

public class SplashActivity extends AppCompatActivity {

    private final int SPLASH_DISPLAY_LENGTH = 2200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent;
                SharedPreferences preferences = getSharedPreferences("autoLogin", MODE_PRIVATE);
                String isLogged = preferences.getString("isLogged", "");
                if (isLogged.equals("true")) {
                    mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                    mainIntent.putExtra("logged_name", preferences.getString("logged_name", ""));
                    mainIntent.putExtra("logged_surname", preferences.getString("logged_surname", ""));
                    mainIntent.putExtra("logged_mail", preferences.getString("logged_mail", ""));
                } else {
                    mainIntent = new Intent(SplashActivity.this, LoginActivity.class);
                }
                SplashActivity.this.startActivity(mainIntent);
                SplashActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

}