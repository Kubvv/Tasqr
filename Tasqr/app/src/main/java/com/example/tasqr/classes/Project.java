package com.example.tasqr.classes;

import android.app.Activity;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.util.Log;

import com.example.tasqr.Utilities;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;

import static android.content.ContentValues.TAG;

public class Project implements Parcelable {

    private String name;
    private String company;
    private String description;
    private String customer;
    private String owner;
    private ArrayList<String> leaders;
    private ArrayList<String> workers;
    private ArrayList<String> tasks;

    /*constructors */

    public Project() {}

    public Project(String name, String company, String description, String owner) {
        this.name = name;
        this.company = company;
        this.description = description;
        this.customer = "";
        this.owner = owner;
        leaders = new ArrayList<>();
        leaders.add(owner);
        workers = new ArrayList<>();
    }

    /* getters */

    protected Project(Parcel in) {
        name = in.readString();
        company = in.readString();
        description = in.readString();
        customer = in.readString();
        owner = in.readString();
        leaders = in.createStringArrayList();
        workers = in.createStringArrayList();
        tasks = in.createStringArrayList();
    }

    public static final Creator<Project> CREATOR = new Creator<Project>() {
        @Override
        public Project createFromParcel(Parcel in) {
            return new Project(in);
        }

        @Override
        public Project[] newArray(int size) {
            return new Project[size];
        }
    };

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

    public ArrayList<String> getTasks() {
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

    public void setTasks(ArrayList<String> tasks) {
        this.tasks = tasks;
    }

    /* Add task to current project object and push it to database */
    public void addTask(Activity context, FirebaseDatabase database, DatabaseReference projectRef, Task task)
    {
        if(this.tasks == null)
            this.tasks = new ArrayList<>();

        DatabaseReference taskRef = database.getReference("Tasks").push();
        task.setId(taskRef.getKey());
        this.tasks.add(task.getId());

        database.getReference("Tasks").child(taskRef.getKey()).setValue(task);

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(company);
        dest.writeString(description);
        dest.writeString(customer);
        dest.writeString(owner);
        dest.writeStringList(leaders);
        dest.writeStringList(workers);
        dest.writeStringList(tasks);
    }
}
