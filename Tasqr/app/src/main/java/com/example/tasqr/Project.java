package com.example.tasqr;

import java.util.ArrayList;

public class Project {

    private String name;
    private String company;
    private String description;
    private String customer;
    private User owner;
    private ArrayList<User> liders;
    private ArrayList<User> workers;

    public Project() {}

    public Project(String name, String company, String description, User owner, ArrayList<User> workers) {
        this.name = name;
        this.company = company;
        this.description = description;
        this.customer = "";
        this.owner = owner;
        liders = new ArrayList<>();
        liders.add(owner);
        this.workers = workers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public ArrayList<User> getLiders() {
        return liders;
    }

    public void setLiders(ArrayList<User> liders) {
        this.liders = liders;
    }

    public ArrayList<User> getWorkers() {
        return workers;
    }

    public void setWorkers(ArrayList<User> workers) {
        this.workers = workers;
    }
}
