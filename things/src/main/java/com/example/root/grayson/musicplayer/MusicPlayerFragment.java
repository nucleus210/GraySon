package com.example.root.grayson.musicplayer;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.root.grayson.R;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MusicPlayerFragment.OnFragmentMusicListener} interface
 * to handle interaction events.
 * Use the {@link MusicPlayerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MusicPlayerFragment extends Fragment implements
        AudioManager.OnAudioFocusChangeListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private OnFragmentMusicListener mListener;
    private static final String TAG = "Main Player Activity";

    // define view objects
    private Boolean mBound;
    private Handler mHandler;
    private SeekBar mSeekBar;
    private Runnable mRunnable;
    private TextView mSongTime;
    private TextView mSongTitle;
    private TextView mSongTimer;
    private TextView mSongArtist;
    private RotateAnimation anim;
    private ImageView mAnimatedView;
    public ImageButton mPlayButton;
    private ImageButton mNextButton;
    private ArrayList<Song> mSongList;
    private ImageButton mPreviousButton;
    private boolean mHomeButtonPressed = false;
    private MediaDataProvider mediaDataProvider;
    private MediaPlayerService mediaPlayerService;
    private MusicBecomingNoisyReceiver mMusicBecomingNoisyReceiver;
    private IntentFilter mNoisyFilter;
    AudioManager.OnAudioFocusChangeListener mAudioFocusListener;
    AudioFocusRequest mAudioFocusRequest;
    AudioAttributes mAudioAttributes;
    AudioManager mAudioManager;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MusicPlayerFragment.
     */
    // TODO: Rename and change types and number of parameters
    @SuppressWarnings("unused")
    public static MusicPlayerFragment newInstance(String param1, String param2) {
        MusicPlayerFragment fragment = new MusicPlayerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // TODO: Rename and change types of parameters
            @SuppressWarnings("unused")
            String mParam1 = getArguments().getString(ARG_PARAM1);
            @SuppressWarnings("unused")
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }
        Log.d(TAG, "onCreate");
        mHandler = new Handler();
        mBound = false;

        mAudioFocusListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        Log.e(TAG, "AUDIO FOCUS_LOSS_TRANSIENT");
                        stopPlayback();
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN:
                        Log.i(TAG, "AUDIO FOCUS_GAIN");
                        startPlayback();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS:
                        Log.e(TAG, "AUDIO FOCUS_LOSS");
                        stopPlayback();
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                        Log.i(TAG, "AUDIO FOCUS_GAIN_TRANSIENT");
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                        Log.i(TAG, "AUDIO FOCUS_GAIN_TRANSIENT_MAY_DUCK");
                        startPlayback();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        Log.e(TAG, "AUDIO FOCUS_LOSS_TRANSIENT_CAN_DUCK");
                        startPlayback();
                        break;
                    case AudioManager.AUDIOFOCUS_REQUEST_FAILED:
                        Log.e(TAG, "AUDIO FOCUS_REQUEST_FAILED");
                        break;
                    default:
                }
            }
        };

        mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        mMusicBecomingNoisyReceiver = new MusicBecomingNoisyReceiver();
        mNoisyFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        getActivity().registerReceiver(mMusicBecomingNoisyReceiver, mNoisyFilter);

        mAudioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        mAudioFocusRequest = new AudioFocusRequest
                .Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(mAudioAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(mAudioFocusListener, mHandler)
                .build();

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_music_player, container, false);
        hideSystemUI();
        mBound=false;
        mHandler = new Handler();
        mSongList = new ArrayList<>();
        mediaDataProvider = new MediaDataProvider();
        mediaPlayerService = new MediaPlayerService();

        // check that list is empty or null and populate list with data
        if (mSongList.size() == 0 || mSongList == null) {
            mSongList = mediaDataProvider.scanSongs(getActivity());
            mediaPlayerService.setSongList(mSongList);
        }

        // initialize view objects by Ids
        mSongTimer = view.findViewById(R.id.timer);
        mNextButton = view.findViewById(R.id.next_button);
        mAnimatedView = view.findViewById(R.id.playing_image);
        mPlayButton = view.findViewById(R.id.play_pause_button);
        mPlayButton = view.findViewById(R.id.play_pause_button);
        mPreviousButton = view.findViewById(R.id.previous_button);
        mSongTitle = view.findViewById(R.id.song_title);
        mSongArtist = view.findViewById(R.id.song_artist);
        mSongTime = view.findViewById(R.id.count_down_timer);

        // Add change listener on seekBar
        mSeekBar = view.findViewById(R.id.seekBar);
        // get progress change listener
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayerService.mPlayer.seekTo(progress * 1000);
                }
            }
        });

        /*
          Run UI thread to get data progress to update text view and update seekBar
         */
