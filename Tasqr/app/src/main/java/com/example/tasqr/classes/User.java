package com.example.tasqr.classes;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;

public class User {

    private String id;
    private String name;
    private String surname;
    private String mail;
    private String password;
    private ArrayList<String> projects;
    private ArrayList<String> companies;


     /*constructors */

    public User() {}

    public User(String id, String name, String surname, String mail, String password) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.mail = mail;
        this.password = password;
        projects = new ArrayList<>();
        projects.add("root");
        companies = new ArrayList<>();
        companies.add("root");
    }

    /* getters */

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getMail() {
        return mail;
    }

    public String getPassword() {
        return password;
    }

    public ArrayList<String> getProjects() { return projects; }

    public ArrayList<String> getCompanies() { return companies; }

    /* setters */

    public void setId(String id) {
        this.id = id;
    }

    public void setProjects(ArrayList<String> projects) {
        this.projects = projects;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCompanies(ArrayList<String> companies) { this.companies = companies; }
}
