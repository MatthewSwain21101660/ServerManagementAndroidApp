package com.example.servermanagementandroidapp;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.type.DateTime;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;

import okhttp3.OkHttpClient;


public class GraphFragment extends Fragment {

    LineChart lineChart;
    String url;

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

        //data = requireView().findViewById(R.id.test);
        lineChart = (LineChart) requireView().findViewById(R.id.lineChart);


        url = "http://10.0.2.2:9000/getUtil?timePeriod=" + timePeriod;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                //Log.d("GraphFragment", String.valueOf(response));
                Log.d("GraphFragment", "success");


                List<Entry> entry = new ArrayList<>();

                for (int i = 0; i < response.length(); i++) {
                    try {
                        float reading = Float.parseFloat(response.getJSONObject(i).getString("cpu"));
                        //DateTime dateTime = response.getJSONObject(i).getString("dateTime");
                        entry.add(new Entry(i, reading));
                        //Log.d("entry", entry.toString());
                        //responseDateTime = responseDateTime + " " + reading.getString("dateTime");

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }

                LineDataSet dataSet = new LineDataSet(entry, "Reading");

                LineData lineData = new LineData(dataSet);

                lineChart.setData(lineData);

/*
                ArrayList<String> xAxis = new ArrayList<String>();

















                //String responseDateTime = "";

                ArrayList<Entry> yAxis = new ArrayList<>();




                //data.setText(responseDateTime);
                //Log.d("GraphFragment", responseDateTime);
                Log.d("GraphFragment", String.valueOf(yAxis));

 */

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error.Response", error.toString());
                if( error instanceof NetworkError) {
                    Log.d("GraphFragement", "NetworkError");
                } else if( error instanceof ServerError) {
                    Log.d("GraphFragement", "ServerError");
                } else if( error instanceof AuthFailureError) {
                    Log.d("GraphFragement", "AuthFailureError");
                } else if( error instanceof ParseError) {
                    Log.d("GraphFragement", "ParseError");
                } else if( error instanceof NoConnectionError) {
                    Log.d("GraphFragement", "NoConnectionError");
                } else if( error instanceof TimeoutError) {
                    Log.d("GraphFragement", "TimeoutError");
                }


            }
        });
        Volley.newRequestQueue(requireActivity().getApplicationContext()).add(request);
    }
}