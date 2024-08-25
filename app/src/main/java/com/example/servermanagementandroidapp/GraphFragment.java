package com.example.servermanagementandroidapp;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.View;

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

        //drawGraph(containerID, timePeriod);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                drawGraph(containerID, timePeriod);
            }
        }, 0, 1000);
    }

    private void drawGraph(@NonNull int[] containerID, @NonNull String timePeriod) {
        lineChart = (LineChart) requireView().findViewById(R.id.lineChart);

        lineChart.invalidate();
        lineChart.clear();

        url = "http://192.168.0.43:9000/getUtil?timePeriod=" + timePeriod;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                SimpleDateFormat dtf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

                long now;
                try {
                    now = Objects.requireNonNull(dtf.parse(response.getJSONObject(0).getString("dateTime"))).getTime();
                } catch (JSONException | ParseException e) {
                    throw new RuntimeException(e);
                }

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


                /*

                Date minDateTime;
                try {
                    minDateTime = dtf.parse(response.getJSONObject(0).getString("dateTime"));
                } catch (JSONException | ParseException e) {
                    throw new RuntimeException(e);
                }

                Date maxDateTime;
                try {
                    maxDateTime = dtf.parse(response.getJSONObject(response.length() - 1).getString("dateTime"));
                } catch (ParseException | JSONException e) {
                    throw new RuntimeException(e);
                }

                assert minDateTime != null;
                assert maxDateTime != null;
                //xAxis.setAxisMinimum(minDateTime.getTime() - now);
                //xAxis.setAxisMaximum(maxDateTime.getTime() - now);

                 */

