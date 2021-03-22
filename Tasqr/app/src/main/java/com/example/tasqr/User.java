package com.example.tasqr;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;

public class User {

    private String name;
    private String surname;
    private String mail;
    private String password;
    private ArrayList<String> projects; /* TO DO change way of identifying projects */


     /*constructors */

    public User() {}

    public User(String name, String surname, String mail, String password) {
        this.name = name;
        this.surname = surname;
        this.mail = mail;
        this.password = password;
        projects = new ArrayList<>();
        projects.add("root");
    }

    /* getters */

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

    public String getNameSurname() { return name + " " + surname; }

    public ArrayList<String> getProjects() { return projects; }

    /* setters */

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
}
