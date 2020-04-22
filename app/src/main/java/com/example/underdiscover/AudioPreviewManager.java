package com.example.underdiscover;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

// CODE INSPIRED BY "veeson" ON STACK OVERFLOW
// https://stackoverflow.com/questions/8486147/how-can-i-play-a-mp3-without-download-from-the-url

public class AudioPreviewManager {
    private static MediaPlayer mediaPlayer;

    protected static void playPreview(final Context context, final String url) throws Exception {

        if (mediaPlayer != null) {
            killMediaPlayer();
        }

        if (mediaPlayer == null) {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer = MediaPlayer.create(context, Uri.parse(url));
            mediaPlayer.prepare();
        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                killMediaPlayer();
            }
        });
        mediaPlayer.start();
    }

    protected static void killMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            } catch (Exception e) {
            }
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
