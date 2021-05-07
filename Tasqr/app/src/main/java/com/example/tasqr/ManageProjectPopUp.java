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
import com.example.tasqr.classes.Project;

public class ManageProjectPopUp extends DialogFragment {

    private static final String TAG = "ManageProjectPopUp";

    private Bundle bundle;

    private Project project;
    private String logged_mail;
    private String logged_name;
    private String logged_surname;

    /* Main on create method */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        /* Creating layout dependencies */
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.popup_manageproject, null);

        bundle = getArguments();
        logged_mail = bundle.getString("logged_mail");
        logged_name = bundle.getString("logged_name");
        logged_surname = bundle.getString("logged_surname");
        project = bundle.getParcelable("project");
        boolean isLeader = bundle.getBoolean("isLeader");

        Log.e(TAG, "onCreateDialog: " + project.getName());

        Button addUsersButton = view.findViewById(R.id.addUsersButton);
        addUsersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAddUsersActivity("manageProjectUsers");
            }
        });
        Button addLeadersButton = view.findViewById(R.id.addLeadersButton);
        addLeadersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (project.getWorkers().size() == 1) {
                    Utilities.toastMessage("No users to choose from", getActivity());
                } else {
                    startAddUsersActivity("manageProjectLeaders");
                }
            }
        });

        if(isLeader){
            addUsersButton.setVisibility(View.VISIBLE);
            addLeadersButton.setVisibility(View.VISIBLE);
        }
        
        Button leaveButton = view.findViewById(R.id.leaveButton);
        leaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (project.getLeaders().size() == 1 && project.getWorkers().size() != 1) {
                    Utilities.toastMessage("Choose a new leader before leaving", getActivity());
                } else {
                    //TODO delete user from project
                    startMainActivity();
                }
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

    private void startAddUsersActivity(String previous_activity) {
        Intent intent = new Intent(getContext(), AddUsersActivity.class);
        intent.putExtra("logged_mail", logged_mail);
        intent.putExtra("logged_name", logged_name);
        intent.putExtra("logged_surname", logged_surname);
        intent.putExtra("project", project);
        intent.putExtra("previous_activity", previous_activity);
        intent.putExtra("project_name", project.getName());
        intent.putExtra("company_name",  bundle.getString("company_name"));
        intent.putExtra("project_id", bundle.getString("project_id"));
        getDialog().dismiss();
        startActivity(intent);
    }

    private void startMainActivity() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Log.e(TAG, "startMainActivity: " + logged_name + " " + logged_surname);
        intent.putExtra("logged_name", logged_name);
        intent.putExtra("logged_surname", logged_surname);
        intent.putExtra("logged_mail", logged_mail);
        getDialog().dismiss();
        startActivity(intent);
    }
}
