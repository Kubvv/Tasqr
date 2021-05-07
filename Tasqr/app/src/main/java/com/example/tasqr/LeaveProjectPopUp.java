package com.example.tasqr;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.tasqr.R;
import com.example.tasqr.classes.Company;
import com.example.tasqr.classes.Project;
import com.example.tasqr.classes.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class LeaveProjectPopUp extends DialogFragment {

    private static final String TAG = "LeaveProjectPopUp";

    private Bundle bundle;

    private Button leaveProjectButton;

    private Project project;
    private String logged_mail;
    private String logged_name;
    private String logged_surname;
    private String position;

    FirebaseDatabase database = FirebaseDatabase.getInstance("https://tasqr-android-default-rtdb.europe-west1.firebasedatabase.app/");
    DatabaseReference rootRef = database.getReference();
    DatabaseReference companiesRef = rootRef.child("Companies");

    /* Main on create method */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        /* Creating layout dependencies */
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.popup_leaveproject, null);

        bundle = getArguments();
        logged_mail = bundle.getString("logged_mail");
        logged_name = bundle.getString("logged_name");
        logged_surname = bundle.getString("logged_surname");
        position = bundle.getString("position");
        project = bundle.getParcelable("project");
        Log.e(TAG, "onCreateDialog: " + project.getName());

        leaveProjectButton = view.findViewById(R.id.leaveProjectButton);
        leaveProjectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //project.leaveCompany(logged_mail, position);
                startMainActivity();
            }
        });

        /* Setting listeners */
        builder.setView(view).setTitle(project.getName())
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        return builder.create();
    }

    private void startMainActivity() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("logged_name", logged_name);
        intent.putExtra("logged_surname", logged_surname);
        intent.putExtra("logged_mail", logged_mail);
        getDialog().dismiss();
        startActivity(intent);
    }
}
