package com.example.servermanagementandroidapp;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class GraphFragment extends Fragment {

    LineChart lineChart;
    String url;
    Timer timer;
    TimerTask timerTask;
    XAxis xAxis;
    YAxis yAxis;
    SimpleDateFormat dtf;

    public GraphFragment() {
        super(R.layout.fragment_graph);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_graph, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Retrieves values from the provided bundle
        Bundle bundle = getArguments();
        assert bundle != null;
        final int[] containerID = {bundle.getInt("containerID")};
        String timePeriod = bundle.getString("timePeriod");
        String ipAddress = bundle.getString("ipAddress");

        //Sets the url to the provided ip address and time period
        url = "http://" + ipAddress + ":9000/getUtil?timePeriod=" + timePeriod;

        //Allows the linechart to be manipulated
        lineChart = (LineChart) view.findViewById(R.id.lineChart);

        //Creates a date time formatter
        dtf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        //Creates variables to manipulate the axis
        xAxis = lineChart.getXAxis();
        yAxis = lineChart.getAxisLeft();

        //Calls the initial timer method to start the process of drawing a graph every certain unit of time
        startTimerTask(containerID, timePeriod);
    }


    private void startTimerTask(int[] containerID, String timePeriod) {
        //Sets how often the graph is redrawn depending on what time period has been selected
        int repeatPeriod = 0;
        if (Objects.equals(timePeriod, "minute")) {
            repeatPeriod = 1000;
        } else if (Objects.equals(timePeriod, "hour")) {
            repeatPeriod = 60000;
        } else if (Objects.equals(timePeriod, "day") || Objects.equals(timePeriod, "week")) {
            repeatPeriod = 3600000;
        } else if (Objects.equals(timePeriod, "month")) {
            repeatPeriod = 86400000;
        }

        timer = new Timer();

        //Calls the drawGraph method to initialize. If this were not called, time periods like day would not be drawn until an hour had passed
        drawGraph(timePeriod);

        //All that is called in the timer task is the update graph method
        timerTask = new TimerTask() {
            @Override
            public void run() {
                updateGraph(containerID);
            }
        };

        timer.schedule(timerTask, 0, repeatPeriod);
    }

    private void stopTimer() {
        //Cancels the timer
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }


    private void drawGraph(String timePeriod) {
        //Sets various stylistic elements such as removing certain grid lines or setting text colour
        lineChart.getAxisRight().setDrawLabels(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getAxisRight().setDrawGridLines(false);

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setGranularity(1f);
        //Calls a custom method that attaches various x labels depending on what the time period is
        xAxis.setValueFormatter(new CustomXAxisValueFormatter(timePeriod));

        yAxis.setTextColor(Color.WHITE);
        yAxis.setAxisMinimum(0f);
    }


    private void updateGraph(int[] containerID) {
        //Sends the API request
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                //Methods make the graph visible
                lineChart.invalidate();
                lineChart.refreshDrawableState();



                //Sets the y axis to the appropriate scale depending on what the graph is for
                if (Arrays.equals(containerID, new int[]{R.id.cpuUtilisationGraph})) {
                    yAxis.setAxisMaximum(100f);
                } else if (Arrays.equals(containerID, new int[]{R.id.ramUtilisationGraph})) {
                    try {
                        //Log.d("rounded", String.valueOf(Math.ceil(Float.parseFloat(response.getJSONObject(response.length() - 1).getString("ramTotal")))));
                        yAxis.setAxisMaximum(Float.parseFloat(response.getJSONObject(response.length() - 1).getString("ramTotal")));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }

                //Gets the dateTime of the most recent entry
                long now;
                try {
                    now = Objects.requireNonNull(dtf.parse(response.getJSONObject(0).getString("dateTime"))).getTime();
                } catch (JSONException | ParseException e) {
                    throw new RuntimeException(e);
                }





                //Creates an array list of entries for the graph and recurses through every JSON entry
                List<Entry> entry = new ArrayList<>();

                for (int i = 0; i < response.length(); i++) {
                    try {
                        float reading = 0;
                        Date date = null;
                        //Sets the reading to what is appropriate for the graph type
                        if (Arrays.equals(containerID, new int[]{R.id.cpuUtilisationGraph})) {
                            reading = Float.parseFloat(response.getJSONObject(i).getString("cpu"));
                        } else if (Arrays.equals(containerID, new int[]{R.id.ramUtilisationGraph})) {
                            reading = Float.parseFloat(response.getJSONObject(i).getString("ram"));
                        }


                        //Retrieves the date and time the entry was taken
                        String dateTime = response.getJSONObject(i).getString("dateTime");
                        try {
                            date = dtf.parse(dateTime);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }



                        //Subtracts the current dateTime from the latest date time.
                        //This needs to be done as all variables put into an entry need to be a float
                        //The int form of time is the number of seconds or miliseconds since the 1st January 1970
                        //For instance 1724611719000 would be 27/08/2024, 21:22:37
                        //However, floats only contain 7 digits of precision and so 1724611719000 becomes 1.72461169E12
                        //This is an issue as 1724611720000 also becomes 1.72461169E12
                        //For this reason, to make the date and time entry small enough to fit in a float for the entry, the current time needs to be subtracted from the maximum time
                        //For instance, if 1724611720000 were the latest time, its float entry would be 0 and 1724611719000 would be 1000
                        assert date != null;
                        float finalValue = date.getTime() - now;


                        //Adds the values as an entry into the array list
                        entry.add(new Entry(finalValue, reading));

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }

                LineDataSet dataSet = new LineDataSet(entry, "Reading");

                //Sets more stylistic aspects of the graph
                dataSet.setDrawCircles(false);
                dataSet.setDrawHighlightIndicators(false);
                dataSet.setDrawValues(false);

                LineData lineData = new LineData(dataSet);

                //Adds the data to the graph
                lineChart.setData(lineData);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Error handling for if the API request fails
                if (error instanceof NetworkError) {
                    Log.d("GraphFragement", "NetworkError");
                } else if (error instanceof ServerError) {
                    Log.d("GraphFragement", "ServerError");
                } else if (error instanceof AuthFailureError) {
                    Log.d("GraphFragement", "AuthFailureError");
                } else if (error instanceof ParseError) {
                    Log.d("GraphFragement", "ParseError");
                } else if (error instanceof NoConnectionError) {
                    Log.d("GraphFragement", "NoConnectionError");
                } else if (error instanceof TimeoutError) {
                    Log.d("GraphFragement", "TimeoutError");
                }


            }
        });

        //Makes the request to the API
        Volley.newRequestQueue(requireActivity().getApplicationContext()).add(request.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)));
    }

    @Override
    public void onDestroy() {
        //When the fragment is destroyed such as when a new time period is selected, the timer is stopped
        super.onDestroy();
        stopTimer();
    }
}

