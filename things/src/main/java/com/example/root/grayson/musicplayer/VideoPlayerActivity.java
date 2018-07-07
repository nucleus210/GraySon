package com.example.root.grayson.musicplayer;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.root.grayson.R;
import com.example.root.grayson.TextToSpeechManager;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A fragment with a Google +1 button.
 * Activities that contain this fragment must implement the
 * {@link OnVideoInteractionListener} interface
 * to handle interaction events.
 * Use the {@link VideoPlayerActivity#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VideoPlayerActivity extends Fragment implements
        TextToSpeechManager.Listener,
        SurfaceHolder.Callback,
        AudioManager.OnAudioFocusChangeListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String LOG_TAG = "Video Player:";
    private static final String TAG = "Video Player";

    Handler mHandler;
    SeekBar mSeekBarVideo;
    Runnable mRunnable;
    Uri mVideoUri;
    TextView mVideoTime;
    TextView mVideoTitle;
    TextView mVideoTimer;
    VideoView mVideoView;
    ArrayList<Video> mVideoList;
    ImageButton mPreviousButton;
    ImageButton mPlayVideoButton;
    ImageButton mNextVideoButton;
    MediaDataProvider mediaDataProvider;
    public static int VIDEO_POS;
    private static final int STATE_PAUSED = 1001;
    private static final int STATE_PLAYING = 1002;
    private int mCurrentState = 0;
    TextToSpeechManager mTextToSpeechManager;
    AudioAttributes mAudioAttributes;
    AudioFocusRequest mAudioFocusRequest;
    AudioManager mAudioManager;
    private OnVideoInteractionListener mListener;
    private IntentFilter mNoisyFilter;
    private VideoBecomingNoisyReceiver mAudioBecomingNoisyReceiver;
    AudioManager.OnAudioFocusChangeListener mAudioFocusListener;

    public VideoPlayerActivity() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment VideoPlayerActivity.
     */
    // TODO: Rename and change types and number of parameters
    @SuppressWarnings("unused")
    public static VideoPlayerActivity newInstance(String param1, String param2) {
        VideoPlayerActivity fragment = new VideoPlayerActivity();
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
        mVideoList = new ArrayList<>();
        mediaDataProvider = new MediaDataProvider();
        mTextToSpeechManager = new TextToSpeechManager(this, this);
        mHandler = new Handler();

        // check that list is empty or null and populate list with data
        if (mVideoList.size() == 0 || mVideoList == null) {
            mVideoList = mediaDataProvider.scanVideo(getActivity());
        }

        Intent callingIntent = Objects.requireNonNull(getActivity()).getIntent();
        if (callingIntent != null) {
            mVideoUri = callingIntent.getData();
        }

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

        mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        mAudioBecomingNoisyReceiver = new VideoBecomingNoisyReceiver();
        mNoisyFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        getActivity().registerReceiver(mAudioBecomingNoisyReceiver, mNoisyFilter);

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_video_player, container, false);
        // initialize view objects by Ids
        mHandler = new Handler();
        mVideoTimer = view.findViewById(R.id.timer);
        mVideoView = view.findViewById(R.id.video_view);
        mVideoTime = view.findViewById(R.id.count_down_video_timer);
        mNextVideoButton = view.findViewById(R.id.next_video_button);
        mPreviousButton = view.findViewById(R.id.previous_video_button);
        mPlayVideoButton = view.findViewById(R.id.play_pause_video_button);
        //  mVideoTitle = view.findViewById(R.id.video_title);

        // Add change listener on seekBar
        mSeekBarVideo = view.findViewById(R.id.seekBar_video);
        // get progress change listener
        mSeekBarVideo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mVideoView.seekTo(progress * 1000);
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
                    if (mVideoView.isPlaying()) {
                        mVideoTimer.setText(milliSecondsToTimer
                                (mVideoView.getCurrentPosition()));
                        mSeekBarVideo.setMax(mVideoView.getDuration() / 1000);
                        int mCurrentPosition =
                                mVideoView.getCurrentPosition() / 1000;
                        mSeekBarVideo.setProgress(mCurrentPosition);
                    }
                    mHandler = new Handler();
                    mHandler.postDelayed(this, 1000);
                } catch (NullPointerException e) {
                    Log.e(LOG_TAG, "Video Player Activity: Video handler is null. No data.");
                }
            }
        };
        mRunnable.run();
