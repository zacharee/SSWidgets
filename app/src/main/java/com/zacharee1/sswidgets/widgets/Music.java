package com.zacharee1.sswidgets.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.media.MediaControlIntent;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.zacharee1.sswidgets.R;
import com.zacharee1.sswidgets.misc.Util;
import com.zacharee1.sswidgets.misc.Values;
import com.zacharee1.sswidgets.services.MusicService;

import java.util.List;

public class Music extends AppWidgetProvider
{
    private RemoteViews mView;
    private Context mContext;
    private Display display;
    private AudioManager audioManager;
    private ContentObserver stateObserver;
    private BroadcastReceiver playingMusicReceiver;

    private Handler mHandler;
    private MediaSessionManager sessionManager;
    private List<MediaController> controllers;

    private AppWidgetManager mManager;
    private int[] mIds;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        mView = new RemoteViews(context.getPackageName(), R.layout.layout_music);

        mContext = context;
        display = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        audioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
        sessionManager = (MediaSessionManager) context.getSystemService(Context.MEDIA_SESSION_SERVICE);
        mManager = appWidgetManager;
        mIds = appWidgetIds;

        mHandler = new Handler(Looper.getMainLooper());

        registerMediaCallbacks();
        setColorsAndStates();
        listenForColorChangeOrMusicChange();

        appWidgetManager.updateAppWidget(appWidgetIds, mView);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds)
    {
        try {
            context.getContentResolver().unregisterContentObserver(stateObserver);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            context.unregisterReceiver(playingMusicReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onDeleted(context, appWidgetIds);
    }

    private void listenForColorChangeOrMusicChange() {
        stateObserver = new ContentObserver(null)
        {
            @Override
            public void onChange(boolean selfChange, Uri uri)
            {
                Uri backUri = Settings.Global.getUriFor("skip_prev_color");
                Uri ppUri = Settings.Global.getUriFor("play_pause_color");
                Uri forUri = Settings.Global.getUriFor("skip_forward_color");
                Uri infoUri = Settings.Global.getUriFor("song_info_color");

                if (uri.equals(backUri) || uri.equals(ppUri) || uri.equals(forUri) || uri.equals(infoUri)) {
                    mHandler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            setColorsAndStates();
                        }
                    });
                }
            }
        };

        mContext.getContentResolver().registerContentObserver(Settings.Global.CONTENT_URI, true, stateObserver);

        playingMusicReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, final Intent intent)
            {
                mHandler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        setColorsAndStates();
                    }
                });
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(MediaControlIntent.ACTION_PLAY);
        filter.addAction(MediaControlIntent.ACTION_PAUSE);
        filter.addAction(MediaControlIntent.ACTION_STOP);

