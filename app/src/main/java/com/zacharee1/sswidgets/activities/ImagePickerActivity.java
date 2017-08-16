package com.zacharee1.sswidgets.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.zacharee1.sswidgets.R;
import com.zacharee1.sswidgets.misc.Values;

public class ImagePickerActivity extends AppCompatActivity
{
    private static final int READ_REQUEST_CODE = 42;
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String img = preferences.getString(Values.EXTRA_IMAGE_URI, null);

        if (img != null && !img.isEmpty()) {
            ImageView imageView = findViewById(R.id.current_image_preview);
            imageView.setImageURI(Uri.parse(img));
        }

        receiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String action = intent.getAction();

                if (action.equals(Values.ACTION_IMAGE_UPDATED)) {
                    ImageView imageView = findViewById(R.id.current_image_preview);
                    imageView.setImageURI(Uri.parse(intent.getStringExtra(Values.EXTRA_IMAGE_URI)));
                }
            }
        };

        IntentFilter filter = new IntentFilter(Values.ACTION_IMAGE_UPDATED);

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    public void performFileSearch(View v) {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("image/*");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                assert uri != null;
                Log.i("SignBoard Image", "Uri: " + uri.toString());

                Intent intent = new Intent(Values.ACTION_IMAGE_UPDATED);
                intent.putExtra(Values.EXTRA_IMAGE_URI, uri.toString());

                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

                PreferenceManager.getDefaultSharedPreferences(this).edit().putString(Values.EXTRA_IMAGE_URI, uri.toString()).apply();
            }
        }
    }
}
