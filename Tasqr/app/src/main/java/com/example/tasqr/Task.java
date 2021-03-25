package com.example.tasqr;

import android.app.Activity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Date;

public class Task {
    private String taskName;
    private User leader;
    private ArrayList<User> workers;
    private Date deadline;
    private ArrayList<SubTask> subTasks;


    public Task(){
    }

    public Task(String taskName, User leader, ArrayList<User> workers, Date deadline) {
        this.taskName = taskName;
        this.leader = leader;
        this.workers = workers;
        this.deadline = deadline;
        this.subTasks = new ArrayList<>();
    }


    /* Getters */
    public User getLeader() {
        return leader;
    }

    public ArrayList<User> getWorkers() {
        return workers;
    }

    public ArrayList<SubTask> getSubTasks() {
        return subTasks;
    }

    public Date getDeadline() {
        return deadline;
    }

    public String getTaskName() {
        return taskName;
    }

    /* Setters */
    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public void setWorkers(ArrayList<User> workers) {
        this.workers = workers;
    }

    public void setSubTasks(ArrayList<SubTask> subTasks) {
        this.subTasks = subTasks;
    }

    public void setLeader(User leader) {
        this.leader = leader;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }


    public void addSubTask(Activity context, DatabaseReference projectRef, Integer position, SubTask subTask)
    {
        if(this.subTasks == null)
            this.subTasks = new ArrayList<>();

        this.subTasks.add(subTask);
        projectRef.child("tasks").child(position.toString()).setValue(this.subTasks).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {  Utilities.toastMessage("Successfully added new subtask", context);
            }
        });
    }
}
