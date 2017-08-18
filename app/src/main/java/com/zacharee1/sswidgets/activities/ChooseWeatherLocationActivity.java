package com.zacharee1.sswidgets.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.zacharee1.sswidgets.R;
import com.zacharee1.sswidgets.misc.Values;

import java.util.List;
import java.util.Locale;

public class ChooseWeatherLocationActivity extends AppCompatActivity implements View.OnClickListener
{
    private static final int PLACE_PICKER_REQUEST = 1001;
    private TextView mCurrent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_weather_location);

        mCurrent = findViewById(R.id.currently_chosen);

        setText();
    }

    private void setText() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String lat = preferences.getString(Values.LOCATION_LAT, null);
        String lon = preferences.getString(Values.LOCATION_LON, null);

        if (lat == null || lon == null) {
            mCurrent.setText(getResources().getString(R.string.current_location));
        } else {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(Double.valueOf(lat), Double.valueOf(lon), 1);
                String cityName = addresses.get(0).getAddressLine(0);
                String stateName = addresses.get(0).getAddressLine(1);
                String countryName = addresses.get(0).getAddressLine(2);
                String addr = cityName + ", " + stateName + " " + countryName;

                mCurrent.setText(addr);
            } catch (Exception e) {
                mCurrent.setText(getResources().getString(R.string.unknown));
            }
        }
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId()) {
            case R.id.pick_location:
                try {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                    String lat = preferences.getString(Values.LOCATION_LAT, null);
                    String lon = preferences.getString(Values.LOCATION_LON, null);
                    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                    if (lat != null && lon != null) builder.setLatLngBounds(new LatLngBounds(new LatLng(Double.valueOf(lat), Double.valueOf(lon)), new LatLng(Double.valueOf(lat), Double.valueOf(lon))));
                    startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesNotAvailableException | GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.use_current_location:
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                preferences.edit().putString(Values.LOCATION_LAT, null).apply();
                preferences.edit().putString(Values.LOCATION_LON, null).apply();
                setText();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

                if (place != null) {
                    preferences.edit().putString(Values.LOCATION_LAT, place.getLatLng().latitude + "").apply();
                    preferences.edit().putString(Values.LOCATION_LON, place.getLatLng().longitude + "").apply();
                } else {
                    preferences.edit().putString(Values.LOCATION_LAT, null).apply();
                    preferences.edit().putString(Values.LOCATION_LON, null).apply();
                }
            }
            setText();
        }
    }
}
