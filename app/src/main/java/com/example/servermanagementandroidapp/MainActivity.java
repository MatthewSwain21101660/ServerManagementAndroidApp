package com.example.servermanagementandroidapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private final String[] timePeriods = { "Minute", "Hour", "Day", "Week", "Month"};

    private String ipAddress;
    private String sharePrefName = "AppPrefs";
    private String sharePrefIP = "IPAddress";


    public MainActivity() {
        super(R.layout.activity_main);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = getSharedPreferences(sharePrefName, Context.MODE_PRIVATE);
        ipAddress = preferences.getString(sharePrefIP, null);

        if (ipAddress == null) {
            inputIPAddress();
        } else {
            initializeGraphs();
        }
    }

    private void initializeGraphs() {
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
        bundle.putString("ipAddress", ipAddress);

        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(containerID, GraphFragment.class, bundle)
                .commit();
    }

    @Override
    public void onSaveInstanceState(Bundle outstate) {
        outstate.putString("ipAddress", "192.168.0.43");
        super.onSaveInstanceState(outstate);
    }

    private void inputIPAddress() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please enter the IP address of the server");

        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        builder.setView(input);

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ipAddress = input.getText().toString();
                if (!ipAddress.isEmpty()) {
                    SharedPreferences.Editor editor = getSharedPreferences(sharePrefName, Context.MODE_PRIVATE).edit();
                    editor.putString(sharePrefIP, ipAddress);
                    editor.apply();
                    initializeGraphs();
                } else {
                    Toast.makeText(MainActivity.this, "Please enter an IP address", Toast.LENGTH_SHORT).show();
                    inputIPAddress();
                }
            }
        });

        builder.setCancelable(false);
        builder.show();
    }
}