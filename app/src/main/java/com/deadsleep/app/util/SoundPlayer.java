package com.deadsleep.app.util;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.deadsleep.app.R;

import java.io.IOException;

/**
 * Handles audio playback for alarm ringing, sound previews, and gradual volume fade-in.
 */
public class SoundPlayer {

    private final Context context;
    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;
    private final Handler fadeHandler = new Handler(Looper.getMainLooper());
    private Runnable fadeRunnable;

    public SoundPlayer(Context context) {
        this.context = context.getApplicationContext();
        this.audioManager = (AudioManager) this.context.getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * Starts playing alarm ringtone in a loop on STREAM_ALARM.
     */
    public void playAlarm(String ringtoneUriString, int targetVolumePercent, boolean gradualFadeIn) {
        stop();
        requestAudioFocus();

        mediaPlayer = new MediaPlayer();
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        mediaPlayer.setAudioAttributes(audioAttributes);
        mediaPlayer.setLooping(true);

        try {
            if (ringtoneUriString != null && !ringtoneUriString.isEmpty() && !ringtoneUriString.startsWith("bundled:")) {
                mediaPlayer.setDataSource(context, Uri.parse(ringtoneUriString));
                mediaPlayer.prepare();
            } else {
                int rawResId = getRawResourceForId(ringtoneUriString);
                mediaPlayer = MediaPlayer.create(context, rawResId, audioAttributes, audioManager.generateAudioSessionId());
                if (mediaPlayer == null) {
                    mediaPlayer = MediaPlayer.create(context, R.raw.tone_gentle_chime, audioAttributes, audioManager.generateAudioSessionId());
                }
                mediaPlayer.setLooping(true);
            }

            if (gradualFadeIn) {
                // Start at minimum volume and ramp up over 30 seconds
                mediaPlayer.setVolume(0.05f, 0.05f);
                startFadeIn(targetVolumePercent);
            } else {
                float vol = (float) Math.max(0.1, targetVolumePercent / 100.0);
                mediaPlayer.setVolume(vol, vol);
            }

            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to gentle chime
            playFallbackTone();
        }
    }

    /**
     * Plays a preview of the tone for 4 seconds.
     */
    public void playPreview(String ringtoneUriString, int targetVolumePercent) {
        stop();
        playAlarm(ringtoneUriString, targetVolumePercent, false);
        fadeHandler.postDelayed(this::stop, 4500);
    }

    private void startFadeIn(int targetVolumePercent) {
        final float targetVol = (float) Math.max(0.1, targetVolumePercent / 100.0);
        final long totalSteps = 30; // 30 seconds
        final long intervalMillis = 1000;
        final float stepIncrement = targetVol / totalSteps;

        fadeRunnable = new Runnable() {
            float currentVol = 0.05f;

            @Override
            public void run() {
                if (mediaPlayer == null || !mediaPlayer.isPlaying()) return;
                currentVol = Math.min(targetVol, currentVol + stepIncrement);
                try {
                    mediaPlayer.setVolume(currentVol, currentVol);
                } catch (Exception ignored) {}
                if (currentVol < targetVol) {
                    fadeHandler.postDelayed(this, intervalMillis);
                }
            }
        };
        fadeHandler.postDelayed(fadeRunnable, intervalMillis);
    }

    private void playFallbackTone() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            mediaPlayer = MediaPlayer.create(context, R.raw.tone_gentle_chime);
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            }
        } catch (Exception ignored) {}
    }

    public static int getRawResourceForId(String uriString) {
        if (uriString == null) return R.raw.tone_gentle_chime;
        if (uriString.contains("morning_dawn")) return R.raw.tone_morning_dawn;
        if (uriString.contains("digital_beep")) return R.raw.tone_digital_beep;
        if (uriString.contains("cosmic_echo")) return R.raw.tone_cosmic_echo;
        if (uriString.contains("wake_bell")) return R.raw.tone_wake_bell;
        return R.raw.tone_gentle_chime;
    }

    public void stop() {
        if (fadeRunnable != null) {
            fadeHandler.removeCallbacks(fadeRunnable);
            fadeRunnable = null;
        }
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.reset();
                mediaPlayer.release();
            } catch (Exception ignored) {}
            mediaPlayer = null;
        }
        abandonAudioFocus();
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    private void requestAudioFocus() {
        if (audioManager == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes playbackAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                    .setAudioAttributes(playbackAttributes)
                    .build();
            audioManager.requestAudioFocus(audioFocusRequest);
        } else {
            audioManager.requestAudioFocus(null, AudioManager.STREAM_ALARM, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
        }
    }

    private void abandonAudioFocus() {
        if (audioManager == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && audioFocusRequest != null) {
            audioManager.abandonAudioFocusRequest(audioFocusRequest);
        } else {
            audioManager.abandonAudioFocus(null);
        }
    }
}
