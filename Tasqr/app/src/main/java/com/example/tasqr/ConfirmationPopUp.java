package com.example.tasqr;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.tasqr.classes.Company;
import com.example.tasqr.classes.Project;
import com.example.tasqr.classes.User;

public class ConfirmationPopUp extends DialogFragment {

    private static final String TAG = "ConfirmationPopUp";
    private final String name;
    private final int position;
    private TextView text;
    private Bundle bundle;
    private ConfirmationListener listener;

    private Button dismiss;
    private Button ok;

    public ConfirmationPopUp(String name, int position){
        this.name = name;
        this.position = position;
    }

    public ConfirmationPopUp(String name) {
        this.name = name;
        this.position = -1;
    }

    /* Main on create method */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        /* Creating layout dependencies */
        ContextThemeWrapper context = new ContextThemeWrapper(getActivity(), R.style.AppDialogTheme);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.popup_confirmation, null);
        bundle = getArguments();

        text = view.findViewById(R.id.conftext);
        dismiss = view.findViewById(R.id.dismiss);
        ok = view.findViewById(R.id.ok);

        text.setText(bundle.getString("text"));
        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.confirmation(position);
                dismiss();
            }
        });

        builder.setView(view);

        return builder.create();
    }

    /* Method to get context for listener */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (ConfirmationListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement ConfirmationListener interface");
        }
    }

    public interface ConfirmationListener
    {
        void confirmation(int position);
    }
}
