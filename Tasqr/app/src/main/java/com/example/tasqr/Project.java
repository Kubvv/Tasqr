package com.example.tasqr;

import android.app.Activity;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class Project {

    private String id;
    private String name;
    private String company;
    private String description;
    private String customer;
    private User owner;
    private ArrayList<User> leaders;
    private ArrayList<User> workers;
    private ArrayList<Task> tasks;

    /*constructors */

    public Project() {}

    public Project(String id, String name, String company, String description, User owner, ArrayList<User> workers) {
        this.id = id;
        this.name = name;
        this.company = company;
        this.description = description;
        this.tasks = new ArrayList<>();
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

    public User getOwner() {
        return owner;
    }

    public ArrayList<User> getLeaders() {
        return leaders;
    }

    public ArrayList<User> getWorkers() {
        return workers;
    }

    public String getId() {
        return id;
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

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public void setLeaders(ArrayList<User> leaders) {
        this.leaders = leaders;
    }

    public void setWorkers(ArrayList<User> workers) {
        this.workers = workers;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTasks(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }

    public void addTask(Activity context, DatabaseReference projectRef, Task task)
    {
        if(this.tasks == null)
            this.tasks = new ArrayList<>();

        this.tasks.add(task);
        projectRef.child("tasks").setValue(this.tasks).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {  Utilities.toastMessage("Successfully added new task", context);
        }
    });
    }
}
