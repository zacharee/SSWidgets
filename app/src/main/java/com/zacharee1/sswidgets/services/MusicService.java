package com.zacharee1.sswidgets.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.media.AudioManager;
import android.os.SystemClock;
import android.view.KeyEvent;

import com.zacharee1.sswidgets.misc.Values;

public class MusicService extends IntentService
{
    private AudioManager audioManager;

    public MusicService()
    {
        super("MusicService");

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        if (intent != null)
        {
            final String action = intent.getAction();

            if (action.equals(Values.MUSIC_INTENT_ACTION)) {
                KeyEvent downEvent;
                KeyEvent upEvent;
                long eventtime;

                switch (intent.getIntExtra(Values.MUSIC_INTENT_ACTION, -2)) {
                    case -2:
                        return;
                    case Values.MUSIC_BACK:
                        eventtime = SystemClock.uptimeMillis();

                        downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS, 0);
                        audioManager.dispatchMediaKeyEvent(downEvent);

                        upEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS, 0);
                        audioManager.dispatchMediaKeyEvent(upEvent);

                        break;
                    case Values.MUSIC_PLAYPAUSE:
                        eventtime = SystemClock.uptimeMillis();

                        downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0);
                        audioManager.dispatchMediaKeyEvent(downEvent);

                        upEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0);
                        audioManager.dispatchMediaKeyEvent(upEvent);

                        break;
                    case Values.MUSIC_NEXT:
                        eventtime = SystemClock.uptimeMillis();

                        downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT, 0);
                        audioManager.dispatchMediaKeyEvent(downEvent);

                        upEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT, 0);
                        audioManager.dispatchMediaKeyEvent(upEvent);

                        break;
                }
            }
        }
    }
}
