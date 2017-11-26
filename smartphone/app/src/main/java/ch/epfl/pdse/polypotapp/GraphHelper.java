package ch.epfl.pdse.polypotapp;

import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.GregorianCalendar;
import android.icu.util.TimeZone;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParsePosition;
import java.util.ArrayList;

public class GraphHelper {
    public static LineGraphSeries<DataPoint> extractSeries(JSONArray data, String keyword) throws JSONException {
        ArrayList<DataPoint> series = new ArrayList<DataPoint>();

        for (int i = 0; i < data.length(); i++) {
            JSONObject point = data.getJSONObject(i);

            float value = Float.parseFloat(point.getString(keyword));

            SimpleDateFormat inputDateFormat = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ssXXXXX");
            Calendar date = new GregorianCalendar();
            inputDateFormat.parse(point.getString("datetime"), date, new ParsePosition(0));
            date.setTimeZone(TimeZone.getDefault());

            DataPoint dataPoint = new DataPoint(date.getTime(), value);
            series.add(dataPoint);
        }

        return new LineGraphSeries<>(series.toArray(new DataPoint[0]));
    }

    public static class DateFormatter extends DefaultLabelFormatter {
        protected final Calendar mCalendar = Calendar.getInstance();
        protected final SimpleDateFormat mDateFormat = new SimpleDateFormat("HH:mm");

        @Override
        public String formatLabel(double value, boolean isValueX) {
            if (isValueX) {
                // format as date
                mCalendar.setTimeInMillis((long) value);
                return mDateFormat.format(mCalendar.getTimeInMillis());
            } else {
                return super.formatLabel(value, isValueX);
            }
        }
    }
}
