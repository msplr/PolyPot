package ch.epfl.pdse.polypotapp;

import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.GregorianCalendar;
import android.icu.util.TimeZone;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParsePosition;
import java.util.ArrayList;

public class GraphHelper {
    public static ArrayList<Entry> extractSeries(JSONArray data, String keyword) throws JSONException {
        ArrayList<Entry> entries = new ArrayList<Entry>();

        for(int i = 0; i < data.length(); i++) {
            JSONObject point = data.getJSONObject(i);

            float value = Float.parseFloat(point.getString(keyword));

            Calendar date = GregorianCalendar.getInstance();

            SimpleDateFormat inputDateFormat = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ssXXXXX");
            inputDateFormat.parse(point.getString("datetime"), date, new ParsePosition(0));

            entries.add(new Entry(date.getTimeInMillis(), value));
        }

        return entries;
    }

    public static class DateAxisFormatter implements IAxisValueFormatter {
        protected final Calendar mCalendar = Calendar.getInstance();
        protected final SimpleDateFormat mDateFormat = new SimpleDateFormat("HH:'00'");

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            mCalendar.setTimeInMillis((long) value);
            mCalendar.setTimeZone(TimeZone.getDefault());
            return mDateFormat.format(mCalendar);
        }

        /** this is only needed if numbers are returned, else return 0 */
        public int getDecimalDigits() { return 0; }
    }
}
