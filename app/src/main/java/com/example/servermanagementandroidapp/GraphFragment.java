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

        Bundle bundle = getArguments();
        assert bundle != null;
        final int[] containerID = {bundle.getInt("containerID")};
        String timePeriod = bundle.getString("timePeriod");

        url = "http://192.168.0.43:9000/getUtil?timePeriod=" + timePeriod;

        lineChart = (LineChart) view.findViewById(R.id.lineChart);

        dtf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        xAxis = lineChart.getXAxis();
        yAxis = lineChart.getAxisLeft();

        startTimerTask(containerID, timePeriod);

    }


    private void startTimerTask(int[] containerID, String timePeriod) {
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

        drawGraph(timePeriod);

        timerTask = new TimerTask() {
            @Override
            public void run() {
                queryAPI(containerID);
            }
        };

        timer.schedule(timerTask, 0, repeatPeriod);
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }


    private void drawGraph(String timePeriod) {
        lineChart.getAxisRight().setDrawLabels(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getAxisRight().setDrawGridLines(false);

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new CustomXAxisValueFormatter(timePeriod));
        Log.d("drawGraph", "drawGraph");

        yAxis.setTextColor(Color.WHITE);
        yAxis.setAxisMinimum(0f);
    }


    private void queryAPI(int[] containerID) {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                lineChart.invalidate();
                lineChart.refreshDrawableState();



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

                long now;
                try {
                    now = Objects.requireNonNull(dtf.parse(response.getJSONObject(0).getString("dateTime"))).getTime();
                } catch (JSONException | ParseException e) {
                    throw new RuntimeException(e);
                }





                List<Entry> entry = new ArrayList<>();

                for (int i = 0; i < response.length(); i++) {
                    try {
                        float reading = 0;
                        Date date = null;
                        if (Arrays.equals(containerID, new int[]{R.id.cpuUtilisationGraph})) {
                            reading = Float.parseFloat(response.getJSONObject(i).getString("cpu"));
                        } else if (Arrays.equals(containerID, new int[]{R.id.ramUtilisationGraph})) {
                            reading = Float.parseFloat(response.getJSONObject(i).getString("ram"));
                        }


                        String dateTime = response.getJSONObject(i).getString("dateTime");
                        try {
                            date = dtf.parse(dateTime);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }


                        //entry.add(new Entry(Float.valueOf(date), reading));
                        //1724611719000, 1.72461169E12
                        //1724611720000, 1.72461169E12

                        assert date != null;
                        float finalValue = date.getTime() - now;


                        entry.add(new Entry(finalValue, reading));

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }

                LineDataSet dataSet = new LineDataSet(entry, "Reading");

                //dataSet.setColor(Color.parseColor("#4682B4FF"));
                dataSet.setDrawCircles(false);
                dataSet.setDrawHighlightIndicators(false);
                dataSet.setDrawValues(false);

                LineData lineData = new LineData(dataSet);

                lineChart.setData(lineData);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
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

        Volley.newRequestQueue(requireActivity().getApplicationContext()).add(request.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTimer();
    }
}

