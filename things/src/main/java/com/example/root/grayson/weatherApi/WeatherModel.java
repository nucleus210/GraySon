package com.example.root.grayson.weatherApi;

import java.io.Serializable;
import java.util.Date;

public class WeatherModel implements Serializable {

    int    pressure;
    int    mWeatherIcon;
    double temperature;
    double humidity;
    double windSpeed;

    String mWeatherIconUrl;
    Date day;
    boolean isFetched;

    public WeatherModel() {
        // Default values
        day = new Date();
        humidity = windSpeed = pressure = 0;
        isFetched = false;
    }
}