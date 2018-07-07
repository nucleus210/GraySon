package com.example.root.grayson;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.util.Log;

import com.example.root.grayson.musicplayer.VideoPlayerActivity;

import java.util.Locale;

public class TextToSpeechManager implements TextToSpeech.OnInitListener {
    @SuppressWarnings("unused")
    public TextToSpeechManager(VideoPlayerActivity videoPlayerActivity,
                               VideoPlayerActivity listener) {
    }

    public interface Listener {

        void onTtsInitialized();

        void onTtsSpoken();
    }

    private static final String TAG = TextToSpeechManager.class.getSimpleName();
    private static final String UTTERANCE_ID = BuildConfig.APPLICATION_ID + ".UTTERANCE_ID";

    private Listener listener;
    private boolean isInitialized = false;
    private TextToSpeech ttsEngine;

    public TextToSpeechManager(Context context, Listener listener) {
        this.listener = listener;
        ttsEngine = new TextToSpeech(context, this, "com.google.android.tts");
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // Setup Language
            ttsEngine.setLanguage(Locale.US);

            // Setup Male Voice
            Voice mVoiceObj = new Voice("en-us-x-sfg#male_1-local",
                    Locale.getDefault(),1,1,false,null);
            ttsEngine.setVoice(mVoiceObj);


            ttsEngine.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    Log.i(TAG, "TTS Start");
                }

                @Override
                public void onDone(String utteranceId) {
                    Log.i(TAG, "TTS Done");
                    listener.onTtsSpoken();
                }

                @Override
                public void onError(String utteranceId, int errorCode) {
                    Log.w(TAG, "TTS Error (" + utteranceId + ")" + ". Error code: " + errorCode);
                }

                @Override
                public void onError(String utteranceId) {
                    Log.w(TAG, "TTS Error");
                }
            });

            ttsEngine.setPitch(1f);
            ttsEngine.setSpeechRate(1f);

            isInitialized = true;
            Log.i(TAG, "TTS initialized successfully");
            listener.onTtsInitialized();
        } else {
            Log.w(TAG, "Could not open TTS Engine (onInit status=" + status + "). " +
                    "Ignoring text to speech");
            ttsEngine = null;
        }
    }

    public void say(String message) {
        if (!isInitialized || ttsEngine == null) {
            Log.w(TAG, "TTS is not initializing, please wait.");
            return;
        }

        ttsEngine.speak(message, TextToSpeech.QUEUE_ADD, null, UTTERANCE_ID);
    }

    void onDestroy() {
        if (ttsEngine != null) {
            ttsEngine.stop();
            ttsEngine.shutdown();
        }
    }
}
