package com.example.tasqr;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemLongClickListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.TaskStackBuilder;

import com.example.tasqr.classes.Company;
import com.example.tasqr.classes.Deadline;
import com.example.tasqr.classes.Project;
import com.example.tasqr.classes.Task;
import com.example.tasqr.classes.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

/* AddUsers Activity is an activity that creates list of users based on context, which is usually
* taken from previous activities. This list contains all users that can be checked. After the button
*  click, checked users are then added to appropriate objects in database, thus adding them to
* projects, companies etc..  */

public class AddUsersActivity extends AppCompatActivity {

    private class UserListItem{
        private CheckedTextView checkBox;
        private String name;
        private String skill1;
        private String skill2;
        private String skill3;

        public UserListItem(UserListItem item){

            this.checkBox = item.checkBox;
            this.name = item.name;
            this.skill1 = item.skill1;
            this.skill2 = item.skill2;
            this.skill3 = item.skill3;
        }

        public UserListItem(String name, String skill1, String skill2, String skill3){
            this.name = name;
            this.skill1 = skill1;
            this.skill2 = skill2;
            this.skill3 = skill3;
        }

        public String getName() {
            return name;
        }

        public String getSkill1() {
            return skill1;
        }

        public String getSkill2() {
            return skill2;
        }

        public String getSkill3() {
            return skill3;
        }

        public CheckedTextView getCheckBox(){
            return checkBox;
        }

        public void setCheckBox(CheckedTextView view){
            this.checkBox = view;
        }

        public void setCheckBoxChecked(boolean toCheck){
            if(checkBox != null) {
                Log.e(TAG, "setCheckBoxChecked: changing to " + toCheck);
                checkBox.setChecked(toCheck);
            }
        }
    }

    private class UserListAdapter extends ArrayAdapter implements Filterable {

        private final Activity context;
        private SparseBooleanArray checkedItems;
        private ArrayList<UserListItem> displayArray;
        private ArrayList<UserListItem> filterArray;
        private HashMap<Integer, Integer> filterToDisplay;

        public UserListAdapter(Activity context, ArrayList<UserListItem> displayArray) {
            super(context, R.layout.project_list_item, displayArray);
            this.context = context;
            this.displayArray = displayArray;
            this.filterArray = displayArray;
            checkedItems = new SparseBooleanArray(displayArray.size());
            filterToDisplay = new HashMap<>();
            for(int i = 0; i < displayArray.size(); i++)
                filterToDisplay.put(i, i);
        }

