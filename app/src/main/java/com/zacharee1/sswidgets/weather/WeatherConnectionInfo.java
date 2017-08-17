package com.zacharee1.sswidgets.weather;

public class WeatherConnectionInfo
{
    public String latitude;
    public String longitude;
//    public String cityName;
//    public String stateName;
//    public String countryName;

    public WeatherListener weatherListener;

    public WeatherConnectionInfo(String lat, String lon, WeatherListener listener) {
        latitude = lat;
        longitude = lon;
        weatherListener = listener;
    }

//    public WeatherConnectionInfo(String city, String state, String country, WeatherListener listener) {
//        cityName = city;
//        stateName = state;
//        countryName = country;
//        weatherListener = listener;
//    }
}
