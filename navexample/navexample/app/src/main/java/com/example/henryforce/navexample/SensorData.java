package com.example.henryforce.navexample;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Henryforce on 7/10/15.
 */
public class SensorData {

    //Date date = new Date();

    // obtener la hora
    //DateFormat hour = new SimpleDateFormat("HH:mm:ss");
    //String time = String.valueOf(hour.format(date));

    /**   DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
     System.out.println("Fecha: "+dateFormat.format(date)); */

    public String time;

    public int id;
    public int uv, co, no2, o3, ppm, hum, temp;
    public float latitude, longitude;
    //public long time;
}
