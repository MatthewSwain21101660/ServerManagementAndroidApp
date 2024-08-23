package com.example.servermanagementandroidapp;

import static android.content.ContentValues.TAG;

import android.graphics.Color;
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
import com.android.volley.DefaultRetryPolicy;
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
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.type.DateTime;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
        final int[] containerID = {bundle.getInt("containerID")};
        String timePeriod = bundle.getString("timePeriod");
        Log.d("ID", String.valueOf(containerID[0]));

        lineChart = (LineChart) requireView().findViewById(R.id.lineChart);


        url = "http://192.168.0.43:9000/getUtil?timePeriod=" + timePeriod;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.d("GraphFragment", "success");

                List<Entry> entry = new ArrayList<>();

                lineChart.getAxisRight().setDrawLabels(false);
                lineChart.getLegend().setEnabled(false);
                lineChart.getDescription().setEnabled(false);
                lineChart.getXAxis().setDrawGridLines(false);
                lineChart.getAxisRight().setDrawGridLines(false);

                XAxis xAxis = lineChart.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setTextColor(Color.WHITE);

                YAxis yAxis = lineChart.getAxisLeft();
                yAxis.setTextColor(Color.WHITE);
                yAxis.setAxisMinimum(0f);
                //yAxis.setAxisLineColor(Color.parseColor("#6b6c6c"));

                if (containerID[0] == 2131361926) {
                    yAxis.setAxisMaximum(100f);
                } else if (containerID[0] == 2131362157) {
                    try {
                        //Log.d("rounded", String.valueOf(Math.ceil(Float.parseFloat(response.getJSONObject(response.length() - 1).getString("ramTotal")))));
                        yAxis.setAxisMaximum(Float.parseFloat(response.getJSONObject(response.length() - 1).getString("ramTotal")));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }


                for (int i = 0; i < response.length(); i++) {
                    try {
                        float reading = 0;
                        if (Arrays.equals(containerID, new int[]{R.id.cpuUtilisationGraph})) {
                            reading = Float.parseFloat(response.getJSONObject(i).getString("cpu"));
                        } else if (Arrays.equals(containerID, new int[]{R.id.ramUtilisationGraph})) {
                            reading = Float.parseFloat(response.getJSONObject(i).getString("ram"));
                        }

                        /*
                        String dateTime = response.getJSONObject(i).getString("dateTime");

                        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        long date = 0;
                        try {
                            date = format.parse(dateTime).toInstant().toEpochMilli();
                            Log.d("datetime", String.valueOf(date));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                         */

                        //entry.add(new Entry(Float.valueOf(date), reading));
                        entry.add(new Entry(i, reading));

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }



                LineDataSet dataSet = new LineDataSet(entry, "Reading");

                //dataSet.setColor(Color.parseColor("#4682B4FF"));
                dataSet.setDrawCircles(false);
                dataSet.setDrawHighlightIndicators(false);
                dataSet.setDrawValues(false);

                dataSet.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return super.getFormattedValue(value);
                    }
                });

                LineData lineData = new LineData(dataSet);

                lineChart.setData(lineData);

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
        Volley.newRequestQueue(requireActivity().getApplicationContext()).add(request.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)));
    }
}

