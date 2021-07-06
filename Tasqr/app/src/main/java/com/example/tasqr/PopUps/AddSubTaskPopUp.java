/*
*   ADD SUBTASK POPUP
*   A dialog fragment which gets user input for creating new subtask
*   CONTAINS        Button Cancel and add buttons
*                   EditText form
* */

package com.example.tasqr.PopUps;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.tasqr.R;

public class AddSubTaskPopUp extends DialogFragment {

    private EditText subTaskName;
    private AddSubTaskListener listener;

    private Button dismiss;
    private Button ok;

    /* MAIN ON CREATE METHOD */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        /* Creating layout dependencies */
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.popup_addsubtask, null);

        /* Finding views */
        subTaskName = view.findViewById(R.id.subtask_nameadd);
        dismiss = view.findViewById(R.id.dismiss);
        ok = view.findViewById(R.id.ok);

        /* Setting up behaviour */
        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.sendSubTaskName(subTaskName.getText().toString());
                dismiss();
            }
        });

        builder.setView(view);

        return builder.create();
    }

    /* METHOD TO GET CONTEXT FOR LISTENER */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (AddSubTaskListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement AddSubTaskListener interface");
        }
    }

    /* LISTENTER INTERFACE TO PASS DATA TO ACTIVITY */
    public interface AddSubTaskListener
    {
        void sendSubTaskName(String subTaskName);
    }

}
