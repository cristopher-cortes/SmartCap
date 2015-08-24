package com.example.henryforce.navexample;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Henryforce on 7/13/15.
 */
public class SensorDataDBHandler extends SQLiteOpenHelper {

    private SQLiteDatabase mainDB;

    public SensorDataDBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        // TODO Auto-generated method stub
        super(context, name, factory, version);
    }

    //Create table if it does not exist on DB
    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL("CREATE TABLE IF NOT EXISTS SensorData (Id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE," +
                "CO INTEGER," +
                "NO2 INTEGER," +
                "O3 INTEGER," +
                "Temperature INTEGER," +
                "Humidity INTEGER," +
                "UV INTEGER," +
                "PPM INTEGER," +
                "Latitude REAL," +
                "Longitude REAL," +
                "Time TEXT" +
                ")");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
        // TODO Auto-generated method stub

    }

    //Get all rows from SensorData table
    public ArrayList<SensorData> getAllData(){
        ArrayList<SensorData> dataList = new ArrayList<SensorData>();
        String[] columns = {"Id", "CO", "NO2", "O3", "Temperature", "Humidity",
                "UV", "PPM", "Latitude", "Longitude", "Time"};

        try{
            mainDB = this.getReadableDatabase();
            Cursor cursor = mainDB.query("SensorData", columns, null, null, null, null, null);

            if(cursor.moveToFirst()){
                do{
                    SensorData data = new SensorData();

                    data.id = cursor.getInt(0);
                    data.co = cursor.getInt(1);
                    data.no2 = cursor.getInt(2);
                    data.o3 = cursor.getInt(3);
                    data.temp = cursor.getInt(4);
                    data.hum = cursor.getInt(5);
                    data.uv = cursor.getInt(6);
                    data.ppm = cursor.getInt(7);
                    data.latitude = cursor.getFloat(8);
                    data.longitude = cursor.getFloat(9);
                    data.time = cursor.getString(10);

                    dataList.add(data);

                }while(cursor.moveToNext());

                cursor.close();
            }
            mainDB.close();
        }catch(SQLiteException e){
            Log.d("SQLiteException", e.toString());
        }finally{
            if(mainDB.isOpen()){
                mainDB.close();
            }
        }

        return dataList;
    }

    //Get SensorData based on Id
    public SensorData getSensorDataById(int id){
        SensorData data = new SensorData();

        String[] columns = {"CO", "NO2", "O3", "Temperature", "Humidity",
                "UV", "PPM", "Latitude", "Longitude", "Time"};
        String select = "Id = ?";
        String[] selection = { String.valueOf(id) };

        try{
            mainDB = this.getReadableDatabase();
            Cursor cursor = mainDB.query("SensorData", columns, select, selection, null, null, null);

            if(cursor.moveToFirst()){
                do{

                    data.id = id;
                    data.co = cursor.getInt(0);
                    data.no2 = cursor.getInt(1);
                    data.o3 = cursor.getInt(2);
                    data.temp = cursor.getInt(3);
                    data.hum = cursor.getInt(4);
                    data.uv = cursor.getInt(5);
                    data.ppm = cursor.getInt(6);
                    data.latitude = cursor.getFloat(7);
                    data.longitude = cursor.getFloat(8);
                    data.time = cursor.getString(9);

                }while(cursor.moveToNext());

                cursor.close();
            }
            mainDB.close();
        }catch(SQLiteException e){
            Log.d("SQLiteException", e.toString());
        }finally{
            if(mainDB.isOpen()){
                mainDB.close();
            }
        }

        return data;
    }

    //Create new SensorData row
    public void newSensorData(SensorData data){
        try{
            mainDB = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("CO", data.co);
            values.put("NO2", data.no2);
            values.put("O3", data.o3);
            values.put("Temperature", data.temp);
            values.put("Humidity", data.hum);
            values.put("UV", data.uv);
            values.put("Latitude", data.latitude);
            values.put("Longitude", data.longitude);
            values.put("PPM", data.ppm);
            values.put("Time", data.time);

            mainDB.insert("SensorData", "nullColumnHack", values);
            mainDB.close();
        }catch(SQLiteException sqlEx){
            Log.d("SQLiteException", sqlEx.toString());
        }finally{
            if(mainDB.isOpen()){
                mainDB.close();
            }
        }
    }

    public void deleteAllData(){
        try{
            mainDB = this.getWritableDatabase();

            mainDB.delete("SensorData", null, null);

            mainDB.close();
        }catch(SQLiteException sqlEx){
            Log.d("SQLiteException", sqlEx.toString());
        }finally{
            if(mainDB.isOpen()){
                mainDB.close();
            }
        }
    }
}
