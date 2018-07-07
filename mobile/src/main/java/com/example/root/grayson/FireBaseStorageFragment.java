package com.example.root.grayson;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

/**
 * A fragment with a Google +1 button.
 * Activities that contain this fragment must implement the
 * {@link FireBaseStorageFragment.OnStorageFragmentListener} interface
 * to handle interaction events.
 * Use the {@link FireBaseStorageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FireBaseStorageFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "StorageActivity";
    //track Choosing Image Intent
    private static final int CHOOSING_IMAGE_REQUEST = 1234;
    private static final int REQUEST_FILE_UPLOAD = 201;
    private static final int REQUEST_BYTE_UPLOAD = 202;
    private static final int REQUEST_STREAM_UPLOAD = 203;

    Context mContext;
    private Uri fileUri;
    private Bitmap bitmap;
    private TextView tvFileName;
    private ImageView mSelectedImage;
    private EditText mUploadText;
    private StorageReference imageReference;
    private OnStorageFragmentListener mListener;
    ProgressDialog progressDialog;
    int mImageUploadState;


    public FireBaseStorageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FireBaseStorageFragment.
     */
    // TODO: Rename and change types and number of parameters
    @SuppressWarnings("unused")
    public static FireBaseStorageFragment newInstance(String param1, String param2) {
        FireBaseStorageFragment fragment = new FireBaseStorageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            // TODO: Rename and change types of parameters
            @SuppressWarnings("unused")
            String mParam1 = getArguments().getString(ARG_PARAM1);
            @SuppressWarnings("unused")
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fire_base_storage, container, false);

        mSelectedImage = view.findViewById(R.id.img_file);
        mUploadText = view.findViewById(R.id.text_message_post);
        tvFileName = view.findViewById(R.id.tv_file_name);
        tvFileName.setText("");

        mSelectedImage.setOnClickListener(this);
        view.findViewById(R.id.btn_post_message).setOnClickListener(this);
        view.findViewById(R.id.btn_choose_file).setOnClickListener(this);
        view.findViewById(R.id.btn_upload_file).setOnClickListener(this);

        imageReference = FirebaseStorage.getInstance().getReference().child("images");
        progressDialog = new ProgressDialog(getActivity());
        setState(REQUEST_FILE_UPLOAD);

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
            mListener.onStorageFragmentListener(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnStorageFragmentListener) {
            mListener = (OnStorageFragmentListener) context;
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


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnStorageFragmentListener {
        // TODO: Update argument type and name
        void onStorageFragmentListener(Uri uri);
    }

    @SuppressLint("SetTextI18n")
    private void uploadBytes() {

        if (fileUri != null) {
            postMessage();  // post message

            // send data to Api
            String fileName = fileNameGenerator();

            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] data = baos.toByteArray();

            StorageReference fileRef = imageReference.child(fileName + "."
                    + getFileExtension(fileUri));
            // send data to Api
            fileRef.putBytes(data)
                    .addOnSuccessListener(taskSnapshot -> {
                        progressDialog.dismiss();

                        Log.e(TAG, "Uri: " + imageReference.getDownloadUrl());
                        Log.e(TAG, "Name: " + Objects.requireNonNull(taskSnapshot
                                .getMetadata()).getName());

                        tvFileName.setText(taskSnapshot.getMetadata().getPath() + " - "
                                + taskSnapshot.getMetadata().getSizeBytes() / 1024 + " KBs");
                        Toast.makeText(getActivity(), "File Uploaded ",
                                Toast.LENGTH_LONG).show();
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference mySecRef = database.getReference("imagesUrls");
                        mySecRef.setValue(fileName + "." + getFileExtension(fileUri));
                    })
                    .addOnFailureListener(exception -> {
                        progressDialog.dismiss();

                        Toast.makeText(getActivity(), exception.getMessage(),
                                Toast.LENGTH_LONG).show();
                    })
                    .addOnProgressListener(taskSnapshot -> {
                        // progress percentage
                        double progress = (100.0 * taskSnapshot.getBytesTransferred())
                                / taskSnapshot.getTotalByteCount();

                        // percentage in progress dialog
                        progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                    })
                    .addOnPausedListener(taskSnapshot -> System.out.println("Upload is paused!"));
        } else {
            Toast.makeText(getActivity(), R.string.file_not_selected, Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("SetTextI18n")
    private void uploadFile() {
        if (fileUri != null) {

            postMessage();  // post message

            String fileName = fileNameGenerator();

            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference fileRef = imageReference.child(fileName +
                    "." + getFileExtension(fileUri));
            fileRef.putFile(fileUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        progressDialog.dismiss();

                        Log.e(TAG, "Uri: " + imageReference.getDownloadUrl());
                        Log.e(TAG, "Name: " + Objects.requireNonNull(taskSnapshot
                                .getMetadata()).getName());

                        tvFileName.setText(taskSnapshot.getMetadata().getPath() + " - "
                                + taskSnapshot.getMetadata().getSizeBytes() / 1024 + " KBs");
                        Toast.makeText(getActivity(), "File Uploaded "
                                , Toast.LENGTH_LONG).show();
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference mySecRef = database.getReference("imagesUrls");
                        mySecRef.setValue(fileName + "." + getFileExtension(fileUri));
                    })
                    .addOnFailureListener(exception -> {
                        progressDialog.dismiss();

                        Toast.makeText(getActivity(), exception.getMessage(),
                                Toast.LENGTH_LONG).show();
                    })
                    .addOnProgressListener(taskSnapshot -> {
                        // progress percentage
                        double progress = (100.0 * taskSnapshot.getBytesTransferred())
                                / taskSnapshot.getTotalByteCount();

                        // percentage in progress dialog
                        progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                    })
                    .addOnPausedListener(taskSnapshot -> System.out.println("Upload is paused!"));
        } else {
            Toast.makeText(getActivity(), R.string.file_not_selected, Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("SetTextI18n")
    private void uploadStream() {
        if (fileUri != null) {

            postMessage();  // post message

            String fileName = fileNameGenerator();

            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            try {
                InputStream stream = Objects.requireNonNull(getActivity())
                        .getContentResolver().openInputStream(fileUri);

                StorageReference fileRef = imageReference.child(fileName + "."
                        + getFileExtension(fileUri));
                fileRef.putStream(Objects.requireNonNull(stream))
                        .addOnSuccessListener(taskSnapshot -> {
                            progressDialog.dismiss();
                            Log.e(TAG, "Uri: " + imageReference.getDownloadUrl());
                            Log.e(TAG, "Name: " + Objects.requireNonNull(taskSnapshot
                                    .getMetadata()).getName());

                            tvFileName.setText(taskSnapshot.getMetadata().getPath() + " - "
                                    + taskSnapshot.getMetadata().getSizeBytes()
                                    / 1024 + " KBs");
                            Toast.makeText(getActivity(), "File Uploaded "
                                    , Toast.LENGTH_LONG).show();
                        })
                        .addOnFailureListener(exception -> {
                            progressDialog.dismiss();

                            Toast.makeText(getActivity(), exception.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        })
                        .addOnProgressListener(taskSnapshot -> {
                            // because this is a stream so:
                            // taskSnapshot.getTotalByteCount() = -1 (always)
                            progressDialog.setMessage("Uploaded "
                                    + taskSnapshot.getBytesTransferred() + " Bytes...");
                        })
                        .addOnPausedListener(taskSnapshot -> System.out.println("Upload is paused!"));

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getActivity(), R.string.file_not_selected, Toast.LENGTH_LONG).show();
        }
    }

    private void showChoosingFile() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(photoPickerIntent, CHOOSING_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (bitmap != null) {
            bitmap.recycle();
        }

        if (requestCode == CHOOSING_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            fileUri = data.getData();

            try {
                bitmap = MediaStore.Images.Media.
                        getBitmap(Objects.requireNonNull(getActivity())
                                .getContentResolver(), fileUri);
                mSelectedImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_choose_file:
                showChoosingFile();
                break;

            case R.id.btn_upload_file:
                if (mImageUploadState == REQUEST_BYTE_UPLOAD) {
                    uploadBytes();
                } else if (mImageUploadState == REQUEST_FILE_UPLOAD) {
                    uploadFile();
                } else if (mImageUploadState == REQUEST_STREAM_UPLOAD) {
                    uploadStream();
                }
                break;

            case R.id.btn_post_message:
                    postMessage();
                break;

            case R.id.img_file:
                showChoosingFile();
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Do something that differs the Activity's menu here
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.byte_upload:
                setState(REQUEST_BYTE_UPLOAD);
                Toast.makeText(getActivity(), R.string.byte_option_selected,
                        Toast.LENGTH_SHORT).show();
                return true;

            case R.id.stream_upload:
                setState(REQUEST_STREAM_UPLOAD);
                Toast.makeText(getActivity(), R.string.stream_option_selected,
                        Toast.LENGTH_SHORT).show();
                return true;

            case R.id.file_upload:
                setState(REQUEST_FILE_UPLOAD);
                Toast.makeText(getActivity(), R.string.file_option_selected,
                        Toast.LENGTH_SHORT).show();
                return true;

            default:
                setState(REQUEST_FILE_UPLOAD);
                return super.onOptionsItemSelected(item);
        }
    }
//------------------------------------------------------------------------------------------------//

    /**
     * File file extension
     * @param uri File uri
     */
//------------------------------------------------------------------------------------------------//

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = Objects.requireNonNull(getActivity()).getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

//------------------------------------------------------------------------------------------------//

    /**
     * File name generator. Method is used when Image is upload from user to Server.
     */
//------------------------------------------------------------------------------------------------//
    private String fileNameGenerator() {
        @SuppressLint("SimpleDateFormat") String logFileName =
                new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
        logFileName = "img" + logFileName;
        return logFileName;
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Method is used to send post message to API
     */
//------------------------------------------------------------------------------------------------//
    private void postMessage() {
        if (mUploadText != null) {
            if (mUploadText.length() > 0) {
                // Write a message to the database
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("messages");
                myRef.setValue(mUploadText.getText().toString());
                mUploadText.setText("");
                Toast.makeText(getActivity(), R.string.message_post, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), R.string.empty_text, Toast.LENGTH_LONG).show();
            }
        }
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Set upload state
     */
//------------------------------------------------------------------------------------------------//
    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mImageUploadState + " -> " + state);
        mImageUploadState = state;
    }
}