//------------------------------------Play/Pause Button-------------------------------------------//

        mPlayVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVideoList != null && mVideoList.size() > 0) {
                    try {
                        if (mVideoView.isPlaying()) {
                            playButtonAnimation(mPlayVideoButton);
                            playPause();                          //play video
                        } else if (VIDEO_POS == 0) {
                            setSelectedVideo(0);
                        } else {
                            playButtonAnimation(mPlayVideoButton);
                            playPause();                          //play video
                        }
                        //updateData();                           //update text views values
                    } catch (NullPointerException e) {
                        Log.d(LOG_TAG, "Null Media Player object", e);
                    }
                } else {
                    ScanForMediaFiles scanForMediaFiles = new ScanForMediaFiles();
                    scanForMediaFiles.execute();
                }
            }
        });
//---------------------------------------Next Button----------------------------------------------//
        mNextVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVideoList != null && mVideoList.size() > 0) {
                    try {
                        playButtonAnimation(mNextVideoButton);     //play onClick animation
                        nextVideo();                               //play next video
                        // updateData();                           //update text views values

                    } catch (NullPointerException e) {
                        Log.d(LOG_TAG, "Null Media Player object", e);
                    }
                } else {
                    ScanForMediaFiles scanForMediaFiles = new ScanForMediaFiles();
                    scanForMediaFiles.execute();
                }
            }
        });
//--------------------------------------Previous Button-------------------------------------------//
        mPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVideoList != null && mVideoList.size() > 0) {
                    try {
                        playButtonAnimation(mPreviousButton);   //play onClick animation
                        previousVideo();                        //play previous video
                        //updateData();                           //update text views values

                    } catch (NullPointerException e) {
                        Log.d(LOG_TAG, "Null Media Player object", e);
                    }
                } else {
                    ScanForMediaFiles scanForMediaFiles = new ScanForMediaFiles();
                    scanForMediaFiles.execute();
                }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    // TODO: Rename method, update argument and hook method into UI event
    @SuppressWarnings("unused")
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onVideoInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnVideoInteractionListener) {
            mListener = (OnVideoInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnVideoInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onTtsInitialized() {

    }

    @Override
    public void onTtsSpoken() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaDataProvider.destroyVideoList();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
//------------------------------------------------------------------------------------------------//

    /**
     * Focus change listener
     * @param focusChange Requested Audio focus from other sources
     */

//------------------------------------------------------------------------------------------------//
    @Override
    public void onAudioFocusChange(int focusChange) {
    }
//------------------------------------------------------------------------------------------------//
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
//------------------------------------------------------------------------------------------------//
    public interface OnVideoInteractionListener {
        // TODO: Update argument type and name
        void onVideoInteraction(Uri uri);
    }

//------------------------------------------------------------------------------------------------//

    /**
     * Method is used to prepare song for playing and update with data notification manager about
     * user action.
     */
//------------------------------------------------------------------------------------------------//
    private void startVideo(Uri videoUri, String videoTitle) {
        mVideoView.stopPlayback();        // Reset Media Player object
        mCurrentState = STATE_PLAYING;    // Change state
        mVideoUri = videoUri;             // Get video Uri
        try {
            mVideoView.setVideoURI(videoUri);
            Log.d(LOG_TAG, "startingSong: " + videoTitle);

        } catch (Exception e) {
            Log.e("Media Service:", "Error setting data", e);
        }
        try {
            mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setLooping(true);
                    mVideoView.start();
                }
            });
        } catch (Exception e) {
            Log.e("Media Service:", "Error preparing data", e);
        }
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Method is used to set selected video
     * @param  pos Video position
     */

