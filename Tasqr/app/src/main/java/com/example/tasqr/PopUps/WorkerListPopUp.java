/*
 *   WORKER LIST POP UP
 *   Popup listing all workers in a given project/task
 *   CONTAINS    ListView list of all workers
 * */

package com.example.tasqr.PopUps;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.tasqr.ProfileActivity;
import com.example.tasqr.R;
import com.example.tasqr.classes.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class WorkerListPopUp extends DialogFragment {

    private final FirebaseDatabase database = FirebaseDatabase.getInstance("https://tasqr-android-default-rtdb.europe-west1.firebasedatabase.app/");
    private final DatabaseReference itemReference;

    private final ArrayList<String> usersMail = new ArrayList<>();

    private ListView workerList;

    public WorkerListPopUp(DatabaseReference itemReference){
        super();
        this.itemReference = itemReference;
    }

    /* MAIN ON CREATE METHOD */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        /* Creating layout dependencies */
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.popup_worker_list, null);

        ListView workerList = view.findViewById(R.id.workerList);
        Button dismiss = view.findViewById(R.id.dismiss);

        fetchData();

        workerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent profileIntent = new Intent(getContext(), ProfileActivity.class);

                profileIntent.putExtra("clicked_mail", usersMail.get(position));
                profileIntent.putExtra("logged_mail", "");

                startActivity(profileIntent);
            }
        });

        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        builder.setView(view);

        return builder.create();
    }

    /* FETCH ITEMS FROM DATABASE */
    private void fetchData(){
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

                        workerList.setAdapter(new ArrayAdapter(getActivity(), R.layout.user_list_popup_item, displayArray));
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
    }
}
