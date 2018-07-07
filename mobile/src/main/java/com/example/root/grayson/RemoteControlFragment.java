package com.example.root.grayson;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.example.root.grayson.adapters.RemoteViewAdapter;

import java.util.Objects;


/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnRemoteControlListener}
 * interface.
 */
public class RemoteControlFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String TAG = "Remote Control";
    // TODO: Customize parameters
    int mColumnCount = 2;
    OnRemoteControlListener mListener;
    GridView mControlGridView;
    Handler mHandler;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RemoteControlFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static RemoteControlFragment newInstance(int columnCount) {
        RemoteControlFragment fragment = new RemoteControlFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
        mHandler = new Handler();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.remote_control_item, container, false);

        mControlGridView = view.findViewById(R.id.remote_control_grid_view);
        mControlGridView.setAdapter(new RemoteViewAdapter(getActivity()));
        mControlGridView.setOnItemClickListener(this);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnRemoteControlListener) {
            mListener = (OnRemoteControlListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnRemoteControlListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.remote_control_grid_view) {
            Toast.makeText(Objects.requireNonNull(getActivity()).getBaseContext(),
                    "Action position: " + position, Toast.LENGTH_SHORT).show();
            String pos = Integer.toString(position);
            ObjectAnimator picMoveX = ObjectAnimator.ofFloat(parent
                    .getChildAt(Integer.parseInt(pos)), "alpha", 100f, 0f);
            picMoveX.setDuration(1000);
            ObjectAnimator fadeIn = ObjectAnimator
                    .ofFloat(parent.getChildAt(Integer.parseInt(pos)),
                            "alpha", 0f, 100f);
            fadeIn.setDuration(2000);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.play(picMoveX).with(fadeIn);
            animatorSet.start();
            switch (position) {

                case 0:
                    sendAction(ActionMenu.TURN_OFF.name());
                    break;
                case 1:
                    sendAction(ActionMenu.TURN_ON.name());
                    break;
                case 2:
                    sendAction(ActionMenu.START_MUSIC_PLAYER.name());
                    break;
                case 3:
                    sendAction(ActionMenu.SHOW_DAY_PHOTO.name());
                    break;
                case 4:
                    sendAction(ActionMenu.CLOUD_STORAGE.name());
                    break;
                case 5:
                    sendAction(ActionMenu.SHOW_WEATHER.name());
                    break;
                case 6:
                    sendAction(ActionMenu.LIGHTS_ON.name());
                    break;
                case 7:
                    sendAction(ActionMenu.LIGHTS_OFF.name());
                    break;
                case 8:
                    sendAction(ActionMenu.GREEN_LIGHTING.name());
                    break;
                case 9:
                    sendAction(ActionMenu.BLUE_LIGHTING.name());
                    break;
                case 10:
                    sendAction(ActionMenu.RED_LIGHTING.name());
                    break;
                case 11:
                    sendAction(ActionMenu.SHOW_TIME.name());
                    break;
                case 12:
                    sendAction(ActionMenu.LISTENING_TO_ACTION.name());
                    break;
                case 13:
                    sendAction(ActionMenu.CONFIRM_ASSISTANT.name());
                    break;
                case 14:
                    sendAction(ActionMenu.FIRE_BASE_STORAGE.name());
                    break;
                case 15:
                    sendAction(ActionMenu.DRIVE_STORAGE.name());
                    break;
            }
        }
    }

//------------------------------------------------------------------------------------------------//

    /**
     * Asynchronous call to Broadcast given intent to all interested BroadcastReceivers
     * Broadcast selected Song position to FullScreenPlayer
     * Message TO:{@link BluetoothActivityFragment}
     */
//------------------------------------------------------------------------------------------------//
    private void sendAction(String mPos) {
        Log.d("fragment sender", "Broadcasting message");
        Intent intent = new Intent("btAction");
        // You can also include some extra data.
        intent.putExtra("remote action", mPos);
        LocalBroadcastManager.getInstance(Objects
                .requireNonNull(getActivity())).sendBroadcast(intent);
    }

//------------------------------------------------------------------------------------------------//

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
//------------------------------------------------------------------------------------------------//
    @SuppressWarnings("unused")
    public interface OnRemoteControlListener {
        // TODO: Update argument type and name
        void onRemoteControlListener();
    }

}
