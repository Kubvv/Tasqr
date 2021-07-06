/*
 * SKILLS ACTIVITY
 * Contains  List of all available skills to choose from
 */

package com.example.tasqr;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.tasqr.Styling.RecyclerViewAdapter;
import com.example.tasqr.classes.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class SkillsActivity extends AppCompatActivity implements RecyclerViewAdapter.OnSkillListener {

    private ArrayList<String> skillNames = new ArrayList<>();

    private ArrayList<String> currentSkills;
    private ArrayList<String> skills;
    private boolean[] isClicked;

    /* MAIN ON CREATE METHOD */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skills);

        /* Variable setup */
        Bundle bundle = getIntent().getExtras();
        User user = bundle.getParcelable("user");
        currentSkills = user.getSkills();
        isClicked = new boolean[Utilities.getSkillSize()];
        Arrays.fill(isClicked, false);
        skills = Utilities.getSkillSet();

        /* Skills states setup */
        int j = 0;
        if (currentSkills != null && currentSkills.size() > 0) {
            Collections.sort(currentSkills);
            for (int i = 0; i < isClicked.length; i++) {
                if (j == currentSkills.size())
                    break;

                if (skills.get(i).equals(currentSkills.get(j))) {
                    isClicked[i] = true;
                    j++;
                }
            }
        }
        else
            currentSkills = new ArrayList<>();

        skillNames = Utilities.getSkillSet();

        /* View setup */
        initRecyclerView();

        Button confirmButton = findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("skills", currentSkills);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });
    }

    private void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.skillsList);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(skillNames, this, this, isClicked, true);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onSkillClick(int position) {
        if (!isClicked[position])
            currentSkills.remove(skills.get(position));
        else
            currentSkills.add(skills.get(position));
    }
}