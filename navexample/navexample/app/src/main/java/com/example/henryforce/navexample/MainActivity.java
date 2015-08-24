package com.example.henryforce.navexample;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, LocationListener {

    // Intent request codes
    static final int REQUEST_CONNECT_DEVICE = 1;
    static final int REQUEST_ENABLE_BT = 2;

    // Message types sent from the DeviceConnector Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    BluetoothAdapter btAdapter;

    private static DeviceConnector connector;
    private static BluetoothResponseHandler mHandler;

    private static final String SAVED_PENDING_REQUEST_ENABLE_BT = "PENDING_REQUEST_ENABLE_BT";
    // do not resend request to enable Bluetooth
    // if there is a request already in progress
    // See: https://code.google.com/p/android/issues/detail?id=24931#c1
    boolean pendingRequestEnableBt = false;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    public static FragmentManager fragmentManager;

    protected LocationManager locationManager;
    protected LocationListener locationListener;

    private float longitudeGPS;
    private float latitudeGPS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        //make sure bluetooth works
        if (savedInstanceState != null) {
            pendingRequestEnableBt = savedInstanceState.getBoolean(SAVED_PENDING_REQUEST_ENABLE_BT);
        }
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            final String no_bluetooth = "No bluetooth support!";
            //showAlertDialog(no_bluetooth);
            Toast.makeText(this, no_bluetooth, Toast.LENGTH_SHORT).show();
            //Utils.log(no_bluetooth);
        }

        if (mHandler == null) mHandler = new BluetoothResponseHandler(this);
        else mHandler.setTarget(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        //txtLat = (TextView) findViewById(R.id.textview1);
        //txtLat.setText("Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude());
        //Toast.makeText(this, "Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude(), Toast.LENGTH_SHORT).show();
        latitudeGPS = (float)location.getLatitude();
        longitudeGPS = (float)location.getLongitude();

        Log.i("LatLong", String.valueOf(latitudeGPS) + "," + String.valueOf(longitudeGPS));
    }

    @Override
    public void onProviderDisabled(String provider) {
        //Log.d("Latitude","disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        //Log.d("Latitude","enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //Log.d("Latitude","status");
    }

    @Override
    public void onStart() {
        super.onStart();
        if (btAdapter == null) return;

        //if bluetooth is not turned on, request to turn it on
        if (!btAdapter.isEnabled() && !pendingRequestEnableBt) {
            pendingRequestEnableBt = true;
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }

    private boolean isConnected() {
        return (connector != null) && (connector.getState() == DeviceConnector.STATE_CONNECTED);
    }
    // ==========================================================================


    /**
     * Разорвать соединение
     */
    private void stopConnection() {
        if (connector != null) {
            connector.stop();
            connector = null;
            //deviceName = null;
        }
    }

    private void setupConnector(BluetoothDevice connectedDevice) {
        stopConnection();
        try {
            //String emptyName = getString(R.string.empty_device_name);
            String emptyName = "Empty Device";
            DeviceData data = new DeviceData(connectedDevice, emptyName);
            connector = new DeviceConnector(data, mHandler);
            connector.connect();
        } catch (IllegalArgumentException e) {
            Utils.log("setupConnector failed: " + e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    BluetoothDevice device = btAdapter.getRemoteDevice(address);
                    if (this.isAdapterReady() && (connector == null)) setupConnector(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                this.pendingRequestEnableBt = false;
                if (resultCode != Activity.RESULT_OK) {
                    Utils.log("BT not enabled");
                }
                break;
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments

        if(position == 0) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            /*fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();*/
            fragmentManager.beginTransaction().replace(R.id.container, new mapFragment()).commit();
        }else if(position == 1){
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.container, new SensorListFragment()).commit();
        }else if(position == 2){
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.container, new SensorsView()).commit();
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = "HeatMap";
                break;
            case 2:
                mTitle = "List all Raw Data";
                break;
            case 3:
                mTitle = "Display actual Sensor Data";
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            stopConnection();
            Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);

            return true;
        }else if(id == R.id.action_settings_2){

            //LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            //Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            //float longitude = (float)location.getLongitude();
            //float latitude = (float)location.getLatitude();

            //Log.i("LOC", String.valueOf(latitude) + "," + String.valueOf(longitude));
            //Toast.makeText(this, String.valueOf(latitude) + "," + String.valueOf(longitude), Toast.LENGTH_LONG).show();

            SensorData auxi = new SensorData();
            saveDBTask task = new saveDBTask();
            task.data = auxi;
            task.execute();

        }else if(id == R.id.action_settings_3){
            resetDBTask task = new resetDBTask();
            task.execute();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_PENDING_REQUEST_ENABLE_BT, pendingRequestEnableBt);
    }

    boolean isAdapterReady() {
        return (btAdapter != null) && (btAdapter.isEnabled());
    }

    private class BluetoothResponseHandler extends Handler {
        private WeakReference<MainActivity> mActivity;

        private String[] auxArray;
        private String auxString;
        boolean moreStrings = false;

        //sensor data
        SensorData data = new SensorData();

        public BluetoothResponseHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        public void setTarget(MainActivity target) {
            mActivity.clear();
            mActivity = new WeakReference<MainActivity>(target);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case MESSAGE_STATE_CHANGE:

                        Utils.log("MESSAGE_STATE_CHANGE: " + msg.arg1);
                        final ActionBar bar = activity.getSupportActionBar();
                        switch (msg.arg1) {
                            case DeviceConnector.STATE_CONNECTED:
                                bar.setSubtitle("Connected");
                                break;
                            case DeviceConnector.STATE_CONNECTING:
                                bar.setSubtitle("Connecting");
                                break;
                            case DeviceConnector.STATE_NONE:
                                bar.setSubtitle("No State");
                                break;
                        }
                        break;

                    case MESSAGE_READ:
                        final String readMessage = (String) msg.obj;

                        if(readMessage != null){

                            auxString = readMessage.trim();

                            saveDBTask task = new saveDBTask();
                            //task.data = data;
                            task.dataString = auxString;
                            task.execute();

                            if (readMessage != null) {
                                //activity.appendLog(readMessage, false, false, activity.needClean);
                                Log.i("DATA", readMessage);
                            }


                        }

                        break;

                    case MESSAGE_DEVICE_NAME:
                        //activity.setDeviceName((String) msg.obj);
                        break;

                    case MESSAGE_WRITE:
                        // stub
                        break;

                    case MESSAGE_TOAST:
                        // stub
                        break;
                }
            }
        }
    }

    private class saveDBTask extends AsyncTask<Void, Void, Void>{

        public SensorData data;
        public String dataString;

        @Override
        public Void doInBackground(Void... a1) {

            //data = new SensorData();
            //data.time = System.currentTimeMillis() / 1000L;

            try{
                if(data == null){
                    String[] auxArray = dataString.split(",");

                    Log.i("Size", String.valueOf(auxArray.length));

                    data = new SensorData();

                    data = new SensorData();
                    data.uv = Integer.parseInt(auxArray[0]);
                    data.co = Integer.parseInt(auxArray[1]);
                    data.no2 = Integer.parseInt(auxArray[2]);
                    data.o3 = Integer.parseInt(auxArray[3]);
                    data.ppm = Integer.parseInt(auxArray[4]);
                    data.hum = Integer.parseInt(auxArray[5]);
                    data.temp = Integer.parseInt(auxArray[6]);
                }
                Date date = new Date();
                // obtener la hora
                DateFormat hour = new SimpleDateFormat("HH:mm:ss");
                data.time = String.valueOf(hour.format(date));

                //LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                //Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                //data.longitude = (float)location.getLongitude();
                //data.latitude = (float)location.getLatitude();
                data.longitude = longitudeGPS;
                data.latitude = latitudeGPS;

                SensorDataDBHandler dbHandler = new SensorDataDBHandler(MainActivity.this, "SensorData.sqlite", null, 1);

                //data.hum = (int)(Math.random() * 1023);
                //data.co = (int)(Math.random() * 1023);

                dbHandler.newSensorData(data);



                dbHandler.close();
                /*SensorData auxData = new SensorData();
                auxData.time = 300;

                dbHandler.newSensorData(auxData);
                ArrayList<SensorData> dataList = dbHandler.getAllData();

                SensorData auxData2 = dataList.get(0);

                Log.i("DB S", String.valueOf(auxData2.time));*/

            }catch(Exception e){
                Log.e("Error", e.toString());
            }



            return null;
        }

        @Override
        public void onProgressUpdate(Void... a2) {

        }

        @Override
        public void onPostExecute(Void a3) {
            //showDialog("Downloaded " + result + " bytes");

            //Log.i("Execute", "Finished Execution");
        }
    }

    private class resetDBTask extends AsyncTask<Void, Void, Void>{

        @Override
        public Void doInBackground(Void... a1) {

            SensorDataDBHandler dbHandler = new SensorDataDBHandler(MainActivity.this, "SensorData.sqlite", null, 1);

            dbHandler.deleteAllData();

            dbHandler.close();

            return null;
        }

        @Override
        public void onProgressUpdate(Void... a2) {

        }

        @Override
        public void onPostExecute(Void a3) {

        }
    }

    private class loadDBTask extends AsyncTask<Void, Void, Void>{

        @Override
        public Void doInBackground(Void... a1) {

            SensorDataDBHandler dbHandler = new SensorDataDBHandler(MainActivity.this, "SensorData.sqlite", null, 1);



            dbHandler.close();

            return null;
        }

        @Override
        public void onProgressUpdate(Void... a2) {

        }

        @Override
        public void onPostExecute(Void a3) {
            //showDialog("Downloaded " + result + " bytes");

            //Log.i("Execute", "Finished Execution");
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}
