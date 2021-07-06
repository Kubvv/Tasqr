package com.example.tasqr.classes;

public class SubTask {

    public enum SubTaskState{
        pending(0), reviewing(1), done(2), abandoned(3);

        private final int value;
        SubTaskState(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
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
