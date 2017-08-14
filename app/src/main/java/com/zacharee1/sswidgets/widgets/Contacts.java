package com.zacharee1.sswidgets.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;

import com.zacharee1.sswidgets.R;
import com.zacharee1.sswidgets.misc.Contact;
import com.zacharee1.sswidgets.misc.Values;
import com.zacharee1.sswidgets.services.ContactService;

import java.util.ArrayList;

import static com.zacharee1.sswidgets.misc.Util.openDisplayPhoto;

/**
 * Implementation of App Widget functionality.
 */
public class Contacts extends AppWidgetProvider
{
    public static final String CONTACT_ID = "contact_id";

    public static final int DEF_KEY = R.string.def_key;
    public static final int CONTACT_KEY = R.string.contact_key;

    public static final String CONTACT_1 = "contact_1";
    public static final String CONTACT_2 = "contact_2";
    public static final String CONTACT_3 = "contact_3";
    public static final String CONTACT_4 = "contact_4";
    public static final String CONTACT_5 = "contact_5";
    public static final String CONTACT_6 = "contact_6";
    public static final String CONTACT_7 = "contact_7";
    public static final String CONTACT_8 = "contact_8";

    private Context mContext;

    private RemoteViews mView;
    private Display display;
    private ContentObserver stateObserver;
    private AppWidgetManager mManager;
    private int[] mIds;

    private Handler mHandler;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        mView = new RemoteViews(context.getPackageName(), R.layout.contacts);
        display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        mContext = context;

        mManager = appWidgetManager;
        mIds = appWidgetIds;
        
        mHandler = new Handler(Looper.getMainLooper());

        addIcons();
        listenForContactChange();

