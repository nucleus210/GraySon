package com.example.root.grayson;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Data provider class
 */

class ImageDataProvider {
    private static final String TAG = "ImageDataProvider: ";

//------------------------------------------------------------------------------------------------//

    /**
     * Method is used to get Song Duration
     */
//------------------------------------------------------------------------------------------------//

    ArrayList<String> getFilePaths(Context context) {

        Uri mUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.ImageColumns.DATA};
        ContentResolver resolver = context.getContentResolver();

        Cursor mCursor = null;
        SortedSet<String> dirList = new TreeSet<>();
        ArrayList<String> resultIAV = new ArrayList<>();

        String[] directories = null;
        if (mUri != null) {

            mCursor = resolver.query(mUri, projection, null, null, null);
        }
        if ((mCursor != null) && (mCursor.moveToFirst())) {
            do {
                String tempDir = mCursor.getString(0);
                tempDir = tempDir.substring(0, tempDir.lastIndexOf("/"));
                try {
                    dirList.add(tempDir);
                } catch (Exception e) {
                    Log.e(TAG, "Error", e);
                }
            }
            while (mCursor.moveToNext());
            mCursor.close();
            directories = new String[dirList.size()];
            dirList.toArray(directories);
        }

        for (int i = 0; i < dirList.size(); i++) {
            File imageDir = new File(Objects.requireNonNull(directories)[i]);
            File[] imageList = imageDir.listFiles();
            if (imageList == null)
                continue;
            for (File imagePath : imageList) {
                try {

                    if (imagePath.isDirectory()) {
                        imageList = imagePath.listFiles();
                    }
                    if (imagePath.getName().contains(".jpg")
                            || imagePath.getName().contains(".JPG")
                            || imagePath.getName().contains(".jpeg")
                            || imagePath.getName().contains(".JPEG")
                            || imagePath.getName().contains(".png")
                            || imagePath.getName().contains(".PNG")
                            || imagePath.getName().contains(".gif")
                            || imagePath.getName().contains(".GIF")
                            || imagePath.getName().contains(".bmp")
                            || imagePath.getName().contains(".BMP")
                            ) {
                        String path = imagePath.getAbsolutePath();
                        resultIAV.add(path);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return resultIAV;
    }
}