        @Override
        public int getCount() {
            return filterArray.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        /* Creates one row of ListView, consisting of task name */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View row = convertView;
            LayoutInflater inflater = context.getLayoutInflater();

            row = inflater.inflate(R.layout.user_list_item, null, true);

            filterArray.get(position).setCheckBox(row.findViewById(R.id.checkedTextView));
            displayArray.get(filterToDisplay.get(position)).setCheckBox(row.findViewById(R.id.checkedTextView));

            TextView skill1 = row.findViewById(R.id.skill1);
            TextView skill2 = row.findViewById(R.id.skill2);
            TextView skill3 = row.findViewById(R.id.skill3);

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    filterArray.get(position).getCheckBox().toggle();
                    checkedItems.put(filterToDisplay.get(position), filterArray.get(position).getCheckBox().isChecked());
                    Log.e(TAG, "onClick: " + filterArray.get(position).getCheckBox().isChecked() + " " + checkedItems.get(filterToDisplay.get(position)));
                }
            });

            filterArray.get(position).getCheckBox().setText(filterArray.get(position).getName());

            skill1.setText(filterArray.get(position).getSkill1());
            skill2.setText(filterArray.get(position).getSkill2());
            skill3.setText(filterArray.get(position).getSkill3());

            if (checkedItems.get(filterToDisplay.get(position))) {
                filterArray.get(position).setCheckBoxChecked(true);
                displayArray.get(filterToDisplay.get(position)).setCheckBoxChecked(true);
            }

            return row;
        }

        public SparseBooleanArray getChecked()
        {
            return checkedItems;
        }

        public void setChecked(int position, boolean toCheck){
            Log.e(TAG, "setChecked: " + position);
            checkedItems.put(position, toCheck);
            filterArray.get(position).setCheckBoxChecked(toCheck);
        }

        public int getSize(){
            return displayArray.size();
        }

        @NonNull
        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {

                @SuppressWarnings("unchecked")
                @Override
                protected void publishResults(CharSequence constraint,FilterResults results) {
                    filterArray = (ArrayList<UserListItem>) results.values; // has the filtered values
                    notifyDataSetChanged();  // notifies the data with new filtered values
                }

                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                    ArrayList<UserListItem> FilteredArrList = new ArrayList<UserListItem>();

                    if (displayArray == null) {
                        displayArray = new ArrayList<UserListItem>(filterArray); // saves the original data in mOriginalValues
                    }

                    /********
                     *
                     *  If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
                     *  else does the Filtering and returns FilteredArrList(Filtered)
                     *
                     ********/
                    if (constraint == null || constraint.length() == 0) {
                        // set the Original result to return
                        results.count = displayArray.size();
                        results.values = displayArray;
                        for(int i = 0; i < displayArray.size(); i++)
                            filterToDisplay.put(i, i);

                    } else {
                        constraint = constraint.toString().toLowerCase();
                        int counter = 0;
                        for (int i = 0; i < displayArray.size(); i++) {
                            String data = displayArray.get(i).getName();
                            if (data.toLowerCase().startsWith(constraint.toString())) {
                                FilteredArrList.add(new UserListItem(displayArray.get(i)));
                                filterToDisplay.put(counter, i);
                                counter++;
                            }
                        }
                        // set the Filtered result to return
                        results.count = FilteredArrList.size();
                        results.values = FilteredArrList;
                    }
                    return results;
                }
            };
            return filter;
        }

    }

    /************END OF INNER CLASSES*****************/

    private static final String TAG = "AddUsersActivity";

    /* basic logged user info */
    private String previous_activity;
    private String logged_name;
    private String logged_surname;
    private String logged_mail;
    private String company;
    private String project;
    private String task;

    /* Owner of currently created project [not in userArray!]*/
    private User owner;
    /* Mail of the task leader */
    private String leader;

    /* Project we are currently in */
    private String projid;
    private Project currProject;
    private User companyOwner;
    private ArrayList<String> projectUsers;

    /* Company info */
    private Company currCompany;
    private ArrayList<String> alreadyWorking = new ArrayList<>();

    /* Arraylists used for creating user listView */
    private ListView listView;
    private ArrayList<User> userArray = new ArrayList<>();
    private ArrayList<UserListItem> displayArray = new ArrayList<>();
    private ArrayList<String> enlisted = new ArrayList<>();
    private UserListAdapter adapter;

    private TextView addUsersTitle;
    private EditText searchUsers;

    /* bundle used in storing items */
    private Bundle bndl;
    private Resources res;

    /* Firebase database */
    private FirebaseDatabase database = FirebaseDatabase.getInstance("https://tasqr-android-default-rtdb.europe-west1.firebasedatabase.app/");
    private DatabaseReference rootRef = database.getReference();
    private DatabaseReference usersRef = rootRef.child("Users");
    private DatabaseReference projectsRef = rootRef.child("Projects");
    private DatabaseReference projectRef;
    private DatabaseReference companiesRef = rootRef.child("Companies");

    private FloatingActionButton nextButton;
    private FloatingActionButton checkAll;
    private FloatingActionButton uncheckAll;

    /* links appropriate view items, initializes listview, sets some attributes */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addusers);

        res = getResources();
        bndl = getIntent().getExtras();
        previous_activity = bndl.getString("previous_activity");
        logged_name = bndl.getString("logged_name");
        logged_surname = bndl.getString("logged_surname");
        logged_mail = bndl.getString("logged_mail");
        company = bndl.getString("company_name");
        project = bndl.getString("project_name");
        task = bndl.getString("taskName");
        currCompany = bndl.getParcelable("company");
        currProject = bndl.getParcelable("project");
        projid = bndl.getString("project_id");

        /* Fetch some data before moving on with creating listviews */
        preFetch();

        addUsersTitle = findViewById(R.id.changeOwnershipTitle);
        searchUsers = findViewById(R.id.editTextOwnership);
        searchUsers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    (AddUsersActivity.this).adapter.getFilter().filter(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        checkAll = findViewById(R.id.checkAllButton);
        checkAll.setOnClickListener(v -> {
            checkUsers(true);
        });

        uncheckAll = findViewById(R.id.uncheckAllButton);
        uncheckAll.setOnClickListener(v -> {
            checkUsers(false);
        });

        nextButton = findViewById(R.id.nextButton2);
        nextButton.setOnClickListener(v -> {

            /* Fetch all of the selected users */
            ArrayList<User> checkedUsers = new ArrayList<>();
            ArrayList<String> usersMail = new ArrayList<>();
            /* Also add the owner user object */
            if (previous_activity.equals("Company") || previous_activity.equals("Project")) {
                checkedUsers.add(owner);
            }
            /* Also add the leader's mail, as he is also a worker */
            else if (previous_activity.equals("Task")) {
                usersMail.add(leader);
            }
            SparseBooleanArray checked = adapter.getChecked();
            for (int i = 0; i < adapter.getSize(); i++) {
                if (checked.get(i)) {
                    checkedUsers.add(userArray.get(i));
                    usersMail.add(userArray.get(i).getMail());
                }
            }

            switch (previous_activity) {
                case "Company":
                    finishAddingCompany(checkedUsers, usersMail);
                    break;
                case "Project":
                    finishAddingProject(checkedUsers, usersMail);
                    break;
                case "Task":
                    finishAddingTask(usersMail);
                    break;
                case "addProjUsers":
                    finishAddingLeaders(usersMail);
                    break;
                case "manageCompanyUsers":
                    finishManageCompany(usersMail, "workers");
                    break;
                case "manageCompanyManagers":
                    finishManageCompany(usersMail, "managers");
                    break;
                case "manageProjectUsers":
                    correctChecked(usersMail, "workers");
                    break;
                case "manageProjectLeaders":
                    correctChecked(usersMail, "leaders");
                    break;
            }
        });

        listView = (ListView) findViewById(R.id.companylist);
        listView.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e(TAG, "onItemLongClick: " + position);
                return false;
            }
        });

        /* establish connection to database and some references */
        database = FirebaseDatabase.getInstance("https://tasqr-android-default-rtdb.europe-west1.firebasedatabase.app/");
        rootRef = database.getReference();
        usersRef = rootRef.child("Users");
        projectsRef = rootRef.child("Projects");

        Log.d(TAG, "onCreate: " + previous_activity);

        /* Fetch required data from database based on previous activity */
        switch (previous_activity) {
            case "Company":
                checkAll.setVisibility(View.INVISIBLE);
                uncheckAll.setVisibility(View.INVISIBLE);
                addUsersTitle.setText(String.format(res.getString(R.string.adduserscompany), company));
                fetchCompany();
                break;
            case "Project":
                addUsersTitle.setText(String.format(res.getString(R.string.addusersproject), project));
                fetchProject();
                break;
            case "Task":
                addUsersTitle.setText(String.format(res.getString(R.string.adduserstask), task));
                fetchTask();
                break;
            case "addProjUsers":
                /* This listview will consist of previously chosen users, so we have to take it from prev intent */
                ArrayList<User> checked = getIntent().getParcelableArrayListExtra("checked_users");
                fetchProjUsers(checked);
                break;
            case "manageCompanyUsers":
                checkAll.setVisibility(View.INVISIBLE);
                uncheckAll.setVisibility(View.INVISIBLE);
                addUsersTitle.setText(String.format("Add %s's employees", company));
                fetchManageCompany("workers");
                break;
            case "manageCompanyManagers":
                addUsersTitle.setText(String.format("Add %s's managers", company));
                fetchManageCompany("managers");
                break;
            case "manageProjectUsers":
                addUsersTitle.setText(String.format("Add %s's workers", project));
                break;
            case "manageProjectLeaders":
                addUsersTitle.setText(String.format("Add %s's leaders", project));
                fetchManageProject("leaders");
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (previous_activity.equals("addProjUsers")) { /* TO DO Zmienic miejsce dodanie projektu */
            openMainActivity();
        }
    }

    private void checkUsers(boolean isCheck) {
        for (int i = 0; i < adapter.getSize(); i++) {
            adapter.setChecked(i, isCheck);
        }
    }

    /* Used to fetch some information from database based on previous activity, before creating listview */
    private void preFetch() {
        if (previous_activity.equals("Project")
                || previous_activity.equals("manageProjectUsers")) { /* we need to parse company that created this project */
            Query q = companiesRef.orderByChild("name").equalTo(company);
            q.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        currCompany = ds.getValue(Company.class);
                        if (previous_activity.equals("manageProjectUsers") &&
                                (currCompany.getWorkers() == null || currCompany.getWorkers().size() == 0)) {
                            Utilities.toastMessage("No workers in company", AddUsersActivity.this);
                            openTasksActivity();
                        }
                        else if (previous_activity.equals("manageProjectUsers")) {
                            Query q = usersRef.orderByChild("mail").equalTo(currCompany.getOwner());
                            q.addListenerForSingleValueEvent(new ValueEventListener() {
                                 @Override
                                 public void onDataChange(@NonNull DataSnapshot snapshot) {
                                     for (DataSnapshot ds : snapshot.getChildren()) {
                                         companyOwner = ds.getValue(User.class);
                                         fetchManageProject("workers");
                                     }
                                 }

                                 @Override
                                 public void onCancelled(@NonNull DatabaseError error) {
                                     Utilities.toastMessage("error" + error.toString(), AddUsersActivity.this);
                                 }
                             });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Utilities.toastMessage("error" + error.toString(), AddUsersActivity.this);
                }
            });
        }
        /* we nedd to parse leader that created the task / project, and also all the users that take part in the project */
        else if (previous_activity.equals("Task") || previous_activity.equals("addProjUsers")) {
            projectRef = database.getReference("Projects/" + getIntent().getStringExtra("projectId"));
            projectRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    currProject = snapshot.getValue(Project.class);
                    projectUsers = currProject.getWorkers();
                    leader = logged_mail;
                    if (projectUsers == null || projectUsers.size() == 1) { /* If there are no workers, create task with only you enlisted */
                        ArrayList<String> usersMail = new ArrayList<>();
                        usersMail.add(leader);
                        if (previous_activity.equals("Task")) {
                            finishAddingTask(usersMail);
                        } else {
                            openMainActivity();
                        }
                    }
                    if (previous_activity.equals("addProjUsers")) {
                        /* add title to activity */
                        addUsersTitle.setText(String.format(res.getString(R.string.addusersleaders), currProject.getName()));
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Utilities.toastMessage("error " + error, AddUsersActivity.this);
                }
            });
        }
    }

    /* All of these functions are used to fetch data from different parts of database and create multiple choice list
    * based on them. Later on checked positions will be used to create new objects. */

    private void fetchCompany() {
        /* fetching all users in order to show them in a listview */
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                /* iterate through users and add each to array */
                for (DataSnapshot ds : snapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    /* If user is not the owner show him on the list of users that can be added */
                    if (!user.getMail().equals(logged_mail)) {
                        userArray.add(user);
                        addToDisplayArray(user);
                    } else {
                        owner = user;
                    }
                }

                setUserListAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utilities.toastMessage("error " + error, AddUsersActivity.this);
            }
        });
    }

    private void fetchProject() {
        /* fetching all company users in order to show them in a listview */
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                /* iterate through users and add each to array */
                for (DataSnapshot ds : snapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    /* If user is not the owner show him on the list of users that can be added */
                    if (!user.getMail().equals(logged_mail) && user.getCompanies().contains(currCompany.getId())) {
                        userArray.add(user);
                        addToDisplayArray(user);
                    } else if (user.getMail().equals(logged_mail)) {
                        owner = user;
                    }
                }

                if (currCompany.getWorkers() == null) { /* If there are no workers, create project with only you enlisted */
                    ArrayList<String> usersMail = new ArrayList<>();
                    ArrayList<User> projUsers = new ArrayList<>();
                    finishAddingProject(projUsers, usersMail);
                }

                setUserListAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utilities.toastMessage("error " + error, AddUsersActivity.this);
            }
        });
    }

    private void fetchTask() {
        /* fetching project users in order to show them in a listview */
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (projectUsers == null || i == projectUsers.size())
                        break;

                    User user = ds.getValue(User.class);
                    if (user.getMail().equals(projectUsers.get(i))) {
                        if (!user.getMail().equals(logged_mail)) {
                            userArray.add(user);
                            addToDisplayArray(user);
                        }
                        i++;
                    }
                }

                setUserListAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utilities.toastMessage("error " + error, AddUsersActivity.this);
            }
        });
    }


    private void fetchProjUsers(ArrayList<User> checked) {
        /* fetching project users in order to show them in a listview */
        for (int i = 1; i < checked.size(); i++) {
            userArray.add(checked.get(i));
            addToDisplayArray(checked.get(i));
        }

        setUserListAdapter();
    }

    private void fetchManageCompany(String context) {
        if (currCompany.getWorkers() != null) {
            alreadyWorking = currCompany.getWorkers();
        }
        ArrayList<String> checked;
        if (context.equals("managers")) {
            checked = currCompany.getManagers();
        }
        else {
            checked = alreadyWorking;
        }
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                /* iterate through users and add each to array */
                if (context.equals("workers")) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        User user = ds.getValue(User.class);
                        String mail = user.getMail();
                        if (!mail.equals(currCompany.getOwner())) {
                            userArray.add(user);
                            addToDisplayArray(user);
                        }
                    }
                }
                else {
                    int j = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        if (j == alreadyWorking.size()) { //all workers were found
                            break;
                        }
                        User user = ds.getValue(User.class);
                        String mail = user.getMail();
                        if (mail.equals(alreadyWorking.get(j))) {
                            userArray.add(user);
                            addToDisplayArray(user);
                            j++;
                        }
                    }
                }

                setUserListAdapter();
                if (checked == null) {
                    return;
                }

                int j = 0;
                for (int i = 0; i < adapter.getSize() && j < checked.size(); i++) {
                     if (userArray.get(i).getMail().equals(checked.get(j))) {
                         adapter.setChecked(i, true);
                         j++;
                     }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utilities.toastMessage("error " + error, AddUsersActivity.this);
            }
        });
    }

    private void fetchManageProject(String context) {
        alreadyWorking = currProject.getWorkers();
        ArrayList<String> checked;
        if (context.equals("leaders")) {
            checked = currProject.getLeaders();
        }
        else {
            checked = alreadyWorking;
        }
        checked.remove(logged_mail);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                /* iterate through users and add each to array */
                if (context.equals("workers")) {
                    boolean ownerAdded = false;
                    int i = 0;
                    ArrayList<String> workers = currCompany.getWorkers();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        if (workers == null || i == workers.size()) {
                            if (!ownerAdded && !companyOwner.getMail().equals(logged_mail)) {
                                userArray.add(companyOwner);
                                addToDisplayArray(companyOwner);
                            }
                            break;
                        }
                        User user = ds.getValue(User.class);
                        String mail = user.getMail();
                        if (mail.equals(workers.get(i)) && mail.equals(logged_mail)) {
                            i++;
                        }
                        else if (mail.equals(workers.get(i))) {
                            userArray.add(user);
                            addToDisplayArray(user);
                            i++;
                        }
                        else if (mail.equals(companyOwner.getMail()) && !mail.equals(logged_mail)) {
                            userArray.add(companyOwner);
                            addToDisplayArray(user);
                            ownerAdded = true;
                        }
                    }
                }
                else {
                    int j = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        if (j == alreadyWorking.size()) { //all workers were found
                            break;
                        }
                        User user = ds.getValue(User.class);
                        String mail = user.getMail();
                        if (mail.equals(alreadyWorking.get(j)) && mail.equals(logged_mail)) {
                            j++;
                        }
                        else if (mail.equals(alreadyWorking.get(j))) {
                            userArray.add(user);
                            addToDisplayArray(user);
                            j++;
                        }
                    }
                }

                setUserListAdapter();

                int j = 0;
                for (int i = 0; i < adapter.getSize() && j < checked.size(); i++) {
                    if (userArray.get(i).getMail().equals(checked.get(j))) {
                        adapter.setChecked(i, true);
                        j++;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utilities.toastMessage("error " + error, AddUsersActivity.this);
            }
        });
    }

    /* create adapter for list view */
    private void setUserListAdapter() {
        adapter = new UserListAdapter(AddUsersActivity.this, displayArray);
        listView.setAdapter(adapter);
    }

    /* Adds company and all it's workers to database.
     * Also updates all added user's companies arrays.
     * If successful, goes to ProfileActivity and closes all previous activities.*/
    private void finishAddingCompany (ArrayList<User> companyUsers, ArrayList<String> usersMail) {
        /* Create new company to be added */
        DatabaseReference pushedCompaniesRef = companiesRef.push();
        String id = pushedCompaniesRef.getKey();

        Company company = new Company(
                id,
                bndl.getString("company_name"),
                bndl.getString("description"),
                owner.getMail(),
                usersMail);


        companiesRef.child(id).setValue(company).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
//                Utilities.toastMessage("Successfully added new Company", AddUsersActivity.this);
            }
        });

        /* add company to owner's companies array and managedCompany array, then leave the activity */
        ArrayList<String> tmp = owner.getCompanies();
        tmp.add(id);
        usersRef.child(owner.getId()).child("companies").setValue(tmp);
        tmp = owner.getManagedCompanies();
        tmp.add(company.getName());
        usersRef.child(owner.getId()).child("managedCompanies").setValue(tmp).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                openManageCompanyActivity();
            }
        });

        /* meanwhile add companies to all other invited users arrays */
        for (int i = 1; i < companyUsers.size(); i++) {
            tmp = companyUsers.get(i).getCompanies();
            tmp.add(id);
            usersRef.child(companyUsers.get(i).getId()).child("companies").setValue(tmp);
        }
    }

    /* Adds project and all it's workers to database.
     * Also updates all added user's projects arrays.
     * If successful, goes to MainActivity and closes all previous activities.*/
    private void finishAddingProject (ArrayList<User> projectUsers, ArrayList<String> usersMail) {
        /* deletion of owner field in project fix */
        usersMail.add(logged_mail);

        DatabaseReference pushedProjectsRef = projectsRef.push();
        projid = pushedProjectsRef.getKey();

        /* Create new project to be added */
        Log.d(TAG, "finishAddingProject: PROJID: " + projid);
        Project project = new Project(
                projid,
                bndl.getString("project_name"),
                bndl.getString("company_name"),
                bndl.getString("description"),
                owner.getMail());

        enlisted = usersMail;
        projectsRef.child(projid).setValue(project).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
//                Utilities.toastMessage("Successfully added new project", AddUsersActivity.this);
            }
        });

        /* Add project id to company's projectsId field */
        rootRef.child("Companies").orderByChild("name").equalTo(project.getCompany()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    currCompany = ds.getValue(Company.class);
                }

                currCompany.getProjectsId().add(projid);
                
                rootRef.child("Companies").child(currCompany.getId()).setValue(currCompany);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> sorted = new ArrayList<>();
                int i = 0;
                boolean ownerAdded = false;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (enlisted == null || i == enlisted.size() || usersMail.size() == 1) {
                        if (!ownerAdded) {
                            sorted.add(logged_mail);
                        }
                        break;
                    }

                    User user = ds.getValue(User.class);
                    if (enlisted.get(i).equals(user.getMail())) {
                        sorted.add(user.getMail());
                        i++;
                    }
                    else if (logged_mail.equals(user.getMail())) {
                        sorted.add(user.getMail());
                        ownerAdded = true;
                    }
                }
                projectsRef.child(projid).child("workers").setValue(sorted);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        /* add project to owner's projects array, then leave the activity */
        ArrayList<String> tmp = owner.getProjects();
        tmp.add(projid);
        usersRef.child(owner.getId()).child("projects").setValue(tmp).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                /* No possible workers to choose as leaders, as no one besides owner is in the project */
                if (projectUsers.size() == 1) {
                    openMainActivity();
                } else {
                    openAddLeaderActivity(projid, projectUsers);
                }
            }
        });

        /* meanwhile add project to all other invited users arrays */
        for (int i = 1; i < projectUsers.size(); i++) {
            tmp = projectUsers.get(i).getProjects();
            tmp.add(projid);
            usersRef.child(projectUsers.get(i).getId()).child("projects").setValue(tmp);
        }
    }

    /* Creates a new task in current project */
    private void finishAddingTask(ArrayList<String> usersMail) {
        projectsRef = database.getReference("Projects/" + getIntent().getStringExtra("projectId"));
        /* Create new task to be added */
        Deadline deadline = new Deadline(bndl.getInt("year"), bndl.getInt("month") + 1, bndl.getInt("day"));
        Task newTask = new Task(getIntent().getStringExtra("taskName"), leader, getIntent().getStringExtra("projectId"), usersMail, deadline, 0);
        currProject.addTask(AddUsersActivity.this, database,  projectRef, newTask);

        openTasksActivity();
    }

    /* Adds leader to current project */
    private void finishAddingLeaders(ArrayList<String> usersMail) {
        projectsRef = database.getReference("Projects/" + getIntent().getStringExtra("projectId"));
        /* Add leaders to current project */
        currProject.addLeaders(AddUsersActivity.this, projectRef, usersMail);

        openMainActivity();
    }

    private void finishManageCompany(ArrayList<String> usersMail, String context) {

        /* segregate users that need to be deleted and added */
        ArrayList<String> toDelete = new ArrayList<>();
        ArrayList<String> toAdd;
        companiesRef.child(currCompany.getId()).child(context).setValue(usersMail);
        if (context.equals("managers")) {
            alreadyWorking = currCompany.getManagers();
        }
        if (alreadyWorking == null || alreadyWorking.size() == 0) { //there is no one to delete in empty company
            toAdd = usersMail;
        }
        else if (usersMail.size() == 0) { //there is no one to add to company that is being emptied
            toAdd = new ArrayList<>();
            toDelete = alreadyWorking;
        }
        else {
            HashSet<String> newChecked = new HashSet<>(usersMail);
            for (int i = 0; i < alreadyWorking.size(); i++) {
                if (!newChecked.contains(alreadyWorking.get(i))) {
                    toDelete.add(alreadyWorking.get(i));
                } else {
                    newChecked.remove(alreadyWorking.get(i));
                }
            }
            toAdd = new ArrayList<>(newChecked);
        }

        for (int i = 0; i < toDelete.size(); i++) {
            Query q = usersRef.orderByChild("mail").equalTo(toDelete.get(i));
            q.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user;
                    ArrayList<String> companies;
                    ArrayList<String> companyManagers = new ArrayList<>();
                    if (currCompany.getManagers() != null) {
                        companyManagers = currCompany.getManagers();
                    }
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        user = ds.getValue(User.class);
                        if (context.equals("workers")) {
                            companies = user.getCompanies();
                            companies.remove(currCompany.getId());
                            usersRef.child(user.getId()).child("companies").setValue(companies);
                        }
                        /* if someone is deleted in manageCompany tab, he is always removed from managing position */
                        companies = user.getManagedCompanies();
                        companies.remove(currCompany.getName());
                        companyManagers.remove(user.getMail());
                        usersRef.child(user.getId()).child("managedCompanies").setValue(companies);
                    }
                    if (context.equals("workers")) {
                        /* set new managers after deletion to database */
                        companiesRef.child(currCompany.getId()).child("managers").setValue(companyManagers);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Utilities.toastMessage("error " + error, AddUsersActivity.this);
                }
            });
        }

        for (int j = 0; j < toAdd.size(); j++) {
            Query q = usersRef.orderByChild("mail").equalTo(toAdd.get(j));
            q.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user;
                    ArrayList<String> companies;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        user = ds.getValue(User.class);
                        if (context.equals("workers")) {
                            companies = user.getCompanies();
                            companies.add(currCompany.getId());
                            usersRef.child(user.getId()).child("companies").setValue(companies);
                        }
                        else {
                            companies = user.getManagedCompanies();
                            companies.add(currCompany.getName());
                            usersRef.child(user.getId()).child("managedCompanies").setValue(companies);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Utilities.toastMessage("error " + error, AddUsersActivity.this);
                }
            });
        }

        openManageCompanyActivity();
    }

    /* this function places currently editing user in correct usersMail place */
    private void correctChecked(ArrayList<String> usersMail, String context) {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i = 0;
                boolean isAdded = false;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (i == usersMail.size()) {
                        if (!isAdded) {
                            usersMail.add(logged_mail);
                            isAdded = true;
                            finishManageProject(usersMail, context);
                        }
                        break;
                    }
                    String mail = ds.getValue(User.class).getMail();
                    if (mail.equals(usersMail.get(i))) {
                        i++;
                    }
                    else if (mail.equals(logged_mail)) {
                        usersMail.add(i, logged_mail);
                        isAdded = true;
                        finishManageProject(usersMail, context);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void finishManageProject(ArrayList<String> usersMail, String context) {
        /* segregate users that need to be deleted and added */
        ArrayList<String> toDelete = new ArrayList<>();
        ArrayList<String> toAdd;
        projectsRef.child(projid).child(context).setValue(usersMail);
        if (context.equals("managers")) {
            alreadyWorking = currProject.getLeaders();
        }
        if (usersMail.size() == 0) { //there is no one to add to array
            toAdd = new ArrayList<>();
            toDelete = alreadyWorking;
            toDelete.remove(logged_mail);
        }
        else {
            HashSet<String> newChecked = new HashSet<>(usersMail);
            for (int i = 0; i < alreadyWorking.size(); i++) {
                if (!newChecked.contains(alreadyWorking.get(i))) {
                    toDelete.add(alreadyWorking.get(i));
                } else {
                    newChecked.remove(alreadyWorking.get(i));
                }
            }
            toAdd = new ArrayList<>(newChecked);
        }
        if (context.equals("workers")) {
            ArrayList<String> projLeaders = currProject.getLeaders();
            for (int i = 0; i < toDelete.size(); i++) {
                projLeaders.remove(toDelete.get(i));

                Query q = usersRef.orderByChild("mail").equalTo(toDelete.get(i));
                q.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user;
                        ArrayList<String> projects;
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            user = ds.getValue(User.class);
                            projects = user.getProjects();
                            projects.remove(projid);
                            usersRef.child(user.getId()).child("projects").setValue(projects);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Utilities.toastMessage("error " + error, AddUsersActivity.this);
                    }
                });
            }

            projectsRef.child(projid).child("leaders").setValue(projLeaders);

            for (int j = 0; j < toAdd.size(); j++) {
                if (!toAdd.get(j).equals(logged_mail)) {
                    Query q = usersRef.orderByChild("mail").equalTo(toAdd.get(j));
                    q.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user;
                            ArrayList<String> projects;
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                user = ds.getValue(User.class);
                                projects = user.getProjects();
                                projects.add(projid);
                                usersRef.child(user.getId()).child("projects").setValue(projects);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Utilities.toastMessage("error " + error, AddUsersActivity.this);
                        }
                    });
                }
            }
        }

        openTasksActivity();
    }

    /* Activities openers */

    /* Opens main activity and closes activites relating to adding project */
    private void openMainActivity () {
        previous_activity = "addedLeaders";
        Intent intent = new Intent(AddUsersActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("logged_name", logged_name);
        intent.putExtra("logged_surname", logged_surname);
        intent.putExtra("logged_mail", logged_mail);
        startActivity(intent);
    }

    /* Opens ManageCompany activity and closes activities relating to adding company */
    private void openManageCompanyActivity () {
        Intent intent = new Intent(AddUsersActivity.this, ManageCompanyActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("logged_mail", logged_mail);
        startActivity(intent);
    }

    /* Opens main activity and closes activites relating to adding project */
    private void openTasksActivity() {
        Intent intent = new Intent(AddUsersActivity.this, TasksActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (projid != null) {
            Log.e(TAG, "openTasksActivity:fafs " + projid);
            intent.putExtra("projectId", projid);
        }
        else {
            Log.e(TAG, "openTasksActivity: kurwa" +  getIntent().getStringExtra("projectId"));
            intent.putExtra("projectId", getIntent().getStringExtra("projectId"));
        }
        intent.putExtra("company_name", company);
        intent.putExtra("logged_mail", getIntent().getStringExtra("logged_mail"));
        intent.putExtra("logged_name", getIntent().getStringExtra("logged_name"));
        intent.putExtra("logged_surname", getIntent().getStringExtra("logged_surname"));
        startActivity(intent);
    }

    /* Opens addUsersActivity in order to choose leaders among picked users */
    private void openAddLeaderActivity(String projectId, ArrayList<User> checked) {
        Intent intent = new Intent(AddUsersActivity.this, AddUsersActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("logged_name", logged_name);
        intent.putExtra("logged_surname", logged_surname);
        intent.putExtra("logged_mail", logged_mail);
        intent.putExtra("previous_activity", "addProjUsers");
        intent.putExtra("company_name", company);
        intent.putExtra("projectId", projectId);
        intent.putParcelableArrayListExtra("checked_users", checked);
        startActivity(intent);
    }

    private void addToDisplayArray(User user)
    {
        String[] skills = {"-", "-", "-"};
        if(user.getSkills() != null) {
            int size = Math.min(3, user.getSkills().size());

            for (int i = 0; i < size; i++)
                skills[i] = user.getSkills().get(i);
        }
        displayArray.add(new UserListItem(user.getName() + " " + user.getSurname(), skills[0], skills[1], skills[2]));
    }
}
