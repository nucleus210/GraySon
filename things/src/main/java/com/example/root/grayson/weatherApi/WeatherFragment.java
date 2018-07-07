package com.example.root.grayson.weatherApi;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.root.grayson.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class WeatherFragment extends Fragment {
    private static final String TAG = "WeatherModel Fragment";
    String mIconUrls = "http://openweathermap.org/img/w/";
    Handler handler;
    TextView mCityField;
    Typeface mWeatherFont;
    ListView mWeatherList;
    TextView mPressureField;
    ImageView mWeatherIconView;
    TextView mTemperatureField;
    WeatherAdapter mWeatherAdapter;
    static ArrayList<WeatherModel> weatherModels;

    public WeatherFragment() {
        handler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.weather_fragment, container, false);
        mTemperatureField = rootView.findViewById(R.id.current_temperature_field);
        mWeatherIconView = rootView.findViewById(R.id.weather_icon);
        mPressureField = rootView.findViewById(R.id.pressure_field);
        mWeatherList = rootView.findViewById(R.id.weather_list);
        mCityField = rootView.findViewById(R.id.city_field);
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"Weather forecast started.");
        mWeatherFont = Typeface.createFromAsset(getActivity().getAssets(),
                "fonts/pacifico_regular.ttf");
        updateWeatherData(new CityPreference(getActivity()).getCity());
    }

    private void updateWeatherData(final String city) {
        new Thread() {
            public void run() {
                final JSONObject json = RemoteFetch.getJSON(getActivity(), city);
                if (json == null) {
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getActivity(),
                                    getActivity().getString(R.string.place_not_found),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        public void run() {
                            renderWeather(json);
                            weekWeatherForecast(json);
                        }
                    });
                }
            }
        }.start();
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void renderWeather(JSONObject json) {
        String jsonString = json.toString();
        JSONObject jsonObject;
        JSONArray list;
        try {
            jsonObject = new JSONObject(jsonString);
            list = jsonObject.getJSONArray("list");
            JSONObject weatherData = list.getJSONObject(0);

            mTemperatureField.setText(String.format("%.2f", weatherData
                    .getJSONObject("main").getDouble("temp")) + " ℃");

            mPressureField.setText(String.format("%.2f", weatherData
                    .getJSONObject("main").getDouble("pressure")) + " hPa");

         //   DateFormat df = DateFormat.getDateTimeInstance();
          //  String updatedOn = df.format(new Date(json.getLong("dt_txt") * 1000));

           // mUpdatedField.setText("Last update: " + updatedOn);

            new DownloadIcons(mWeatherIconView).execute(mIconUrls + weatherData
                    .getJSONArray("weather").getJSONObject(0)
                    .getString("icon") + ".png");


            mCityField.setText(jsonObject.getJSONObject("city")
                    .getString("name") + ", " +
                    jsonObject.getJSONObject("city").getString("country"));

        } catch (Exception e) {
            Log.e("SimpleWeather", "One or more fields not found in the JSON data");
        }
    }

    @SuppressLint("DefaultLocale")
    private void weekWeatherForecast(JSONObject object) {
        String jsonString = object.toString();
        JSONObject jsonObject;
        JSONArray list;
        Date day;

        ArrayList<WeatherModel> weatherModels = new ArrayList<>();
        try {
            jsonObject = new JSONObject(jsonString);
            list = jsonObject.getJSONArray("list");

            for (int i = 0; i < list.length(); i++) {
                WeatherModel dayWeatherModel = new WeatherModel();
                JSONObject weatherData = list.getJSONObject(i);
                day = new Date();
                final Calendar date = Calendar.getInstance();
                date.setTimeInMillis(weatherData.getLong("dt") * 1000);
                day.getTime();

                        final Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(weatherData.getLong("dt") * 1000);
                        dayWeatherModel.day = cal.getTime();

                        dayWeatherModel.temperature = weatherData
                                .getJSONObject("main").getDouble("temp");
                        dayWeatherModel.humidity = weatherData
                                .getJSONObject("main").getDouble("humidity");
                        dayWeatherModel.pressure = (int) weatherData
                                .getJSONObject("main").getDouble("pressure");
                        dayWeatherModel.mWeatherIcon = weatherData
                                .getJSONArray("weather").getJSONObject(0)
                                .getInt("id");
                        dayWeatherModel.mWeatherIconUrl = mIconUrls + weatherData
                                .getJSONArray("weather").getJSONObject(0)
                                .getString("icon") + ".png";

                        dayWeatherModel.isFetched = true;
                        weatherModels.add(dayWeatherModel);
                    }

            mWeatherAdapter = new WeatherAdapter(getActivity(), weatherModels);
            mWeatherList.setAdapter(mWeatherAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String setWeatherIcon(int actualId, long sunrise, long sunset) {
        int id = actualId / 100;
        String icon = "";
        if (actualId == 800) {
            long currentTime = new Date().getTime();
            if (currentTime >= sunrise && currentTime < sunset) {
                icon = getActivity().getString(R.string.weather_sunny);
            } else {
                icon = getActivity().getString(R.string.weather_clear_night);
            }
        } else {
            switch (id) {
                case 2:
                    icon = getActivity().getString(R.string.weather_thunder);
                    break;
                case 3:
                    icon = getActivity().getString(R.string.weather_drizzle);
                    break;
                case 7:
                    icon = getActivity().getString(R.string.weather_foggy);
                    break;
                case 8:
                    icon = getActivity().getString(R.string.weather_cloudy);
                    break;
                case 6:
                    icon = getActivity().getString(R.string.weather_snowy);
                    break;
                case 5:
                    icon = getActivity().getString(R.string.weather_rainy);
                    break;
            }
        }
       // mWeatherIconView.setText(icon);
        return icon;
    }

    public void changeCity(String city) {
        updateWeatherData(city);
    }

    public double kelvinToCelsius(double kelvin) {
        return kelvin - 273.15;
    }
}