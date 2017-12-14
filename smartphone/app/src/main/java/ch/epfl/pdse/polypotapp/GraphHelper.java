package ch.epfl.pdse.polypotapp;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

class GraphHelper {
    public static void configureChart(LineChart chart, int color, float min, float max) {
        YAxis yAxis = chart.getAxisLeft();
        yAxis.setAxisMinimum(min);
        yAxis.setAxisMaximum(max);

        Description description = new Description();
        description.setText("");
        chart.setDescription(description);

        chart.getAxisRight().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.setNoDataTextColor(color);
    }

    public static void updateChartWithData(LineChart chart, int color, String keyword, String label, JSONObject response, ActivityMain activity) throws JSONException, ParseException {
        JSONArray data = response.getJSONArray("data");

        SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        inputDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        Calendar fromDate = GregorianCalendar.getInstance();
        fromDate.setTime(inputDateFormat.parse(response.getString("from")));
        fromDate.setTimeZone(TimeZone.getDefault());

        Calendar toDate = GregorianCalendar.getInstance();
        toDate.setTime(inputDateFormat.parse(response.getString("to")));
        toDate.setTimeZone(TimeZone.getDefault());

        // Adjustment for better rendering
        fromDate.add(Calendar.MINUTE, 5);
        toDate.add(Calendar.MINUTE, 5);

        ArrayList<Entry> entries = new ArrayList<>();

        for (int i = 0; i < data.length(); i++) {
            JSONObject point = data.getJSONObject(i);

            float value = Float.parseFloat(point.getString(keyword));

            Calendar date = GregorianCalendar.getInstance();
            date.setTime(inputDateFormat.parse(point.getString("datetime")));

            entries.add(new Entry(date.getTimeInMillis(), value));
        }

        String date = activity.getDateFormat().format(fromDate.getTime());

        if (entries.size() == 0) {
            chart.clear();
            chart.setNoDataText(String.format(activity.getString(R.string.chart_no_data), date));
        } else {
            LineDataSet dataSet = new LineDataSet(entries, String.format(label, date));
            dataSet.setColor(color);
            dataSet.setDrawValues(false);
            dataSet.setLineWidth(2f);
            dataSet.setDrawCircles(false);

            LineData lineData = new LineData(dataSet);
            chart.setData(lineData);
        }

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new GraphHelper.DateAxisFormatter(activity.getAxisDateFormat()));
        xAxis.setLabelCount(activity.getLabelCount(), true);
        xAxis.setAxisMinimum(fromDate.getTimeInMillis());
        xAxis.setAxisMaximum(toDate.getTimeInMillis());

        chart.invalidate();
    }

    public static class DateAxisFormatter implements IAxisValueFormatter {
        private final Calendar mCalendar = GregorianCalendar.getInstance();
        private final SimpleDateFormat mDateFormat;

        public DateAxisFormatter(SimpleDateFormat dateFormat) {
            mDateFormat = dateFormat;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            // Get time and convert to local timezone
            mCalendar.setTimeInMillis((long) value);
            mCalendar.setTimeZone(TimeZone.getDefault());

            // Adjustment for better rendering
            mCalendar.add(Calendar.MINUTE, 5);

            return mDateFormat.format(mCalendar.getTime());
        }
    }
}
