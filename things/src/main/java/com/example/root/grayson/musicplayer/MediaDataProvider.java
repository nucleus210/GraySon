package com.example.root.grayson.musicplayer;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

/**
 * Data provider class
 */

class MediaDataProvider {

    private final ArrayList<Song> songs = new ArrayList<>();
    private final ArrayList<Video> videos = new ArrayList<>();

//------------------------------------------------------------------------------------------------//

    /**
     * Scanning for all music files in External Storage
     *
     * @param context given context.
     * @return song object.
     */
//------------------------------------------------------------------------------------------------//
    ArrayList<Song> scanSongs(Context context) {

        Uri musicUri = (MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        ContentResolver resolver = context.getContentResolver();
        Cursor mCursor;

        // Strings metaData projections
        // String[] genresProjection = {MediaStore.Audio.Genres.NAME,
        //                             MediaStore.Audio.Genres._ID};

        String SONG_ID = MediaStore.Audio.Media._ID;
        String SONG_TITLE = MediaStore.Audio.Media.TITLE;
        String SONG_ARTIST = MediaStore.Audio.Media.ARTIST;
        String SONG_ALBUM = MediaStore.Audio.Media.ALBUM;
        String SONG_FILEPATH = MediaStore.Audio.Media.DATA;
        String SONG_DURATION = MediaStore.Audio.Media.DURATION;
        String SONG_GENRES = MediaStore.Audio.Genres._ID;

        String[] columns =
                        {
                        SONG_ID,
                        SONG_TITLE,
                        SONG_ARTIST,
                        SONG_ALBUM,
                        SONG_FILEPATH,
                        SONG_DURATION,
                        SONG_GENRES
                        };

        final String musicsOnly = MediaStore.Audio.Media.IS_MUSIC + "=1";

        // Actually querying the system
        mCursor = resolver.query(musicUri, columns, musicsOnly, null, null);

        if (isExternalStorageReadable() && mCursor != null && mCursor.moveToFirst()) {
            do {
                Song song = new Song();
                song.set_genres(mCursor.getString(mCursor.getColumnIndex(SONG_GENRES)));
                song.set_title(mCursor.getString(mCursor.getColumnIndex(SONG_TITLE)));
                song.set_artist(mCursor.getString(mCursor.getColumnIndex(SONG_ARTIST)));
                song.set_album(mCursor.getString(mCursor.getColumnIndex(SONG_ALBUM)));
                song.set_duration(mCursor.getString(mCursor.getColumnIndex(SONG_DURATION)));
                song.set_songUri(ContentUris.withAppendedId
                        (MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                mCursor.getInt(mCursor.getColumnIndex(MediaStore.Audio.Media._ID))));
                String duration = getDuration(Integer.parseInt(mCursor.getString
                        (mCursor.getColumnIndex(MediaStore.Audio.Media.DURATION))));
                song.set_duration(duration);

                // String genre_column_index = Integer.toString(mCursor.getColumnIndexOrThrow
                //        (MediaStore.Audio.Genres._ID));

                MediaMetadataRetriever mr = new MediaMetadataRetriever();

                Uri trackUri = ContentUris.withAppendedId
                        (MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                mCursor.getInt(mCursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                try {
                    mr.setDataSource(context, trackUri);
                } catch (Exception ignored) {
                }

                songs.add(song);  //add object to the list
            } while (mCursor.moveToNext());
        } else {
            Log.d("mCursor", "Cursor is empty.");
        }
        if (mCursor != null) {
            mCursor.close();
        }

        // sort the song list alphabetically based on the song title.
        Collections.sort(songs, new Comparator<Song>() {
            public int compare(Song a, Song b) {
                return a.get_title().compareTo(b.get_title());
            }
        });

        return songs;
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Clear Song List.
     */
//------------------------------------------------------------------------------------------------//
    void destroySongList() {
        songs.clear();
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Clear Video List.
     */
//------------------------------------------------------------------------------------------------//
    void destroyVideoList() {
        videos.clear();
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Method is used to get Song Duration
     */
//------------------------------------------------------------------------------------------------//
    private static String getDuration(long millis) {
        if (millis < 1) {
            throw new IllegalArgumentException("Duration more than zero.");
        }
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MICROSECONDS.toSeconds(millis);

        return (minutes < 10 ? "0" + minutes : minutes) +
                ":" +
                (seconds < 10 ? "0" + seconds : seconds);
    }
//------------------------------------------------------------------------------------------------//

    /**
     * Method is used to check Readable Permission
     */
//------------------------------------------------------------------------------------------------//
    private boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

//------------------------------------------------------------------------------------------------//

    /**
     * Scanning for all videos files in External Storage
     *
     * @param context given context.
     * @return video object.
     */
//------------------------------------------------------------------------------------------------//
    ArrayList<Video> scanVideo(Context context) {

        Uri musicUri = (MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        ContentResolver resolver = context.getContentResolver();
        Cursor mCursor;

        String VIDEO_ID = MediaStore.Video.Media._ID;
        String VIDEO_TITLE = MediaStore.Video.Media.DISPLAY_NAME;
        String VIDEO_FILEPATH = MediaStore.Video.Media.DATA;
        String VIDEO_DURATION = MediaStore.Video.Media.DURATION;

        String[] projection =
                              {
                               VIDEO_ID,
                               VIDEO_TITLE,
                               VIDEO_FILEPATH,
                               VIDEO_DURATION,
                              };

        // Actually querying the system

        mCursor = resolver.query(musicUri, projection, null, null,
                null);
        if (isExternalStorageReadable() && mCursor != null && mCursor.moveToFirst()) {
            do {
                Video video = new Video();
                video.set_title(mCursor.getString(mCursor.getColumnIndex(VIDEO_TITLE)));
                video.set_duration(mCursor.getString(mCursor.getColumnIndex(VIDEO_DURATION)));
                video.set_videoUri(ContentUris.withAppendedId
                        (MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                mCursor.getInt(mCursor.getColumnIndex(MediaStore.Video.Media._ID))));
                String duration = getDuration(Integer.parseInt(mCursor.getString
                        (mCursor.getColumnIndex(MediaStore.Video.Media.DURATION))));
                video.set_duration(duration);

                MediaMetadataRetriever mr = new MediaMetadataRetriever();

                Uri trackUri = ContentUris.withAppendedId
                        (MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                mCursor.getInt(mCursor.getColumnIndex(MediaStore.Video.Media._ID)));
                try {
                    mr.setDataSource(context, trackUri);
                } catch (Exception ignored) {
                }

                videos.add(video);  //add object to the list
            } while (mCursor.moveToNext());
        } else {
            Log.d("mCursor", "Cursor is empty.");
        }
        if (mCursor != null) {
            mCursor.close();
        }

        // sort the song list alphabetically based on the song title.
        Collections.sort(videos, new Comparator<Video>() {
            public int compare(Video a, Video b) {
                return a.get_title().compareTo(b.get_title());
            }
        });

        return videos;
    }
}