//------------------------------------------------------------------------------------------------//
        //Make new object handler for song timer
        mRunnable = new Runnable() {
            @Override
            public void run() {
                try {

                    if (mediaPlayerService.mPlayer.isPlaying()) {
                        mSongTimer.setText(milliSecondsToTimer
                                (mediaPlayerService.mPlayer.getCurrentPosition()));
                        mSeekBar.setMax(mediaPlayerService.mPlayer.getDuration() / 1000);
                        int mCurrentPosition =
                                mediaPlayerService.mPlayer.getCurrentPosition() / 1000;
                        mSeekBar.setProgress(mCurrentPosition);
                    }
                    mHandler.postDelayed(this, 1000);
                } catch (NullPointerException e) {
                    Log.e(TAG, "MainPlayerActivity: Song handler is null. No data.");

                }
            }
        };
        mRunnable.run();

        // Play(Pause) Button onClick Listener
//------------------------------------------------------------------------------------------------//
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int requestAudioFocusResult = mAudioManager.requestAudioFocus(mAudioFocusRequest);
                if (requestAudioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    if (mSongList != null && mSongList.size() > 0) {
                        try {
                            playButtonAnimation(mPlayButton);       //play onClick animation
                            if (mediaPlayerService.mPlayer.isPlaying()) {
                                mediaPlayerService.playPause();
                                mAnimatedView.setAnimation(null);
                                mAudioManager.abandonAudioFocusRequest(mAudioFocusRequest);

                            } else if (MediaPlayerService.SONG_POS == 0) {
                                mediaPlayerService.setSongList(mSongList);
                                mediaPlayerService.setSelectedSong(0);
                                mAnimatedView.startAnimation(anim);
                                getActivity().registerReceiver(mMusicBecomingNoisyReceiver,
                                        mNoisyFilter);
                            } else {
                                mediaPlayerService.playPause();
                                mAnimatedView.startAnimation(anim);
                                getActivity().registerReceiver(mMusicBecomingNoisyReceiver,
                                        mNoisyFilter);
                            }
                            updateData();                           //update text views values
                        } catch (NullPointerException e) {
                            Log.d(TAG, "Null Media Player object", e);
                        }
                    } else {
                        ScanForMediaFiles scanForMediaFiles = new ScanForMediaFiles();
                        scanForMediaFiles.execute();
                    }
                }
            }
        });

        // Next Song Button onClick Listener
//------------------------------------------------------------------------------------------------//
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int requestAudioFocusResult = mAudioManager.requestAudioFocus(mAudioFocusRequest);
                if (requestAudioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    if (mSongList != null && mSongList.size() > 0) {
                        try {
                            playButtonAnimation(mNextButton);       //play onClick animation
                            mediaPlayerService.nextSong();          //play next song
                            updateData();                           //update text views values
                            getActivity().registerReceiver(mMusicBecomingNoisyReceiver,
                                    mNoisyFilter);
                            if (mAnimatedView.getAnimation() == null) {
                                mAnimatedView.startAnimation(anim);
                            }
                        } catch (NullPointerException e) {
                            Log.d(TAG, "Null Media Player object", e);
                        }
                    } else {
                        ScanForMediaFiles scanForMediaFiles = new ScanForMediaFiles();
                        scanForMediaFiles.execute();
                    }
                }
            }
        });

        // Previous Song Button onClick Listener
