package ch.epfl.pdse.polypotapp;

import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.GregorianCalendar;
import android.icu.util.TimeZone;

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

import java.text.ParsePosition;
import java.util.ArrayList;

public class GraphHelper {
    public static void configureChart(LineChart chart, int color, float min, float max) {
        YAxis yAxis = chart.getAxisLeft();
        yAxis.setAxisMinimum(min);
        yAxis.setAxisMaximum(max);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new GraphHelper.DateAxisFormatter());
        xAxis.setLabelCount(7,true);

        Description description = new Description();
        description.setText("");
        chart.setDescription(description);

        chart.getLegend().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.setNoDataTextColor(color);
    }

    public static void updateChartWithData(LineChart chart, int color, String keyword, JSONArray data, Calendar fromDate, Calendar toDate) throws JSONException {
        fromDate.setTimeZone(TimeZone.getDefault());
        fromDate.add(Calendar.MINUTE, 5);
        toDate.setTimeZone(TimeZone.getDefault());
        toDate.add(Calendar.MINUTE, 5);

        ArrayList<Entry> entries = new ArrayList<Entry>();

        for(int i = 0; i < data.length(); i++) {
            JSONObject point = data.getJSONObject(i);

            float value = Float.parseFloat(point.getString(keyword));

            Calendar date = GregorianCalendar.getInstance();

            SimpleDateFormat inputDateFormat = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ssXXXXX");
            inputDateFormat.parse(point.getString("datetime"), date, new ParsePosition(0));

            entries.add(new Entry(date.getTimeInMillis(), value));
        }

        if(entries.size() == 0) {
            chart.clear();
        } else {
            LineDataSet dataSet = new LineDataSet(entries, "Label");
            dataSet.setColor(color);
            dataSet.setDrawValues(false);
            dataSet.setLineWidth(2f);
            dataSet.setDrawCircles(false);

            LineData lineData = new LineData(dataSet);
            chart.setData(lineData);
        }

        XAxis xAxis = chart.getXAxis();
        xAxis.setAxisMinimum(fromDate.getTimeInMillis());
        xAxis.setAxisMaximum(toDate.getTimeInMillis());

        chart.invalidate();
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
