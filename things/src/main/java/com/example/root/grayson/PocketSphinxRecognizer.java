package com.example.root.grayson;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

public class PocketSphinxRecognizer implements RecognitionListener {

    public interface Listener {
        @SuppressWarnings("unused")
        void onAudioFocusChange(int focusChange);

        void initializeSpeechRecognizer();

        void activationPhraseDetected();

        void actionPhraseRecognized(String recognizedText);

        void assistantPhraseRecognized(String recognized);

        void speechTimeOut();

        void logRocketSphinx(String states);
    }

    private static final String TAG = PocketSphinxRecognizer.class.getSimpleName();
    private static final String ACTIVATION_PHRASE = "okay gray";

    /* Named searches allow to quickly reconfigure the decoder */
    private static final String WAKEUP_PHRASE = "wakeup";
    private static final String ACTION_PHRASE = "action";
    private static final String GOOGLE_ASSISTANT_PHRASE = "okay google";

    private final Listener mListener;

    private SpeechRecognizer mRecognizer;

    PocketSphinxRecognizer(Context context, Listener listener) {
        this.mListener = listener;
        runRecognizer(context);
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d(TAG, "onBeginningOfSpeech");
        String message = "onBeginningOfSpeech";
        mListener.logRocketSphinx(message);
    }

    /**
     * We stop mRecognizer here to get a final result
     */
    @Override
    public void onEndOfSpeech() {
        Log.d(TAG, "onEndOfSpeech");
        String message = "onEndOfSpeech";
        mListener.logRocketSphinx(message);

        if (!mRecognizer.getSearchName().equals(WAKEUP_PHRASE)) {
            Log.i(TAG, "End of speech. Stop mRecognizer");
            String messages = "End of speech. Stop mRecognizer";
            mListener.logRocketSphinx(messages);
            mRecognizer.stop();
        }
    }

    /**
     * In partial result we get quick updates about current hypothesis. In
     * keyword spotting mode we can react here, in other modes we need to wait
     * for final result in onResult.
     */
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null) {
            return;
        }
        String text = hypothesis.getHypstr();
        if (text.equals(ACTIVATION_PHRASE)) {
            Log.i(TAG, "Activation keyphrase detected during a partial result");
            mListener.logRocketSphinx(text);
            mRecognizer.stop();
        } else {
            Log.i(TAG, "On partial result: " + text);
        }
    }

    /**
     * This callback is called when we stop the mRecognizer.
     */
    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis == null) {
            return;
        }

        String text = hypothesis.getHypstr();
        Log.i(TAG, "On result: " + text);
        mListener.logRocketSphinx(text);

        switch (text) {
            case ACTIVATION_PHRASE:
                mListener.activationPhraseDetected();
                mListener.logRocketSphinx(text);
                break;
            case GOOGLE_ASSISTANT_PHRASE:
                mListener.assistantPhraseRecognized(text);
                mListener.logRocketSphinx(text);
                break;

            default:
                mListener.actionPhraseRecognized(text);
                mListener.logRocketSphinx(text);
                break;
        }
    }

    @Override
    public void onError(Exception e) {
        Log.e(TAG, "On error", e);
    }

    @Override
    public void onTimeout() {
        Log.i(TAG, "Timeout!");
        String message = "onTimeOut!";
        mListener.logRocketSphinx(message);
        mRecognizer.stop();
        mListener.speechTimeOut();
    }

    void startListeningToActivationPhrase() {
        Log.i(TAG, "Start listening for the \"ok gray\" keyphrase");
        mRecognizer.startListening(WAKEUP_PHRASE);
    }

    void startListeningToAction() {
        Log.i(TAG, "Start listening for some actions with a 10secs timeout");
        mRecognizer.startListening(ACTION_PHRASE, 10000);
    }

    @SuppressWarnings("unused")
    void startRecognizer () {
        mRecognizer.addListener(this);
    }

    @SuppressWarnings("unused")
    void stopListening () {
        Log.i(TAG, "Stop listening");
        stopRecognized();
    }

    void onDestroy() {
        if (mRecognizer != null) {
            mRecognizer.cancel();
            mRecognizer.shutdown();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void runRecognizer(final Context context) {
        Log.d(TAG, "Recognizer setup");

        // Recognizer initialization is a time-consuming and it involves IO, so we execute it in async task
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(context);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    Log.e(TAG, "Failed to initialize recognizer: " + result);
                } else {
                    mListener.initializeSpeechRecognizer();
                }
            }
        }.execute();
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        mRecognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "custom.dic"))

                // To disable logging of raw audio comment out this call (takes a lot of space on the device)
                // .setRawLogDir(assetsDir)

                // Threshold to tune for keyphrase to balance between false alarms and misses
                .setKeywordThreshold(1e-35f)

                .setFloat("-lw", 8.5)

                // Use context-independent phonetic search, context-dependent is too slow for mobile
                .setBoolean("-allphone_ci", true)

                .getRecognizer();
        mRecognizer.addListener(this);
        // Custom mRecognizer
        // Create keyword-activation search.
        mRecognizer.addKeyphraseSearch(WAKEUP_PHRASE, ACTIVATION_PHRASE);
        mRecognizer.addNgramSearch(ACTION_PHRASE, new File(assetsDir, "adapt.lm"));
    }

    private void stopRecognized(){
        mRecognizer.removeListener(this);
    }
}
