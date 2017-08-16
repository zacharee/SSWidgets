package com.zacharee1.sswidgets.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.RemoteViews;

import com.zacharee1.sswidgets.R;
import com.zacharee1.sswidgets.activities.ImagePickerActivity;
import com.zacharee1.sswidgets.misc.Values;

public class Image extends AppWidgetProvider
{
    @Override
    public void onUpdate(Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds)
    {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        final RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.layout_image);

        Intent startActivity = new Intent(context, ImagePickerActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1001, startActivity, 0);

        view.setOnClickPendingIntent(R.id.custom_image, pendingIntent);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String img = preferences.getString(Values.EXTRA_IMAGE_URI, null);

        if (img != null && !img.isEmpty()) {
            view.setImageViewUri(R.id.custom_image, Uri.parse(img));
        }

        BroadcastReceiver receiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String action = intent.getAction();

                if (action.equals(Values.ACTION_IMAGE_UPDATED)) {
                    view.setImageViewUri(R.id.custom_image, Uri.parse(intent.getStringExtra(Values.EXTRA_IMAGE_URI)));
                    appWidgetManager.updateAppWidget(appWidgetIds, view);
                }
            }
        };

        IntentFilter filter = new IntentFilter(Values.ACTION_IMAGE_UPDATED);

        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter);

        appWidgetManager.updateAppWidget(appWidgetIds, view);
    }
}
