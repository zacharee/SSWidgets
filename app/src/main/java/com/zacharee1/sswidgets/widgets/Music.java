package com.zacharee1.sswidgets.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Color;
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

import com.zacharee1.sswidgets.R;
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
    private RemoteViews back;
    private RemoteViews playpause;
    private RemoteViews next;
    private RemoteViews title;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        mView = new RemoteViews(context.getPackageName(), R.layout.layout_music);

        back = new RemoteViews(context.getPackageName(), R.layout.layout_music_back);
        playpause = new RemoteViews(context.getPackageName(), R.layout.layout_music_pp);
        next = new RemoteViews(context.getPackageName(), R.layout.layout_music_next);
        title = new RemoteViews(context.getPackageName(), R.layout.layout_music_title);

        mView.addView(R.id.skip_back, back);
        mView.addView(R.id.play_pause, playpause);
        mView.addView(R.id.skip_forward, next);
        mView.addView(R.id.song_info, title);

        mContext = context;
        display = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        audioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
        sessionManager = (MediaSessionManager) context.getSystemService(Context.MEDIA_SESSION_SERVICE);

        mHandler = new Handler(Looper.getMainLooper());

        registerMediaCallbacks();
        setColorsAndStates();
        listenForColorChangeOrMusicChange();

        appWidgetManager.updateAppWidget(appWidgetIds[0], mView);

    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds)
    {
        super.onDeleted(context, appWidgetIds);

        context.getContentResolver().unregisterContentObserver(stateObserver);
        context.unregisterReceiver(playingMusicReceiver);
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

        back.setInt(R.id.skip_back, "setColorFilter", backColor);
        this.playpause.setInt(R.id.play_pause, "setColorFilter", playpauseColor);
        this.next.setInt(R.id.skip_forward, "setColorFilter", forwardColor);
        title.setInt(R.id.song_info, "setColorFilter", songColor);

        back.setImageViewResource(R.id.skip_back, R.drawable.ic_skip_previous_black_24dp);
        this.playpause.setImageViewResource(R.id.play_pause, isMusicPlaying() ? R.drawable.ic_pause_black_24dp : R.drawable.ic_play_arrow_black_24dp);
        this.next.setImageViewResource(R.id.skip_forward, R.drawable.ic_skip_next_black_24dp);

        if (controllers != null) {
            String title = controllers.get(0).getMetadata().getString(MediaMetadata.METADATA_KEY_TITLE);
            String artist = controllers.get(0).getMetadata().getString(MediaMetadata.METADATA_KEY_ARTIST);

            StringBuilder info = new StringBuilder();

            if (title != null) {
                info.append(title);

                if (artist != null) {
                    info.append(" — ").append(artist);
                }
            } else {
                info.append(mContext.getResources().getString(R.string.play_something));
            }

            this.title.setTextViewText(R.id.song_info, info.toString());
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

        this.back.setOnClickPendingIntent(R.id.skip_back, back);
        this.playpause.setOnClickPendingIntent(R.id.play_pause, playpause);
        this.next.setOnClickPendingIntent(R.id.skip_forward, next);
    }

    private boolean isMusicPlaying()
    {
        Log.e("SSWidgets", "isMusicActive()" + audioManager.isMusicActive());
        return audioManager.isMusicActive();
    }


}
