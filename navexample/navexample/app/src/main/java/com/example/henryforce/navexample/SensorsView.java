package com.example.henryforce.navexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by Henryforce on 7/19/15.
 */
public class SensorsView extends Fragment{
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.sensorsview, container, false);

        Button button1 = (Button)rootView.findViewById(R.id.gr1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Toast.makeText(getActivity(), "ORBE", Toast.LENGTH_SHORT).show();

                Intent downloadIntent = new Intent(getActivity(), GraphActivity.class);
                getActivity().startActivity(downloadIntent);
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(0);
    }
}
