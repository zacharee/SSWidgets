package com.zacharee1.sswidgets.weather;

public interface WeatherListener
{
    void onWeatherInfoFound(WeatherInfo info);
    void onWeatherConnectionFailed(String message);
}
