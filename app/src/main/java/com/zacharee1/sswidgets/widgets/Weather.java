package com.zacharee1.sswidgets.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.WeatherConfig;
import com.survivingwithandroid.weather.lib.client.volley.WeatherClientDefault;
import com.survivingwithandroid.weather.lib.exception.WeatherLibException;
import com.survivingwithandroid.weather.lib.model.CurrentWeather;
import com.survivingwithandroid.weather.lib.provider.IProviderType;
import com.survivingwithandroid.weather.lib.provider.forecastio.ForecastIOProviderType;
import com.survivingwithandroid.weather.lib.provider.openweathermap.OpenweathermapProviderType;
import com.survivingwithandroid.weather.lib.provider.wunderground.WeatherUndergroundProviderType;
import com.survivingwithandroid.weather.lib.request.WeatherRequest;
import com.zacharee1.sswidgets.R;
import com.zacharee1.sswidgets.misc.GPSTracker;
import com.zacharee1.sswidgets.misc.Util;
import com.zacharee1.sswidgets.misc.Values;

import java.io.IOException;

public class Weather extends AppWidgetProvider implements WeatherClient.WeatherClientListener, WeatherClient.WeatherEventListener, WeatherClient.WeatherImageListener
{
    private RemoteViews mView;
    private Context mContext;
    private int[] mIds;
    private AppWidgetManager mManager;
    private WeatherClient mWeatherClient;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        WeatherConfig config = new WeatherConfig();
        config.ApiKey = PreferenceManager.getDefaultSharedPreferences(context).getString(Values.WEATHER_API_KEY, null);

        if (config.ApiKey == null || config.ApiKey.isEmpty()) {
            Toast.makeText(context, context.getResources().getString(R.string.set_api_weather), Toast.LENGTH_LONG).show();
            return;
        }

        mView = new RemoteViews(context.getPackageName(), R.layout.layout_weather);
        try {
            mWeatherClient = new WeatherClient.ClientBuilder()
                    .attach(context)
                    .provider(new OpenweathermapProviderType())
                    .config(config)
                    .httpClient(WeatherClientDefault.class)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mContext = context;
        mIds = appWidgetIds;
        mManager = appWidgetManager;

        queryWeatherInfo();

        appWidgetManager.updateAppWidget(appWidgetIds, mView);
    }

    private void queryWeatherInfo() {
        GPSTracker tracker = new GPSTracker(mContext);
        mWeatherClient.getCurrentCondition(new WeatherRequest(tracker.getLongitude(), tracker.getLatitude()), this);
    }

    @Override
    public void onConnectionError(Throwable t)
    {

    }

    @Override
    public void onWeatherError(WeatherLibException wle)
    {

    }

    @Override
    public void onWeatherRetrieved(final CurrentWeather weather)
    {
        CharSequence temp = Math.round(weather.weather.temperature.getTemp()) + weather.getUnit().tempUnit;
        CharSequence condition = weather.weather.currentCondition.getCondition();
        CharSequence location = weather.weather.location.getCity();

        mWeatherClient.getDefaultProviderImage(weather.weather.currentCondition.getIcon(), this);

        mView.setCharSequence(R.id.current_temp, "setText", temp);
        mView.setCharSequence(R.id.current_condition_desc, "setText", condition);
        mView.setCharSequence(R.id.current_location, "setText", location);

        mView.setInt(R.id.weather_loading, "setVisibility", View.GONE);
        mView.setInt(R.id.current_temp, "setVisibility", View.VISIBLE);
        mView.setInt(R.id.current_location, "setVisibility", View.VISIBLE);
        mView.setInt(R.id.current_condition_desc, "setVisibility", View.VISIBLE);
        mView.setInt(R.id.current_condition_icon, "setVisibility", View.VISIBLE);

        mManager.updateAppWidget(mIds, mView);
    }

    @Override
    public void onImageReady(Bitmap image)
    {
        mView.setImageViewBitmap(R.id.current_condition_icon, image);
//        mView.setInt(R.id.current_condition_icon, "setBackgroundColor", Color.argb(255, 255, 255, 255));
        mManager.updateAppWidget(mIds, mView);
    }
}
