package com.example.root.grayson;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import java.nio.charset.Charset;
import java.util.Objects;

import static android.content.ContentValues.TAG;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ImageUploadFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ImageUploadFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ImageUploadFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int PICK_IMAGE_REQUEST = 1001;

    Button mImagePickerBt;
    Button mImageUploadBt;
    ImageView mImage;
    SharedViewModel sharedViewModel;


    private OnFragmentInteractionListener mListener;

    public ImageUploadFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ImageUploadFragment.
     */
    // TODO: Rename and change types and number of parameters
    @SuppressWarnings("unused")
    public static ImageUploadFragment newInstance(String param1, String param2) {
        ImageUploadFragment fragment = new ImageUploadFragment();
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
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_image_upload, container, false);
        mImagePickerBt = view.findViewById(R.id.image_pick_new);
        mImageUploadBt = view.findViewById(R.id.image_send);
        mImage = view.findViewById(R.id.image_container);

        // View Model for communication between fragments
        try {
            sharedViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity()))
                    .get(SharedViewModel.class);
            sharedViewModel.getSelected().observe(this, item -> {
                Log.d(TAG, "passed");

                mImage.setImageBitmap(item);
            });

        } catch (NullPointerException w) {
            Log.d(TAG, "Null pointer.", w);
        }


//----------------------------------ButtonUploadImage---------------------------------------------//
        mImageUploadBt.setOnClickListener(v -> {
            BluetoothActivityFragment parentFrag = ((BluetoothActivityFragment)
                    ImageUploadFragment.this.getParentFragment());
            try {
                String message =ActionMenu.FILE_OPERATION.name();
                byte[] bytes = message.getBytes(Charset.defaultCharset());
                Objects.requireNonNull(parentFrag).mBluetoothConnectionService.writeMessage(bytes);
                Objects.requireNonNull(parentFrag).prepareImageUpload();
                //Objects.requireNonNull(parentFrag).imageUpload();
            } catch (NullPointerException w) {
                Log.d(TAG, "NuLL", w);
            }
        });
//------------------------------------ButtonSendImage---------------------------------------------//

        mImagePickerBt.setOnClickListener(v -> {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(photoPickerIntent, PICK_IMAGE_REQUEST);
        });

        return view;
    }


    // TODO: Rename method, update argument and hook method into UI event
    @SuppressWarnings("unused")
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onImageSendFragment(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
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
//------------------------------------------------------------------------------------------------//

    /**
     * onActivityResult method
     */
//------------------------------------------------------------------------------------------------//
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case PICK_IMAGE_REQUEST:
                try {
                    if (resultCode == Activity.RESULT_OK) {
                        Uri selectedImage = data.getData();
                        if (selectedImage != null) {
                            String uri = selectedImage.getPath();
                            Bitmap bmp = decodeUri(selectedImage);
                            sharedViewModel.select(bmp);
                            System.out.print("Image Path: " + uri);
                        }
                    }

                } catch (NullPointerException w) {
                    Log.v(TAG, "Please select image.", w);
                }
                break;
        }
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Convert and resize our image to 400dp for faster uploading our images to DB
     */
//------------------------------------------------------------------------------------------------//
    private Bitmap decodeUri(Uri selectedImage) {

        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(Objects.requireNonNull(getActivity()).
                    getContentResolver().openInputStream(selectedImage), null, o);

            // The new size we want to scale to
            // final int REQUIRED_SIZE =  size;
            int imageHeight = o.outHeight;
            int imageWidth = o.outWidth;
            // Find the correct scale value. It should be the power of 2.
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (width_tmp / 2 >= imageWidth &&
                    height_tmp / 2 >= imageHeight) {
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(getActivity().
                    getContentResolver().openInputStream(selectedImage), null, o2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onImageSendFragment(Uri uri);
    }
}
