package com.zacharee1.sswidgets.widgets;

import android.Manifest;
import android.app.Notification;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.ContentObserver;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextClock;
import android.widget.TextView;

import com.zacharee1.sswidgets.R;
import com.zacharee1.sswidgets.activities.RequestPermissionsActivity;
import com.zacharee1.sswidgets.misc.Contact;
import com.zacharee1.sswidgets.misc.Util;
import com.zacharee1.sswidgets.misc.Values;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import static android.content.Context.BATTERY_SERVICE;
import static android.content.Context.CONNECTIVITY_SERVICE;

/**
 * Implementation of App Widget functionality.
 */
public class Information extends AppWidgetProvider
{
    private RemoteViews mView;
    private BroadcastReceiver actionChange;
    private Display display;
    private BroadcastReceiver localReceiver;
    private ContentObserver mObserver;
    private Context mContext;
    private AppWidgetManager mManager;
    private int[] mIds;
    private ArrayList<Integer> mNotifIds = new ArrayList<Integer>() {{
        add(R.id.notif_1);
        add(R.id.notif_2);
        add(R.id.notif_3);
        add(R.id.notif_4);
        add(R.id.notif_5);
        add(R.id.notif_6);
    }};

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        mView = new RemoteViews(context.getPackageName(), R.layout.layout_info);
        mContext = context;
        mManager = appWidgetManager;
        mIds = appWidgetIds;

        display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        registerObserversAndReceivers();
        cellLevels();
        batteryValues();
        wifiLevels();

        mView.setInt(R.id.current_time, "setTextColor", Settings.Global.getInt(context.getContentResolver(), "clock_color", Color.WHITE));

        setContentObserver();

        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Values.ACTION_INFORMATION_ADDED));

        appWidgetManager.updateAppWidget(appWidgetIds, mView);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds)
    {
        super.onDeleted(context, appWidgetIds);

        try {
            context.getApplicationContext().unregisterReceiver(actionChange);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(localReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            context.getContentResolver().unregisterContentObserver(mObserver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Register a {@link ContentObserver} and listen for relevant changes
     */
    private void setContentObserver() {
        mObserver = new ContentObserver(null)
        {
            @Override
            public void onChange(boolean selfChange, final Uri uri)
            {
                Uri notifs = Settings.Global.getUriFor("notification_icon_color");
                Uri wifi = Settings.Global.getUriFor("wifi_signal_color");
                Uri mobile = Settings.Global.getUriFor("cell_signal_color");
                Uri battery = Settings.Global.getUriFor("battery_color");
                Uri clock = Settings.Global.getUriFor("clock_color");
                Uri airplane = Settings.Global.getUriFor("airplane_icon_color");

                if (uri.equals(notifs)) {
                    for (int i : mNotifIds) {
                        mView.setInt(i, "setColorFilter", Settings.Global.getInt(mContext.getContentResolver(), "notification_icon_color", Color.WHITE));
                    }
                    mManager.updateAppWidget(mIds, mView);
                }

                if (uri.equals(wifi)) {
                    mView.setInt(R.id.wifi_level, "setColorFilter", Settings.Global.getInt(mContext.getContentResolver(), "wifi_signal_color", Color.WHITE));
                    mManager.updateAppWidget(mIds, mView);
                }

                if (uri.equals(mobile)) {
                    mView.setInt(R.id.signal_level, "setColorFilter", Settings.Global.getInt(mContext.getContentResolver(), "cell_signal_color", Color.WHITE));
                    mManager.updateAppWidget(mIds, mView);
                }

                if (uri.equals(battery)) {
                    mView.setInt(R.id.battery_percent, "setTextColor", Settings.Global.getInt(mContext.getContentResolver(), "battery_color", Color.WHITE));
                    mView.setInt(R.id.battery_percent_image, "setColorFilter", Settings.Global.getInt(mContext.getContentResolver(), "battery_color", Color.WHITE));
                    mManager.updateAppWidget(mIds, mView);
                }

                if (uri.equals(clock)) {
                    mView.setInt(R.id.current_time, "setTextColor", Settings.Global.getInt(mContext.getContentResolver(), "clock_color", Color.WHITE));
                    mManager.updateAppWidget(mIds, mView);
                }

                if (uri.equals(airplane)) {
                    mView.setInt(R.id.airplane_mode, "setColorFilter", Settings.Global.getInt(mContext.getContentResolver(), "airplane_icon_color", Color.WHITE));
                    mManager.updateAppWidget(mIds, mView);
                }
            }
        };

        mContext.getContentResolver().registerContentObserver(Settings.Global.CONTENT_URI, true, mObserver);
    }

    /**
     * Register {@link BroadcastReceiver}s
     */
    private void registerObserversAndReceivers() {
        IntentFilter changeFilter = new IntentFilter();
        changeFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        changeFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        changeFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        changeFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        changeFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        changeFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        changeFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        changeFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        changeFilter.addAction(Intent.ACTION_SCREEN_OFF);
        changeFilter.addAction(Intent.ACTION_SCREEN_ON);

        actionChange = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String action = intent.getAction();

                if (action.equals(Intent.ACTION_BATTERY_CHANGED) || action.equals(Intent.ACTION_POWER_CONNECTED) || action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
                    batteryValues();
                    new Handler().postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            mManager.updateAppWidget(mIds, mView);
                        }
                    }, 300);
                }
                if (action.equals(WifiManager.RSSI_CHANGED_ACTION) || action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
                    wifiLevels();
                }

                if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION) || action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                    wifiLevels();
                    cellLevels();
                }

                if (action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
                    cellLevels();
                }