//------------------------------------------------------------------------------------------------//
        mPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int requestAudioFocusResult = mAudioManager.requestAudioFocus(mAudioFocusRequest);
                if (requestAudioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    if (mSongList != null && mSongList.size() > 0) {
                        try {
                            playButtonAnimation(mPreviousButton);   //play onClick animation
                            mediaPlayerService.previousSong();      //play previous song
                            updateData();                           //update text views values
                            getActivity().registerReceiver(mMusicBecomingNoisyReceiver,
                                    mNoisyFilter);
                            if (mAnimatedView.getAnimation() == null) {
                                mAnimatedView.startAnimation(anim);
                            }
                        } catch (NullPointerException e) {
                            Log.d(TAG, "Null Media Player object", e);
                        }
                    } else {
                        ScanForMediaFiles scanForMediaFiles = new ScanForMediaFiles();
                        scanForMediaFiles.execute();
                    }
                }
            }
        });
        return view;
    }

    public void playSong() {
        ImageButton mPlayButton = getActivity().findViewById(R.id.play_pause_button);
        mPlayButton.setEnabled(true);
        mPlayButton.performClick();
        playAnimation();
    }




    public void nextSong() {
       // ImageButton mPlayButton = getActivity().findViewById(R.id.next_button);
        mNextButton.setEnabled(true);
        mNextButton.performClick();
        playAnimation();
    }




    public void previousSong() {
      //  ImageButton mPlayButton = getActivity().findViewById(R.id.previous_button);
        mPreviousButton.setEnabled(true);
        mPreviousButton.performClick();
        playAnimation();
    }


    // TODO: Rename method, update argument and hook method into UI event
    @SuppressWarnings("unused")
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onMusicInitializeListener();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentMusicListener) {
            mListener = (OnFragmentMusicListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnDateTimeFragmentListener");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentMusicListener {
        // TODO: Update argument type and name
        void onMusicInitializeListener();
    }
    //------------------------------------------------------------------------------------------------//

    /**
     * Method is used to update Seek Bar
     * <p>
     * Refresh Song List in background task. This is invoke in first run. But Work not.
     */

//------------------------------------------------------------------------------------------------//
    @SuppressLint("StaticFieldLeak")
    class ScanForMediaFiles extends AsyncTask<Void, Integer, Long> {
        // Do the long-running work in here
        @Override
        protected Long doInBackground(Void... voids) {
            mSongList = mediaDataProvider.scanSongs(getActivity());
            mediaPlayerService.setSongList(mSongList);
            return null;
        }

        // This is called each time you call publishProgress()
        protected void onProgressUpdate(Integer... progress) {

        }

        // This is called when doInBackground() is finished
        protected void onPostExecute(Long result) {
            int findSongs = mSongList.size();
            Log.d(TAG, "Media Scan Complete. List contains: " + findSongs + " songs");
        }
    }

//------------------------------------------------------------------------------------------------//

    /**
     * Method is used to update Seek Bar
     *
     * @param milliseconds input time in milliseconds and convert duration into time
     */
//------------------------------------------------------------------------------------------------//
    private String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }
        // Prepending 0 to seconds if it is one digit
        String secondsString;
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }
        finalTimerString = finalTimerString + minutes + ":" + secondsString;
        // return timer string
        return finalTimerString;
    }

//------------------------------------------------------------------------------------------------//

    /**
     * Method is used play button animation.
     *
     * @param obj pass animation object
     */
//------------------------------------------------------------------------------------------------//
    private void playButtonAnimation(Object obj) {
        final Animator mButtonAnimationA =
                AnimatorInflater.loadAnimator(getActivity(),
                        R.animator.button_click_anim);
        mButtonAnimationA.setTarget(obj);
        mButtonAnimationA.start();
    }

//------------------------------------------------------------------------------------------------//
    /**
     * Start Media Player Service connection and bind to that service.
     */
//------------------------------------------------------------------------------------------------//
    private final ServiceConnection connectMusic = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlayerService.PlayerBinder binder = (MediaPlayerService.PlayerBinder) service;
            mediaPlayerService = binder.getService();
            mediaPlayerService.setSongList(mSongList);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

