package ch.epfl.pdse.polypotapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.MenuItem;
import android.widget.DatePicker;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    private MenuItem mCurrentDay;
    private Calendar mDate;
    private SimpleDateFormat mDateFormat;

    public DatePickerFragment(MenuItem menuItem, Calendar calendar, SimpleDateFormat dateFormat) {
        mCurrentDay = menuItem;
        mDate = calendar;
        mDateFormat = dateFormat;
    }

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
        mCurrentDay.setTitle(mDateFormat.format(mDate));

        // Update data and graphs
        CommunicationManager.getInstance(getContext()).getData();
    }
}