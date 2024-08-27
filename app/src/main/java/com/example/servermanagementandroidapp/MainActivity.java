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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private final String[] timePeriods = { "Minute", "Hour", "Day", "Week", "Month"};

    private String ipAddress;
    private final String sharePrefName = "AppPrefs";
    private final String sharePrefIP = "IPAddress";


    public MainActivity() {
        super(R.layout.activity_main);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //When the app is first launched, check if there are any shared preferences containing a previously entered IP address. This way the user can enter a custom IP address but does not have to do so every time they launch the app
        SharedPreferences preferences = getSharedPreferences(sharePrefName, Context.MODE_PRIVATE);
        ipAddress = preferences.getString(sharePrefIP, null);

        //If there is no stored IP address, launches the method to get an IP, otherwise continues to setup the app as normal
        if (ipAddress == null) {
            inputIPAddress();
        } else {
            initializeGraphs();
        }
    }

    private void initializeGraphs() {
        //The following code initialises the spinners so that the selectable options are from the previously defined timePeriods array. When an option is selected, the onItemSelectedListener is called and thus the graph is drawn
        Spinner cpuTimePeriod = findViewById(R.id.cpuTimePeriod);
        Spinner ramTimePeriod = findViewById(R.id.ramTimePeriod);

        cpuTimePeriod.setOnItemSelectedListener(this);
        ramTimePeriod.setOnItemSelectedListener(this);

        ArrayAdapter ad = new ArrayAdapter(this, android.R.layout.simple_spinner_item, timePeriods);

        ad.setDropDownViewResource(android.R.layout.simple_spinner_item);

        cpuTimePeriod.setAdapter(ad);
        ramTimePeriod.setAdapter(ad);

        //OnClickListener that launches the set IP address method when the user clicks the change IP button
        Button IPButton = findViewById(R.id.changeIPBuuton);
        IPButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputIPAddress();
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //Sets the time period variable to the one the user has selected and converts it to lower case for compatibility with the API
        String timePeriod = timePeriods[position].toLowerCase();

        //Calls the update graph fragment with the appropriate ID
        if (parent.getId() == R.id.cpuTimePeriod) {
            updateGraphFragment(R.id.cpuUtilisationGraph, timePeriod);
        } else if (parent.getId() == R.id.ramTimePeriod) {
            updateGraphFragment(R.id.ramUtilisationGraph, timePeriod);
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //If nothing is selected, nothing happens
    }

    private void updateGraphFragment(int containerID, String timePeriod) {
        //Create a bundle with the required variables
        Bundle bundle = new Bundle();
        bundle.putInt("containerID", containerID);
        bundle.putString("timePeriod", timePeriod);
        bundle.putString("ipAddress", ipAddress);

        //Calls the fragment with the bundle
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(containerID, GraphFragment.class, bundle)
                .commit();
    }


    private void inputIPAddress() {
        //Creates an alert builder that contains the required components
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("Please enter the IP address of the server");

        //Creates an input for the user to enter the ip address
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        alertBuilder.setView(input);

        alertBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ipAddress = input.getText().toString();
                if (!ipAddress.isEmpty()) {
                    String ipPattern = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$";
                    //Checks to make sure the entered string is in the form of X.X.X.X. This makes sure the user enters a valid string and protects against any sort of injection attacks
                    if (ipAddress.matches(ipPattern)) {
                        //If the something has been entered into the input, it will be saved into shared preferences and the app will continue to be initialised.
                        SharedPreferences.Editor editor = getSharedPreferences(sharePrefName, Context.MODE_PRIVATE).edit();
                        editor.putString(sharePrefIP, ipAddress);
                        editor.apply();
                        initializeGraphs();
                    } else {
                        //If the entered string is not in the form of X.X.X.X, asks the user to enter a validate ip v4 address and calls the method again
                        Toast.makeText(MainActivity.this, "Please enter an IP address in the form of X.X.X.X", Toast.LENGTH_SHORT).show();
                        inputIPAddress();
                    }
                } else {
                    //If the user does not enter anything, a toast will appear asking them to enter an ip and the method it called again
                    Toast.makeText(MainActivity.this, "Please enter an IP address", Toast.LENGTH_SHORT).show();
                    inputIPAddress();
                }
            }
        });

        //Calls the alert that has just been built
        alertBuilder.setCancelable(false);
        alertBuilder.show();
    }

}