//        mContext.registerReceiver(playingMusicReceiver, filter);

        sessionManager.addOnActiveSessionsChangedListener(new MediaSessionManager.OnActiveSessionsChangedListener()
        {
            @Override
            public void onActiveSessionsChanged(@Nullable List<MediaController> list)
            {
                registerMediaCallbacks();
            }
        }, null);
    }

    private void registerMediaCallbacks() {
        controllers = sessionManager.getActiveSessions(null);

        MediaController.Callback callback = new MediaController.Callback()
        {
            @Override
            @MainThread
            public void onPlaybackStateChanged(@NonNull PlaybackState state)
            {
                mHandler.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        setColorsAndStates();
                    }
                }, 300);
            }
        };

        for (MediaController controller : controllers) {
            controller.registerCallback(callback);
        }
    }

    private void setColorsAndStates() {
        int backColor = Settings.Global.getInt(mContext.getContentResolver(), "skip_prev_color", Color.WHITE);
        int playpauseColor = Settings.Global.getInt(mContext.getContentResolver(), "play_pause_color", Color.WHITE);
        int forwardColor = Settings.Global.getInt(mContext.getContentResolver(), "skip_forward_color", Color.WHITE);
        int songColor = Settings.Global.getInt(mContext.getContentResolver(), "song_info_color", Color.WHITE);

        mView.setInt(R.id.skip_back, "setColorFilter", backColor);
        mView.setInt(R.id.play_pause, "setColorFilter", playpauseColor);
        mView.setInt(R.id.skip_forward, "setColorFilter", forwardColor);
        mView.setInt(R.id.song_info, "setTextColor", songColor);

        mView.setImageViewResource(R.id.skip_back, R.drawable.ic_skip_previous_black_24dp);
        mView.setImageViewResource(R.id.play_pause, isMusicPlaying() ? R.drawable.ic_pause_black_24dp : R.drawable.ic_play_arrow_black_24dp);
        mView.setImageViewResource(R.id.skip_forward, R.drawable.ic_skip_next_black_24dp);

        if (controllers != null && controllers.size() > 0 && controllers.get(0).getMetadata() != null) {
            String title = controllers.get(0).getMetadata().getString(MediaMetadata.METADATA_KEY_TITLE);
            String artist = controllers.get(0).getMetadata().getString(MediaMetadata.METADATA_KEY_ARTIST);
            String packageName = controllers.get(0).getPackageName();

            StringBuilder info = new StringBuilder();

            if (title != null) {
                info.append(title);

                if (artist != null) {
                    info.append(" — ").append(artist);
                }
            } else {
                info.append(mContext.getResources().getString(R.string.play_something));
            }

            mView.setTextViewText(R.id.song_info, info.toString());
            try {
                mView.setImageViewBitmap(R.id.music_icon, Util.drawableToBitmap(mContext.getPackageManager().getApplicationIcon(packageName)));

                Intent openMusicIntent = new Intent(mContext, MusicService.class);
                openMusicIntent.putExtra(Values.MUSIC_INTENT_ACTION, Values.MUSIC_OPEN);
                openMusicIntent.putExtra("packageName", packageName);
                openMusicIntent.setAction(Values.MUSIC_INTENT_ACTION);

                PendingIntent open = PendingIntent.getService(mContext, Values.MUSIC_OPEN, openMusicIntent, 0);

                mView.setOnClickPendingIntent(R.id.music_icon, open);
            } catch (Exception e) {
                e.printStackTrace();
                mView.setInt(R.id.music_icon, "setColorFilter", Color.WHITE);
            }
        } else {
            mView.setInt(R.id.music_icon, "setColorFilter", Color.WHITE);
        }

        Intent backIntent = new Intent(mContext, MusicService.class);
        Intent playpauseIntent = new Intent(mContext, MusicService.class);
        Intent nextIntent = new Intent(mContext, MusicService.class);

        backIntent.putExtra(Values.MUSIC_INTENT_ACTION, Values.MUSIC_BACK);
        backIntent.setAction(Values.MUSIC_INTENT_ACTION);

        playpauseIntent.putExtra(Values.MUSIC_INTENT_ACTION, Values.MUSIC_PLAYPAUSE);
        playpauseIntent.setAction(Values.MUSIC_INTENT_ACTION);

        nextIntent.putExtra(Values.MUSIC_INTENT_ACTION, Values.MUSIC_NEXT);
        nextIntent.setAction(Values.MUSIC_INTENT_ACTION);

        PendingIntent back = PendingIntent.getService(mContext, Values.MUSIC_BACK, backIntent, 0);
        PendingIntent playpause = PendingIntent.getService(mContext, Values.MUSIC_PLAYPAUSE, playpauseIntent, 0);
        PendingIntent next = PendingIntent.getService(mContext, Values.MUSIC_NEXT, nextIntent, 0);

        mView.setOnClickPendingIntent(R.id.skip_back, back);
        mView.setOnClickPendingIntent(R.id.play_pause, playpause);
        mView.setOnClickPendingIntent(R.id.skip_forward, next);

        mManager.updateAppWidget(mIds, mView);
    }

    private boolean isMusicPlaying()
    {
        Log.e("SSWidgets", "isMusicActive()" + audioManager.isMusicActive());
        return audioManager.isMusicActive();
    }


}
