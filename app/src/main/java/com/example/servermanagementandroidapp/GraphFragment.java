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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;

import okhttp3.OkHttpClient;


public class GraphFragment extends Fragment {

    TextView data;
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


        url = "http://192.168.0.43:9000/getUtil?timePeriod=minute";
        //url = "https://worldtimeapi.org/api/timezone/Europe/London";


        String result;

        HttpGetRequest getRequest = new HttpGetRequest();

        try {
            result = getRequest.execute(url).get();
            if (result == null) {
                result = "sdfhklsfdhklhsfold";
            }
            Log.d("lkjh", result);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        /*
        //JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET)

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //String dateTime = response.getString("dateTime");
                //data.setText(dateTime);
                Log.d("GraphFragement", "success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("GraphFragement", "failure");
            }
        });

        Volley.newRequestQueue(requireActivity().getApplicationContext()).add(request);


/*
        OkHttpClient client = new OkHttpClient();

        url = "http://192.168.0.43:9000/getUtil?timePeriod=" + timePeriod;



            Request getRequest = new Request.Builder()
                    .url(url)
                    .build();

            client.newCall(getRequest).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    //Toast.makeText(getContext(), "Could not connect", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "failure");
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseData = response.body().string();
                        Log.d(TAG, "success");
                    }
                }
            });

        /*
        url = "https://worldtimeapi.org/api/timezone/Europe/London" + timePeriod;
        //Toast.makeText(getContext(), url, Toast.LENGTH_SHORT).show();
        data = getView().findViewById(R.id.test);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,  new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                String dateTime = null;
                try {
                    dateTime = response.getString("dateTime");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                Toast.makeText(getContext(), "Successful connection", Toast.LENGTH_SHORT).show();
                data.setText(dateTime);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), "Could not connect", Toast.LENGTH_SHORT).show();
                data.setText("Could not connect");
            }
        });
        Volley.newRequestQueue(requireActivity().getApplicationContext()).add(request);

        //GraphFragment.getInstance
/*
        if (containerID == R.id.cpuUtilisationGraph) {
            Toast.makeText(getContext(), "cpu" + timePeriod, Toast.LENGTH_SHORT).show();
        } else if (containerID == R.id.ramUtilisationGraph) {
            Toast.makeText(getContext(), "ram" + timePeriod, Toast.LENGTH_SHORT).show();
        }

 */
    }
}