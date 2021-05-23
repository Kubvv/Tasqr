/*
 *   MANAGE COMPANY POPUP
 *   A dialog fragment which gets user input for managing company
 *   CONTAINS    buttons
 *               EditText form
 * */

package com.example.tasqr;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

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
    private Button leaveCompanyButton;

    private Company company;
    private String position;
    private String logged_mail;
    private boolean isOwner;
    private boolean isManager;

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
        position = bundle.getString("position");
        isOwner = bundle.getBoolean("isOwner");
        isManager = bundle.getBoolean("isManager");

        addUsersButton = view.findViewById(R.id.addUsersButton);
        addUsersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAddUsersActivity("manageCompanyUsers");
            }
        });
        addManagersButton = view.findViewById(R.id.addLeadersButton);
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
                if (company.getWorkers() == null || company.getWorkers().size() == 0) {
                    Utilities.toastMessage("No users to choose from", getActivity());
                }
                else {
                    startChangeOwnershipActivity();
                }
            }
        });

        deleteCompanyButton = view.findViewById(R.id.deleteComapnyButton);
        deleteCompanyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO delete company
            }
        });

        leaveCompanyButton = view.findViewById(R.id.leaveCompanyButton);
        leaveCompanyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                company.leaveCompany(logged_mail, position);
                refreshManageCompanies();
            }
        });

        if (isOwner) {
            addUsersButton.setVisibility(View.VISIBLE);
            addManagersButton.setVisibility(View.VISIBLE);
            changeOwnerButton.setVisibility(View.VISIBLE);
            deleteCompanyButton.setVisibility(View.VISIBLE);
        }
        else if (isManager) {
            addUsersButton.setVisibility(View.VISIBLE);
            leaveCompanyButton.setVisibility(View.VISIBLE);
        }
        else {
            leaveCompanyButton.setVisibility(View.VISIBLE);
        }

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

    private void startChangeOwnershipActivity() {
        Intent intent = new Intent(getContext(), ChangeOwnershipActivity.class);
        intent.putExtra("logged_mail", logged_mail);
        intent.putExtra("company", company);
        intent.putExtra("previous_activity", "manageCompany");
        getDialog().dismiss();
        startActivity(intent);
    }

    private void refreshManageCompanies() {
        Intent intent = new Intent(getContext(), ManageCompanyActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("logged_mail", logged_mail);
        getDialog().dismiss();
        startActivity(intent);
    }
}
