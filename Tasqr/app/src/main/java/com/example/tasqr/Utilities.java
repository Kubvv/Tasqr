package com.example.tasqr;

import android.app.Activity;
import android.widget.Toast;

/* Class for static methods that repeat themselves in project */
public class Utilities {

    /* Messages user with long toast message in activity a */
    public static void toastMessage(String message, Activity a) {
        Toast.makeText(a, message, Toast.LENGTH_LONG).show();
    }
}
