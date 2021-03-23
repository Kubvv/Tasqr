package com.example.tasqr;

public class SubTask {

    private enum SubTaskState{
        abandoned, pending, done
    }

    private String desc;
    private SubTaskState state;

    public SubTask(){
    }

    public SubTask(String desc, SubTaskState state) {
        this.desc = desc;
        this.state = state;
    }

    /* Getters */
    public String getDesc() {
        return desc;
    }

    public SubTaskState getState() {
        return state;
    }

    /* Setters */
    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setState(SubTaskState state) {
        this.state = state;
    }

}
