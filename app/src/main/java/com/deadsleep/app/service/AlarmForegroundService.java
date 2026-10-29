package com.deadsleep.app.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.annotation.Nullable;

import com.deadsleep.app.data.AppDatabase;
import com.deadsleep.app.data.DeadSleepRepository;
import com.deadsleep.app.model.SleepSchedule;
import com.deadsleep.app.util.Constants;
import com.deadsleep.app.util.NotificationHelper;
import com.deadsleep.app.util.SoundPlayer;
import com.deadsleep.app.util.VibrationHelper;

/**
 * High-priority foreground service running while the wake alarm rings.
 * Enforces hard-to-ignore volume rules, plays looping audio, and handles wake locks.
 */
public class AlarmForegroundService extends Service {

    private SoundPlayer soundPlayer;
    private VibrationHelper vibrationHelper;
    private PowerManager.WakeLock wakeLock;
    private AudioManager audioManager;
    private ContentObserver volumeObserver;
    private int targetVolumeLevel = 100;

    @Override
    public void onCreate() {
        super.onCreate();
        this.soundPlayer = new SoundPlayer(this);
        this.vibrationHelper = new VibrationHelper(this);
        this.audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // Acquire WakeLock to keep CPU running
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DeadSleep:AlarmWakeLock");
            wakeLock.acquire(30 * 60 * 1000L); // 30 min max
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        long scheduleId = -1;
        String scheduleName = "Dead Sleep";
        if (intent != null) {
            scheduleId = intent.getLongExtra(Constants.EXTRA_SCHEDULE_ID, -1);
            scheduleName = intent.getStringExtra(Constants.EXTRA_SCHEDULE_NAME);
        }

        final long targetId = scheduleId;
        final String label = scheduleName;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                        Constants.NOTIF_ID_ALARM,
                        NotificationHelper.buildAlarmNotification(this, targetId, label),
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                );
            } else {
                startForeground(
                        Constants.NOTIF_ID_ALARM,
                        NotificationHelper.buildAlarmNotification(this, targetId, label)
                );
            }
        } catch (Exception ignored) {}

        AppDatabase.getExecutor().execute(() -> {
            SleepSchedule schedule = (targetId != -1) ? DeadSleepRepository.getInstance(this).getScheduleById(targetId) : null;

            String ringtone = (schedule != null) ? schedule.getRingtoneUri() : "bundled:tone_gentle_chime";
            int volume = (schedule != null) ? schedule.getVolume() : 85;
            boolean vibrate = (schedule != null) ? schedule.isVibrate() : true;
            boolean gradual = (schedule != null) ? schedule.isGradualVolume() : true;

            // Start audio and vibration
            soundPlayer.playAlarm(ringtone, volume, gradual);
            if (vibrate) {
                vibrationHelper.startAlarmVibration();
            }

            // Enforce volume observer
            setupVolumeEnforcement(volume);
        });

        return START_STICKY;
    }

    private void setupVolumeEnforcement(int targetVolPercent) {
        if (audioManager == null) return;
        int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        targetVolumeLevel = Math.max(1, (int) (maxVol * (targetVolPercent / 100.0f)));

        try {
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, targetVolumeLevel, 0);
        } catch (Exception ignored) {}

        volumeObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                int current = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
                if (current < targetVolumeLevel) {
                    // Prevent turning alarm volume down
                    try {
                        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, targetVolumeLevel, 0);
                    } catch (Exception ignored) {}
                }
            }
        };

        getContentResolver().registerContentObserver(
                Settings.System.CONTENT_URI, true, volumeObserver
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (soundPlayer != null) {
            soundPlayer.stop();
        }
        if (vibrationHelper != null) {
            vibrationHelper.stop();
        }
        if (volumeObserver != null) {
            getContentResolver().unregisterContentObserver(volumeObserver);
            volumeObserver = null;
        }
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
