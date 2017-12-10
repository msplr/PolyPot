package ch.epfl.pdse.polypotapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.gson.internal.LinkedTreeMap;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;

public class ActivityMain extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private TabLayout mTabLayout;

    private String mServer;
    private String mUUID;
    private SharedPreferences mSharedPreferences;

    private MenuItem mPreviousPeriod;
    private MenuItem mCurrentPeriod;
    private MenuItem mNextPeriod;
    private MenuItem mCurrentPeriodMode;

    private PeriodMode mPeriodMode;
    private SimpleDateFormat mDateFormatDay;
    private SimpleDateFormat mDateFormatWeek;
    private SimpleDateFormat mDateFormatMonth;
    private SimpleDateFormat mAxisDateFormatDay;
    private SimpleDateFormat mAxisDateFormatWeek;
    private SimpleDateFormat mAxisDateFormatMonth;
    private Calendar mDate;
    private Calendar mWaterDate;
    private Calendar mFromDate;
    private Calendar mToDate;

    private boolean mFirstDateChange;
    private boolean mFirstConfigLoad;

    private HashMap<String, String> mConfigTypeMap;
    private Snackbar mConfigurationSnackbar;
    private int mConfigurationBeingSent;

    private LinkedTreeMap<String, Object> mPlants;
    private Plant mPlant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CommunicationManager.getDefault(this.getApplicationContext()).clearCache();

        Bundle b = getIntent().getExtras();
        mServer = b.getString("server");
        mUUID = b.getString("uuid");

        mSharedPreferences = getSharedPreferences(Pot.getPreferenceName(mServer, mUUID), MODE_PRIVATE);

        mDateFormatDay = new SimpleDateFormat(getResources().getString(R.string.date_format_day), Locale.US);
        mDateFormatWeek = new SimpleDateFormat(getResources().getString(R.string.date_format_week), Locale.US);
        mDateFormatMonth = new SimpleDateFormat(getResources().getString(R.string.date_format_month), Locale.US);

        mAxisDateFormatDay = new SimpleDateFormat(getResources().getString(R.string.axis_date_format_day), Locale.US);
        mAxisDateFormatWeek = new SimpleDateFormat(getResources().getString(R.string.axis_date_format_week), Locale.US);
        mAxisDateFormatMonth = new SimpleDateFormat(getResources().getString(R.string.axis_date_format_month), Locale.US);

        mFirstDateChange = true;
        mFirstConfigLoad = true;

        mConfigTypeMap = new HashMap<>();
        mConfigTypeMap.put("target_soil_moisture", "int");
        mConfigTypeMap.put("water_volume_pumped", "string");
        mConfigTypeMap.put("logging_interval", "string");
        mConfigTypeMap.put("sending_interval", "string");
        mConfigTypeMap.put("water_tank", "string");
        mConfigTypeMap.put("plant", "string");

        mPlants = Plant.getPlantsList(this);
        mPlant = new Plant(mPlants, getSharedPreferences().getString("plant", "Unspecified"));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mTabLayout = findViewById(R.id.tabs);

        // Make icons in tab independent from other same icons
        for(int i=0; i < mSectionsPagerAdapter.getCount(); i++) {
            mTabLayout.getTabAt(i).getIcon().mutate();
        }

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));

        mConfigurationSnackbar = Snackbar.make(mViewPager, R.string.configuration_sending, Snackbar.LENGTH_INDEFINITE);
        mConfigurationBeingSent = 0;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);

        // Restore date
        mPeriodMode = PeriodMode.valueOf(savedInstanceState.getString("period_mode"));
        mDate = GregorianCalendar.getInstance();
        mDate.setTimeInMillis(savedInstanceState.getLong("date"));
        updateFromToDates();

        // Restore first date change and config load state
        mFirstDateChange = savedInstanceState.getBoolean("firstDateChange");
        mFirstConfigLoad = savedInstanceState.getBoolean("firstConfigLoad");

        // Restore tab
        mViewPager.setCurrentItem(savedInstanceState.getInt("activeTab"));
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

        // If mDate is not null, we are restoring the activity and can skip that section
        if(mDate == null) {
            // Use today by default
            mPeriodMode = PeriodMode.DAY;
            mDate = GregorianCalendar.getInstance();
            updateFromToDates();
        }

        // Register to EventBus
        EventBus.getDefault().register(this);

        EventBus.getDefault().post(new CommunicationManager.LatestRequest(mServer, mUUID));
        EventBus.getDefault().post(new CommunicationManager.DataRequest(mServer, mUUID, mFromDate, mToDate));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        // Save reference to items in toolbar
        mPreviousPeriod = menu.findItem(R.id.previous_day);
        mCurrentPeriod = menu.findItem(R.id.current_day);
        mNextPeriod = menu.findItem(R.id.next_day);
        mCurrentPeriodMode = menu.findItem(R.id.date_mode);

        // Update date in toolbar
        updateDateInToolbar();

        // Hide date if on Summary and Configuration tabs, update icons transparency
        updateToolbarAndTablayout(mViewPager.getCurrentItem());

        // Same thing, but on tab change
        mTabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager) {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                super.onTabSelected(tab);

                // Hide date if on Summary and Configuration tabs, update icons transparency
                updateToolbarAndTablayout(tab.getPosition());
            }
        });
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the current tab
        savedInstanceState.putInt("activeTab", mViewPager.getCurrentItem());

        // Save first date change and config load state
        savedInstanceState.putBoolean("firstDateChange", mFirstDateChange);
        savedInstanceState.putBoolean("firstConfigLoad", mFirstConfigLoad);

        // Save the date
        savedInstanceState.putString("period_mode", mPeriodMode.toString());
        savedInstanceState.putLong("date", mDate.getTimeInMillis());

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);

        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.previous_day:
                backwardDateByOne();

                // Update data and graphs
                EventBus.getDefault().post(new CommunicationManager.DataRequest(mServer, mUUID, mFromDate, mToDate));
                return true;

            case R.id.current_day:
                // Let the user pick the date
                DialogFragmentDatePicker datePicker = DialogFragmentDatePicker.newInstance(mServer, mUUID);
                datePicker.show(getSupportFragmentManager(), "datePicker");
                return true;

            case R.id.next_day:
                forwardDateByOne();

                // Update data and graphs
                EventBus.getDefault().post(new CommunicationManager.DataRequest(mServer, mUUID, mFromDate, mToDate));
                return true;

            case R.id.date_mode:
                switch(mPeriodMode) {
                    case DAY:
                        mPeriodMode = PeriodMode.WEEK;
                        break;
                    case WEEK:
                        mPeriodMode = PeriodMode.MONTH;
                        break;
                    case MONTH:
                        mPeriodMode = PeriodMode.DAY;
                        break;
                }
                updateFromToDates();
                updateDateInToolbar();

                // Update data and graphs
                EventBus.getDefault().post(new CommunicationManager.DataRequest(mServer, mUUID, mFromDate, mToDate));
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        // If not on summary, get back to it, else let superclass handle it
        if(mViewPager.getCurrentItem() != Tabs.SUMMARY) {
            mViewPager.setCurrentItem(Tabs.SUMMARY);
        } else {
            super.onBackPressed();
        }
    }

    @Subscribe
    public void handleLatestData(CommunicationManager.LatestDataReady event) {
        if(mFirstDateChange) {
            try {
                JSONObject data = event.response.getJSONObject("data");

                // Switch to latest data date
                Calendar date = Calendar.getInstance();
                SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                inputDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                date.setTime(inputDateFormat.parse(data.getString("datetime")));
                date.setTimeZone(TimeZone.getDefault());
                setDate(date.getTime());

                // Only switch the toolbar date once
                mFirstDateChange = false;
            } catch (NullPointerException | JSONException | ParseException e) {
                Snackbar.make(mViewPager, R.string.reception_summary_error, Snackbar.LENGTH_LONG).show();
            }
        }

        if(mFirstConfigLoad) {
            try {
                // Parse and store configuration from the server
                JSONObject config = event.response.getJSONObject("configuration");

                SharedPreferences.Editor editor = mSharedPreferences.edit();

                Iterator<String> it = config.keys();
                while (it.hasNext()) {
                    String key = it.next();

                    switch (mConfigTypeMap.get(key)) {
                        case "int":
                            editor.putInt(key, config.getInt(key));
                            break;
                        case "string":
                            editor.putString(key, config.getString(key));
                            break;
                    }
                }
                editor.apply();

                // Only update the local configuration once
                mFirstConfigLoad = false;
            } catch (NullPointerException | JSONException e) {
                Snackbar.make(mViewPager, R.string.reception_summary_error, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    class Tabs {
        private static final int SUMMARY = 0;
        private static final int WATER_LEVEL = 1;
        private static final int TEMPERATURE = 2;
        private static final int SOIL_MOISTURE = 3;
        private static final int LUMINOSITY = 4;
        private static final int PLANT = 5;
        private static final int CONFIGURATION = 6;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            switch (position) {
                case Tabs.SUMMARY:
                    return TabFragmentSummary.newInstance(mServer, mUUID);
                case Tabs.WATER_LEVEL:
                    return TabFragmentWaterLevel.newInstance(mServer, mUUID);
                case Tabs.TEMPERATURE:
                    return TabFragmentTemperature.newInstance(mServer, mUUID);
                case Tabs.SOIL_MOISTURE:
                    return TabFragmentSoilMoisture.newInstance(mServer, mUUID);
                case Tabs.LUMINOSITY:
                    return TabFragmentLuminosity.newInstance(mServer, mUUID);
                case Tabs.PLANT:
                    return TabFragmentPlant.newInstance(mServer, mUUID);
                case Tabs.CONFIGURATION:
                    return TabFragmentPotConfiguration.newInstance(mServer, mUUID);
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show total pages.
            return 7;
        }
    }

    // Hide date if on Summary and Configuration tabs, update icons transparency
    private void updateToolbarAndTablayout(int position) {
        if(position == Tabs.SUMMARY || position == Tabs.PLANT || position == Tabs.CONFIGURATION) {
            // Hide date on Summary and Configuration tabs
            mPreviousPeriod.setVisible(false);
            mCurrentPeriod.setVisible(false);
            mNextPeriod.setVisible(false);
            mCurrentPeriodMode.setVisible(false);
        } else {
            // Show date on others
            mPreviousPeriod.setVisible(true);
            mCurrentPeriod.setVisible(true);
            mNextPeriod.setVisible(true);
            mCurrentPeriodMode.setVisible(true);
        }

        for(int i=0; i < mSectionsPagerAdapter.getCount(); i++) {
            if(i == position) {
                mTabLayout.getTabAt(i).getIcon().setAlpha(255);
            } else {
                mTabLayout.getTabAt(i).getIcon().setAlpha(160);
            }
        }
    }

    // Switch to the good tab when the user press on a CardView
    public void cardClick(View v) {
        switch(v.getId()) {
            case R.id.water_level_card:
                mViewPager.setCurrentItem(Tabs.WATER_LEVEL);
                break;
            case R.id.temperature_card:
                mViewPager.setCurrentItem(Tabs.TEMPERATURE);
                break;
            case R.id.soil_moisture_card:
                mViewPager.setCurrentItem(Tabs.SOIL_MOISTURE);
                break;
            case R.id.luminosity_card:
                mViewPager.setCurrentItem(Tabs.LUMINOSITY);
                break;
            case R.id.last_watering_card:
                if(mWaterDate != null) {
                    //mPeriodMode = DateMode.DAY;
                    setDate(mWaterDate.getTime());
                }
                mViewPager.setCurrentItem(Tabs.SOIL_MOISTURE);
                break;
            case R.id.plant_card:
                mViewPager.setCurrentItem(Tabs.PLANT);
                break;
        }
    }

    // Water plant
    public void waterPlant(View v) {
        long time = (mSharedPreferences.getLong("block_water_command_until", 0) - Calendar.getInstance().getTimeInMillis())/1000;

        if(time > 0) {
            long hours = time / 3600;
            long minutes = (time % 3600 ) / 60;
            long seconds = (time % 3600 ) % 60;

            Snackbar.make(mViewPager, String.format(getString(R.string.command_water_wait), hours, minutes, seconds), Snackbar.LENGTH_LONG).show();
            return;
        }

        SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);

        Calendar datetime = Calendar.getInstance();
        datetime.setTimeZone(TimeZone.getTimeZone("UTC"));

        HashMap<String, Object> command = new HashMap<>();
        command.put("type", "water");
        command.put("status", "new");
        command.put("datetime", outputDateFormat.format(datetime.getTime()));

        HashMap<String, Object> commands = new HashMap<>();
        commands.put("commands", command);

        JSONObject jsonRequest = new JSONObject(commands);
        EventBus.getDefault().post(new CommunicationManager.ConfAndCommandsRequest(mServer, mUUID, jsonRequest, "command.water"));
    }

    class PreferenceChanged {
        public final String key;
        public final boolean failed;

        public PreferenceChanged(String key, boolean failed) {
            this.key = key;
            this.failed = failed;
        }
    }

    // Send configuration change to server, update plant and broadcast change
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(mConfigTypeMap.containsKey(key)) {
            HashMap<String, Object> option = new HashMap<>();
            switch (mConfigTypeMap.get(key)) {
                case "int":
                    option.put(key, sharedPreferences.getInt(key, 0));
                    break;
                case "string":
                    option.put(key, sharedPreferences.getString(key, null));
                    break;
            }

            HashMap<String, Object> configuration = new HashMap<>();
            configuration.put("configuration", option);

            JSONObject jsonRequest = new JSONObject(configuration);
            EventBus.getDefault().post(new CommunicationManager.ConfAndCommandsRequest(mServer, mUUID, jsonRequest, "configuration/" + key));

            if(mConfigurationBeingSent == 0) {
                mConfigurationSnackbar.show();
            }

            mConfigurationBeingSent++;
        }
    }

    @Subscribe
    public void handleConfAndCommandsData(CommunicationManager.ConfAndCommandsDataReady event) {
        if (event.hint.startsWith("configuration")) {
            String key = event.hint.split("/")[1];

            mConfigurationBeingSent--;

            if(mConfigurationBeingSent == 0) {
                mConfigurationSnackbar.dismiss();
            }

            if (event.response != null) {
                Snackbar.make(mViewPager, R.string.configuration_updated, Snackbar.LENGTH_LONG).show();

                if(key.equals("plant")) {
                    mPlant = new Plant(mPlants, mSharedPreferences.getString("plant", "Unspecified"));
                }

                EventBus.getDefault().post(new PreferenceChanged(key, false));
            } else {
                Snackbar.make(mViewPager, R.string.configuration_update_error, Snackbar.LENGTH_LONG).show();

                EventBus.getDefault().post(new PreferenceChanged(key, true));
            }
        } else if(event.hint.equals("command/water")) {
            if (event.response != null) {
                long time = Long.decode(mSharedPreferences.getString("sending_interval", "0"));

                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putLong("block_water_command_until", Calendar.getInstance().getTimeInMillis() + time*1000);
                editor.apply();

                long hours = time / 3600;
                long minutes = (time % 3600 ) / 60;
                long seconds = (time % 3600 ) % 60;

                Snackbar.make(mViewPager, String.format(getString(R.string.command_water_sent), hours, minutes, seconds), Snackbar.LENGTH_LONG).show();
            } else {
                Snackbar.make(mViewPager, R.string.command_water_error, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private enum PeriodMode {
        DAY, WEEK, MONTH
    }

    private void forwardDateByOne() {
        // Add one
        switch(mPeriodMode) {
            case DAY:
                mDate.add(Calendar.DAY_OF_MONTH, 1);
                break;
            case WEEK:
                mDate.add(Calendar.WEEK_OF_YEAR, 1);
                break;
            case MONTH:
                mDate.add(Calendar.MONTH, 1);
                break;
        }

        updateFromToDates();
        updateDateInToolbar();
    }

    private void backwardDateByOne() {
        // Remove one day
        switch(mPeriodMode) {
            case DAY:
                mDate.add(Calendar.DAY_OF_MONTH, -1);
                break;
            case WEEK:
                mDate.add(Calendar.WEEK_OF_YEAR, -1);
                break;
            case MONTH:
                mDate.add(Calendar.MONTH, -1);
                break;
        }

        updateFromToDates();
        updateDateInToolbar();
    }

    public void setDate(Date date) {
        mDate.setTime(date);

        updateFromToDates();
        updateDateInToolbar();
    }

    private void updateFromToDates() {
        mFromDate = (Calendar) mDate.clone();

        // Set everything more specific than day to zero
        mFromDate.set(Calendar.HOUR_OF_DAY, 0);
        mFromDate.set(Calendar.MINUTE, 0);
        mFromDate.set(Calendar.SECOND, 0);
        mFromDate.set(Calendar.MILLISECOND, 0);

        switch(mPeriodMode) {
            case DAY:
                mToDate = (Calendar) mFromDate.clone();
                mToDate.add(Calendar.DAY_OF_MONTH, 1);
                break;
            case WEEK:
                mFromDate.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                mToDate = (Calendar) mFromDate.clone();
                mToDate.add(Calendar.WEEK_OF_YEAR, 1);
                break;
            case MONTH:
                mFromDate.set(Calendar.DAY_OF_MONTH, 1);
                mToDate = (Calendar) mFromDate.clone();
                mToDate.add(Calendar.MONTH, 1);
                break;
        }
    }

    private void updateDateInToolbar() {
        switch(mPeriodMode) {
            case DAY:
                mCurrentPeriod.setTitle(mDateFormatDay.format(mDate.getTime()));
                mCurrentPeriodMode.setTitle(R.string.period_mode_day);
                break;
            case WEEK:
                mCurrentPeriod.setTitle(mDateFormatWeek.format(mDate.getTime()));
                mCurrentPeriodMode.setTitle(R.string.period_mode_week);
                break;
            case MONTH:
                mCurrentPeriod.setTitle(mDateFormatMonth.format(mDate.getTime()));
                mCurrentPeriodMode.setTitle(R.string.period_mode_month);
                break;
        }
    }

    public Calendar getFromDate() {
        return mFromDate;
    }

    public Calendar getToDate() {
        return mToDate;
    }

    public SimpleDateFormat getDateFormat() {
        switch(mPeriodMode) {
            case DAY:
                return mDateFormatDay;
            case WEEK:
                return mDateFormatWeek;
            case MONTH:
                return mDateFormatMonth;
        }
        return null;
    }

    public SimpleDateFormat getAxisDateFormat() {
        switch(mPeriodMode) {
            case DAY:
                return mAxisDateFormatDay;
            case WEEK:
                return mAxisDateFormatWeek;
            case MONTH:
                return mAxisDateFormatMonth;
        }
        return null;
    }

    public int getLabelCount() {
        switch(mPeriodMode) {
            case DAY:
                return 9;
            case WEEK:
                return 8;
            case MONTH:
                return 10;
        }
        return 0;
    }

    public void setWaterDate(Date date) {
        mWaterDate = GregorianCalendar.getInstance();
        mWaterDate.setTime(date);
    }

    public SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    public Plant getPlant() {
        return mPlant;
    }
}
