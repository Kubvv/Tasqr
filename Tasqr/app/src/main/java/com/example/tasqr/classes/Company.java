package com.example.tasqr.classes;

import java.util.ArrayList;

public class Company {

    private String name;
    private String description;
    private String owner;
    private ArrayList<String> workers;
    private ArrayList<String> managers;
    private ArrayList<String> projectsId;

    /* Constructors */
    public Company(String name, String description, String owner, ArrayList<String> workers) {
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.workers = workers;
        managers = new ArrayList<String>();
        projectsId = new ArrayList<String>();
        projectsId.add("root");
    }

    public Company() {}

    /* Getters */
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getOwner() {
        return owner;
    }

    public ArrayList<String> getWorkers() {
        return workers;
    }

    public ArrayList<String> getManagers() {
        return managers;
    }

    public ArrayList<String> getProjectsId() {
        return projectsId;
    }

    /* Setters */

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setWorkers(ArrayList<String> workers) {
        this.workers = workers;
    }

    public void setManagers(ArrayList<String> managers) {
        this.managers = managers;
    }

    public void setProjectsId(ArrayList<String> projectsId) {
        this.projectsId = projectsId;
    }
}
