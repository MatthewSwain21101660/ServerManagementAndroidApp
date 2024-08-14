package com.example.servermanagementandroidapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URL;


public class GraphFragment extends Fragment {
    public GraphFragment() {
        super(R.layout.fragment_graph);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = getArguments();
        assert bundle != null;
        int containerID = bundle.getInt("containerID");
        String timePeriod = bundle.getString("timePeriod");


        if (containerID == R.id.cpuUtilisationGraph) {
            Toast.makeText(getContext(), "cpu", Toast.LENGTH_SHORT).show();
        } else if (containerID == R.id.ramUtilisationGraph) {
            Toast.makeText(getContext(), "ram", Toast.LENGTH_SHORT).show();
        }

        //String containerID = bundle.getString("containerID");

    }
}