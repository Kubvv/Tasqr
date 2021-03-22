package com.example.tasqr;

import android.app.Activity;
import android.widget.Toast;

public class Utilities {

    /* Messages user with long toast message in activity a */
    public static void toastMessage(String message, Activity a) {
        Toast.makeText(a, message, Toast.LENGTH_LONG).show();
    }
}
