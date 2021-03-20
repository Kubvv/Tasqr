package com.example.tasqr;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private class ProjectList extends ArrayAdapter {
        private String[] projectNames;
        private String[] ownerNames;
        private Integer[] projectImages;
        private Activity context;

        public ProjectList(Activity context, String[] projectNames, String[] ownerNames, Integer[] projectImages) {
            super(context, R.layout.project_list_item, projectNames);
            this.context = context;
            this.projectNames = projectNames;
            this.ownerNames = ownerNames;
            this.projectImages = projectImages;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            LayoutInflater inflater = context.getLayoutInflater();
            if(convertView == null)
                row = inflater.inflate(R.layout.project_list_item, null, true);
            TextView projectName = (TextView) row.findViewById(R.id.projectName);
            TextView ownerName = (TextView) row.findViewById(R.id.projectOwner);
            ImageView projectImage = (ImageView) row.findViewById(R.id.projectImage);

            projectName.setText(projectNames[position]);
            ownerName.setText(ownerNames[position]);
            projectImage.setImageResource(projectImages[position]);
            return row;
        }
    }

    //Sample project list data
    private String[] projectNames =
            {"CuberPunkt 2069",
                    "Tasqr 2.0",
                    "Kill me pls",
                    "nasm_printf",
                    "yo mama fat",
                    "Bitcho",
                    "projekt 3",
                    "mamma mia",
                    "wiedźmiak 4",
                    "Tasqr 3.0",
                    "gamma the game"};

    private String[] ownerNames =
            {"DVDprj blue",
                    "3D studio",
                    "pls help",
                    "XDDD",
                    ":0000",
                    ":ccccc",
                    ">:D",
                    ":{:{:{",
                    "3D studio",
                    "3D studio",
                    "3D studio"};

    private Integer[] projectImages =
            {R.drawable.templateproject,
                    R.drawable.templateproject,
                    R.drawable.templateproject,
                    R.drawable.templateproject,
                    R.drawable.templateproject,
                    R.drawable.templateproject,
                    R.drawable.templateproject,
                    R.drawable.templateproject,
                    R.drawable.templateproject,
                    R.drawable.templateproject,
                    R.drawable.templateproject};

    private ListView projectList;

    private ImageButton addProjectButton;
    private Button profileButton;

    private TextView name;
    private TextView surname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addProjectButton = findViewById(R.id.addProjectButton);
        profileButton = findViewById(R.id.profileButton);
        name = findViewById(R.id.name);
        surname = findViewById(R.id.surname);

        addProjectButton.setOnClickListener(this);
        profileButton.setOnClickListener(this);
        name.setText(getIntent().getStringExtra("name"));
        surname.setText(getIntent().getStringExtra("surname"));

        projectList = findViewById(R.id.projectList);
        projectList.setAdapter(new ProjectList(this, projectNames, ownerNames, projectImages));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.addProjectButton:
                Intent addProjectIntent = new Intent(this, AddProjectActivity.class);
                addProjectIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(addProjectIntent);
                break;
            case R.id.profileButton:
                Intent profileIntent = new Intent(this, ProfileActivity.class);
                profileIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(profileIntent);
                break;
        }
    }
}