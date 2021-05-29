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
    private String salt;
    private ArrayList<String> projects;
    private ArrayList<String> companies;
    private ArrayList<String> managedCompanies;
    private ArrayList<String> skills;

     /* constructors */

    public User() {}

    public User(String id, String name, String surname, String mail, String password, String salt) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.mail = mail;
        this.password = password;
        this.salt = salt;
        projects = new ArrayList<>();
        projects.add("root");
        companies = new ArrayList<>();
        companies.add("root");
        managedCompanies = new ArrayList<>();
        managedCompanies.add("root");
        skills = new ArrayList<>();
    }

    /* copy constructor */
    public User(User user) {
        this.id = user.id;
        this.name = user.name;
        this.surname = user.surname;
        this.mail = user.mail;
        this.password = user.password;
        this.salt = user.salt;
        this.projects = user.projects;
        this.companies = user.companies;
        this.managedCompanies = user.managedCompanies;
        this.skills = user.skills;
    }

    protected User(Parcel in) {
        id = in.readString();
        name = in.readString();
        surname = in.readString();
        mail = in.readString();
        password = in.readString();
        salt = in.readString();
        projects = in.createStringArrayList();
        companies = in.createStringArrayList();
        managedCompanies = in.createStringArrayList();
        skills = in.createStringArrayList();
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
        dest.writeString(this.salt);
        dest.writeStringList(this.projects);
        dest.writeStringList(this.companies);
        dest.writeStringList(this.managedCompanies);
        dest.writeStringList(this.skills);
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

    public String getSalt() {
        return salt;
    }

    public String getPassword() {
        return password;
    }

    public ArrayList<String> getProjects() { return projects; }

    public ArrayList<String> getCompanies() { return companies; }

    public ArrayList<String> getManagedCompanies() { return managedCompanies; }

    public ArrayList<String> getSkills() { return skills; }

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

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCompanies(ArrayList<String> companies) { this.companies = companies; }

    public void setManagedCompanies(ArrayList<String> managedCompanies) { this.managedCompanies = managedCompanies; }

    public void setSkills(ArrayList<String> skills) { this.skills = skills; }
}
