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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.tasqr.classes.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;

public class WorkerListPopUp extends DialogFragment {

    private final FirebaseDatabase database = FirebaseDatabase.getInstance("https://tasqr-android-default-rtdb.europe-west1.firebasedatabase.app/");
    private final DatabaseReference itemReference;

    private ArrayList<String> usersMail = new ArrayList<>();

    public WorkerListPopUp(DatabaseReference itemReference){
        super();
        this.itemReference = itemReference;
    }

    /* Main on create method */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        /* Creating layout dependencies */
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.popup_worker_list, null);

        ListView workerList = view.findViewById(R.id.workerList);

        /* Getting item list from reference */
        itemReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                /* Getting items into hashset */
                HashSet<String> items = new HashSet<>();
                for (DataSnapshot item : snapshot.getChildren())
                    items.add(item.getValue(String.class));

                ArrayList<String> displayArray = new ArrayList<>(items.size());

                /* Matching set items to user reference */
                DatabaseReference userRef = database.getReference("Users");
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot user : snapshot.getChildren())
                            if (items.contains(user.getValue(User.class).getMail())) {
                                displayArray.add(user.getValue(User.class).getName() + " " + user.getValue(User.class).getSurname());
                                usersMail.add(user.getValue(User.class).getMail());
                            }

                        workerList.setAdapter(new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, displayArray));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        /* Setting listeners */
        builder.setView(view).setTitle("Team")
                .setNegativeButton("return", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        workerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent profileIntent = new Intent(getContext(), ProfileActivity.class);

                profileIntent.putExtra("clicked_mail", usersMail.get(position));
                profileIntent.putExtra("logged_mail", "");

                startActivity(profileIntent);
            }
        });

        return builder.create();
    }
}
