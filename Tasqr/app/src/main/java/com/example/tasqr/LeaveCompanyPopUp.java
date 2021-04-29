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

import com.example.tasqr.classes.Company;
import com.example.tasqr.classes.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class LeaveCompanyPopUp extends DialogFragment {

    private static final String TAG = "LeaveCompanyPopUp";

    private Bundle bundle;

    private Button leaveCompanyButton;

    private User user;
    private Company company;
    private String logged_mail;
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
        View view = inflater.inflate(R.layout.popup_leavecompany, null);

        bundle = getArguments();
        logged_mail = bundle.getString("logged_mail");
        position = bundle.getString("position");
        company = bundle.getParcelable("company");
        Log.e(TAG, "onCreateDialog: " + company.getName());

        leaveCompanyButton = view.findViewById(R.id.leaveCompanyButton);
        leaveCompanyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                company.leaveCompany(logged_mail, position);
                refreshManageCompanies();
            }
        });

        /* Setting listeners */
        builder.setView(view).setTitle(bundle.getString("company_name"))
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        return builder.create();
    }

    private void refreshManageCompanies() {
        Intent intent = new Intent(getContext(), ManageCompanyActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("logged_mail", logged_mail);
        getDialog().dismiss();
        startActivity(intent);
    }
}
