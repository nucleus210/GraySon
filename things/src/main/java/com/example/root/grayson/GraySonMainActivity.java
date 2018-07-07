package com.example.root.grayson;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioDeviceInfo;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.example.root.grayson.bluetooth.BluetoothActivityFragment;
import com.example.root.grayson.bluetooth.BluetoothConnectionService;
import com.example.root.grayson.dateTime.DateTimeFragment;
import com.example.root.grayson.googleAssistant.EmbeddedAssistant;
import com.example.root.grayson.googleAssistant.EmbeddedAssistant.ConversationCallback;
import com.example.root.grayson.googleAssistant.EmbeddedAssistant.RequestCallback;
import com.example.root.grayson.musicplayer.MediaPlayerService;
import com.example.root.grayson.musicplayer.MusicPlayerFragment;
import com.example.root.grayson.musicplayer.VideoPlayerActivity;
import com.example.root.grayson.weatherApi.CityPreference;
import com.example.root.grayson.weatherApi.WeatherFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.assistant.embedded.v1alpha2.SpeechRecognitionResult;
import com.google.auth.oauth2.UserCredentials;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static java.lang.Thread.sleep;

public class GraySonMainActivity extends AppCompatActivity implements

        TextToSpeechManager.Listener,
        PocketSphinxRecognizer.Listener,
        DateTimeFragment.OnDateTimeFragmentListener,
        BluetoothActivityFragment.OnFragmentInteractionListener,
        BluetoothActivityFragment.OnHeadlineSelectedListener,
        MusicPlayerFragment.OnFragmentMusicListener,
        VideoPlayerActivity.OnVideoInteractionListener,
        AudioManager.OnAudioFocusChangeListener,
        GoogleCloud.CloudStorageAccountListener {

    private static final String TAG = GraySonMainActivity.class.getSimpleName();
    private static final String LANGUAGE_CODE = "en-US";

    // Assistant SDK constants.
    private static final String DEVICE_MODEL_ID = "grayson-a2a66";
    private static final String DEVICE_INSTANCE_ID = "PLACEHOLDER";
    private static final int RC_SIGN_IN = 55;

    // Audio constants.
    private static final String PREF_CURRENT_VOLUME = "current_volume";
    private static final int SAMPLE_RATE = 16000;
    private static final int DEFAULT_VOLUME = 100;

    public MusicPlayerFragment musicPlayerFragment;                     // Music fragment
    GoogleSignInClient mGoogleSignInClient;                             // Declare Google client
    ImageDataProvider mImageProvider;                                   // Images scan provider
    Thread showDayPhotoThread;                                          // Thread for day photo
    Handler mHandler;                                                   // Handler
    Context mContext;                                                   // Context
    private EmbeddedAssistant mEmbeddedAssistant;                       // Google assistant embedded
    private ArrayList<String> mAssistantRespond = new ArrayList<>();    // Google assistant respond
    private ArrayList<String> mImagesPaths = new ArrayList<>();         // Images Uris
    ArrayAdapter<String> mPocketSphinxLogAdapter;                       // Speech adapter
    ArrayAdapter<String> mRealTimeDatabaseAdapter;                      // API adapter
    AudioManager mAudioManager;                                         // Audio manager
    ListView mPocketShinsLogger;                                        // Speech Logger
    ListView mRealTimeDatabaseLogger;                                   // API database logger
    AudioAttributes mAudioAttributes;                                   // Audio Focus attr
    AudioFocusRequest mAudioFocusRequest;                               // Audio focus request
    ImageShapeContainer mMidImageContainer;                             // Day photo draw
    EmbeddedAssistant embeddedAssistant;                                // Google assistant
    ImageShapeContainer mImageContainer;                                // Day photo draw
    FirebaseDatabase mFireBaseDatabase;                                 // API Database
    DatabaseReference mRootDatabaseRef;                                 // Database root reference
    DatabaseReference mChildDatabaseRef;                                // Database child reference
    DatabaseReference mChildTextDatabaseRef;                            // Database child reference

    boolean mFirstBoot = false;                                         // First boot boolean
    private Handler mMainHandler;                                       // Google assistant handler
    boolean isBluetoothOn = true;                                       // Bt boolean
    Object mFocusLock = null;                                           // Audio focus object
    AudioManager.OnAudioFocusChangeListener mAudioFocusListener;        // Audio focus listener
    SpeechBecomingNoisyReceiver mSpeechBecomingNoisyReceiver;           // Audio BroadCast Receiver
    GoogleCloud mGoogleCloud;

    IntentFilter mNoisyFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

    @Override
    public void onFragmentInteraction(Uri uri) {
    }

    @Override
    public void onArticleSelected(int position) {
    }

    @Override
    public void onMusicInitializeListener() {
    }

    @Override
    public void onTimerListener(Uri uri) {
    }

    @Override
    public void onVideoInteraction(Uri uri) {
    }


    @Override
    public void cloudStorageAccountListener(Bitmap bitmap) {
        mTextToSpeechManager.say("Downloading files from FireBase complete");
        mImageContainer.setImageBitmap(bitmap);
    }

    @Override
    public void cloudStorageErrorListener(String error) {
        mTextToSpeechManager.say("Error downloading files from FireBase");
        mPocketSphinxLogAdapter.add(error);
    }

    private enum State {
        INITIALIZING,
        LISTENING_TO_KEYPHRASE,
        CONFIRMING_KEYPHRASE,
        LISTENING_TO_ACTION,
        CONFIRMING_ACTION,
        LISTENING_TO_ASSISTANT,
        CONFIRM_ASSISTANT,
        TIMEOUT,
        ERROR
    }

    private TextToSpeechManager mTextToSpeechManager;
    private PocketSphinxRecognizer mPocketSphinxRecognizer;
    private State state;

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gray_main_activity);
        mContext = getApplicationContext();
        // Run First Time and upload demo song to Android device. This code run only in installation
        // process. After that
        SharedPreferences wmbPreference = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isFirstRun = wmbPreference.getBoolean("FirstRun", true);
        if (isFirstRun) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    copyRawData();
                    return null;
                }
            }.execute();
            SharedPreferences.Editor editor = wmbPreference.edit();
            editor.putBoolean("FirstRun", false);
            editor.apply();
        }

        // Register service broadcast receiver
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mActionThingsReceiver,
                new IntentFilter("serviceAction"));

        // Register service broadcast receiver
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mFileUriReceiver,
                new IntentFilter("serviceActionFile"));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).hide();

        mGoogleCloud = new GoogleCloud(this, this);
        embeddedAssistant = new EmbeddedAssistant(this);

        mHandler = new Handler(getMainLooper());
        mMainHandler = new Handler(getMainLooper());
        mRealTimeDatabaseLogger = findViewById(R.id.real_time_database_logger);
        mPocketShinsLogger = findViewById(R.id.speech_logger);
        mMidImageContainer = findViewById(R.id.middle_image_container);
        mImageContainer = findViewById(R.id.main_image_container);
        mImageContainer.setVisibility(View.GONE);
        mImageProvider = new ImageDataProvider();
        mMainHandler = new Handler();
        mFocusLock = new Object();

        mPocketSphinxLogAdapter = new ArrayAdapter<>(mContext, R.layout.main_logger);
        mRealTimeDatabaseAdapter = new ArrayAdapter<>(mContext, R.layout.real_time_database_message);
        mPocketShinsLogger.setAdapter(mPocketSphinxLogAdapter);
        mRealTimeDatabaseLogger.setAdapter(mRealTimeDatabaseAdapter);

        // get Images paths from Public Storage Dirs
        mImagesPaths = mImageProvider.getFilePaths(mContext);

        mAudioFocusListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        Log.e(TAG, "AUDIO FOCUS_LOSS_TRANSIENT");
                        // stopPlayback();
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN:
                        Log.i(TAG, "AUDIO FOCUS_GAIN");
                        //startPlayback();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS:
                        Log.e(TAG, "AUDIO FOCUS_LOSS");
                        //  stopPlayback();
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                        Log.i(TAG, "AUDIO FOCUS_GAIN_TRANSIENT");
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                        Log.i(TAG, "AUDIO FOCUS_GAIN_TRANSIENT_MAY_DUCK");
                        break;

                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        Log.e(TAG, "AUDIO FOCUS_LOSS_TRANSIENT_CAN_DUCK");
                        break;
                    case AudioManager.AUDIOFOCUS_REQUEST_FAILED:
                        Log.e(TAG, "AUDIO FOCUS_REQUEST_FAILED");
                        break;
                    default:
                }
            }
        };

        // Setup Audio focus attributes and references
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mNoisyFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        mSpeechBecomingNoisyReceiver = new SpeechBecomingNoisyReceiver();
        registerReceiver(mSpeechBecomingNoisyReceiver, mNoisyFilter);

        mAudioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();
        mAudioFocusRequest = new AudioFocusRequest
                .Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(mAudioAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(mAudioFocusListener, mHandler)
                .build();


//----------------------------Inflate Bluetooth fragment to UI------------------------------------//
        if (findViewById(R.id.bluetooth_fragment) != null) {
            if (savedInstanceState != null) {
                return;
            }
            BluetoothActivityFragment bluetoothFragment = new BluetoothActivityFragment();
            bluetoothFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.bluetooth_fragment,
                            bluetoothFragment,
                            "BLUETOOTH_FRAGMENT")
                    .commit();
        }
