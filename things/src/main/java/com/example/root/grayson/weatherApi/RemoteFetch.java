package com.example.root.grayson.weatherApi;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.example.root.grayson.R;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class RemoteFetch {
    private static final String TAG = "Remote fetch";
    private static final String OPEN_WEATHER_FORECAST_API =
            "http://api.openweathermap.org/data/2.5/forecast?q=%s&units=metric&lang={bg}";

    //private static final String OPEN_WEATHER_MAP_API =
    //        "http://api.openweathermap.org/data/2.5/weather?q=%s&units=metric";


    static JSONObject getJSON(Context context, String city) {
        Log.d(TAG,"Starting remote fetch");
        try {
            URL url = new URL(String.format(OPEN_WEATHER_FORECAST_API, city));
            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();

            connection.addRequestProperty("x-api-key",
                    context.getString(R.string.open_weather_maps_app_id));

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            //StringBuffer json = new StringBuffer(1024);

            StringBuilder json = new StringBuilder(1024);
            String tmp;
            while ((tmp = reader.readLine()) != null)
                json.append(tmp).append("\n");
            reader.close();

            JSONObject data = new JSONObject(json.toString());
            downloadAndStoreJson(data);
            // This value will be 404 if the request was not
            // successful
            if (data.getInt("cod") != 200) {
                return null;
            }

            return data;
        } catch (Exception e) {
            return null;
        }
    }

    private static void downloadAndStoreJson(JSONObject object) {
        String fileName = "weather.json";
        String jsonString = object.toString();
        byte[] jsonArray = jsonString.getBytes();
        String path = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM));

        File fileToSaveJson = new File(path, fileName);

        BufferedOutputStream bos;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(fileToSaveJson));
            bos.write(jsonArray);
            bos.flush();
            bos.close();

        } catch (IOException e) {
            Log.e(TAG,"Error on remote fetch");
        } finally {
            System.gc();
        }
    }
}
