package ch.epfl.pdse.polypotapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.MenuItem;
import android.widget.DatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    private MenuItem mCurrentDay;
    private Calendar mDate;
    private SimpleDateFormat mDateFormat;

    public DatePickerFragment(MenuItem menuItem, Calendar calendar, SimpleDateFormat dateFormat) {
        mCurrentDay = menuItem;
        mDate = calendar;
        mDateFormat = dateFormat;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date in toolbar
        int year = mDate.get(Calendar.YEAR);
        int month = mDate.get(Calendar.MONTH);
        int day = mDate.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Update date
        mDate.set(year, month, day);

        // Update date in toolbar
        mCurrentDay.setTitle(mDateFormat.format(mDate.getTime()));

        // Update data and graphs
        CommunicationManager.getInstance().getData();
    }
}