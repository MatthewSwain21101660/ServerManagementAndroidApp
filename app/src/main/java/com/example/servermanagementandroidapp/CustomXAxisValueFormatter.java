package com.example.servermanagementandroidapp;

import android.util.Log;

import com.github.mikephil.charting.formatter.ValueFormatter;

public class CustomXAxisValueFormatter extends ValueFormatter {
    private final String timePeriod;

    public CustomXAxisValueFormatter(String timePeriod) {
        this.timePeriod = timePeriod;
    }

    @Override
    public String getFormattedValue(float value) {
        //Calls the appropriate method depending on what the time period is
        if ("minute".equals(timePeriod)) {
            return getMinuteLabel(value);
        } else if ("hour".equals(timePeriod)) {
            return getHourLabel(value);
        } else if ("day".equals(timePeriod)) {
            return getDayLabel(value);
        } else if ("week".equals(timePeriod)) {
        return getWeekLabel(value);
        } else if ("month".equals(timePeriod)) {
            return getMonthLabel(value);
    }

        return super.getFormattedValue(value);
    }

    //The following all sets appropriate labels for the time periods

    private String getMinuteLabel(float value) {
        if (value == 0) {
            return "1 minute";
        } else if (value == 30000.0) {
            return "30 seconds";
        }
        return "";
    }

    private String getHourLabel(float value) {
        if (value == 0) {
            return "1 hour";
        } else if (value == 2000000.0) {
            return "30 minutes";
        }
        return "";
    }

    private String getDayLabel(float value) {
        Log.d("valueDay", String.valueOf(value));
        if (value == 0) {
            return "1 day";
        } else if (value == 4.0E7) {
            return "12 hours";
        }
        return "";
    }
    private String getWeekLabel(float value) {
        Log.d("valueWeek", String.valueOf(value));
        if (value == 0) {
            return "1 week";
        } else if (value == 3.0E8) {
            return "4 days";
        }
        return "";
    }
    private String getMonthLabel(float value) {
        Log.d("valueMonth", String.valueOf(value));
        if (value == 0) {
            return "1 month";
        } else if (value == 9.0E8) {
            return "15 days";
        }
        return "";
    }
}