//----------------------------Inflate weather fragment to UI--------------------------------------//
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.weather_container, new WeatherFragment(), "WEATHER_FRAGMENT")
                    .commit();
        }
//----------------------------------Music player object-------------------------------------------//
        musicPlayerFragment =
                (MusicPlayerFragment) getFragmentManager()
                        .findFragmentById(R.id.music_fragment);

//----------------------------Inflate DateTime fragment to UI------------------------------------//
        if (findViewById(R.id.date_time_frag) != null) {
            DateTimeFragment dateTimeFragment = new DateTimeFragment();
            dateTimeFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.date_time_frag,
                            dateTimeFragment,
                            "DATE_FRAGMENT")
                    .commit();
        }
//--------------------------------Inflate Video fragment to UI------------------------------------//
        if (findViewById(R.id.video_frame) != null) {
            VideoPlayerActivity videoPlayerActivity = new VideoPlayerActivity();
            videoPlayerActivity.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.video_frame,
                            videoPlayerActivity,
                            "VIDEO_FRAGMENT")
                    .commit();
        }
//------------------------------------Text-to-Speech----------------------------------------------//
        // Using Text-to-Speech to indicate status change to the user
        mTextToSpeechManager = new TextToSpeechManager(this, this);

//-----------------------------------Real Time Database-------------------------------------------//

        // Obtain the FireBaseDatabase instance.
        mFireBaseDatabase = FirebaseDatabase.getInstance();

        // Obtain the FireBaseDatabase reference.
        mRootDatabaseRef = mFireBaseDatabase.getReference();

        // Obtain the FireBaseStorage instance.
        mChildDatabaseRef = mRootDatabaseRef.child("imagesUrls");
        mChildTextDatabaseRef = mRootDatabaseRef.child("messages");

//--------------------------------------Google account--------------------------------------------//
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(mContext, gso);

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(mContext);
        GoogleCloud.updateUIGoogle(account);


        // Audio routing configuration: use default routing.
        AudioDeviceInfo audioInputDevice;
        AudioDeviceInfo audioOutputDevice;
        mMainHandler = new Handler(getMainLooper());

        LocalBroadcastManager.getInstance(Objects.requireNonNull(this)).
                registerReceiver(mAssistantReceiver,
                        new IntentFilter("assistant"));

        // Force using USB:
        audioInputDevice = findAudioDevice(AudioManager.GET_DEVICES_INPUTS);
        audioOutputDevice = findAudioDevice(AudioManager.GET_DEVICES_OUTPUTS);
        // Set volume from preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int initVolume = preferences.getInt(PREF_CURRENT_VOLUME, DEFAULT_VOLUME);
        Log.i(TAG, "setting audio track volume to: " + initVolume);

        UserCredentials userCredentials = null;
        try {
            userCredentials =
                    EmbeddedAssistant.generateCredentials(this, R.raw.credentials);
        } catch (IOException | JSONException e) {
            Log.e(TAG, "error getting user credentials", e);
        }
        mEmbeddedAssistant = new EmbeddedAssistant.Builder()
                .setCredentials(userCredentials)
                .setDeviceInstanceId(DEVICE_INSTANCE_ID)
                .setDeviceModelId(DEVICE_MODEL_ID)
                .setLanguageCode(LANGUAGE_CODE)
                .setAudioInputDevice(audioInputDevice)
                .setAudioOutputDevice(audioOutputDevice)
                .setAudioSampleRate(SAMPLE_RATE)
                .setAudioVolume(initVolume)
                .setDeviceModelId(DEVICE_MODEL_ID)
                .setDeviceInstanceId(DEVICE_INSTANCE_ID)
                .setLanguageCode(LANGUAGE_CODE)
                .setRequestCallback(new RequestCallback() {
                    @Override
                    public void onRequestStart() {
                        Log.i(TAG, "starting assistant request, enable microphones");
                    }

                    @Override
                    public void onSpeechRecognition(List<SpeechRecognitionResult> results) {
                        for (final SpeechRecognitionResult result : results) {
                            Log.i(TAG, "assistant request text: " + result.getTranscript() +
                                    " stability: " + Float.toString(result.getStability()));
                            mEmbeddedAssistant.mGoogleAssistant = result.getTranscript();
                        }
                    }
                })
                .setConversationCallback(new ConversationCallback() {
                    @Override
                    public void onResponseStarted() {
                        super.onResponseStarted();
                    }

                    @Override
                    public void onResponseFinished() {
                        super.onResponseFinished();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e(TAG, "assist error: " + throwable.getMessage(), throwable);
                    }

                    @Override
                    public void onVolumeChanged(int percentage) {
                        Log.i(TAG, "assistant volume changed: " + percentage);
                        // Update our shared preferences
                        Editor editor = PreferenceManager
                                .getDefaultSharedPreferences(GraySonMainActivity.this)
                                .edit();
                        editor.putInt(PREF_CURRENT_VOLUME, percentage);
                        editor.apply();
                    }

                    @Override
                    public void onConversationFinished() {
                        Log.i(TAG, "assistant conversation finished");
                    }

                    @Override
                    public void onAssistantResponse(final String response) {
                        if (!response.isEmpty()) {
                            mMainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mAssistantRespond.add("Google Assistant: " + response);
                                }
                            });
                        }
                    }

                    public void onDeviceAction(String intentName, JSONObject parameters) {
                        if (parameters != null) {
                            Log.d(TAG, "Get device action " + intentName + " with parameters: "
                                    + parameters.toString());
                        } else {
                            Log.d(TAG, "Get device action " + intentName + " with no parameter"
                                    + "rs");
                        }
                        if (intentName.equals("action.devices.commands.OnOff")) {
                            Log.d(TAG, "Got Action from cloud");
                            // TODO
                        }
                    }
                })
                .build();
        mEmbeddedAssistant.connect();

    }