//------------------------------------------------------------------------------------------------//

    /**
     * Override Activity Live Cycle methods
     */
//------------------------------------------------------------------------------------------------//
    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        try {
            if (!mBound) {
                Intent playIntent = new Intent(getActivity(), MediaPlayerService.class);
                getActivity().bindService(playIntent, connectMusic, Context.BIND_AUTO_CREATE);
            } else {
                Log.d(TAG, "Main Player Activity: Service is already started");
            }
        }catch(NullPointerException e){
            Log.w(TAG,"Null object", e);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.d(TAG, "onLowMemory");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        if (!mHomeButtonPressed) {
            if (mBound) {
                getActivity().unbindService(connectMusic);
            }

            try {
                anim.cancel();
            } catch (NullPointerException w) {
                Log.d(TAG, "Null animation object", w);
            }
            mBound = true;
            mHandler.removeCallbacks(mRunnable);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        mHomeButtonPressed = false;
        // bind to the service
        if (!mBound) {
            Intent playIntent = new Intent(getActivity(), MediaPlayerService.class);
            getActivity().bindService(playIntent, connectMusic, Context.BIND_AUTO_CREATE);
        } else {
            Log.d(TAG, "Main Player Activity: Service is already started");
        }
        try {
            if (mediaPlayerService.mPlayer.isPlaying()) {
                playAnimation();
            }
        } catch (NullPointerException e) {
            Log.d(TAG, "Null media player object", e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaDataProvider.destroySongList();
        mHandler.removeCallbacks(mRunnable);
        System.runFinalization();
        Runtime.getRuntime().gc();
        System.gc();
        getActivity().finish();
    }

//------------------------------------------------------------------------------------------------//

    /**
     * Update views with new data
     */
//------------------------------------------------------------------------------------------------//
    private void updateData() {

        if (mSongList != null && mSongList.size() != 0) {
            mSongTitle.setText(mSongList.get(MediaPlayerService.SONG_POS).get_title());
            mSongArtist.setText(mSongList.get(MediaPlayerService.SONG_POS).get_artist());
            mSongTime.setText(mSongList.get(MediaPlayerService.SONG_POS).get_duration());

        } else {
           ScanForMediaFiles scanForMediaFiles = new ScanForMediaFiles();
            scanForMediaFiles.execute();

        }
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Play animation
     */
//------------------------------------------------------------------------------------------------//
    private void playAnimation() {
        // Initialize play animation
        anim = new RotateAnimation(0f, 890f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setStartOffset(10);
        anim.setFillAfter(true);
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(10000);
        mAnimatedView.startAnimation(anim);
    }

//------------------------------------------------------------------------------------------------//

    /**
     * Music become noisy broadcast receiver
     */
//------------------------------------------------------------------------------------------------//

    public class MusicBecomingNoisyReceiver extends BroadcastReceiver {
        public MusicBecomingNoisyReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.requireNonNull(intent.getAction()).equals(
                    android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                stopPlayback();
                // Pause the playback
                Intent pauseIntent = new Intent(context, VideoPlayerActivity.class);
                pauseIntent.setAction(MediaPlayerService.TELEPHONY_SERVICE);
                context.startService(pauseIntent);
            }
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
            View decorView = getActivity().getWindow().getDecorView();
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



    private void startPlayback() {
        int requestAudioFocusResult = mAudioManager.requestAudioFocus(mAudioFocusRequest);
        if (requestAudioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            getActivity().registerReceiver(mMusicBecomingNoisyReceiver, mNoisyFilter);
            mediaPlayerService.playPause();
        }
    }

    private void stopPlayback() {
        mediaPlayerService.playPause();
        mAudioManager.abandonAudioFocusRequest(mAudioFocusRequest);
        try {
            getActivity().unregisterReceiver(mMusicBecomingNoisyReceiver);
        }catch (IllegalArgumentException e) {
            Log.e(TAG,"Unable to unregister receiver", e);
        }
    }
}
