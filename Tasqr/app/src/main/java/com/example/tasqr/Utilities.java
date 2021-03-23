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





    /* Register activity firestore path */

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



    /* Login activity firestore path */

        /*{
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists() && doc.get("password").toString().equals(pass)) {
                        loginUser(doc.get("name").toString(), doc.get("surname").toString());
                    }
                    else {
                        Utilities.toastMessage("Wrong mail or password");
                    }
                }
            }
        }); */
