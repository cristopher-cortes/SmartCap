package com.example.henryforce.navexample;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.util.ArrayList;

/**
 * Created by Henryforce on 7/15/15.
 */
public class SensorListFragment extends ListFragment{
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        /*String[] values = new String[] { "Android", "iPhone", "WindowsMobile",
                "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
                "Linux", "OS/2" };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, values);
        setListAdapter(adapter);*/

        loadDBTask task = new loadDBTask();
        task.execute();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // TODO implement some logic
    }

    private class loadDBTask extends AsyncTask<Void, Void, Void> {

        private ArrayList<SensorData> dataList;

        @Override
        public Void doInBackground(Void... a1) {

            SensorDataDBHandler dbHandler = new SensorDataDBHandler(SensorListFragment.this.getActivity(), "SensorData.sqlite", null, 1);

            dataList = dbHandler.getAllData();

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

            String[] strings = new String[dataList.size()];

            for(int i = 0; i < dataList.size(); i++){
                SensorData auxData = dataList.get(i);
                strings[i] = String.valueOf(auxData.latitude) + "," +
                        String.valueOf(auxData.longitude) + ",  " + String.valueOf(auxData.uv) + "," +
                        String.valueOf(auxData.co) + "," + String.valueOf(auxData.no2) + "," +
                        String.valueOf(auxData.o3) +",  "+ auxData.time;

            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_list_item_1, strings);
            setListAdapter(adapter);
        }
    }
}
