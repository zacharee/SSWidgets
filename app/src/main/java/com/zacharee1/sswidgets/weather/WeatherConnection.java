package com.zacharee1.sswidgets.weather;

import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.JsonReader;

import com.zacharee1.sswidgets.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class WeatherConnection extends AsyncTask<WeatherConnectionInfo, Void, WeatherInfo>
{
    private WeatherListener mListener;
    private WeatherConnectionInfo mInfo;

    @Override
    protected WeatherInfo doInBackground(WeatherConnectionInfo... weatherConnectionInfos)
    {
        mInfo = weatherConnectionInfos[0];
        mListener = mInfo.weatherListener;

        String lat = mInfo.latitude;
        String lon = mInfo.longitude;

        WeatherInfo info = new WeatherInfo();

        try {
            InputStream stream = new URL("https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text%3D%22(" + lat + "%2C%20" + lon + ")%22)&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys").openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

            StringBuilder resultJson = new StringBuilder();
            String s;
            while ((s = reader.readLine()) != null) {
                resultJson.append(s).append("\n");
            }

            JSONObject object = new JSONObject(resultJson.toString()).getJSONObject("query").getJSONObject("results").getJSONObject("channel");
            JSONObject location = object.getJSONObject("location");
            JSONObject units = object.getJSONObject("units");
            JSONObject item = object.getJSONObject("item");
            JSONObject current = item.getJSONObject("condition");

            info.cityName = location.getString("city");
            info.stateName = location.getString("region");
            info.currentCondition = current.getString("text");
            info.currentTemp = current.getString("temp");
            info.currentTempUnit = units.getString("temperature");
            info.pubDate = item.getString("pubDate");
            info.iconRes = parseCode(Integer.decode(current.getString("code")));
            info.yahooUrl = item.getString("link").split("[*]")[1];

            if (info.yahooUrl == null || info.yahooUrl.isEmpty()) {
                info.yahooUrl = "https://www.yahoo.com/?ilc=401";
            }

        } catch (Exception e) {
            mListener.onWeatherConnectionFailed(e.getMessage());
            return null;
        }

        return info;
    }

    @Override
    protected void onPostExecute(WeatherInfo info)
    {
        if (info != null) {
            mListener.onWeatherInfoFound(info);
        } else {
            mListener.onWeatherConnectionFailed("Info was null");
        }
    }

    private int parseCode(int code) {
        switch (code) {
            case 0:
            case 1:
            case 2:
                return R.drawable.weather_windy;
            case 3:
            case 4:
                return R.drawable.weather_lightning;
            case 5:
            case 6:
            case 7:
                return R.drawable.weather_snowy_rainy;
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 40:
                return R.drawable.weather_rainy;
            case 13:
            case 14:
            case 15:
            case 16:
            case 41:
            case 42:
            case 43:
            case 46:
                return R.drawable.weather_snowy;
            case 17:
            case 18:
            case 35:
                return R.drawable.weather_hail;
            case 19:
                return R.drawable.weather_windy;
            case 20:
            case 21:
            case 22:
                return R.drawable.weather_fog;
            case 23:
            case 24:
                return R.drawable.weather_windy_variant;
            case 25:
                return R.drawable.snowflake;
            case 26:
            case 27:
            case 28:
                return R.drawable.weather_cloudy;
            case 29:
            case 30:
            case 44:
                return R.drawable.weather_partlycloudy;
            case 31:
            case 33:
                return R.drawable.weather_night;
            case 32:
            case 34:
                return R.drawable.weather_sunny;
            case 36:
                return R.drawable.fire;
            case 37:
            case 38:
            case 39:
                return R.drawable.weather_lightning;
            case 45:
            case 47:
                return R.drawable.weather_lightning_rainy;
            default:
                return R.drawable.ic_help_white_24dp;
        }
    }
}