//                if (action.equals(Intent.ACTION_SCREEN_OFF)) {
//                    if (!wakeLock.isHeld()) wakeLock.acquire();
//                    WindowManager.LayoutParams params = (WindowManager.LayoutParams) ((SignBoardService) mContext).getViewPager().getLayoutParams();
//                    params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
//                    params.screenBrightness = 0;
//                    ((SignBoardService) mContext).getViewPager().setLayoutParams(params);
//                    updateClockOnWakelock();
//                }
//
//                if (action.equals(Intent.ACTION_SCREEN_ON)) {
//                    if (wakeLock.isHeld()) wakeLock.release();
//                    WindowManager.LayoutParams params = (WindowManager.LayoutParams) ((SignBoardService) mContext).getViewPager().getLayoutParams();
//                    params.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
//                    params.screenBrightness = -1;
//                    ((SignBoardService) mContext).getViewPager().setLayoutParams(params);
//                }

                showAirplaneModeIfNeeded();
            }
        };

        localReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String action = intent.getAction() != null ? intent.getAction() : "";

                if (action.equals(Values.ACTION_NOTIFICATIONS_RECOMPILED)) {
                    ArrayList extra = intent.getExtras().getParcelableArrayList("notifications");
                    Log.e("MustardCorp Received", "Might be null");
                    if (extra != null) {
                        Log.e("MustardCorp Received", extra.toString());
                        ArrayList<Notification> notifications = new ArrayList<Notification>(extra);

                        mView.setInt(R.id.notif_1, "setVisibility", View.GONE);
                        mView.setInt(R.id.notif_2, "setVisibility", View.GONE);
                        mView.setInt(R.id.notif_3, "setVisibility", View.GONE);
                        mView.setInt(R.id.notif_4, "setVisibility", View.GONE);
                        mView.setInt(R.id.notif_5, "setVisibility", View.GONE);
                        mView.setInt(R.id.notif_6, "setVisibility", View.GONE);

                        for (int i = 0; i < (notifications.size() > 6 ? 6 : notifications.size()); i++) { //we don't want to cause layout problems, so set the max displayed icons to 6
                            Notification notification = notifications.get(i);
                            Icon icon = notification.getSmallIcon();

                            mView.setImageViewIcon(mNotifIds.get(i), icon);
                            mView.setInt(mNotifIds.get(i), "setVisibility", View.VISIBLE);
                            mView.setInt(mNotifIds.get(i), "setColorFilter", Settings.Global.getInt(mContext.getContentResolver(), "notification_icon_color", Color.WHITE));
                        }

                        mManager.updateAppWidget(mIds, mView);
                    }
                }
            }
        };
        IntentFilter localFilter = new IntentFilter();
        localFilter.addAction(Values.ACTION_NOTIFICATIONS_RECOMPILED);

        LocalBroadcastManager.getInstance(mContext).registerReceiver(localReceiver, localFilter);

        TelephonyManager manager = ((TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE));

        //Make the listener
        PhoneStateListener listener = new PhoneStateListener() {
            public void onDataConnectionStateChanged(int state, int networkType)
            {
                cellLevels();
            }
            public void onCellLocationChanged(CellLocation location) {
                cellLevels();
            }
        };

        try
        {
            //Add the listener made above into the telephonyManager
            manager.listen(listener,
                    PhoneStateListener.LISTEN_DATA_CONNECTION_STATE //connection changes 2G/3G/etc
                            | PhoneStateListener.LISTEN_CELL_LOCATION       //or tower/cell changes
            );
        } catch (SecurityException e) {
        }

        mContext.getApplicationContext().registerReceiver(actionChange, changeFilter);
    }

    /**
     * Set proper battery image and text to correspond with current battery level
     */
    private void batteryValues() {
        BatteryManager bm = (BatteryManager)mContext.getSystemService(BATTERY_SERVICE);
        int batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        boolean isCharging = bm.isCharging();
        CharSequence sequence = batLevel + "%";

        mView.setCharSequence(R.id.battery_percent, "setText", sequence);

        int resId;

        if (batLevel >= 95) {
            resId = isCharging ? R.drawable.ic_battery_charging_full_black_24dp : R.drawable.ic_battery_full_black_24dp;
        } else if (batLevel >= 85) {
            resId = isCharging ? R.drawable.ic_battery_charging_90_black_24dp : R.drawable.ic_battery_90_black_24dp;
        } else if (batLevel >= 70) {
            resId = isCharging ? R.drawable.ic_battery_charging_60_black_24dp : R.drawable.ic_battery_60_black_24dp;
        } else if (batLevel >= 55) {
            resId = isCharging ? R.drawable.ic_battery_charging_50_black_24dp : R.drawable.ic_battery_50_black_24dp;
        } else if (batLevel >= 40) {
            resId = isCharging ? R.drawable.ic_battery_charging_30_black_24dp : R.drawable.ic_battery_30_black_24dp;
        } else if (batLevel >= 25) {
            resId = isCharging ? R.drawable.ic_battery_charging_20_black_24dp : R.drawable.ic_battery_20_black_24dp;
        } else {
            resId = isCharging ? R.drawable.ic_battery_charging_20_black_24dp : R.drawable.ic_battery_alert_black_24dp;
        }

        mView.setImageViewResource(R.id.battery_percent_image, resId);
        mView.setInt(R.id.battery_percent_image, "setColorFilter", Settings.Global.getInt(mContext.getContentResolver(), "battery_color", Color.WHITE));
        mView.setInt(R.id.battery_percent, "setTextColor", Settings.Global.getInt(mContext.getContentResolver(), "battery_color", Color.WHITE));

        mManager.updateAppWidget(mIds, mView);
    }

    /**
     * If Airplane Mode is on, disable the WiFi and Mobile signal bars and show an airplane icon
     */
    private void showAirplaneModeIfNeeded() {
        boolean show = Settings.Global.getInt(mContext.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
        mView.setInt(R.id.airplane_mode, "setVisibility", show ? View.VISIBLE : View.GONE);

        mManager.updateAppWidget(mIds, mView);
    }

    /**
     * Set proper WiFi signal levels
     */
    private void wifiLevels() {
        WifiManager wm = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        int currentLevel = WifiManager.calculateSignalLevel(wm.getConnectionInfo().getRssi(), 5);

        if (wifi.isConnected() && wifi.isAvailable())
        {
            int resId = R.drawable.ic_signal_wifi_0_bar_black_24dp;
            switch (currentLevel)
            {
                case 0:
                    resId = R.drawable.ic_signal_wifi_0_bar_black_24dp;
                    break;
                case 1:
                    resId = R.drawable.ic_signal_wifi_1_bar_black_24dp;
                    break;
                case 2:
                    resId = R.drawable.ic_signal_wifi_2_bar_black_24dp;
                    break;
                case 3:
                    resId = R.drawable.ic_signal_wifi_3_bar_black_24dp;
                    break;
                case 4:
                    resId = R.drawable.ic_signal_wifi_4_bar_black_24dp;
                    break;
            }
            mView.setImageViewResource(R.id.wifi_level, resId);
            mView.setInt(R.id.wifi_level, "setColorFilter", Settings.Global.getInt(mContext.getContentResolver(), "wifi_signal_color", Color.WHITE));
            mView.setInt(R.id.wifi_level, "setVisibility", View.VISIBLE);
        } else {
            mView.setImageViewResource(R.id.wifi_level, R.drawable.blank);
            mView.setInt(R.id.wifi_level, "setVisibility", View.GONE);
        }

        mManager.updateAppWidget(mIds, mView);
    }

    /**
     * Set proper Mobile signal levels
     */
    private void cellLevels() {
        TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);

        try {
            if (tm.getAllCellInfo().size() > 0)
            {
                CellInfo info = tm.getAllCellInfo().get(0);
                int strength = 0;

                if (info instanceof CellInfoLte)
                    strength = ((CellInfoLte) info).getCellSignalStrength().getLevel();
                if (info instanceof CellInfoWcdma)
                    strength = ((CellInfoWcdma) info).getCellSignalStrength().getLevel();
                if (info instanceof CellInfoCdma)
                    strength = ((CellInfoCdma) info).getCellSignalStrength().getLevel();
                if (info instanceof CellInfoGsm)
                    strength = ((CellInfoGsm) info).getCellSignalStrength().getLevel();

                int resId = R.drawable.ic_signal_cellular_4_bar_black_24dp;
                if (tm.getSimState() == TelephonyManager.SIM_STATE_READY && Settings.Global.getInt(mContext.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) == 0)
                {
                    switch (strength)
                    {
                        case 0:
                            resId = info.isRegistered() ? R.drawable.ic_signal_cellular_0_bar_black_24dp : R.drawable.ic_signal_cellular_connected_no_internet_0_bar_black_24dp;
                            break;
                        case 1:
                            resId = info.isRegistered() ? R.drawable.ic_signal_cellular_1_bar_black_24dp : R.drawable.ic_signal_cellular_connected_no_internet_1_bar_black_24dp;
                            break;
                        case 2:
                            resId = info.isRegistered() ? R.drawable.ic_signal_cellular_2_bar_black_24dp : R.drawable.ic_signal_cellular_connected_no_internet_2_bar_black_24dp;
                            break;
                        case 3:
                            resId = info.isRegistered() ? R.drawable.ic_signal_cellular_3_bar_black_24dp : R.drawable.ic_signal_cellular_connected_no_internet_3_bar_black_24dp;
                            break;
                        case 4:
                            resId = info.isRegistered() ? R.drawable.ic_signal_cellular_4_bar_black_24dp : R.drawable.ic_signal_cellular_connected_no_internet_4_bar_black_24dp;
                            break;
                    }
                    mView.setImageViewResource(R.id.signal_level, resId);
                    mView.setInt(R.id.signal_level, "setVisibility", View.VISIBLE);
                } else if (Settings.Global.getInt(mContext.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) == 1)
                {
                    mView.setImageViewResource(R.id.signal_level, R.drawable.blank);
                    mView.setInt(R.id.signal_level, "setVisibility", View.GONE);
                } else if (tm.getSimState() != TelephonyManager.SIM_STATE_READY)
                {
                    mView.setImageViewResource(R.id.signal_level, R.drawable.ic_signal_cellular_no_sim_black_24dp);
                    mView.setInt(R.id.signal_level, "setVisibility", View.VISIBLE);
                }
            } else {
                mView.setImageViewResource(R.id.signal_level, R.drawable.ic_signal_cellular_null_black_24dp);
                mView.setInt(R.id.signal_level, "setVisibility", View.VISIBLE);
            }
            mView.setInt(R.id.signal_level, "setColorFilter", Settings.Global.getInt(mContext.getContentResolver(), "cell_signal_color", Color.WHITE));
        } catch (SecurityException e) {
        }

        mManager.updateAppWidget(mIds, mView);
    }

    /**
     * Listen for notification changes
     */
    public static class NotificationListener extends NotificationListenerService
    {
        private ArrayList<Notification> notifNames = new ArrayList<>();
        private BroadcastReceiver mReceiver;

        @Override
        public void onListenerConnected()
        {
            reAddNotifs();
            mReceiver = new BroadcastReceiver()
            {
                @Override
                public void onReceive(Context context, Intent intent)
                {
                    reAddNotifs();
                }
            };
            IntentFilter filter = new IntentFilter(Values.ACTION_INFORMATION_ADDED);

            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mReceiver, filter);
        }

        @Override
        public void onDestroy()
        {
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mReceiver);
            super.onDestroy();
        }

        @Override
        public void onNotificationRankingUpdate(RankingMap rankingMap)
        {
            reAddNotifs();
            super.onNotificationRankingUpdate(rankingMap);
        }

        @Override
        public void onNotificationPosted(StatusBarNotification sbn)
        {
            reAddNotifs();
            super.onNotificationPosted(sbn);
        }

        @Override
        public void onNotificationRemoved(StatusBarNotification sbn)
        {
            reAddNotifs();
            super.onNotificationRemoved(sbn);
        }

        private void reAddNotifs() {
            StatusBarNotification[] notifs = getActiveNotifications();
            notifNames = new ArrayList<>();
            ArrayList<String> notifGroups = new ArrayList<>();

            outerloop:
            for (StatusBarNotification notification : notifs) {
                Notification notif = notification.getNotification();
                int importance = notif.priority;
                Log.e("MustardCorp Importance", importance + "");

                try
                {
//                    Class INotificationManager = Class.forName("android.app.INotificationManager");
//                    Class INotificationManager$Stub = Class.forName("android.app.INotificationManager$Stub");
//                    Method asInterface = INotificationManager$Stub.getMethod("asInterface", IBinder.class);
//                    Class ServiceManager = Class.forName("android.os.ServiceManager");
//                    Method getService = ServiceManager.getMethod("getService", String.class);
//                    Method getImportance = INotificationManager.getMethod("getImportance", String.class, int.class);
//
//                    Object notifService = getService.invoke(null, Context.NOTIFICATION_SERVICE);
//
//                    Object manager = asInterface.invoke(null, notifService);
//
//                    Method getUid = StatusBarNotification.class.getMethod("getUid");
//                    Object uid = getUid.invoke(notification);
//
//                    Integer imp = (Integer) getImportance.invoke(manager, notification.getPackageName(), uid);

                    Log.e("MustardCorp", Arrays.toString(getCurrentRanking().getOrderedKeys()));

                    Log.e("MustardCorp Group", notif.getGroup() + "GROUP");

                    if (notif.getGroup() != null && notifGroups.contains(notif.getGroup())) {
                        continue;
                    }

                    if (importance > -2) {
                        notifNames.add(notif);
                        notifGroups.add(notif.getGroup());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Intent intent = new Intent(Values.ACTION_NOTIFICATIONS_RECOMPILED);
            intent.putParcelableArrayListExtra("notifications", notifNames);

            Log.e("MustardCorp", "Sending BC");
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
    }
}