//------------------------------------------------------------------------------------------------//

    /**
     * Method is used to sing in user google account. Start the sign-in flow.
     */
//------------------------------------------------------------------------------------------------//
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
        mTextToSpeechManager.say("I'm sing in Google account");
    }

//------------------------------------------------------------------------------------------------//

    /**
     * Method is used to sing out user google account. Start the sign-out flow.
     */
//------------------------------------------------------------------------------------------------//
    void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });
    }

//------------------------------------------------------------------------------------------------//

    /**
     * Method is used to change Weather forecast city.
     */
//------------------------------------------------------------------------------------------------//
    public void changeCity(String city) {
        WeatherFragment wf = (WeatherFragment) getFragmentManager()
                .findFragmentById(R.id.weather_container);
        wf.changeCity(city);
        new CityPreference(this).setCity(city);
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Method is used to change Weather forecast city and show dialog menu. TODO speech setup
     */
//------------------------------------------------------------------------------------------------//
    private void showInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.weather_dialog_menu_title);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton(R.string.weather_dialog_menu_bt,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        changeCity(input.getText().toString());
                    }
                });
        builder.show();
    }

    //------------------------------------------------------------------------------------------------//
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.e(TAG, "Low memory");
    }

    @Override
    protected void onStart() {
        super.onStart();
        signIn();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mGoogleCloud.mAuth.getCurrentUser();
        mGoogleCloud.updateUIFirebase(currentUser);

        // Read from the database
        mChildDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                if (mFirstBoot) {
                    try {
                        String value = dataSnapshot.getValue(String.class);
                        Log.d(TAG, "Value is: " + value);
                        if (value != null) {
                            mGoogleCloud.mIncomeFileName = value;
                            downloadCloudImage();
                        }
                    } catch (NullPointerException w) {
                        Log.d(TAG, "NuLL Data FireBaseDatabase", w);
                    }
                }
                mFirstBoot = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        // Read from the database
        mChildTextDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                try {
                    String value = dataSnapshot.getValue(String.class);
                    Log.d(TAG, "Value is: " + value);
                    if (value != null) {
                        mRealTimeDatabaseAdapter.clear();
                        mRealTimeDatabaseAdapter.add(value);
                    }
                } catch (NullPointerException w) {
                    Log.d(TAG, "NuLL Data FireBaseDatabase", w);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTextToSpeechManager.onDestroy();
        mPocketSphinxRecognizer.onDestroy();
        mEmbeddedAssistant.destroy();
        signOut();
    }

//------------------------------------------------------------------------------------------------//

    /**
     * Text to speech interface
     */
//------------------------------------------------------------------------------------------------//
    @Override
    public void onTtsInitialized() {
        // Request permission RECORD_AUDIO
        mPocketSphinxRecognizer = new PocketSphinxRecognizer(this, this);
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Text to speech interface
     */
//------------------------------------------------------------------------------------------------//
    @Override
    public void onTtsSpoken() {
        switch (state) {
            case INITIALIZING:

            case CONFIRMING_ACTION:

            case TIMEOUT:
                state = State.LISTENING_TO_KEYPHRASE;
                mPocketSphinxRecognizer.startListeningToActivationPhrase();
                // release audio focus
                releaseAudioFocus(mContext);
                break;

            case CONFIRMING_KEYPHRASE:
                state = State.LISTENING_TO_ACTION;
                mPocketSphinxRecognizer.startListeningToAction();
                break;

            case CONFIRM_ASSISTANT:
                state = State.LISTENING_TO_ASSISTANT;
                requestPermanentAudioFocus(mContext);
                mEmbeddedAssistant.startConversation(getString(R.string.google_conversation_start));
                break;

            case ERROR:

        }
    }

//------------------------------------------------------------------------------------------------//

    /**
     * Interface is used to manage Audio focus requests
     *
     * @param focusChange Requested Audio focus
     */
//------------------------------------------------------------------------------------------------//
    @Override
    public void onAudioFocusChange(int focusChange) {
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Interface is used to initialize the Local Speech Recognizer and notify user when System is
     * online.
     */
//------------------------------------------------------------------------------------------------//
    @Override
    public void initializeSpeechRecognizer() {
        state = State.INITIALIZING;
        mTextToSpeechManager.say("I'm ready!");
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Interface is used to start local Speech Recognizer to wait for Action from user.
     * System notify the user and wait for Action.
     * Now user can say what have to be done and provide Action to be recognized.
     */
//------------------------------------------------------------------------------------------------//
    @Override
    public void activationPhraseDetected() {
        boolean mBool = requestAudioFocus(mContext);
        if (mBool) {
            state = State.CONFIRMING_KEYPHRASE;
            mTextToSpeechManager.say("How to help?");
        }
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Interface is used to run Google Assistant and start conversation with Google Assistant API.
     */
//------------------------------------------------------------------------------------------------//
    @Override
    public void assistantPhraseRecognized(String recognized) {
        int requestAudioFocusResult = mAudioManager.requestAudioFocus(mAudioFocusRequest);
        if (requestAudioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            boolean mBool = requestPermanentAudioFocus(mContext);
            if (mBool) {
                state = State.CONFIRM_ASSISTANT;
                mTextToSpeechManager.say("Starting Google Assistant");
            }
        }
    }

    public void logRocketSphinx(String states) {
        mPocketSphinxLogAdapter.add(states);
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Method is used to change Speech Recognizer state if user not provide any Action to the system.
     * System output will be say "Time Out" to notify user that system cancel waiting for Action.
     */
//------------------------------------------------------------------------------------------------//
    @Override
    public void speechTimeOut() {
        state = State.TIMEOUT;
        mTextToSpeechManager.say("Timeout!");
        releaseAudioFocus(mContext);
    }

//------------------------------------------------------------------------------------------------//

    /**
     * Interface is used to check what action is provided by the user and notify the system what has
     * to be done. Here is most important part where are all available Actions.
     */
//------------------------------------------------------------------------------------------------//
    @Override
    public void actionPhraseRecognized(String recognizedText) {
        state = State.CONFIRMING_ACTION;
        String answer = "";
        String mInputPhrase = recognizedText == null ? "" : recognizedText;

//------------------------------------TELL ME THE TIME--------------------------------------------//

        if (mInputPhrase.contains("time")) {
            DateFormat dateFormat = new SimpleDateFormat("HH mm", Locale.US);
            answer = "It is " + dateFormat.format(new Date());

//------------------------------------TELL ME THE DATE--------------------------------------------//

        } else if (mInputPhrase.contains("date")) {
            DateFormat dateFormat = new SimpleDateFormat("HH mm", Locale.US);
            answer = "It is " + dateFormat.format(new Date());

//------------------------------------ --LIGHT ACTIONS--------------------------------------------//

        } else if (mInputPhrase.contains("lights on") |
                mInputPhrase.contains("turn lighting on")) {
            executeActions("LIGHTS_ON");

        } else if (mInputPhrase.contains("lights off") |
                mInputPhrase.contains("turn lighting off")) {
            executeActions("LIGHTS_OFF");

        } else if (mInputPhrase.matches("red light.*")) {
            executeActions("RED_LIGHTING");

        } else if (mInputPhrase.matches("green light.*")) {
            executeActions("GREEN_LIGHTING");

        } else if (mInputPhrase.contains("blue light") |
                mInputPhrase.contains("turn lighting on")) {
            executeActions("BLUE_LIGHTING");

        } else if (mInputPhrase.contains("dim lights") |
                mInputPhrase.contains("turn lighting on")) {
            executeActions("DIM_LIGHTS");

        } else if (mInputPhrase.contains("automatic change color of the lights")
                | mInputPhrase.contains("automatic change the lights")
                | mInputPhrase.contains("automatic change color")) {
            executeActions("AUTO_LIGHTS");

//------------------------------------START MUSIC PLAYER------------------------------------------//

        } else if (mInputPhrase.matches("start music.*")
                | mInputPhrase.contains("start music player")
                | mInputPhrase.contains("music player")) {
            executeActions("START_MUSIC_PLAYER");

//------------------------------------------PLAY SONG---------------------------------------------//

        } else if (mInputPhrase.contains("play song")) {
            executeActions("PLAY_SONG");

//-----------------------------------------STOP MUSIC---------------------------------------------//

        } else if (mInputPhrase.contains("stop song")) {
            executeActions("STOP_SONG");

//------------------------------------------NEXT SONG---------------------------------------------//

        } else if (mInputPhrase.contains("next song")) {
            executeActions("NEXT_SONG");

//----------------------------------------PREVIOUS SONG-------------------------------------------//

        } else if (mInputPhrase.contains("previous song")) {
            executeActions("PREVIOUS_SONG");

//-----------------------------------------STOP MUSIC---------------------------------------------//

        } else if (mInputPhrase.contains("stop music player")) {
            executeActions("STOP_MUSIC_PLAYER");

//-----------------------------------------PLAY VIDEO---------------------------------------------//

        } else if (mInputPhrase.contains("play video")) {
            executeActions("START_VIDEO_PLAYER");

//-----------------------------------------NEXT VIDEO---------------------------------------------//

        } else if (mInputPhrase.contains("next video")) {
            executeActions("NEXT_VIDEO");

//---------------------------------------PREVIOUS VIDEO-------------------------------------------//

        } else if (mInputPhrase.contains("previous video")) {
            executeActions("PREVIOUS_VIDEO");

//-----------------------------------------STOP VIDEO---------------------------------------------//

        } else if (mInputPhrase.contains("stop video")
                |mInputPhrase.contains("stop the video")) {
            executeActions("STOP_VIDEO");

//----------------------------------REMOVE VIDEO FRAGMENT-----------------------------------------//

        } else if (mInputPhrase.matches("stop video player")) {
            executeActions("STOP_VIDEO_PLAYER");

//----------------------------------------HOW ARE YOU---------------------------------------------//

        } else if (mInputPhrase.matches("how are you.*")) {
            answer = "Could not be worst with you.";

//-------------------------------------SHOW DAY PHOTO---------------------------------------------//

        } else if (mInputPhrase.contains("show day photo")
                | mInputPhrase.contains("show photo of the day")) {
            executeActions("SHOW_DAY_PHOTO");

//-------------------------------------STOP DAY PHOTO---------------------------------------------//

        } else if (mInputPhrase.matches("stop day photo")
                | mInputPhrase.contains("stop photo")) {
            executeActions("STOP_DAY_PHOTO");

//---------------------------------------BLUETOOTH ON---------------------------------------------//

        } else if (mInputPhrase.contains("bluetooth on")) {
            executeActions("BLUETOOTH_ON");

//---------------------------------------BLUETOOTH OFF--------------------------------------------//

        } else if (mInputPhrase.contains("bluetooth off")) {
            executeActions("BLUETOOTH_OFF");

        } else if (mInputPhrase.contains("bluetooth visible")
                | mInputPhrase.contains("bluetooth discovery")) {
            executeActions("BLUETOOTH_DISCOVERY");
//----------------------------------------MENU OPERATIONS-----------------------------------------//

        } else if (mInputPhrase.contains("show menu")) {
            executeActions("SHOW_MENU");

        } else if (mInputPhrase.contains("menu next")) {
            executeActions("MENU_NEXT");

        } else if (mInputPhrase.contains("menu previous")) {
            executeActions("MENU_PREVIOUS");

        } else if (mInputPhrase.contains("menu select")) {
            executeActions("MENU_SELECT");

        } else if (mInputPhrase.contains("menu back")) {
            executeActions("MENU_BACK");

        } else if (mInputPhrase.contains("menu hide")
                |mInputPhrase.contains("hide menu")) {
            executeActions("HIDE_MENU");

//----------------------------------------GOOGLE ACTIONS------------------------------------------//

        } else if (mInputPhrase.contains("sing google")
                | mInputPhrase.contains("sing google")
                | mInputPhrase.contains("sing google account")) {
            executeActions("SING_IN_GOOGLE");

        } else if (mInputPhrase.contains("sing out google")) {
            executeActions("SING_OUT_GOOGLE");

        } else if (mInputPhrase.contains("change google")) {
            executeActions("CHANGE_GOOGLE_ACCOUNT");

        } else if (mInputPhrase.contains("change weather city")) {
            executeActions("CHANGE_WEATHER_CITY");

        } else if (mInputPhrase.contains("play youtube")) {
            executeActions("PLAY_YOUTUBE");

        } else {
            answer = "Sorry, try again.";
        }
        mTextToSpeechManager.say(answer);
        releaseAudioFocus(mContext);
    }

//------------------------------------------------------------------------------------------------//

    /**
     * Check for available Audio device. This method is used to provide other services like Google
     * Assistant to use correct Audio devices.
     */
//------------------------------------------------------------------------------------------------//
    private AudioDeviceInfo findAudioDevice(int deviceFlag) {
        AudioManager manager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        AudioDeviceInfo[] adis = manager != null ? manager.getDevices(deviceFlag) : new AudioDeviceInfo[0];
        for (AudioDeviceInfo adi : adis) {
            if (adi.getType() == AudioDeviceInfo.TYPE_USB_DEVICE) {
                return adi;
            }
        }
        return null;
    }

    //------------------------------------------------------------------------------------------------//
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

//------------------------------------------------------------------------------------------------//

    /**
     * Method to handle user selection from input dialog menu
     *
     * @param item Menu item
     */
//------------------------------------------------------------------------------------------------//
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.power:
                executeActions("TURN_OFF");
                return true;

            case R.id.change_city:
                showInputDialog();
                return true;

            case R.id.bt_on_off:
                executeActions("BLUETOOTH_ON");
                return true;

            case R.id.bt_discovery:
                executeActions("BLUETOOTH_DISCOVERY");
                return true;

            case R.id.bt_search:
                executeActions("BLUETOOTH_SCAN");
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Method is used to handle sing in results and provide information to the user.
     */
//------------------------------------------------------------------------------------------------//
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            GoogleCloud.updateUIGoogle(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            GoogleCloud.updateUIGoogle(null);
        }
    }


//------------------------------------------------------------------------------------------------//

    /**
     * Hide System UI to get full screen mode
     */
//------------------------------------------------------------------------------------------------//
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Hide System UI to get full screen mode
     */
//------------------------------------------------------------------------------------------------//
    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Shows the system bars by removing all the flags
     * except for the ones that make the content appear under the system bars.
     */
//------------------------------------------------------------------------------------------------//
    @SuppressWarnings("unused")
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

//------------------------------------------------------------------------------------------------//
    /**
     * Broadcast receiver. Receive messages with actions from remote control device.
     * Message FROM: {@link BluetoothConnectionService}
     */
//------------------------------------------------------------------------------------------------//
    private final BroadcastReceiver mActionThingsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("service action");
            Log.d("receiver", "Got action position message: " + message);
            try {
                executeActions(message);         //execute current action from remote control device
            } catch (NumberFormatException nfe) {
                System.out.println("Could not parse " + nfe);
            }
        }
    };
//------------------------------------------------------------------------------------------------//
    /**
     * Broadcast receiver. Receive messages with actions from remote control device.
     * Message FROM: {@link BluetoothConnectionService}
     */
//------------------------------------------------------------------------------------------------//
    private final BroadcastReceiver mFileUriReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String uri = intent.getStringExtra("service uri");
            Uri mUri;
            mUri = Uri.parse(uri);
            Log.d("receiver", "Got action position message: " + uri);
            try {
                Bitmap bitmap = decodeUriToBitmap(mContext, mUri);
                mMidImageContainer.setVisibility(View.VISIBLE);
                mMidImageContainer.setImageBitmap(bitmap);
            } catch (NumberFormatException nfe) {
                System.out.println("Could not parse " + nfe);
            }
        }
    };
//------------------------------------------------------------------------------------------------//

    /**
     * Action received from Broadcast receiver.
     * Receive message with actions from remote control.
     */
//------------------------------------------------------------------------------------------------//
    public void executeActions(String action) {
        ActionMenu actions = ActionMenu.valueOf(action);
        switch (actions) {
            case INITIALIZING:
                Log.d(TAG, "Initializing");
                break;
            case LISTENING_TO_KEYPHRASE:
                Log.d(TAG, "listening keyphrase");
                break;

            case LISTENING_TO_ACTION:
                Log.d(TAG, "Listening action");
                state = State.LISTENING_TO_ACTION;
                mPocketSphinxRecognizer.startListeningToAction();
                break;

            case LISTENING_TO_ASSISTANT:
                Log.d(TAG, "Listening assistant");
                break;

            case BT_DOWNLOAD:
                mTextToSpeechManager.say("Download complete");

                break;

            case START_MUSIC_PLAYER:
                // check Fragment is visible
                Fragment playerFragment = getSupportFragmentManager().
                        findFragmentByTag("MUSIC_FRAGMENT");

                if (playerFragment != null) {
                    Log.d(TAG, "Music Player is running");
                    return;

                } else {
                    // Inflate music player weatherFragment
                    if (findViewById(R.id.music_fragment) != null) {
                        musicPlayerFragment = new MusicPlayerFragment();
                        musicPlayerFragment.setArguments(getIntent().getExtras());
                        getFragmentManager().beginTransaction()
                                .add(R.id.music_fragment,
                                        musicPlayerFragment,
                                        "MUSIC_FRAGMENT")
                                .commit();
                        Log.d(TAG, "Start music player");
                        // System output
                        mTextToSpeechManager.say("I'm starting music player");
                    }
                }
                break;

            case PLAY_SONG:
                // System output
                mTextToSpeechManager.say("I'm playing music");
                // Stop music player
                try {
                    MusicPlayerFragment musicPlayerFragment =
                            (MusicPlayerFragment) getFragmentManager()
                                    .findFragmentById(R.id.music_fragment);
                    musicPlayerFragment.playSong();
                } catch (NullPointerException s) {
                    Log.e(TAG, "Null pointer: ", s);
                }
                break;

            case STOP_SONG:
                mTextToSpeechManager.say("I'm stop music");
                // Stop music player
                try {
                    MusicPlayerFragment musicPlayerFragment =
                            (MusicPlayerFragment) getFragmentManager()
                                    .findFragmentById(R.id.music_fragment);
                    musicPlayerFragment.playSong();
                } catch (NullPointerException s) {
                    Log.e(TAG, "Null pointer: ", s);
                }
                break;

            case NEXT_SONG:
                Log.d(TAG, "Next Song");
                // System output
                mTextToSpeechManager.say("Playing next song");
                // Play next song
                try {
                    MusicPlayerFragment musicPlayerFragment =
                            (MusicPlayerFragment) getFragmentManager()
                                    .findFragmentById(R.id.music_fragment);
                    musicPlayerFragment.nextSong();
                } catch (NullPointerException s) {
                    Log.e(TAG, "Null pointer: ", s);
                }
                break;

            case PREVIOUS_SONG:
                Log.d(TAG, "Previous song");
                // System output
                mTextToSpeechManager.say("Playing previous song");
                // Play previous song
                try {
                    MusicPlayerFragment musicPlayerFragment =
                            (MusicPlayerFragment) getFragmentManager()
                                    .findFragmentById(R.id.music_fragment);
                    musicPlayerFragment.previousSong();
                } catch (NullPointerException s) {
                    Log.e(TAG, "Null pointer: ", s);
                }
                break;

            case STOP_MUSIC_PLAYER:
                Log.d(TAG, "Stop music player");
                // System output
                mTextToSpeechManager.say("Stop music player");
                // Remove player Fragment
                Fragment player = getSupportFragmentManager().findFragmentByTag("MUSIC_FRAGMENT");
                if (player != null)
                    getSupportFragmentManager().beginTransaction()
                            .remove(player).commit();

                break;

            case START_VIDEO_PLAYER:
                // System output
                mTextToSpeechManager.say("I'm playing video");
                // play video
                try {
                    VideoPlayerActivity videoPlayerActivity =
                            (VideoPlayerActivity) getSupportFragmentManager()
                                    .findFragmentById(R.id.video_frame);
                    videoPlayerActivity.playPause();
                } catch (NullPointerException s) {
                    Log.e(TAG, "Null pointer: ", s);
                }
                break;

            case STOP_VIDEO:
                // System output
                mTextToSpeechManager.say("I'm stop video");
                // play video
                try {
                    VideoPlayerActivity videoPlayerActivity =
                            (VideoPlayerActivity) getSupportFragmentManager()
                                    .findFragmentById(R.id.video_frame);
                    videoPlayerActivity.playPause();
                } catch (NullPointerException s) {
                    Log.e(TAG, "Null pointer: ", s);
                }
                break;

            case PREVIOUS_VIDEO:
                // System output
                mTextToSpeechManager.say("I'm playing previous video");
                // play video
                try {
                    VideoPlayerActivity videoPlayerActivity =
                            (VideoPlayerActivity) getSupportFragmentManager()
                                    .findFragmentById(R.id.video_frame);
                    videoPlayerActivity.previousVideo();
                } catch (NullPointerException s) {
                    Log.e(TAG, "Null pointer: ", s);
                }
                break;

            case NEXT_VIDEO:
                // System output
                mTextToSpeechManager.say("I'm playing next video");
                // play video
                try {
                    VideoPlayerActivity videoPlayerActivity =
                            (VideoPlayerActivity) getSupportFragmentManager()
                                    .findFragmentById(R.id.video_frame);
                    videoPlayerActivity.nextVideo();
                } catch (NullPointerException s) {
                    Log.e(TAG, "Null pointer: ", s);
                }
                break;

            case SHOW_MENU:
                Log.d(TAG, "Showing menu");
                mTextToSpeechManager.say("I'm showing menu");
                invalidateOptionsMenu();
                Objects.requireNonNull(getSupportActionBar()).show();
                //TODO
                break;
            case MENU_SELECT:
                Log.d(TAG, "Selecting from menu");
                mTextToSpeechManager.say("Selecting");
                //TODO
                break;
            case MENU_NEXT:
                Log.d(TAG, "Menu next");
                mTextToSpeechManager.say("menu next");
                //TODO
                break;

            case MENU_BACK:
                Log.d(TAG, "Menu back");
                mTextToSpeechManager.say("menu back");
                //TODO
                break;
            case MENU_PREVIOUS:
                Log.d(TAG, "Menu previous");
                mTextToSpeechManager.say("menu previous");
                //TODO
                break;
            case HIDE_MENU:
                Log.d(TAG, "Hide Menu");
                mTextToSpeechManager.say("I'm hiding menu");
                invalidateOptionsMenu();
                Objects.requireNonNull(getSupportActionBar()).hide();
                //TODO
                break;
            case PLAY_YOUTUBE:
                Log.d(TAG, "Play Youtube");
                mTextToSpeechManager.say("I'm playing Youtube");
                //TODO
                break;

            case BLUETOOTH_ON:
                // System output
                mTextToSpeechManager.say("Turn Bluetooth on");
                // play video
                if (!isBluetoothOn) {
                    try {
                        BluetoothActivityFragment bluetoothActivityFragment =
                                (BluetoothActivityFragment) getSupportFragmentManager()
                                        .findFragmentById(R.id.bluetooth_fragment);
                        bluetoothActivityFragment.enableDisableBT();
                        isBluetoothOn = true;
                    } catch (NullPointerException s) {
                        Log.e(TAG, "Null pointer: ", s);
                    }
                }
                break;

            case BLUETOOTH_OFF:
                // System output
                mTextToSpeechManager.say("Turn Bluetooth off");
                // play video
                if (isBluetoothOn) {
                    try {
                        BluetoothActivityFragment bluetoothActivityFragment =
                                (BluetoothActivityFragment) getSupportFragmentManager()
                                        .findFragmentById(R.id.bluetooth_fragment);
                        bluetoothActivityFragment.enableDisableBT();
                        isBluetoothOn = false;
                    } catch (NullPointerException s) {
                        Log.e(TAG, "Null pointer: ", s);
                    }
                }
                break;

            case BLUETOOTH_DISCOVERY:
                // System output
                mTextToSpeechManager.say("Turn Bluetooth discovery for 30 seconds");
                // Bluetooth discovery
                try {
                    BluetoothActivityFragment bluetoothActivityFragment =
                            (BluetoothActivityFragment) getSupportFragmentManager()
                                    .findFragmentById(R.id.bluetooth_fragment);
                    bluetoothActivityFragment.btnEnableDisable_Discoverable();
                } catch (NullPointerException s) {
                    Log.e(TAG, "Null pointer: ", s);
                }
                break;

            case BLUETOOTH_SCAN:
                // System output
                mTextToSpeechManager.say("Scanning for unpaired devices");
                // Bluetooth discovery
                try {
                    BluetoothActivityFragment bluetoothActivityFragment =
                            (BluetoothActivityFragment) getSupportFragmentManager()
                                    .findFragmentById(R.id.bluetooth_fragment);
                    bluetoothActivityFragment.discoverUnpairedDevices();
                } catch (NullPointerException s) {
                    Log.e(TAG, "Null pointer: ", s);
                }
                break;

            case FIRE_BASE_STORAGE:
                Log.d(TAG, "Fire base storage");
                // System output
                mTextToSpeechManager.say("I am hunger for files. I'm will eat em all");
                //downloadImageFromFireBase();
                if (showDayPhotoThread != null && showDayPhotoThread.isAlive()) {
                    try {
                        Thread.sleep(5000);
                        showDayPhotoThread.interrupt();
                        mImageContainer.setImageBitmap(null);
                    } catch (InterruptedException ex) {
                        Log.d(TAG, "Interrupt Running Thread", ex);
                    } finally {
                        // download images from cloud
                        mImageContainer.setVisibility(View.VISIBLE);
                        mGoogleCloud.setImageFromCloud();
                    }
                } else {
                    // download images from cloud
                    mImageContainer.setVisibility(View.VISIBLE);
                    mGoogleCloud.setImageFromCloud();
                }
                break;

            case CONFIRM_ASSISTANT:
                Log.d(TAG, "Google Assistant");
                mTextToSpeechManager.say("Starting Google Assistant");
                state = State.CONFIRM_ASSISTANT;
                break;

            case SHOW_DAY_PHOTO:
                Log.d(TAG, "Showing day photo");
                // System output
                mTextToSpeechManager.say("Showing day photo");

                if (showDayPhotoThread != null && showDayPhotoThread.isAlive()) {
                    try {
                        Thread.sleep(5000);
                        showDayPhotoThread.interrupt();
                        mImageContainer.setImageBitmap(null);
                        mImageContainer.setVisibility(View.GONE);
                        mMidImageContainer.setVisibility(View.GONE);
                    } catch (InterruptedException ex) {
                        Log.d(TAG, "Interrupt Running Thread", ex);
                    }
                    mImageContainer.setImageBitmap(null);
                    mImageContainer.setVisibility(View.VISIBLE);
                    Runnable task = new ShowDayPhoto();
                    showDayPhotoThread = new Thread(task);
                    showDayPhotoThread.setName("showImageThread");
                    showDayPhotoThread.start();
                } else {
                    mImageContainer.setImageBitmap(null);
                    mImageContainer.setVisibility(View.VISIBLE);
                    Runnable task = new ShowDayPhoto();
                    showDayPhotoThread = new Thread(task);
                    showDayPhotoThread.setName("showImageThread");
                    showDayPhotoThread.start();
                }
                break;

            case STOP_DAY_PHOTO:
                Log.d(TAG, "Remove day photo");
                // System output
                mTextToSpeechManager.say("Stop day photo");
                if (showDayPhotoThread != null && showDayPhotoThread.isAlive()) {
                    // Remove day photo
                    try {
                        Thread.sleep(5000);
                        showDayPhotoThread.interrupt();
                        mImageContainer.setImageBitmap(null);
                        mImageContainer.setVisibility(View.GONE);
                        mMidImageContainer.setVisibility(View.GONE);
                    } catch (InterruptedException ex) {
                        Log.d(TAG, "Interrupt Running Thread", ex);
                    }
                }
                break;

            case CLOUD_STORAGE:
                Log.d(TAG, "Cloud Storage");
                mTextToSpeechManager.say("Connecting Cloud Storage");
                String email = getString(R.string.TYPE_EMAIL_HERE);
                String pass = getString(R.string.ACCOUNT_PASSWORD);
                mGoogleCloud.singInFireBaseStorage(email, pass);
                break;

            case SING_IN_GOOGLE:
                Log.d(TAG, "Sing In Google account");
                mTextToSpeechManager.say("Sing In Google account");
                signIn(); //start firebase connection
                mPocketSphinxLogAdapter.add(mGoogleCloud.showGoogleAccount());
                break;

            case SING_OUT_GOOGLE:
                Log.d(TAG, "Sing Out Google account");
                mTextToSpeechManager.say("Sing Out Google account");
                signOut();
                break;

            case SHOW_WEATHER:
                Log.d(TAG, "Start WeatherModel");
                mTextToSpeechManager.say("Ok i am starting weather forecast");
                //TODO
                break;

            case CHANGE_WEATHER_CITY:
                mTextToSpeechManager.say("I'm changing weather city");
                hideSystemUI();
                showInputDialog();
                break;

            case UPLOAD_IMAGE:
                Log.d(TAG, "Uploading files...");
                mTextToSpeechManager.say("Starting file uploading.");
                break;

            case LIGHTS_ON:
                Log.d(TAG, "Lights on");
                mTextToSpeechManager.say("Turning light on");
                break;

            case LIGHTS_OFF:
                Log.d(TAG, "Lights off");
                mTextToSpeechManager.say("Turning light off");
                break;

            case RED_LIGHTING:
                Log.d(TAG, "Turning light red");
                mTextToSpeechManager.say("Turning light red");
                break;

            case GREEN_LIGHTING:
                Log.d(TAG, "Turning light green");
                mTextToSpeechManager.say("Turning light green");
                break;

            case BLUE_LIGHTING:
                Log.d(TAG, "Turning light blue");
                mTextToSpeechManager.say("Turning light blue");
                break;

            case AUTO_LIGHTS:
                Log.d(TAG, "Automatic change lights colors");
                mTextToSpeechManager.say("Automatic change lights colors");
                break;

            case SHOW_TIME:
                Log.d(TAG, "Ok, you need a time");
                mTextToSpeechManager.say("Ok i am starting weather forecast");
                break;

            case TURN_OFF:
                Log.d(TAG, "Turn Off");
                mTextToSpeechManager.say("Again, Ok I'm turning off");
                // Start listening wake up phrase
                state = State.LISTENING_TO_KEYPHRASE;
                mPocketSphinxRecognizer.startListeningToActivationPhrase();
                // Remove day photo
                if (showDayPhotoThread != null && showDayPhotoThread.isAlive()) {
                    try {
                        Thread.sleep(5000);
                        showDayPhotoThread.interrupt();
                        mImageContainer.setImageBitmap(null);
                        mImageContainer.setVisibility(View.GONE);
                    } catch (InterruptedException ex) {
                        Log.d(TAG, "Interrupt Running Thread", ex);
                    }
                }
                turnOffScreen();
                break;

            case TURN_ON:
                Log.d(TAG, "Turn On");
                mTextToSpeechManager.say("Yaaaaaaaaahoooooo");
                turnOnScreen();
                break;
        }
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Starting new thread and show day photo on screen. Change photos every 10 sec.
     */
//------------------------------------------------------------------------------------------------//
    public class ShowDayPhoto implements Runnable {
        boolean mainCon = true;

        public void run() {
            int i;
            try {
                sleep(2000);
                for (i = 0; i < mImagesPaths.size() - 1; i++) {
                    if (i < mImagesPaths.size()) {
                        if (i != 0) {
                            sleep(14000);
                        }
                        final int finalI = i;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                String path = mImagesPaths.get(finalI);
                                File imgFile = new File(path);
                                if (imgFile.exists()) {
                                    final Bitmap myBitmap = BitmapFactory
                                            .decodeFile(imgFile.getAbsolutePath());

                                    if (mainCon) {
                                        mMidImageContainer.clearColorFilter();
                                        mImageContainer.setImageBitmap(myBitmap);
                                        ObjectAnimator fadeInMain = ObjectAnimator
                                                .ofFloat(mImageContainer,
                                                        "alpha", 0f, 100f);
                                        fadeInMain.setDuration(4000);
                                        fadeInMain.start();
                                        mainCon = false;
                                    } else {
                                        mImageContainer.clearColorFilter();
                                        mMidImageContainer.setImageBitmap(myBitmap);
                                        ObjectAnimator fadeIn = ObjectAnimator
                                                .ofFloat(mMidImageContainer,
                                                        "alpha", 0f, 100f);
                                        fadeIn.setDuration(4000);
                                        fadeIn.start();
                                        mainCon = true;
                                    }

                                    if (mainCon) {
                                        ObjectAnimator fadeOutMain = ObjectAnimator
                                                .ofFloat(mImageContainer,
                                                        "alpha", 100f, 0f);
                                        fadeOutMain.setDuration(4000);
                                        fadeOutMain.start();
                                    } else {
                                        ObjectAnimator fadeOut = ObjectAnimator
                                                .ofFloat(mMidImageContainer,
                                                        "alpha", 100f, 0f);
                                        fadeOut.setDuration(4000);
                                        fadeOut.start();
                                    }
                                }
                            }
                        });
                        if (finalI == mImagesPaths.size() - 2) {
                            i = 0;
                        }
                    } else {
                        i = 1;
                    }
                }
            } catch (InterruptedException e) {
                Log.d(TAG, "Interrupted Threat Day Photo.", e);
            } finally {
                Log.d(TAG, "Stopping Day Photo.");
            }
        }
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Method is used to copy content of raw directory to External Device Storage.
     */
