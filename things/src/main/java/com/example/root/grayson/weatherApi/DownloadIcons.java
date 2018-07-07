package com.example.root.grayson.weatherApi;

import java.io.InputStream;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.os.AsyncTask;
        import android.util.Log;
        import android.widget.ImageView;

public class DownloadIcons extends AsyncTask<String, Void, Bitmap> {
    private static final String TAG = "AppWeather";
    @SuppressLint("StaticFieldLeak")
    private ImageView imageView;

    DownloadIcons(ImageView imageView)
    {
        this.imageView = imageView;
    }

    protected Bitmap doInBackground(String... urls)
    {
        Bitmap bitmap = null;
        try
        {
            InputStream in = new java.net.URL(urls[0]).openStream();
            bitmap = BitmapFactory.decodeStream(in);
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage());
        }
        return bitmap;
    }
    protected void onPostExecute(Bitmap bitmap)
    {
        imageView.setImageBitmap(bitmap);
    }
}