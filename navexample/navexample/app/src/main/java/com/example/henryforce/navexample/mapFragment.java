package com.example.henryforce.navexample;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Henryforce on 7/9/15.
 */
public class mapFragment extends Fragment {

    private ArrayList<SensorData> dataList;
    private ArrayList<WeightedLatLng> latlngList, latlngList2;
    private ArrayList<LatLng> latlngListOnly;

    private static final String ARG_SECTION_NUMBER = "section_number";
    Context context;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private HeatmapTileProvider mProvider, mProvider2;
    private TileOverlay mOverlay;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.map_layout, container, false);

        //mMap = ((SupportMapFragment)this.getFragmentManager().findFragmentById(R.id.map)).getMap();

        context = this.getActivity();

        setUpMapIfNeeded();

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(0);
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            //mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
            //        .getMap();
            //mMap = ((SupportMapFragment)this.getFragmentManager().findFragmentById(R.id.map)).getMap();
            mMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMap();

            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                //setUpMap();
                //addHeatMap();

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(30.5836, -96.3424), 14.0f));

                loadDBTask task = new loadDBTask();
                task.execute();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker").visible(false));
    }

    private void addHeatMap() {
        ArrayList<LatLng> list = new ArrayList<LatLng>();

        // Get the data: latitude/longitude positions of police stations.
        /*try {
            list = readItems(R.raw.police_stations);
        } catch (JSONException e) {
            Toast.makeText(this, "Problem reading list of locations.", Toast.LENGTH_LONG).show();
        }*/
        LatLng lg1 = new LatLng(-37.1886, 145.708);
        LatLng lg2 = new LatLng(-37.8361, 144.845);
        LatLng lg3 = new LatLng(-38.4034, 144.192);

        list.add(lg1);
        list.add(lg2);
        list.add(lg3);

        // Create a heat map tile provider, passing it the latlngs of the police stations.
        mProvider = new HeatmapTileProvider.Builder()
                .data(list)
                .build();
        // Add a tile overlay to the map, using the heat map tile provider.
        mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
    }

    private class loadDBTask extends AsyncTask<Void, Void, Void> {

        @Override
        public Void doInBackground(Void... a1) {

            SensorDataDBHandler dbHandler = new SensorDataDBHandler(context, "SensorData.sqlite", null, 1);

            //if(dataList != null){
            //    dataList.clear();
            //}
            dataList = dbHandler.getAllData();

            dbHandler.close();

            //if(dataList == null) {
            latlngList = new ArrayList<WeightedLatLng>();
            latlngList2 = new ArrayList<WeightedLatLng>();
            latlngListOnly = new ArrayList<LatLng>();
            //}else{
                //latlngList.clear();
            //}
            for(int i = 0; i < dataList.size(); i++){
                SensorData auxData = dataList.get(i);
                LatLng auxil = new LatLng(auxData.latitude, auxData.longitude);
                WeightedLatLng lg1 = new WeightedLatLng(auxil, auxData.uv);
                WeightedLatLng lg2 = new WeightedLatLng(auxil, auxData.co);
                //Log.i("DATX", String.valueOf(auxData.latitude) + "," + String.valueOf(auxData.longitude));

                latlngList.add(lg1);
                latlngList2.add(lg2);
                latlngListOnly.add(auxil);
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

            // Create the gradient.
            int[] colors = {
                    Color.rgb(0, 0, 255), // green
                    Color.rgb(255, 0, 0)    // red
            };

            // Create the gradient.
            int[] colors2 = {
                    Color.rgb(102, 225, 0), // green
                    Color.rgb(255, 0, 0)    // red
            };

            float[] startPoints = {
                    0.2f, 1f
            };

            Gradient gradient = new Gradient(colors, startPoints);
            Gradient gradient2 = new Gradient(colors2, startPoints);


            if(latlngList.size() > 0) {

                mProvider = new HeatmapTileProvider.Builder()
                        //.data(latlngList)
                        .weightedData(latlngList)
                        .gradient(gradient)
                        .opacity(0.8)
                        .radius(50)
                        .build();
                mProvider2 = new HeatmapTileProvider.Builder()
                        .weightedData(latlngList2)
                        .gradient(gradient2)
                        .opacity(0.5)
                        .radius(20)
                        .build();
                // Add a tile overlay to the map, using the heat map tile provider.
                //mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
                mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
                mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider2));

                //setUpMap();
                BitmapDescriptor imgDes = BitmapDescriptorFactory.fromResource(R.drawable.transparente);

                for(int i = 0; i < latlngList.size(); i++){
                    //WeightedLatLng auxlatlng = latlngList2.get(i);
                    SensorData auxSense = dataList.get(i);
                    mMap.addMarker(new MarkerOptions().position(latlngListOnly.get(i))
                            .title(String.valueOf("UV:"+(auxSense.uv)+" CO:"+
                                    (auxSense.co)+" "+auxSense.time)).visible(true).icon(imgDes));
                }

            }
        }
    }
}
