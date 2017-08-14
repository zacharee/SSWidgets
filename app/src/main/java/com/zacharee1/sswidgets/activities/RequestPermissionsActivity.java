package com.zacharee1.sswidgets.activities;

import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

/**
 * Activity to request runtime permissions directly from {}
 */

public class RequestPermissionsActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();

        if (extras != null && extras.getString("permission") != null) {
            ActivityCompat.requestPermissions(this,
                    new String[]{extras.getString("permission")},
                    10001);
        }
        finish();
    }
}
