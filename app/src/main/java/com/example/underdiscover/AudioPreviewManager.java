package com.example.underdiscover;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

// CODE INSPIRED BY "veeson" ON STACK OVERFLOW
// https://stackoverflow.com/questions/8486147/how-can-i-play-a-mp3-without-download-from-the-url

public class AudioPreviewManager {
    /*
    Class to handle audio previews when requested by user
     */
    private static MediaPlayer mediaPlayer;

    protected static void playPreview(final Context context, final String url) throws Exception {

        if (mediaPlayer != null) {
            killMediaPlayer();
        }
        mediaPlayer = new MediaPlayer();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
            );
        } else {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }

        mediaPlayer.setDataSource(context, Uri.parse(url));
        mediaPlayer.prepare();
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                killMediaPlayer();
            }
        });
    }

    protected static void killMediaPlayer() {
        try {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        } catch (Exception e) {
        }
    }

    protected static Boolean isMediaPlayerActive() {
        if (mediaPlayer != null) {
            return true;
        }
        else {
            return false;
        }
    }



}
