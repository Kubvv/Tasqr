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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.tasqr.classes.Company;
import com.example.tasqr.classes.User;

public class ChangeOwnershipPopUp extends DialogFragment {

    private Bundle bundle;

    private User clickedUser;
    private Company company;
    private String logged_mail;

    private TextView acceptText;

    /* Main on create method */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        /* Creating layout dependencies */
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.popup_acceptchoice, null);

        bundle = getArguments();
        logged_mail = bundle.getString("logged_mail");
        company = bundle.getParcelable("company");
        clickedUser = bundle.getParcelable("user");

        acceptText = (TextView) view.findViewById(R.id.this_user);

        acceptText.setText(String.format("Are you sure tou want to pass ownership to %s? You'll be degraded to regular worker.",
                clickedUser.getName() + " " + clickedUser.getSurname()));
        /* Setting listeners */
        builder.setView(view).setTitle("Confirm choice")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setPositiveButton("add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((ChangeOwnershipActivity)getActivity()).changeOwner(company, clickedUser);
                    }
                });

        return builder.create();
    }
}