//------------------------------------------------------------------------------------------------//
    private void copyRawData() {
        createExternalStoragePublicFile(GraySonMainActivity.this,
                R.raw.always_the_alibi_ain_t_another_girl,
                "always_the_alibi_ain_t_another_girl.mp3",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
        createExternalStoragePublicFile(GraySonMainActivity.this, R.raw.chris_zabriskie,
                "chris_zabriskie.mp3",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
        createExternalStoragePublicFile(GraySonMainActivity.this,
                R.raw.igor_pumphonia_coffee_time_rock_me_roll_me,
                "igor_pumphonia_coffee_time_rock_me_roll_me.mp3",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
        createExternalStoragePublicFile(GraySonMainActivity.this,
                R.raw.jasmine_jordan_time_travel_feat_blanchard_de_wave,
                "jasmine_jordan_time_travel_feat_blanchard_de_wave.mp3",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
        createExternalStoragePublicFile(GraySonMainActivity.this,
                R.raw.jekk_so_strong_la_style_remix, "jekk_so_strong_la_style_remix.mp3",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
        createExternalStoragePublicFile(GraySonMainActivity.this,
                R.raw.jekk_strong, "jekk_strong.mp3",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
        createExternalStoragePublicFile(GraySonMainActivity.this,
                R.raw.michael_mc_eachern_gone, "michael_mc_eachern_gone.mp3",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
        createExternalStoragePublicFile(GraySonMainActivity.this,
                R.raw.mickey_blue_what_i_wouldn_t_do,
                "mickey_blue_what_i_wouldn_t_do.mp3",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
        createExternalStoragePublicFile(GraySonMainActivity.this,
                R.raw.roller_genoa_safe_and_warm_in_hunter_s_arms,
                "roller_genoa_safe_and_warm_in_hunter_s_arms.mp3",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
        createExternalStoragePublicFile(GraySonMainActivity.this,
                R.raw.silent_partner_highway_danger,
                "silent_partner_highway_danger.mp3",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
        createExternalStoragePublicFile(GraySonMainActivity.this,
                R.raw.silent_partner_pomade, "silent_partner_pomade.mp3",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
        createExternalStoragePublicFile(GraySonMainActivity.this,
                R.raw.the_madpix_project_moments, "the_madpix_project_moments.mp3",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
        createExternalStoragePublicFile(GraySonMainActivity.this,
                R.raw.tracing_arcs_voodoo_zengineers_undecided_remix,
                "tracing_arcs_voodoo_zengineers_undecided_remix.mp3",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));

        createExternalStoragePublicFile(GraySonMainActivity.this,
                R.raw.jasmine_jordan_time_travel_feat_blanchard_de_wave,
                "jasmine_jordan_time_travel_feat_blanchard_de_wave.mp4",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES));

        createExternalStoragePublicFile(GraySonMainActivity.this,
                R.raw.jasmine_jordan_time_travel_feat_blanchard_de_wave,
                "janji_heroes_tonight_feat_johnning_nsc_release_music_video.mp4",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES));

        createExternalStoragePublicFile(GraySonMainActivity.this,
                R.raw.aero_chord_anuka_incomplete_nsc_release,
                "aero_chord_anuka_incomplete_nsc_release.mp4",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES));

        createExternalStoragePublicFile(GraySonMainActivity.this,
                R.raw.img1018629897479156934,
                "img1018629897479156934.png",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));

        createExternalStoragePublicFile(GraySonMainActivity.this,
                R.raw.img2570173737262741414,
                "img2570173737262741414.png",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
        createExternalStoragePublicFile(GraySonMainActivity.this,
                R.raw.img2773532792530558229,
                "img2773532792530558229.png",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
        createExternalStoragePublicFile(GraySonMainActivity.this,
                R.raw.img7888210504008230974,
                "img7888210504008230974.png",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
        File file = new File("file://" + Environment.getExternalStorageDirectory());
        Uri uri = Uri.fromFile(file);
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
        sendBroadcast(intent);

    }
//------------------------------------------------------------------------------------------------//

    /**
     * Copy files to public external storage
     *
     * @param context      default context
     * @param resourceId   file resource id
     * @param resourceName file name
     * @param filePath     file path
     */
//------------------------------------------------------------------------------------------------//
    void createExternalStoragePublicFile(Context context,
                                         int resourceId,
                                         String resourceName,
                                         File filePath) {
        // Create a path where we will place our picture in the user's
        // public pictures directory.  Note that you should be careful about
        // what you place here, since the user often manages these files.  For
        // pictures and other media owned by the application, consider
        // Context.getExternalMediaDir().

        File file = new File(filePath, resourceName);

        try {
            // Make sure the Pictures directory exists.
            filePath.mkdirs();

            // Very simple code to copy a picture from the application's
            // resource into the external file.  Note that this code does
            // no error checking, and assumes the picture is small (does not
            // try to copy it in chunks).  Note that if external storage is
            // not currently mounted this will silently fail.
            InputStream is = getResources().openRawResource(resourceId);
            OutputStream os = new FileOutputStream(file);
            byte[] data = new byte[is.available()];
            is.read(data);
            os.write(data);
            is.close();
            os.close();

            // Tell the media scanner about the new file so that it is
            // immediately available to the user.
            MediaScannerConnection.scanFile(context,
                    new String[]{file.toString()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                        }
                    });
        } catch (IOException e) {
            // Unable to create file, likely because external storage is
            // not currently mounted.
            Log.w("ExternalStorage", "Error writing " + file, e);
        }
    }

//------------------------------------------------------------------------------------------------//

    /**
     * Request Audio Focus
     *
     * @param context default context
     */
//------------------------------------------------------------------------------------------------//
    private boolean requestAudioFocus(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        // Request audio focus for playback
        int result = Objects.requireNonNull(audioManager).requestAudioFocus(mAudioFocusRequest);
        synchronized (mFocusLock) {
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                Log.d("AudioFocus", "Audio focus received");
                return true;
            } else {
                Log.d("AudioFocus", "Audio focus NOT received");
                return false;
            }
        }
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Release Audio Focus
     *
     * @param context default context
     */
//------------------------------------------------------------------------------------------------//
    private void releaseAudioFocus(Context context) {
        mAudioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();
        mAudioFocusRequest = new AudioFocusRequest
                .Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(mAudioAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(mAudioFocusListener, mHandler)
                .build();
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.abandonAudioFocusRequest(mAudioFocusRequest);
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Request Audio Focus
     *
     * @param context default context
     */
//------------------------------------------------------------------------------------------------//
    private boolean requestPermanentAudioFocus(Context context) {
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mAudioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();
        mAudioFocusRequest = new AudioFocusRequest
                .Builder(AudioManager.STREAM_MUSIC)
                .setFocusGain(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(mAudioAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(mAudioFocusListener, mHandler)
                .build();
        synchronized (mFocusLock) {
            // Request audio focus for playback
            int result = mAudioManager.requestAudioFocus(mAudioFocusRequest);
            return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        }
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Music become noisy broadcast receiver
     */
//------------------------------------------------------------------------------------------------//

    public class SpeechBecomingNoisyReceiver extends BroadcastReceiver {
        public SpeechBecomingNoisyReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.requireNonNull(intent.getAction()).equals(
                    android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                // Pause the playback
                Intent pauseIntent = new Intent(context, VideoPlayerActivity.class);
                pauseIntent.setAction(MediaPlayerService.TELEPHONY_SERVICE);
                context.startService(pauseIntent);
            }
        }
    }

//------------------------------------------------------------------------------------------------//
    /**
     * Broadcast Receiver that detects conversation with assistant is finish and give control
     * back to local Speech Recognizer
     * Message send from {@link EmbeddedAssistant}
     */
//------------------------------------------------------------------------------------------------//

    private final BroadcastReceiver mAssistantReceiver = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("receiver", "Got action assistant action: ");
            mPocketSphinxRecognizer.startListeningToActivationPhrase();
            mEmbeddedAssistant.stopConversation();
        }
    };
//------------------------------------------------------------------------------------------------//

    /**
     * Method is used to download images from FireBaseStorage and show on screen
     */
//------------------------------------------------------------------------------------------------//
    private void downloadCloudImage() {
        // System output
        mTextToSpeechManager.say("I am hunger for files. I'm will eat em all");
        //downloadImageFromFireBase();
        if (showDayPhotoThread != null && showDayPhotoThread.isAlive()) {
            try {
                Thread.sleep(5000);
                showDayPhotoThread.interrupt();
                mImageContainer.setImageBitmap(null);
            } catch (InterruptedException ex) {
                Log.d(TAG, "Interrupt Running Thread", ex);
            } finally {
                // download images from cloud
                mImageContainer.setVisibility(View.VISIBLE);
                mGoogleCloud.setImageFromCloud();
            }
        } else {
            // download images from cloud
            mImageContainer.setVisibility(View.VISIBLE);
            mGoogleCloud.setImageFromCloud();
        }
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Method is used to set screen in sleep mode
     * TODO
     */
//------------------------------------------------------------------------------------------------//
    private void turnOffScreen() {
        try {
            Settings.Global.putInt(getContentResolver(),
                    Settings.Global.STAY_ON_WHILE_PLUGGED_IN, 0);
        } catch (Exception e) {
            Log.d(TAG, "Turn screen off failed", e);
        }
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Method is used to wake up screen from sleep mode
     * TODO
     */
//------------------------------------------------------------------------------------------------//
    public void turnOnScreen() {
        Settings.Global.putInt(getContentResolver(),
                Settings.Global.STAY_ON_WHILE_PLUGGED_IN, 1);
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Method to set bitmap from file uri
     *
     * @param context Default application context
     * @param sendUri File uri
     * @return bitmap Return bitmap
     */
//------------------------------------------------------------------------------------------------//
    public static Bitmap decodeUriToBitmap(Context context, Uri sendUri) {
        Bitmap getBitmap = null;
        try {
            InputStream image_stream;
            try {
                image_stream = context.getContentResolver().openInputStream(sendUri);
                getBitmap = BitmapFactory.decodeStream(image_stream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getBitmap;
    }
}