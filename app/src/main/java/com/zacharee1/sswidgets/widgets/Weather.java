package com.zacharee1.sswidgets.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.AsyncTask;
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
import com.survivingwithandroid.weather.lib.model.City;
import com.survivingwithandroid.weather.lib.model.CurrentWeather;
import com.survivingwithandroid.weather.lib.provider.IProviderType;
import com.survivingwithandroid.weather.lib.provider.forecastio.ForecastIOProviderType;
import com.survivingwithandroid.weather.lib.provider.openweathermap.OpenweathermapProviderType;
import com.survivingwithandroid.weather.lib.provider.wunderground.WeatherUndergroundProviderType;
import com.survivingwithandroid.weather.lib.provider.yahooweather.YahooProviderType;
import com.survivingwithandroid.weather.lib.request.WeatherRequest;
import com.zacharee1.sswidgets.R;
import com.zacharee1.sswidgets.misc.GPSTracker;
import com.zacharee1.sswidgets.misc.Util;
import com.zacharee1.sswidgets.misc.Values;
import com.zacharee1.sswidgets.weather.WeatherConnection;
import com.zacharee1.sswidgets.weather.WeatherConnectionInfo;
import com.zacharee1.sswidgets.weather.WeatherInfo;
import com.zacharee1.sswidgets.weather.WeatherListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;

public class Weather extends AppWidgetProvider implements WeatherListener
{
    private RemoteViews mView;
    private Context mContext;
    private int[] mIds;
    private AppWidgetManager mManager;
    private WeatherConnection mConnection;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        WeatherConfig config = new WeatherConfig();
//        config.ApiKey = PreferenceManager.getDefaultSharedPreferences(context).getString(Values.WEATHER_API_KEY, null);
//
//        if (config.ApiKey == null || config.ApiKey.isEmpty()) {
//            Toast.makeText(context, context.getResources().getString(R.string.set_api_weather), Toast.LENGTH_LONG).show();
//            return;
//        }

//        config.ApiKey = "dj0yJmk9TlV4NGl6c2UzQ0c5JmQ9WVdrOWRXczBUVTVWTnpBbWNHbzlNQS0tJnM9Y29uc3VtZXJzZWNyZXQmeD1hOA--";
//
        mView = new RemoteViews(context.getPackageName(), R.layout.layout_weather);
//        try {
//            mWeatherClient = new WeatherClient.ClientBuilder()
//                    .attach(context)
//                    .provider(new YahooProviderType())
//                    .config(config)
//                    .httpClient(WeatherClientDefault.class)
//                    .build();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        mConnection = new WeatherConnection();

        mContext = context;
        mIds = appWidgetIds;
        mManager = appWidgetManager;

        queryWeatherInfo();

        appWidgetManager.updateAppWidget(appWidgetIds, mView);
    }

    private void queryWeatherInfo() {
        GPSTracker tracker = new GPSTracker(mContext);
        mConnection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new WeatherConnectionInfo(tracker.getLatitude() + "", tracker.getLongitude() + "", this));
    }

    @Override
    public void onWeatherConnectionFailed(String message)
    {
        Log.e("SignBoard Weather Error", message);
    }

    @Override
    public void onWeatherInfoFound(WeatherInfo info)
    {
        CharSequence temp = info.currentTemp + "°" + info.currentTempUnit;
        CharSequence condition = info.currentCondition;
        CharSequence location = info.cityName + ", " + info.stateName;
        CharSequence date = info.pubDate;

        ArrayList<CharSequence> dateElements = new ArrayList<CharSequence>(Arrays.asList(date.toString().split("[ ]")));

        CharSequence day = dateElements.get(1);
        CharSequence month = dateElements.get(2);
        CharSequence time = dateElements.get(4);
        CharSequence ampm = dateElements.get(5);

        mView.setCharSequence(R.id.current_temp, "setText", temp);
        mView.setCharSequence(R.id.current_condition_desc, "setText", condition);
        mView.setCharSequence(R.id.current_location, "setText", location);
        mView.setImageViewResource(R.id.current_condition_icon, info.iconRes);
        mView.setInt(R.id.current_condition_icon, "setColorFilter", Color.WHITE);

//        mView.setInt(R.id.weather_loading, "setVisibility", View.GONE);
//        mView.setInt(R.id.current_temp, "setVisibility", View.VISIBLE);
//        mView.setInt(R.id.current_location, "setVisibility", View.VISIBLE);
//        mView.setInt(R.id.current_condition_desc, "setVisibility", View.VISIBLE);
//        mView.setInt(R.id.current_condition_icon, "setVisibility", View.VISIBLE);

//        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, h:mm a", Locale.US);
//        CharSequence currentDateandTime = sdf.format(new Date());

        Intent toYahoo = new Intent(Intent.ACTION_VIEW, Uri.parse(info.yahooUrl));
        PendingIntent yahooPend = PendingIntent.getActivity(mContext, 0, toYahoo, 0);

        mView.setOnClickPendingIntent(R.id.yahoo_image, yahooPend);

        mView.setTextViewText(R.id.weather_time, month + " " + day + ", " + time + " " + ampm);

        mManager.updateAppWidget(mIds, mView);
    }
}
