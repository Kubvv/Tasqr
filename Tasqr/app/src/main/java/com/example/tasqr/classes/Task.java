package com.example.tasqr.classes;

import android.app.Activity;
import android.util.SparseBooleanArray;

import com.example.tasqr.SubTasksActivity;
import com.example.tasqr.Utilities;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Date;

public class Task implements Comparable<Task>{
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

    public void setProgress(int progress){
        this.progress = progress;
    }

    /* Adds new substask to the database within a given task (taskRef)*/
    public void addSubTask(Activity context, DatabaseReference projectRef, SubTask subTask, Project project, int position)
    {
        if(this.subTasks == null)
            this.subTasks = new ArrayList<>();

        this.subTasks.add(subTask);
        calcProgress();
        project.setTask(position, this);
        project.sortTasks();
        projectRef.setValue(project);
    }

    /* Sets states of subtasks of a given task (taskRef) and saves in the database*/
    public void setSubTasksState(Activity context, DatabaseReference projectRef, SparseBooleanArray checked, Project project, int position)
    {
        for (int i = 0; i < this.subTasks.size(); i++) {
            if (checked.get(i)) {
               this.subTasks.get(i).setState(SubTask.SubTaskState.done);
            } else {
                this.subTasks.get(i).setState(SubTask.SubTaskState.pending);
            }
        }

        calcProgress();
        project.setTask(position, this);
        project.sortTasks();
        projectRef.setValue(project);
    }

    private void calcProgress(){
        int counter = 0;
        for (SubTask subtask : this.subTasks)
            if (subtask.getState() == SubTask.SubTaskState.done)
                counter++;

        progress = counter == 0 ? 0 : (int)(((double)counter / (double)this.subTasks.size()) * 100);
    }

    @Override
    public int compareTo(Task o) {
        return Integer.compare(o.progress, this.progress);
    }
}
