package com.example.tasqr;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class SubTasksActivity extends AppCompatActivity {

    private ListView subTaskList;

    private ArrayList<String> testArray = new ArrayList<>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subtasks);

        for(int i = 0; i < 10; i ++)
                testArray.add("test");

        subTaskList = findViewById(R.id.subTaskList);
        subTaskList.setAdapter(new ArrayAdapter<>(SubTasksActivity.this, R.layout.task_list_item, testArray));
    }
}