        appWidgetManager.updateAppWidget(appWidgetIds, mView);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds)
    {
        super.onDeleted(context, appWidgetIds);

        try {
            context.getContentResolver().unregisterContentObserver(stateObserver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listenForContactChange() {
        stateObserver = new ContentObserver(null)
        {
            @Override
            public void onChange(boolean selfChange, final Uri uri)
            {
                Uri contact1 = Settings.Global.getUriFor(CONTACT_1);
                Uri contact2 = Settings.Global.getUriFor(CONTACT_2);
                Uri contact3 = Settings.Global.getUriFor(CONTACT_3);
                Uri contact4 = Settings.Global.getUriFor(CONTACT_4);
                Uri contact5 = Settings.Global.getUriFor(CONTACT_5);
                Uri contact6 = Settings.Global.getUriFor(CONTACT_6);
                Uri contact7 = Settings.Global.getUriFor(CONTACT_7);
                Uri contact8 = Settings.Global.getUriFor(CONTACT_8);

                if (uri.equals(contact1) ||
                        uri.equals(contact2) ||
                        uri.equals(contact3) ||
                        uri.equals(contact4) ||
                        uri.equals(contact5) ||
                        uri.equals(contact6) ||
                        uri.equals(contact7) ||
                        uri.equals(contact8)) {
                    mHandler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            addIcons();
                        }
                    });
                }
            }
        };

        mContext.getContentResolver().registerContentObserver(Settings.Global.CONTENT_URI, true, stateObserver);
    }

    private void addIcons() {
        String a1 = Settings.Global.getString(mContext.getContentResolver(), CONTACT_1);
        String a2 = Settings.Global.getString(mContext.getContentResolver(), CONTACT_2);
        String a3 = Settings.Global.getString(mContext.getContentResolver(), CONTACT_3);
        String a4 = Settings.Global.getString(mContext.getContentResolver(), CONTACT_4);
        String a5 = Settings.Global.getString(mContext.getContentResolver(), CONTACT_5);
        String a6 = Settings.Global.getString(mContext.getContentResolver(), CONTACT_6);
        String a7 = Settings.Global.getString(mContext.getContentResolver(), CONTACT_7);
        String a8 = Settings.Global.getString(mContext.getContentResolver(), CONTACT_8);

        if (a1 != null && !a1.isEmpty()) {
            try {
                mView.setImageViewBitmap(R.id.contact_1, openDisplayPhoto(mContext, Long.decode(a1)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            mView.setImageViewResource(R.id.contact_1, R.drawable.ic_account_circle_white_24dp);
        }

        if (a2 != null && !a2.isEmpty()) {
            try {
                mView.setImageViewBitmap(R.id.contact_2, openDisplayPhoto(mContext, Long.decode(a2)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            mView.setImageViewResource(R.id.contact_2, R.drawable.ic_account_circle_white_24dp);
        }

        if (a3 != null && !a3.isEmpty()) {
            try {
                mView.setImageViewBitmap(R.id.contact_3, openDisplayPhoto(mContext, Long.decode(a3)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            mView.setImageViewResource(R.id.contact_3, R.drawable.ic_account_circle_white_24dp);
        }

        if (a4 != null && !a4.isEmpty()) {
            try {
                mView.setImageViewBitmap(R.id.contact_4, openDisplayPhoto(mContext, Long.decode(a4)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            mView.setImageViewResource(R.id.contact_4, R.drawable.ic_account_circle_white_24dp);
        }

        if (a5 != null && !a5.isEmpty()) {
            try {
                mView.setImageViewBitmap(R.id.contact_5, openDisplayPhoto(mContext, Long.decode(a5)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            mView.setImageViewResource(R.id.contact_5, R.drawable.ic_account_circle_white_24dp);
        }

        if (a6 != null && !a6.isEmpty()) {
            try {
                mView.setImageViewBitmap(R.id.contact_6, openDisplayPhoto(mContext, Long.decode(a6)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            mView.setImageViewResource(R.id.contact_6, R.drawable.ic_account_circle_white_24dp);
        }

        if (a7 != null && !a7.isEmpty()) {
            try {
                mView.setImageViewBitmap(R.id.contact_7, openDisplayPhoto(mContext, Long.decode(a7)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            mView.setImageViewResource(R.id.contact_7, R.drawable.ic_account_circle_white_24dp);
        }

        if (a8 != null && !a8.isEmpty()) {
            try {
                mView.setImageViewBitmap(R.id.contact_8, openDisplayPhoto(mContext, Long.decode(a8)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            mView.setImageViewResource(R.id.contact_8, R.drawable.ic_account_circle_white_24dp);
        }

        Intent c1 = new Intent(mContext, ContactService.class);
        c1.putExtra(Values.CONTACT_INTENT_ACTION, Values.CONTACT_1);
        c1.setAction(Values.CONTACT_INTENT_ACTION);
        
        Intent c2 = new Intent(mContext, ContactService.class);
        c2.putExtra(Values.CONTACT_INTENT_ACTION, Values.CONTACT_2);
        c2.setAction(Values.CONTACT_INTENT_ACTION);
        
        Intent c3 = new Intent(mContext, ContactService.class);
        c3.putExtra(Values.CONTACT_INTENT_ACTION, Values.CONTACT_3);
        c3.setAction(Values.CONTACT_INTENT_ACTION);
        
        Intent c4 = new Intent(mContext, ContactService.class);
        c4.putExtra(Values.CONTACT_INTENT_ACTION, Values.CONTACT_4);
        c4.setAction(Values.CONTACT_INTENT_ACTION);
        
        Intent c5 = new Intent(mContext, ContactService.class);
        c5.putExtra(Values.CONTACT_INTENT_ACTION, Values.CONTACT_5);
        c5.setAction(Values.CONTACT_INTENT_ACTION);
        
        Intent c6 = new Intent(mContext, ContactService.class);
        c6.putExtra(Values.CONTACT_INTENT_ACTION, Values.CONTACT_6);
        c6.setAction(Values.CONTACT_INTENT_ACTION);
        
        Intent c7 = new Intent(mContext, ContactService.class);
        c7.putExtra(Values.CONTACT_INTENT_ACTION, Values.CONTACT_7);
        c7.setAction(Values.CONTACT_INTENT_ACTION);
        
        Intent c8 = new Intent(mContext, ContactService.class);
        c8.putExtra(Values.CONTACT_INTENT_ACTION, Values.CONTACT_8);
        c8.setAction(Values.CONTACT_INTENT_ACTION);

        PendingIntent contact1 = PendingIntent.getService(mContext, Values.CONTACT_1, c1, 0);
        PendingIntent contact2 = PendingIntent.getService(mContext, Values.CONTACT_2, c2, 0);
        PendingIntent contact3 = PendingIntent.getService(mContext, Values.CONTACT_3, c3, 0);
        PendingIntent contact4 = PendingIntent.getService(mContext, Values.CONTACT_4, c4, 0);
        PendingIntent contact5 = PendingIntent.getService(mContext, Values.CONTACT_5, c5, 0);
        PendingIntent contact6 = PendingIntent.getService(mContext, Values.CONTACT_6, c6, 0);
        PendingIntent contact7 = PendingIntent.getService(mContext, Values.CONTACT_7, c7, 0);
        PendingIntent contact8 = PendingIntent.getService(mContext, Values.CONTACT_8, c8, 0);
        
        mView.setOnClickPendingIntent(R.id.contact_1, contact1);
        mView.setOnClickPendingIntent(R.id.contact_2, contact2);
        mView.setOnClickPendingIntent(R.id.contact_3, contact3);
        mView.setOnClickPendingIntent(R.id.contact_4, contact4);
        mView.setOnClickPendingIntent(R.id.contact_5, contact5);
        mView.setOnClickPendingIntent(R.id.contact_6, contact6);
        mView.setOnClickPendingIntent(R.id.contact_7, contact7);
        mView.setOnClickPendingIntent(R.id.contact_8, contact8);
        
        mManager.updateAppWidget(mIds, mView);
    }
}

