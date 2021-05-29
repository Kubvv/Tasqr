package com.example.tasqr;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.example.tasqr.classes.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import io.grpc.okhttp.internal.Util;

public class SkillsActivity extends AppCompatActivity implements RecyclerViewAdapter.OnSkillListener {

    private static final String TAG = "SkillsActivity";

    private ArrayList<String> skillNames = new ArrayList<>();
    private RecyclerView recyclerView;

    private User user;
    private ArrayList<String> currentSkills;
    private ArrayList<String> skills;
    private boolean[] isClicked;

    private Button confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skills);

        Bundle bundle = getIntent().getExtras();
        user = bundle.getParcelable("user");
        currentSkills = user.getSkills();
        isClicked = new boolean[Utilities.getSkillSize()];
        Arrays.fill(isClicked, false);
        Log.e(TAG, "onCreate1: " + isClicked[0]);
        skills = Utilities.getSkillSet();
        int j = 0;
        if (currentSkills != null && currentSkills.size() > 0) {
            Collections.sort(currentSkills);
            for (int i = 0; i < isClicked.length; i++) {
                if (j == currentSkills.size()) {
                    break;
                }
                if (skills.get(i).equals(currentSkills.get(j))) {
                    Log.e(TAG, "onCreate: co ty robisz " + i);
                    isClicked[i] = true;
                    j++;
                }
            }
        }
        else {
            currentSkills = new ArrayList<>();
        }

        Log.e(TAG, "onCreate2: " + isClicked[0]);

        skillNames = Utilities.getSkillSet();
        Log.e(TAG, "onCreate: " + skillNames.size());
        initRecyclerView();

        confirmButton = findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("skills", currentSkills);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });

        Log.e(TAG, "onCreate3: " + isClicked[0]);
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.skillsList);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(skillNames, this, this, isClicked, true);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
    }

    @Override
    public void onSkillClick(int position) {
        Log.e(TAG, "onSkillClick: " + position + " " + isClicked[position]);
        if (!isClicked[position]) {
            currentSkills.remove(skills.get(position));
        }
        else {
            currentSkills.add(skills.get(position));
        }
    }
}