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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.tasqr.classes.Project;
import com.example.tasqr.classes.Task;
import com.example.tasqr.classes.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ManageProjectPopUp extends DialogFragment {

    private static final String TAG = "ManageProjectPopUp";

    private Bundle bundle;

    private String logged_mail;
    private String logged_name;
    private String logged_surname;

    private FirebaseDatabase database = FirebaseDatabase.getInstance("https://tasqr-android-default-rtdb.europe-west1.firebasedatabase.app/");
    private DatabaseReference rootRef = database.getReference();

    private User user;
    private Project project;

    private Button dismiss;
    private TextView title;

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

        dismiss = view.findViewById(R.id.dismiss);
        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        title = view.findViewById(R.id.title);
        title.setText(project.getName());

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
        
        Button leaveButton = view.findViewById(R.id.changeOwnerButton);
        leaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* fetch user and project to have up to date data */
                readUser(new FirebaseUserCallback() {
                    @Override
                    public void onUserCallback(User usr) {
                        user = usr;

                        readProject(new FirebaseProjectCallback() {
                            @Override
                            public void onProjectCallback(Project prj) {
                                project = prj;

                                /* actual leave project function */
                                if (leaveProject())
                                    startMainActivity();
                            }
                        }, project.getId());

                    }
                }, logged_mail);

            }
        });

        builder.setView(view);

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

    private boolean leaveProject() {
        if (project.getLeaders().size() == 1 && project.getLeaders().get(0).equals(logged_mail) && project.getWorkers().size() != 1) {
            Utilities.toastMessage("Choose a new leader before leaving", getActivity());
            return false;
        }

        deleteProjectFromUser();

        Log.e(TAG, "leaveProject: " + project.getWorkers().size());
        if (project.getWorkers() == null || project.getWorkers().size() == 0) {
            deleteProject();
        } else {
            deleteUserFromLeadersAndWorkers();
            deleteUserFromProjectTasks();
        }

        return true;
    }

    private void deleteProjectFromUser() {
        user.getProjects().remove(project.getId());
        rootRef.child("Users").child(user.getId()).setValue(user);
    }

    private void deleteUserFromLeadersAndWorkers() {
        project.getLeaders().remove(user.getMail());
        project.getWorkers().remove(user.getMail());
        rootRef.child("Projects").child(project.getId()).setValue(project);
    }

    private void deleteProject() {
        for (int i = 0; i < project.getTasks().size(); i++) {
            rootRef.child("Tasks").child(project.getTasks().get(i)).removeValue();
        }
        rootRef.child("Projects").child(project.getId()).removeValue();
    }

    public interface FirebaseTaskCallback {
        void onTaskCallback(Task task);
    }

    public interface FirebaseUserCallback {
        void onUserCallback(User user);
    }

    public interface FirebaseProjectCallback {
        void onProjectCallback(Project project);
    }

    public void readTask(FirebaseTaskCallback firebaseCallback, String task_id) {
        rootRef.child("Tasks").child(task_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Task task = dataSnapshot.getValue(Task.class);
                firebaseCallback.onTaskCallback(task);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void readUser(FirebaseUserCallback firebaseCallback, String user_mail) {
        rootRef.child("Users").orderByChild("mail").equalTo(user_mail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    firebaseCallback.onUserCallback(user);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void readProject(FirebaseProjectCallback firebaseCallback, String project_id) {
        rootRef.child("Projects").child(project_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Project project = dataSnapshot.getValue(Project.class);
                firebaseCallback.onProjectCallback(project);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void deleteUserFromProjectTasks() {
        ArrayList<Task> projectTasks = new ArrayList<>();
        AtomicInteger atomic = new AtomicInteger(0);

        if (project.getTasks() == null)
            return;

        for (String taskId : project.getTasks()) {
            readTask(new FirebaseTaskCallback() {
                @Override
                public void onTaskCallback(Task task) {
                    projectTasks.add(task);
                    if (atomic.incrementAndGet() == project.getTasks().size())
                        deleteUserFromProjectTasksContinue(projectTasks);
                }
            }, taskId);
        }
    }

    private void deleteUserFromProjectTasksContinue(ArrayList<Task> projectTasks) {
        for (Task task : projectTasks) {
            if (task.getLeader().equals(user.getMail()) && task.getWorkers().size() > 1) {
                String newLeader = project.getWorkers().get(0);
                if (newLeader.equals(user.getMail()))
                    newLeader = project.getWorkers().get(1);

                task.setLeader(newLeader);
            }

            task.getWorkers().remove(user.getMail());

            if (task.getWorkers().isEmpty()) {
                project.getTasks().remove(task.getId());
                rootRef.child("Tasks").child(task.getId()).removeValue();
            }
            else {
                rootRef.child("Tasks").child(task.getId()).setValue(task);
            }
        }

        rootRef.child("Projects").child(project.getId()).setValue(project);
    }

}
