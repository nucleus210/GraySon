package com.example.root.grayson;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

class GoogleCloud {

    private static final String TAG = "Google Cloud";
    private static final String DEVICE_MODEL_ID = "grayson-a2a66";
    private static final String DEVICE_INSTANCE_ID = "PLACEHOLDER";

    private CloudStorageAccountListener mListener;
    private Context mContext;
    private StorageReference mStorageRef;                        // Declare Cloud Storage
    FirebaseDatabase mFireBaseDatabase;
    DatabaseReference mDatabaseRef;
    FirebaseAnalytics mFireBaseAnalytics;                        // Declare Fire base client
    FirebaseAuth mAuth;

    String mIncomeFileName;

    public interface CloudStorageAccountListener {
        @SuppressWarnings("unused")
        void cloudStorageAccountListener(Bitmap bitmap);
        void cloudStorageErrorListener(String error);

    }

    GoogleCloud(Context context, CloudStorageAccountListener listener){
        this.mContext = context;
        this.mListener = listener;

        // Obtain the FireBase Authentication instance.
        mAuth = FirebaseAuth.getInstance();
        // Obtain the FireBaseDatabase instance.
        mFireBaseDatabase = FirebaseDatabase.getInstance();
        // Obtain the FireBaseStorage instance.
        mStorageRef = FirebaseStorage.getInstance().getReference();
        // Obtain the FireBaseDatabase reference.
        mDatabaseRef = mFireBaseDatabase.getReference();

//------------------------------------FireBaseAnalytic--------------------------------------------//
        // Obtain the FireBaseAnalytics instance.
        mFireBaseAnalytics = FirebaseAnalytics.getInstance(mContext);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, DEVICE_MODEL_ID);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, DEVICE_INSTANCE_ID);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
        mFireBaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUIFirebase(currentUser);
    }

    @SuppressWarnings("unused")
    static void updateUIGoogle(GoogleSignInAccount account) {
    }

    String showGoogleAccount() {
        // Get profile information
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(mContext);
        String personName = Objects.requireNonNull(acct).getDisplayName();
        String personGivenName = acct.getGivenName();
        String personFamilyName = acct.getFamilyName();
        String personEmail = acct.getEmail();

        return personName + "\n" + personFamilyName + "\n" + personGivenName + "\n"
                + personEmail;
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Method is used to sing in user Fire Base account.
     *
     * @param email E-mail address
     * @param password User password
     */
//------------------------------------------------------------------------------------------------//
    void singInFireBaseStorage(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");

                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUIFirebase(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(mContext, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            mListener.cloudStorageErrorListener("signInWithEmail:failure :( "
                                    + task.getException());
                            updateUIFirebase(null);
                        }
                    }
                });
    }

//------------------------------------------------------------------------------------------------//

    /**
     * Initialize FireBase Storage and fetch data from API
     * File method
     */
//------------------------------------------------------------------------------------------------//
    private void DownloadFireBaseStorage(String fileName) {

        // Create an instance of Fire base Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();

        // Create a storage reference from our app
        mStorageRef = storage.getReference();

        StorageReference islandRef = mStorageRef.child("images/" + fileName);

        File localFile = null;
        try {
            localFile = File.createTempFile("images", "jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        final File finalLocalFile = localFile;
        islandRef.getFile(Objects.requireNonNull(localFile)).addOnSuccessListener
                (new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        final Bitmap[] bitmap = new Bitmap[1];
                        Log.e("Test", "success!");
                        bitmap[0] = BitmapFactory.decodeFile(finalLocalFile.getAbsolutePath());
                        String name = null;
                        try {
                            name = fileNameGenerator();
                            // Inform listener for new file
                            mListener.cloudStorageAccountListener(bitmap[0]);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            saveBitMap(mContext, bitmap[0], name);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                mListener.cloudStorageErrorListener("Download error :( ");
            }
        });
    }

//------------------------------------------------------------------------------------------------//

    /**
     * FireBase Storage and fetch data from API
     * Byte method
     */
//------------------------------------------------------------------------------------------------//
    void setImageFromCloud(){
        new DownloadFireBaseImage().execute();
    }

//------------------------------------------------------------------------------------------------//

    /**
     * File name generator. Method is used when Image is upload from user to Server.
     */
//------------------------------------------------------------------------------------------------//
    private String fileNameGenerator() throws IOException {
        return File.createTempFile("img", ".jpg",
                Environment.getExternalStoragePublicDirectory
                        (Environment.DIRECTORY_PICTURES)).toString();
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Save image to externalPublicStorage from bitmap.
     *
     * @param context get default context
     * @param bmp     Bitmap object
     * @param name    Filename for new file
     */
//------------------------------------------------------------------------------------------------//
    private static void saveBitMap(Context context, Bitmap bmp, String name) throws IOException {
        File file = new File(name);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        try (FileOutputStream fo = new FileOutputStream(file)) {
            bmp.compress(Bitmap.CompressFormat.JPEG, 50, bytes);
            file.createNewFile();
            fo.write(bytes.toByteArray());
        } catch (NullPointerException w) {
            Log.d(TAG, "NuLL", w);
        }
        try {
            MediaScannerConnection.scanFile(context,
                    new String[]{file.toString()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                        }
                    });
        } catch (NullPointerException w) {
            Log.d("Media Scanner: NuLL", String.valueOf(w));
        }
    }

    void updateUIFirebase(FirebaseUser account) {

    }

//------------------------------------------------------------------------------------------------//

    /**
     * AsyncTask to fetch data from Api
     */
//------------------------------------------------------------------------------------------------//
    @SuppressLint("StaticFieldLeak")
    private class DownloadFireBaseImage extends AsyncTask<Bitmap, Bitmap, Bitmap> {
        @Override
        protected Bitmap doInBackground(Bitmap... bitmaps) {
            try {
                DownloadFireBaseStorage(mIncomeFileName);
            } catch (NullPointerException w) {
                Log.d(TAG,"NuLL", w);
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
        }
    }
}
