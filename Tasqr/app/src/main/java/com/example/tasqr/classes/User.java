package com.example.tasqr.classes;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;

public class User implements Parcelable {

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

    protected User(Parcel in) {
        id = in.readString();
        name = in.readString();
        surname = in.readString();
        mail = in.readString();
        password = in.readString();
        projects = in.createStringArrayList();
        companies = in.createStringArrayList();
    }

    /* Parcelable part of code */

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.surname);
        dest.writeString(this.mail);
        dest.writeString(this.password);
        dest.writeStringList(this.projects);
        dest.writeStringList(this.companies);
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
