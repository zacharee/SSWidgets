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
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
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

public class Weather extends AppWidgetProvider implements WeatherListener, LocationListener
{
    private RemoteViews mView;
    private Context mContext;
    private int[] mIds;
    private AppWidgetManager mManager;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        mView = new RemoteViews(context.getPackageName(), R.layout.layout_weather);

        mContext = context;
        mIds = appWidgetIds;
        mManager = appWidgetManager;

        queryWeatherInfo();

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 900000, 1610, this);

        appWidgetManager.updateAppWidget(appWidgetIds, mView);
    }

    private void queryWeatherInfo() {
        GPSTracker tracker = new GPSTracker(mContext);
        Log.e("SignBoard Weather", "Location: " + tracker.getLatitude() + " " + tracker.getLongitude());
        new WeatherConnection().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new WeatherConnectionInfo(tracker.getLatitude() + "", tracker.getLongitude() + "", this));
    }

    @Override
    public void onProviderDisabled(String s)
    {

    }

    @Override
    public void onProviderEnabled(String s)
    {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle)
    {

    }

    @Override
    public void onLocationChanged(Location location)
    {
        Log.e("SignBoard Weather", "Location Changed: " + location.getLatitude() + " " + location.getLongitude());
        new WeatherConnection().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new WeatherConnectionInfo(location.getLatitude() + "", location.getLongitude() + "", this));
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

        Intent toYahoo = new Intent(Intent.ACTION_VIEW, Uri.parse(info.yahooUrl));
        PendingIntent yahooPend = PendingIntent.getActivity(mContext, 0, toYahoo, 0);

        mView.setOnClickPendingIntent(R.id.yahoo_image, yahooPend);

        mView.setTextViewText(R.id.weather_time, month + " " + day + ", " + time + " " + ampm);

        mManager.updateAppWidget(mIds, mView);
    }
}
