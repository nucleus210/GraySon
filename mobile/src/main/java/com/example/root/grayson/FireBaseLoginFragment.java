package com.example.root.grayson;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

/**
 * A fragment with a Google +1 button.
 * Activities that contain this fragment must implement the
 * {@link FireBaseLoginFragment.OnLoginFragmentListener} interface
 * to handle interaction events.
 * Use the {@link FireBaseLoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FireBaseLoginFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // The request code must be 0 or greater.
    private OnLoginFragmentListener mListener;
    private static final String TAG = "FireBase Login";
    private static final String DEVICE_MODEL_ID = "PLACEHOLDER";
    private static final String DEVICE_INSTANCE_ID = "PLACEHOLDER";
    Button mSingInBt;
    Button mSingOutBt;
    Button mCreateAccountBt;
    Button mAccountVerifyBt;
    TextView mEmailStatus;
    TextView mEmailDetails;
    TextView mConnection;
    EditText mEmailAddress;
    EditText mEmailPassword;
    private FirebaseAuth mAuth;
    FirebaseAnalytics mFireBaseAnalytics;

    public FireBaseLoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FireBaseLoginFragment.
     */
    // TODO: Rename and change types and number of parameters
    @SuppressWarnings("unused")
    public static FireBaseLoginFragment newInstance(String param1, String param2) {
        FireBaseLoginFragment fragment = new FireBaseLoginFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "OnCreate");
        if (getArguments() != null) {
            // TODO: Rename and change types of parameters
            @SuppressWarnings("unused")
            String mParam1 = getArguments().getString(ARG_PARAM1);
            @SuppressWarnings("unused")
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // Obtain the FireBaseAut instance.
        mAuth = FirebaseAuth.getInstance();

//------------------------------------FireBaseAnalytic--------------------------------------------//
        // Obtain the FireBaseAnalytics instance.
        mFireBaseAnalytics = FirebaseAnalytics.getInstance(Objects.requireNonNull(getActivity()));
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, DEVICE_MODEL_ID);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, DEVICE_INSTANCE_ID);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
        mFireBaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fire_base_login, container, false);
        mEmailDetails = view.findViewById(R.id.detail_text);
        mEmailStatus = view.findViewById(R.id.status_text);
        mEmailAddress = view.findViewById(R.id.email_address_type);
        mEmailPassword = view.findViewById(R.id.email_password_type);
        mCreateAccountBt = view.findViewById(R.id.create_account);
        mAccountVerifyBt = view.findViewById(R.id.verify_account);
        mConnection = view.findViewById(R.id.connect_text);
        mSingInBt = view.findViewById(R.id.sing_in_button);
        mSingOutBt = view.findViewById(R.id.sing_out);

        mAccountVerifyBt.setOnClickListener(this);
        mCreateAccountBt.setOnClickListener(this);
        mSingOutBt.setOnClickListener(this);
        mSingInBt.setOnClickListener(this);
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
            mListener.onLoginFragmentListener(uri);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnLoginFragmentListener) {
            mListener = (OnLoginFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sing_in_button:
                signIn(mEmailAddress.getText().toString(),
                        mEmailPassword.getText().toString());
                break;

            case R.id.create_account:
                createAccount(mEmailAddress.getText().toString(),
                        mEmailPassword.getText().toString());
                break;

            case R.id.sing_out:
                signOut();
                break;

            case R.id.verify_account:
                sendEmailVerification();
                break;
        }
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnLoginFragmentListener {
        // TODO: Update argument type and name
        void onLoginFragmentListener(Uri uri);
    }

//------------------------------------------------------------------------------------------------//

    /**
     * Create new Fire base account
     *
     * @param email    E-maiL Address
     * @param password User password
     */
//------------------------------------------------------------------------------------------------//
    private void createAccount(String email, String password) {
        Log.e(TAG, "createAccount:" + email);
        if (validateForm(email, password)) {
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(Objects.requireNonNull(getActivity()), task -> {
                    if (task.isSuccessful()) {
                        Log.e(TAG, "createAccount: Success!");
                        // update UI with the signed-in user's information
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        Log.e(TAG, "createAccount: Fail!", task.getException());
                        Toast.makeText(getActivity(), R.string.toast_authentication_failed,
                                Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Sing In Fire base account
     *
     * @param email    E-maiL Address
     * @param password User password
     */
//------------------------------------------------------------------------------------------------//
    private void signIn(String email, String password) {
        Log.e(TAG, "signIn:" + email);
        boolean isAccountSet = validateForm(email, password);
        if (isAccountSet) {

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(Objects.requireNonNull(getActivity()), task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(getActivity(), R.string.toast_authentication_failed,
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    });
        }
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Sing Out Fire base account
     */
//------------------------------------------------------------------------------------------------//
    private void signOut() {
        mAuth.signOut();
        updateUI(null);
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Verify Fire Base account
     */
//------------------------------------------------------------------------------------------------//
    private void sendEmailVerification() {
        // Disable Verify Email button
        Objects.requireNonNull(getActivity()).findViewById(R.id.create_account).setEnabled(false);

        final FirebaseUser user = mAuth.getCurrentUser();
        Objects.requireNonNull(user).sendEmailVerification()
                .addOnCompleteListener(getActivity(), task -> {
                    // Re-enable Verify Email button
                    getActivity().findViewById(R.id.create_account).setEnabled(true);

                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(),
                                getString(R.string.toast_email_verification)
                                + user.getEmail(), Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "sendEmailVerification failed!", task.getException());
                        Toast.makeText(getActivity(), R.string.toast_verification_failed,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Validate account
     *
     * @param email    E-maiL Address
     * @param password User password
     */
//------------------------------------------------------------------------------------------------//
    private boolean validateForm(String email, String password) {
        if (email == null | password == null) {
            Toast.makeText(getActivity(), R.string.toast_provide_data,
                    Toast.LENGTH_SHORT).show();
        } else {
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(getActivity(), R.string.toast_no_email_address,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(getActivity(), R.string.toast_enter_password,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
            if (password.length() < 6) {
                Toast.makeText(getActivity(), R.string.toast_password_too_short,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Update UI
     *
     * @param user FireBase user
     */
//------------------------------------------------------------------------------------------------//
    @SuppressLint("SetTextI18n")
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            mEmailStatus.setText(user.getEmail()
                    + "(verified: "
                    + user.isEmailVerified() + ")");

            mEmailDetails.setText(user.getUid());

            Objects.requireNonNull(getActivity()).findViewById(R.id.password_layout)
                    .setVisibility(View.GONE);
            getActivity().findViewById(R.id.email_layout).setVisibility(View.GONE);
            getActivity().findViewById(R.id.connect_text).setVisibility(View.VISIBLE);
            getActivity().findViewById(R.id.verify_account).setEnabled(!user.isEmailVerified());
        } else {
            mEmailStatus.setText(R.string.text_view_signed_out);
            mEmailDetails.setText(null);

            Objects.requireNonNull(getActivity()).findViewById(R.id.password_layout)
                    .setVisibility(View.VISIBLE);
            getActivity().findViewById(R.id.email_layout).setVisibility(View.VISIBLE);
            getActivity().findViewById(R.id.connect_text).setVisibility(View.GONE);

        }
    }
}
