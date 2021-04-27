/*
 *   ADD SUBTASK POPUP
 *   A dialog fragment which gets user input for creating new subtask
 *   CONTAINS    Cancel and add buttons
 *               EditText form
 * */

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

public class ManageCompanyPopUp extends DialogFragment {

    private static final String TAG = "ManageCompanyPopUp";

    private Bundle bundle;

    private Button addUsersButton;
    private Button addManagersButton;
    private Button changeOwnerButton;
    private Button deleteCompanyButton;

    private Company company;
    private String logged_mail;

    /* Main on create method */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        /* Creating layout dependencies */
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.popup_managecompany, null);

        bundle = getArguments();
        logged_mail = bundle.getString("logged_mail");
        company = bundle.getParcelable("company");
        Log.e(TAG, "onCreateDialog: " + company.getName());

        addUsersButton = view.findViewById(R.id.addUsersButton);
        addUsersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAddUsersActivity("manageCompanyUsers");
            }
        });
        addManagersButton = view.findViewById(R.id.addManagersButton);
        addManagersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (company.getWorkers() == null || company.getWorkers().size() == 0) {
                    Utilities.toastMessage("No users to choose from", getActivity());
                }
                else {
                    startAddUsersActivity("manageCompanyManagers");
                }
            }
        });

        changeOwnerButton = view.findViewById(R.id.changeOwnerButton);
        changeOwnerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO change owner
            }
        });

        deleteCompanyButton = view.findViewById(R.id.deleteComapnyButton);
        deleteCompanyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO delete company
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

    private void startAddUsersActivity(String previous_activity) {
        Intent intent = new Intent(getContext(), AddUsersActivity.class);
        intent.putExtra("logged_mail", logged_mail);
        intent.putExtra("company", company);
        intent.putExtra("previous_activity", previous_activity);
        intent.putExtra("company_name", company.getName());
        getDialog().dismiss();
        startActivity(intent);
    }
}
