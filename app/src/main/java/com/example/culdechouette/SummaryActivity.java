package com.example.culdechouette;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;

public class SummaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        ListView summaryListView = findViewById(R.id.summaryListView);

        ArrayList<HashMap<String, String>> gameLog = (ArrayList<HashMap<String, String>>) getIntent().getSerializableExtra("gameLog");

        SimpleAdapter adapter = new SimpleAdapter(
                this,
                gameLog,
                android.R.layout.simple_list_item_2,
                new String[]{"player", "figure", "points", "totalScore", "tokens"},
                new int[]{android.R.id.text1, android.R.id.text2}
        );

        summaryListView.setAdapter(adapter);
    }
}
