package com.example.root.grayson.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.root.grayson.R;

public class RemoteViewAdapter extends BaseAdapter {
    private Context mContext;

    public RemoteViewAdapter(Context context) {
        this.mContext = context;
    }

    public int getCount() {
        return mThumbIds.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return position;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 210));
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setPadding(8, 16, 16, 8);
            imageView.animate();
            imageView.setElevation(4f);
            imageView.setBackgroundColor(mContext.getResources().getColor(R.color.gridItems));
            imageView.setTag(position);
            imageView.setClickable(false);
            imageView.setFocusable(false);
            imageView.setFocusableInTouchMode(false);
        } else {
            imageView = (ImageView) convertView;
        }
        imageView.setImageResource(mThumbIds[position]);
        return imageView;
    }
    // references to our images
    private Integer[] mThumbIds = {
            R.drawable.ic_power_settings_red_48dp, R.drawable.ic_power_settings_new_on_48dp,
            R.drawable.ic_music_note_yellow_48, R.drawable.ic_photo_filter_black_48dp,
            R.drawable.ic_cloud_blue_48dp, R.drawable.ic_new_releases_24dp,
            R.drawable.ic_wb_incandescent_on_24dp, R.drawable.ic_light_bulb_red_off_48dp,
            R.drawable.ic_light_bulb_green_on_48dp, R.drawable.ic_lightbulb_outline_blue_24dp,
            R.drawable.ic_light_bulb_red_off_48dp, R.drawable.ic_timer_purple_48dp,
            R.drawable.ic_record_voice_over_neutral_48dp, R.drawable.ic_new_google_assistant_logo,
            R.drawable.ic_firebase_logo, R.drawable.ic_color_palette,
    };
}