//------------------------------------------------------------------------------------------------//

    public void setSelectedVideo(int pos) {
        VIDEO_POS = pos;
        if (mVideoList.size() != 0) {
            setVideoUri(mVideoList.get(VIDEO_POS).get_videoUri());
            startVideo(mVideoList.get(VIDEO_POS).get_videoUri(),
                    mVideoList.get(VIDEO_POS).get_title());
        } else {
            int listSize = mVideoList.size();
            Log.d(LOG_TAG, "Song Array List size is:" + listSize);
        }
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Handle play/pause Song action
     */
//------------------------------------------------------------------------------------------------//
    public void playPause() {
        int requestAudioFocusResult = mAudioManager.requestAudioFocus(mAudioFocusRequest);
        if (requestAudioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // Check current state of mediaPlayer object
            if (mCurrentState == STATE_PAUSED) {
                mVideoView.start();                       // start video
                mCurrentState = STATE_PLAYING;            // set state Playing
                Objects.requireNonNull(getActivity())
                        .registerReceiver(mAudioBecomingNoisyReceiver, mNoisyFilter);
            } else if (mCurrentState == STATE_PLAYING) {
                mVideoView.pause();                       // if player is playing stop Media Player
                mCurrentState = STATE_PAUSED;             // set state Paused
                mAudioManager.abandonAudioFocusRequest(mAudioFocusRequest);
                try {
                    Objects.requireNonNull(getActivity())
                            .unregisterReceiver(mAudioBecomingNoisyReceiver);

                } catch (IllegalArgumentException e) {
                    Log.e(TAG,"Unable to unregister receiver", e);
                }
            }
        }
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Handle stop Song action
     */
//------------------------------------------------------------------------------------------------//
    @SuppressWarnings("unused")
    private void stopVideo() {
        mVideoView.stopPlayback();                     // stop mediaPlayer
        mAudioManager.abandonAudioFocusRequest(mAudioFocusRequest);
        try {
            Objects.requireNonNull(getActivity()).unregisterReceiver(mAudioBecomingNoisyReceiver);
        } catch (IllegalArgumentException e) {
            Log.e(TAG,"Unable to unregister receiver", e);
        }
        try {
            System.exit(0);                     // calls the exit method in class Runtime
        } catch (Exception SecurityException) {
            Log.e("Media Service:", "Error Stop Song", SecurityException);
        }
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Handle play next Song action
     */
//------------------------------------------------------------------------------------------------//
    public void nextVideo() {
        int requestAudioFocusResult = mAudioManager.requestAudioFocus(mAudioFocusRequest);
        if (requestAudioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            if (VIDEO_POS == 0) {
                startVideo(mVideoList.get(VIDEO_POS + 1).get_videoUri(),
                        mVideoList.get(VIDEO_POS + 1).get_title());
                VIDEO_POS++;
                Objects.requireNonNull(getActivity())
                        .registerReceiver(mAudioBecomingNoisyReceiver, mNoisyFilter);
            } else if (VIDEO_POS != mVideoList.size() - 1) {
                startVideo(mVideoList.get(VIDEO_POS + 1).get_videoUri(),
                        mVideoList.get(VIDEO_POS + 1).get_title());
                Objects.requireNonNull(getActivity())
                        .registerReceiver(mAudioBecomingNoisyReceiver, mNoisyFilter);
                VIDEO_POS++;
            }
        }
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Handle play previous Song
     */
//------------------------------------------------------------------------------------------------//
    public void previousVideo() {
        int requestAudioFocusResult = mAudioManager.requestAudioFocus(mAudioFocusRequest);
        if (requestAudioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            if (VIDEO_POS != 0) {
                startVideo(mVideoList.get(VIDEO_POS - 1).get_videoUri(),
                        mVideoList.get(VIDEO_POS - 1).get_title());
                VIDEO_POS--;
                Objects.requireNonNull(getActivity())
                        .registerReceiver(mAudioBecomingNoisyReceiver, mNoisyFilter);
            }
        }
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Set Song Uri path
     *
     * @param uri File uri
     */
//------------------------------------------------------------------------------------------------//
    private void setVideoUri(Uri uri) {
        this.mVideoUri = uri;
    }

//------------------------------------------------------------------------------------------------//

    /**
     * Method is used to update Seek Bar
     * <p>
     * Refresh Song List in background task. This is invoke in first run. But Work not.
     */

//------------------------------------------------------------------------------------------------//
    @SuppressLint("StaticFieldLeak")
    private class ScanForMediaFiles extends AsyncTask<Void, Integer, Long> {
        // Do the long-running work in here
        @Override
        protected Long doInBackground(Void... voids) {
            mVideoList = mediaDataProvider.scanVideo(getActivity());
            return null;
        }
        // This is called each time you call publishProgress()
        protected void onProgressUpdate(Integer... progress) {

        }
        // This is called when doInBackground() is finished
        protected void onPostExecute(Long result) {
            int findVideo = mVideoList.size();
            Log.d(LOG_TAG, "Media Scan Complete. List contains: " + findVideo + " songs");
        }
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
     * Music become noisy broadcast receiver
     */
//------------------------------------------------------------------------------------------------//
    public class VideoBecomingNoisyReceiver extends BroadcastReceiver {
        public VideoBecomingNoisyReceiver() {}

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

    private void startPlayback() {
        int requestAudioFocusResult = mAudioManager.requestAudioFocus(mAudioFocusRequest);
        if (requestAudioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Objects.requireNonNull(getActivity()).registerReceiver(mAudioBecomingNoisyReceiver,
                    mNoisyFilter);
            playPause();
        }
    }

    private void stopPlayback() {
        mVideoView.pause();
        mAudioManager.abandonAudioFocusRequest(mAudioFocusRequest);
        try {
            Objects.requireNonNull(getActivity()).unregisterReceiver(mAudioBecomingNoisyReceiver);
        } catch (IllegalArgumentException e) {
            Log.e(TAG,"Unable to unregister receiver", e);
        }
    }
}
