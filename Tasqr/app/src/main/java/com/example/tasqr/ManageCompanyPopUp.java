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

public class ManageCompanyPopUp extends DialogFragment {

    private static final String TAG = "ManageCompanyPopUp";

    private FirebaseDatabase database = FirebaseDatabase.getInstance("https://tasqr-android-default-rtdb.europe-west1.firebasedatabase.app/");
    private DatabaseReference rootRef = database.getReference();

    private Bundle bundle;

    private Button addUsersButton;
    private Button addManagersButton;
    private Button changeOwnerButton;
    private Button leaveCompanyButton;

    private User user;
    private Project project;
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
        user = bundle.getParcelable("user");

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

        leaveCompanyButton = view.findViewById(R.id.leaveCompanyButton);
        leaveCompanyButton.setVisibility(View.VISIBLE);


        leaveCompanyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* fetch user and project to have up to date data */
                readUser(new ManageCompanyPopUp.FirebaseUserCallback() {
                    @Override
                    public void onUserCallback(User usr) {
                        user = usr;

                        readCompany(new ManageCompanyPopUp.FirebaseCompanyCallback() {
                            @Override
                            public void onCompanyCallback(Company cmp) {
                                company = cmp;

                                /* actual leave company function */
                                if (leaveCompany())
                                    refreshManageCompanies();
                            }
                        }, company.getId());

                    }
                }, logged_mail);

            }
        });


        if (isOwner) {
            addUsersButton.setVisibility(View.VISIBLE);
            addManagersButton.setVisibility(View.VISIBLE);
            changeOwnerButton.setVisibility(View.VISIBLE);
        }
        else if (isManager) {
            addUsersButton.setVisibility(View.VISIBLE);
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

    /* -------------------------------callbacks and read functions------------------------------- */

    public interface FirebaseUserCallback {
        void onUserCallback(User user);
    }

    public interface FirebaseCompanyCallback {
        void onCompanyCallback(Company company);
    }

    public interface FirebaseTaskCallback {
        void onTaskCallback(Task task);
    }

    public interface FirebaseProjectCallback {
        void onProjectCallback(Project project);
    }

    public void readUser(ManageCompanyPopUp.FirebaseUserCallback firebaseCallback, String user_mail) {
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

    public void readCompany(ManageCompanyPopUp.FirebaseCompanyCallback firebaseCallback, String company_id) {
        rootRef.child("Companies").child(company_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Company cmp = dataSnapshot.getValue(Company.class);
                firebaseCallback.onCompanyCallback(cmp);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void readTask(ManageCompanyPopUp.FirebaseTaskCallback firebaseCallback, String task_id) {
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

    public void readProject(ManageCompanyPopUp.FirebaseProjectCallback firebaseCallback, String project_id) {
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

    /* ------------------------------------------------------------------------------------------ */

    private boolean leaveCompany() {
        if (isOwner && company.getWorkers() != null && !company.getWorkers().isEmpty()) {
            Utilities.toastMessage("Please pass ownership of company before leaving", getActivity());
            return false;
        }

        deleteCompanyFromUser();

        if (company.getWorkers() == null || company.getWorkers().isEmpty()) {
            deleteCompany();
        } else {
            deleteUserFromManagersAndWorkers();
            deleteUserFromCompanyProjects();
        }

        return true;
    }

    private void deleteCompanyFromUser() {
        if (isOwner || isManager) {
            user.getManagedCompanies().remove(company.getName());
        }

        user.getCompanies().remove(company.getId());
        rootRef.child("Users").child(user.getId()).setValue(user);
    }

    public void deleteCompany() {
        for (int i = 0; i < company.getProjectsId().size(); i++) {
            rootRef.child("Projects").child(company.getProjectsId().get(i)).removeValue();
        }
        rootRef.child("Companies").child(company.getId()).removeValue();
    }

    public void deleteUserFromManagersAndWorkers() {
        company.getManagers().remove(user.getMail());
        company.getWorkers().remove(user.getMail());
        rootRef.child("Companies").child(company.getId()).setValue(company);
    }

    public void deleteUserFromCompanyProjects() {
        ArrayList<Project> companyProjects = new ArrayList<>();
        AtomicInteger atomic = new AtomicInteger(0);

        if (company.getProjectsId() == null || company.getProjectsId().size() <= 1)
            return;

        for (int i = 1; i < company.getProjectsId().size(); i++) {
            String projectId = company.getProjectsId().get(i);
            readProject(new ManageCompanyPopUp.FirebaseProjectCallback() {
                @Override
                public void onProjectCallback(Project prj) {
                    companyProjects.add(prj);
                    if (atomic.incrementAndGet() == company.getProjectsId().size() - 1)
                        deleteUserFromCompanyProjectsContinue(companyProjects);
                }
            }, projectId);
        }
    }

    private void deleteUserFromCompanyProjectsContinue(ArrayList<Project> companyProjects) {
        for (Project prj : companyProjects) {
            Log.d(TAG, "deleteUserFromCompanyProjectsContinue: " + prj);
        }
        Log.d(TAG, "deleteUserFromCompanyProjectsContinue: end");
        for (Project prj : companyProjects) {
            project = prj;
            leaveProject();
        }

        rootRef.child("Companies").child(company.getId()).setValue(company);
    }

    /* ------------------------------------------------------------------------------------------ */

    private boolean leaveProject() {
        if (project.getLeaders().size() == 1 && project.getLeaders().get(0).equals(logged_mail) && project.getWorkers().size() != 1) {
            /* if user is the only leader, add first worker to leaders */
            String newLeader = new String();
            if (project.getWorkers().get(0).equals(user.getMail()))
                newLeader = project.getWorkers().get(1);
            else
                newLeader = project.getWorkers().get(0);

            project.getLeaders().add(newLeader);
        }

        deleteProjectFromUser();

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

    private void deleteUserFromProjectTasks() {
        ArrayList<Task> projectTasks = new ArrayList<>();
        AtomicInteger atomic = new AtomicInteger(0);

        if (project.getTasks() == null)
            return;

        for (String taskId : project.getTasks()) {
            readTask(new ManageCompanyPopUp.FirebaseTaskCallback() {
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
