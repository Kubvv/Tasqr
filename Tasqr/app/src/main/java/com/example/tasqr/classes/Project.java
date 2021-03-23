package com.example.tasqr.classes;

import java.util.ArrayList;

public class Project {

    private String name;
    private String company;
    private String description;
    private String customer;
    private String owner;
    private ArrayList<String> leaders;
    private ArrayList<String> workers;

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
}
