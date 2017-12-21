package ch.epfl.pdse.polypotapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
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
import java.util.Map;
import java.util.TimeZone;

public class ActivityMain extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    private String mName;
    private String mServer;
    private String mUUID;
    private SharedPreferences mSharedPreferences;
    private Map<String, Object> mPreviousPreferences;

    private NotificationManager mNotificationManager;

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
    private Calendar mStatsFromDate;
    private Calendar mStatsToDate;
    private Calendar mFromDate;
    private Calendar mToDate;

    private boolean mFirstDateChange;
    private boolean mFirstConfigLoad;

    private HashMap<String, Float> mSensorsData;
    private HashMap<String, Calendar> mDatesData;
    private HashMap<String, Float> mStats;

    private HashMap<String, String> mConfigTypeMap;
    private Snackbar mConfigurationSnackbar;
    private int mConfigurationBeingSent;

    private Plant mPlant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle b = getIntent().getExtras();
        mName = b.getString("name");
        mServer = b.getString("server");
        mUUID = b.getString("uuid");

        mSharedPreferences = getSharedPreferences(Pot.getPreferenceName(mServer, mUUID), MODE_PRIVATE);
        mPreviousPreferences = (Map<String, Object>) mSharedPreferences.getAll();

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

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

        Plant.getPlantsList(getResources().openRawResource(R.raw.plants));
        mPlant = new Plant(getSharedPreferences().getString("plant", "Unspecified"));

        Ad.getAdsList(getResources().openRawResource(R.raw.ads));

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

        // Restore dates
        mPeriodMode = PeriodMode.valueOf(savedInstanceState.getString("period_mode"));
        mDate = GregorianCalendar.getInstance();
        mDate.setTimeInMillis(savedInstanceState.getLong("date"));

        mFromDate = GregorianCalendar.getInstance();
        mToDate = GregorianCalendar.getInstance();
        updateFromToDates();

        mStatsFromDate = GregorianCalendar.getInstance();
        mStatsFromDate.setTimeInMillis(savedInstanceState.getLong("stats_from_date"));
        mStatsToDate = GregorianCalendar.getInstance();
        mStatsToDate.setTimeInMillis(savedInstanceState.getLong("stats_to_date"));

        // Restore first date change and config load state
        mFirstDateChange = savedInstanceState.getBoolean("first_date_change");
        mFirstConfigLoad = savedInstanceState.getBoolean("first_config_load");

        // Restore tab
        mViewPager.setCurrentItem(savedInstanceState.getInt("active_tab"));
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

            mFromDate = GregorianCalendar.getInstance();
            mToDate = GregorianCalendar.getInstance();
            updateFromToDates();

            mStatsFromDate = GregorianCalendar.getInstance();
            mStatsFromDate.add(Calendar.WEEK_OF_YEAR, -1);
            mStatsToDate = GregorianCalendar.getInstance();

            CommunicationManager.getDefault().clearCache();
            EventBus.getDefault().removeAllStickyEvents();

            // Register to EventBus (need to be after all dates initialisation)
            EventBus.getDefault().register(this);

            EventBus.getDefault().post(new CommunicationManager.LatestRequest(mServer, mUUID));
            EventBus.getDefault().post(new CommunicationManager.DataRequest(mServer, mUUID, mFromDate.getTime(), mToDate.getTime()));
        } else {
            // Register to EventBus
            EventBus.getDefault().register(this);
        }
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
        savedInstanceState.putInt("active_tab", mViewPager.getCurrentItem());

        // Save first date change and config load state
        savedInstanceState.putBoolean("first_date_change", mFirstDateChange);
        savedInstanceState.putBoolean("first_config_load", mFirstConfigLoad);

        // Save the dates
        savedInstanceState.putString("period_mode", mPeriodMode.toString());
        savedInstanceState.putLong("date", mDate.getTimeInMillis());

        savedInstanceState.putLong("stats_form_date", mStatsFromDate.getTimeInMillis());
        savedInstanceState.putLong("stats_to_date", mStatsToDate.getTimeInMillis());

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onPause() {
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);

        EventBus.getDefault().unregister(this);

        mNotificationManager.cancel(1);
        mNotificationManager.cancel(2);
        mNotificationManager.cancel(3);
        super.onPause();
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
                EventBus.getDefault().post(new CommunicationManager.DataRequest(mServer, mUUID, mFromDate.getTime(), mToDate.getTime()));
                return true;

            case R.id.current_day:
                // Let the user pick the date
                DialogFragmentDatePicker datePicker = DialogFragmentDatePicker.newInstance(mServer, mUUID);
                datePicker.show(getSupportFragmentManager(), "datePicker");
                return true;

            case R.id.next_day:
                forwardDateByOne();

                // Update data and graphs
                EventBus.getDefault().post(new CommunicationManager.DataRequest(mServer, mUUID, mFromDate.getTime(), mToDate.getTime()));
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
                EventBus.getDefault().post(new CommunicationManager.DataRequest(mServer, mUUID, mFromDate.getTime(), mToDate.getTime()));
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

    /********** Communication **********/

    public void forceRefresh() {
        CommunicationManager.getDefault().clearCache();
        EventBus.getDefault().post(new CommunicationManager.LatestRequest(mServer, mUUID));
        EventBus.getDefault().post(new CommunicationManager.DataRequest(mServer, mUUID, mFromDate.getTime(), mToDate.getTime()));
    }

    @Subscribe(sticky = true, priority = 1)
    public void handleLatestResponse(CommunicationManager.LatestResponse event) {
        if(event.response == null) {
            Snackbar.make(mViewPager, R.string.reception_data_error, Snackbar.LENGTH_LONG).show();
            mSensorsData = null;
            mDatesData = null;

            EventBus.getDefault().post(new CommunicationManager.StatsRequest(mServer, mUUID, mStatsFromDate.getTime(), mStatsToDate.getTime()));
            return;
        }

        mSensorsData = new HashMap<>();
        mDatesData = new HashMap<>();

        data: try {
            // Data part
            JSONObject data = event.response.getJSONObject("data");

            if(data.isNull("water_level")) {
                Snackbar.make(mViewPager, R.string.parsing_no_data, Snackbar.LENGTH_LONG).show();

                mSensorsData = null;
                break data;
            }

            mSensorsData.put("water_level", Float.parseFloat(data.getString("water_level")));
            mSensorsData.put("temperature", Float.parseFloat(data.getString("temperature")));
            mSensorsData.put("soil_moisture", Float.parseFloat(data.getString("soil_moisture")));
            mSensorsData.put("luminosity", Float.parseFloat(data.getString("luminosity")));
            mSensorsData.put("battery_level", Float.parseFloat(data.getString("battery_level")));

            // Date and Time
            SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            inputDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            Calendar latestDataDate = GregorianCalendar.getInstance();
            latestDataDate.setTime(inputDateFormat.parse(data.getString("datetime")));
            latestDataDate.setTimeZone(TimeZone.getDefault());

            mDatesData.put("latest", latestDataDate);

            if(mFirstDateChange) {
                // Only switch the toolbar date once
                setDate(latestDataDate.getTime());
                EventBus.getDefault().post(new CommunicationManager.DataRequest(mServer, mUUID, mFromDate.getTime(), mToDate.getTime()));
                mFirstDateChange = false;
            }

            mStatsFromDate.setTime(latestDataDate.getTime());
            mStatsFromDate.add(Calendar.WEEK_OF_YEAR, -1);
            mStatsToDate.setTime(latestDataDate.getTime());
            mStatsToDate.add(Calendar.SECOND, 1);

            // Notifications
            if(mSensorsData.get("water_level") < 25) {
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), 0);
                String message = String.format(getString(R.string.notif_water_level_text), mSensorsData.get("water_level"));

                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(this, "PolyPotNotificationChannel")
                                .setSmallIcon(R.drawable.ic_pot)
                                .setContentTitle(mName + " - " + getString(R.string.notif_water_level_title))
                                .setContentText(message)
                                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                                .setAutoCancel(true)
                                .setContentIntent(pendingIntent);

                mNotificationManager.notify(1, builder.build());
            } else {
                mNotificationManager.cancel(1);
            }

            if(mSensorsData.get("battery_level") < 25) {
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), 0);
                String message = String.format(getString(R.string.notif_battery_level_text), mSensorsData.get("battery_level"));

                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(this, "PolyPotNotificationChannel")
                                .setSmallIcon(R.drawable.ic_pot)
                                .setContentTitle(mName + " - " + getString(R.string.notif_battery_level_title))
                                .setContentText(message)
                                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                                .setAutoCancel(true)
                                .setContentIntent(pendingIntent);

                mNotificationManager.notify(2, builder.build());
            } else {
                mNotificationManager.cancel(2);
            }

            Calendar limitDate = GregorianCalendar.getInstance();
            limitDate.add(Calendar.DAY_OF_MONTH, -1);

            if(latestDataDate.before(limitDate)) {
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), 0);
                String message = getString(R.string.notif_data_text);

                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(this, "PolyPotNotificationChannel")
                                .setSmallIcon(R.drawable.ic_pot)
                                .setContentTitle(mName + " - " + getString(R.string.notif_data_title))
                                .setContentText(message)
                                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                                .setAutoCancel(true)
                                .setContentIntent(pendingIntent);

                mNotificationManager.notify(3, builder.build());
            } else {
                mNotificationManager.cancel(3);
            }
        } catch (NullPointerException | JSONException | ParseException e) {
            Snackbar.make(mViewPager, R.string.parsing_latest_error, Snackbar.LENGTH_LONG).show();
        }

        EventBus.getDefault().post(new CommunicationManager.StatsRequest(mServer, mUUID, mStatsFromDate.getTime(), mStatsToDate.getTime()));

        try {
            // Command part
            JSONArray commands = event.response.getJSONArray("commands");

            SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            inputDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            // Search last watering and display it
            for (int i = 0; i < commands.length(); i++) {
                JSONObject command = commands.getJSONObject(i);
                if (command.getString("type").equals("water")) {
                    Calendar lastWateringDate = GregorianCalendar.getInstance();
                    lastWateringDate.setTime(inputDateFormat.parse(command.getString("datetime")));
                    lastWateringDate.setTimeZone(TimeZone.getDefault());

                    mDatesData.put("watering", lastWateringDate);
                }
            }
        } catch (NullPointerException | JSONException | ParseException e) {
            Snackbar.make(mViewPager, R.string.parsing_watering_error, Snackbar.LENGTH_LONG).show();
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

                mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
                editor.apply();
                mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

                // Only update the local configuration once
                mFirstConfigLoad = false;
            } catch (NullPointerException | JSONException e) {
                Snackbar.make(mViewPager, R.string.parsing_configuration_error, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    public HashMap<String, Float> getSensorsData() {
        return mSensorsData;
    }

    public HashMap<String, Calendar> getDatesData() {
        return mDatesData;
    }

    @Subscribe(sticky = true, priority = 1)
    public void handleStatsResponse(CommunicationManager.StatsResponse event) {
        if(event.response == null) {
            Snackbar.make(mViewPager, R.string.reception_data_error, Snackbar.LENGTH_LONG).show();
            mStats = null;
            return;
        }

        mStats = new HashMap<>();

        try {
            JSONArray data = event.response.getJSONArray("data");

            if(data.length() == 0) {
                mStats = null;
                return;
            }

            float waterLevel = 0;
            float temperature = 0;
            float soilMoisture = 0;
            float luminosity = 0;

            for(int i = 0; i<data.length();i++)
            {
                JSONObject point = data.getJSONObject(i);

                waterLevel += Float.parseFloat(point.getString("water_level"));
                temperature += Float.parseFloat(point.getString("temperature"));
                soilMoisture += Float.parseFloat(point.getString("soil_moisture"));
                luminosity += Float.parseFloat(point.getString("luminosity"));
            }

            mStats.put("water_level", waterLevel/data.length());
            mStats.put("temperature", temperature/data.length());
            mStats.put("soil_moisture", soilMoisture/data.length());
            mStats.put("luminosity", luminosity/data.length());
        } catch (NullPointerException | JSONException e) {
            Snackbar.make(mViewPager, R.string.parsing_stats_error, Snackbar.LENGTH_LONG).show();
        }
    }

    public HashMap<String, Float> getStats() {
        return mStats;
    }

    /********** Tabs **********/

    class Tabs {
            private static final int SUMMARY = 0;
            private static final int WATER_LEVEL = 1;
            private static final int TEMPERATURE = 2;
            private static final int SOIL_MOISTURE = 3;
            private static final int LUMINOSITY = 4;
            private static final int PLANT = 5;
            private static final int CONFIGURATION = 6;
        }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            switch (position) {
                case Tabs.SUMMARY:
                    return new TabFragmentSummary();
                case Tabs.WATER_LEVEL:
                    return new TabFragmentWaterLevel();
                case Tabs.TEMPERATURE:
                    return new TabFragmentTemperature();
                case Tabs.SOIL_MOISTURE:
                    return new TabFragmentSoilMoisture();
                case Tabs.LUMINOSITY:
                    return new TabFragmentLuminosity();
                case Tabs.PLANT:
                    return new TabFragmentPlant();
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
    public void cardClick(View view) {
        switch(view.getId()) {
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
                if(mDatesData != null && mDatesData.get("watering") != null) {
                    //mPeriodMode = DateMode.WEEK;
                    setDate(mDatesData.get("watering").getTime());
                    EventBus.getDefault().post(new CommunicationManager.DataRequest(mServer, mUUID, mFromDate.getTime(), mToDate.getTime()));
                }
                mViewPager.setCurrentItem(Tabs.SOIL_MOISTURE);
                break;
            case R.id.plant_card:
                mViewPager.setCurrentItem(Tabs.PLANT);
                break;
        }
    }

    public void adClick(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(view.getTag().toString()));
        startActivity(browserIntent);
    }

    /********** Commands **********/

    // Water plant button handling
    public void waterPlant(View v) {
        long time = (mSharedPreferences.getLong("block_water_command_until", 0) - GregorianCalendar.getInstance().getTimeInMillis())/1000;

        if(time > 0) {
            long hours = time / 3600;
            long minutes = (time % 3600 ) / 60;
            long seconds = (time % 3600 ) % 60;

            Snackbar.make(mViewPager, String.format(getString(R.string.command_water_wait), hours, minutes, seconds), Snackbar.LENGTH_LONG).show();
            return;
        }

        SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);

        Calendar datetime = GregorianCalendar.getInstance();
        datetime.setTimeZone(TimeZone.getTimeZone("UTC"));

        HashMap<String, Object> command = new HashMap<>();
        command.put("type", "water");
        command.put("status", "new");
        command.put("datetime", outputDateFormat.format(datetime.getTime()));

        HashMap<String, Object> commands = new HashMap<>();
        commands.put("commands", command);

        JSONObject jsonRequest = new JSONObject(commands);
        EventBus.getDefault().post(new CommunicationManager.CommandsRequest(mServer, mUUID, jsonRequest, "water"));
    }

    @Subscribe(priority = 1)
    public void handleCommandsResponse(CommunicationManager.CommandsResponse event) {
        if(event.key.equals("water")) {
            if (event.response != null) {
                long time = Long.decode(mSharedPreferences.getString("sending_interval", "0"));

                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putLong("block_water_command_until", GregorianCalendar.getInstance().getTimeInMillis() + time*1000);
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

    /********** Configuration **********/

    // Send configuration change to server, update plant
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(mConfigTypeMap.containsKey(key)) {
            HashMap<String, Object> option = new HashMap<>();
            option.put(key, sharedPreferences.getAll().get(key));

            HashMap<String, Object> configuration = new HashMap<>();
            configuration.put("configuration", option);

            JSONObject jsonRequest = new JSONObject(configuration);
            EventBus.getDefault().post(new CommunicationManager.ConfigurationRequest(mServer, mUUID, jsonRequest, key, option.get(key)));

            if(mConfigurationBeingSent == 0) {
                mConfigurationSnackbar.show();
            }

            mConfigurationBeingSent++;
        }
    }

    @Subscribe(priority = 1)
    public void handleConfigurationResponse(CommunicationManager.ConfigurationResponse event) {
        mConfigurationBeingSent--;


        if (event.response != null) {
            Snackbar.make(mViewPager, R.string.configuration_updated, Snackbar.LENGTH_LONG).addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar snackbar, int event) {
                    if(mConfigurationBeingSent != 0) {
                        mConfigurationSnackbar.show();
                    }
                }
            }).show();

            mPreviousPreferences.put(event.key, event.value);

            if(event.key.equals("plant")) {
                mPlant = new Plant(mSharedPreferences.getString("plant", "Unspecified"));
            }
        } else {
            Snackbar.make(mViewPager, R.string.configuration_update_error, Snackbar.LENGTH_LONG).addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar snackbar, int event) {
                    if(mConfigurationBeingSent != 0) {
                        mConfigurationSnackbar.show();
                    }
                }
            }).show();

            SharedPreferences.Editor editor = mSharedPreferences.edit();
            switch (mConfigTypeMap.get(event.key)) {
                case "int":
                    editor.putInt(event.key, (Integer) mPreviousPreferences.get(event.key));
                    break;
                case "string":
                    editor.putString(event.key, (String) mPreviousPreferences.get(event.key));
                    break;
            }

            mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
            editor.apply();
            mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
        }
    }

    /********** Date and time management **********/

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
        mFromDate.setTime(mDate.getTime());

        // Set everything more specific than day to zero
        mFromDate.set(Calendar.HOUR_OF_DAY, 0);
        mFromDate.set(Calendar.MINUTE, 0);
        mFromDate.set(Calendar.SECOND, 0);
        mFromDate.set(Calendar.MILLISECOND, 0);

        switch(mPeriodMode) {
            case DAY:
                mToDate.setTime(mFromDate.getTime());
                mToDate.add(Calendar.DAY_OF_MONTH, 1);
                break;
            case WEEK:
                mFromDate.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                mToDate.setTime(mFromDate.getTime());
                mToDate.add(Calendar.WEEK_OF_YEAR, 1);
                break;
            case MONTH:
                mFromDate.set(Calendar.DAY_OF_MONTH, 1);
                mToDate.setTime(mFromDate.getTime());
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

    public Date getFromDate() {
        return mFromDate.getTime();
    }

    public Date getToDate() {
        return mToDate.getTime();
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

    /********** Misc **********/

    public SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    public Plant getPlant() {
        return mPlant;
    }
}
