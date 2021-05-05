package com.example.tasqr.classes;

import android.app.Activity;
import android.provider.ContactsContract;
import android.util.Log;

import com.example.tasqr.Utilities;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Collections;

import static android.content.ContentValues.TAG;

public class Project {

    private String name;
    private String company;
    private String description;
    private String customer;
    private String owner;
    private ArrayList<String> leaders;
    private ArrayList<String> workers;
    private ArrayList<Task> tasks;

    /*constructors */

    public Project() {}

    public Project(String name, String company, String description, String owner, ArrayList<String> workers) {
        this.name = name;
        this.company = company;
        this.description = description;
        this.customer = "";
        this.owner = owner;
        leaders = new ArrayList<>();
        leaders.add(owner);
        this.workers = workers;
    }

    /* getters */

    public String getName() {
        return name;
    }

    public String getCompany() {
        return company;
    }

    public String getDescription() {
        return description;
    }

    public String getCustomer() {
        return customer;
    }

    public String getOwner() {
        return owner;
    }

    public ArrayList<String> getLeaders() {
        return leaders;
    }

    public ArrayList<String> getWorkers() {
        return workers;
    }

    public ArrayList<Task> getTasks() {
        return tasks;
    }

    /* setters */

    public void setName(String name) {
        this.name = name;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setLeaders(ArrayList<String> leaders) {
        this.leaders = leaders;
    }

    public void setWorkers(ArrayList<String> workers) {
        this.workers = workers;
    }

    public void setTasks(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }

    /* Add task to current project object and push it to database */
    public void addTask(Activity context, DatabaseReference projectRef, Task task)
    {
        if(this.tasks == null)
            this.tasks = new ArrayList<>();

        this.tasks.add(task);
        projectRef.child("tasks").setValue(this.tasks).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Utilities.toastMessage("Successfully added new task", context);
            }
        });
    }

    /* Add leaders to current project object and push it to database */
    public void addLeaders(Activity context, DatabaseReference projectRef, ArrayList<String> toAdd) {
        this.leaders.addAll(toAdd);
        projectRef.child("leaders").setValue(this.leaders).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Utilities.toastMessage("Successfully added leaders", context);
            }
        });
    }

    public void sortTasks() {
        Collections.sort(this.tasks);
    }

    public void setTask(int position, Task task){
        this.tasks.set(position, task);
    }
}
