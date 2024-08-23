package com.example.servermanagementandroidapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    String[] timePeriods = { "Minute", "Hour", "Day", "Week", "Month"};

    public MainActivity() {
        super(R.layout.activity_main);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Spinner cpuTimePeriod = findViewById(R.id.cpuTimePeriod);
        Spinner ramTimePeriod = findViewById(R.id.ramTimePeriod);

        cpuTimePeriod.setOnItemSelectedListener(this);
        ramTimePeriod.setOnItemSelectedListener(this);

        ArrayAdapter ad = new ArrayAdapter(this, android.R.layout.simple_spinner_item, timePeriods);

        ad.setDropDownViewResource(android.R.layout.simple_spinner_item);

        cpuTimePeriod.setAdapter(ad);
        ramTimePeriod.setAdapter(ad);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String timePeriod = timePeriods[position].toLowerCase();

        if (parent.getId() == R.id.cpuTimePeriod) {
            updateGraphFragment(R.id.cpuUtilisationGraph, timePeriod);
        } else if (parent.getId() == R.id.ramTimePeriod) {
            updateGraphFragment(R.id.ramUtilisationGraph, timePeriod);
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void updateGraphFragment(int containerID, String timePeriod) {
        Bundle bundle = new Bundle();
        bundle.putInt("containerID", containerID);
        bundle.putString("timePeriod", timePeriod);

        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(containerID, GraphFragment.class, bundle)
                .commit();
    }
}