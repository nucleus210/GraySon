package com.example.root.grayson.dateTime;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.root.grayson.R;

public class DateTimeFragment extends Fragment {

    Time mTimer;
    Handler mHandler;
    Runnable mRunnable;
    DateTimeDraw dateTimeDraw;
    OnDateTimeFragmentListener mListener;

    // Required empty public constructor
    public DateTimeFragment() {
    }
    @SuppressWarnings("unused")
    public static DateTimeFragment newInstance() {
        DateTimeFragment fragment = new DateTimeFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_date_time, container,
                false);
        dateTimeDraw = view.findViewById(R.id.date_time_draw);
        mTimer = new Time();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                mTimer.setToNow();
                dateTimeDraw = view.findViewById(R.id.date_time_draw);
                dateTimeDraw.setParameter(
                        mTimer.hour,
                        mTimer.minute,
                        mTimer.second,
                        mTimer.weekDay,
                        mTimer.monthDay);

                mHandler.postDelayed(mRunnable, 1000);
            }
        };
        mHandler = new Handler();
        mHandler.postDelayed(mRunnable, 1000);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDateTimeFragmentListener) {
            mListener = (OnDateTimeFragmentListener) context;
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    @SuppressWarnings("unused")
    public interface OnDateTimeFragmentListener {
        // TODO: Update argument type and name
        void onTimerListener(Uri uri);
    }
}
