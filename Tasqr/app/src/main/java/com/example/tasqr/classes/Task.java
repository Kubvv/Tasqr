package com.example.tasqr.classes;

import android.app.Activity;
import android.util.SparseBooleanArray;

import com.example.tasqr.SubTasksActivity;
import com.example.tasqr.Utilities;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Date;

public class Task {
    private String taskName;
    private String leader;
    private String parentProject;
    private ArrayList<String> workers;
    private Date deadline;
    private ArrayList<SubTask> subTasks;
    private int progress;

    public Task(){
    }

    public Task(String taskName, String leader, String parentProject, ArrayList<String> workers, Date deadline, int progress) {
        this.taskName = taskName;
        this.leader = leader;
        this.parentProject = parentProject;
        this.workers = workers;
        this.deadline = deadline;
        this.subTasks = new ArrayList<>();
        this.progress = progress;
    }


    /* Getters */
    public String getLeader() {
        return leader;
    }

    public ArrayList<String> getWorkers() {
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

    public String getParentProject(){
        return parentProject;
    }

    public int getProgress(){
        return progress;
    }

    public ArrayList<String> getSubTasksString()
    {
        ArrayList<String> subTaskStrings = new ArrayList<>();
        if(this.subTasks != null)
            for(SubTask subtask: this.subTasks)
                subTaskStrings.add(subtask.getDesc());

        return subTaskStrings;
    }

    /* Setters */
    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public void setWorkers(ArrayList<String> workers) {
        this.workers = workers;
    }

    public void setSubTasks(ArrayList<SubTask> subTasks) {
        this.subTasks = subTasks;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public void setParentProject(String parentProject){
        this.parentProject = parentProject;
    }

    /* Adds new substask to the database within a given task (taskRef)*/
    public void addSubTask(Activity context, DatabaseReference taskRef, SubTask subTask)
    {
        if(this.subTasks == null)
            this.subTasks = new ArrayList<>();

        this.subTasks.add(subTask);
        taskRef.child("subTasks").setValue(this.subTasks).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {  Utilities.toastMessage("Successfully added new sub task", context);
            }
        });
    }

    /* Sets states of subtasks of a given task (taskRef) and saves in the database*/
    public void setSubTasksState(Activity context, DatabaseReference taskRef, SparseBooleanArray checked)
    {
        for (int i = 0; i < this.subTasks.size(); i++) {
            if (checked.get(i)) {
               this.subTasks.get(i).setState(SubTask.SubTaskState.done);
            } else {
                this.subTasks.get(i).setState(SubTask.SubTaskState.pending);
            }
        }

        taskRef.child("subTasks").setValue(this.subTasks).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {  Utilities.toastMessage("Successfully set states", context);
            }
        });

        setProgress(taskRef);
    }

    private void setProgress(DatabaseReference taskRef){
        int counter = 0;
        for (SubTask subtask : this.subTasks)
            if (subtask.getState() == SubTask.SubTaskState.done)
                counter++;

        progress = counter == 0 ? 0 : (this.subTasks.size() / counter) * 100;

        taskRef.child("progress").setValue(progress);
    }
}
