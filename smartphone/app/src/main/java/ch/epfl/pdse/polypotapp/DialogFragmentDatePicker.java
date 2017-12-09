package ch.epfl.pdse.polypotapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;

public class DialogFragmentDatePicker extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    private ActivityMain mActivity;

    private String mServer;
    private String mUUID;

    public static DialogFragmentDatePicker newInstance(String server, String uuid) {
        DialogFragmentDatePicker f = new DialogFragmentDatePicker();

        Bundle args = new Bundle();
        args.putString("server", server);
        args.putString("uuid", uuid);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mActivity = (ActivityMain) context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        mServer = args.getString("server");
        mUUID = args.getString("uuid");

        // Use the current date in toolbar
        Calendar date = mActivity.getFromDate();

        int year = date.get(Calendar.YEAR);
        int month = date.get(Calendar.MONTH);
        int day = date.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar date = mActivity.getFromDate();

        // Update date
        date.set(year, month, day);
        mActivity.setDate(date.getTime());

        // Update data and graphs
        EventBus.getDefault().post(new CommunicationManager.DataRequest(mServer, mUUID, mActivity.getFromDate(), mActivity.getToDate()));
    }
}