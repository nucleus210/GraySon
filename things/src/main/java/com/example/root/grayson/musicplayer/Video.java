package com.example.root.grayson.musicplayer;

import android.net.Uri;


public class Video {
    private int    _ID;         // Video ID integer
    private String _title;      // Video Title
    private String _dataPath;   // Video Path to External Public Storage
    private String _duration;   // Video Duration
    private Uri    _videoUri;   // Video Uri

//------------------------------------------------------------------------------------------------//
    /**
     * Empty Constructor
     */
//---------------------------------------------------------------------------------------------...//
    Video () {
    }
    @SuppressWarnings("unused")
    public Video (
                String title,
                String dataPath,
                String duration,
                Uri songUri) {

        this.set_title(title);
        this.set_dataPath(dataPath);
        this.set_duration(duration);
        this.set_videoUri(songUri);
    }

//------------------------------------------------------------------------------------------------//
    /**
     * @return Video Metadata
     */
//---------------------------------------------------------------------------------------------...//
    @SuppressWarnings("unused")
    public int get_ID() {
        return _ID;
    }

    @SuppressWarnings("unused")
    public void set_ID(int _ID) {
        this._ID = _ID;
    }


    public String get_title() {
        return _title;
    }

    public void set_title(String _title) {
        this._title = _title;
    }

    @SuppressWarnings("unused")
    public String get_dataPath() {
        return _dataPath;
    }

    private void set_dataPath(String _dataPath) {
        this._dataPath = _dataPath;
    }

    @SuppressWarnings("unused")
    public CharSequence get_duration() {
        return _duration;
    }

    public void set_duration(String _duration) {
        this._duration = _duration;
    }

    public Uri get_videoUri() {
        return _videoUri;
    }

    public void set_videoUri(Uri _songUri) {
        this._videoUri = _songUri;
    }
}
