package com.example.root.grayson.weatherApi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.root.grayson.R;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class WeatherAdapter extends BaseAdapter {
    private static final String TAG = " WeatherModel Adapter";
    private ArrayList<WeatherModel> listData;
    private LayoutInflater layoutInflater;

    WeatherAdapter(Context context, ArrayList<WeatherModel> listData) {
        this.listData = listData;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint({"DefaultLocale", "InflateParams"})
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d(TAG,"OnCreateView");
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.weather_forecast_list, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.mTemp = convertView.findViewById(R.id.current_temperature_field_list);
            viewHolder.mPressure = convertView.findViewById(R.id.current_pressure_field_list);
            viewHolder.mHumanity = convertView.findViewById(R.id.current_humanity_field_list);
            viewHolder.mDateView = convertView.findViewById(R.id.current_date_field_list);
            viewHolder.mIconView = convertView.findViewById(R.id.weather_icon_list);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        WeatherModel weatherModel = listData.get(position);
        viewHolder.mTemp.setText(String.format("%.1f", weatherModel.temperature));
        viewHolder.mPressure.setText(String.format("%d", weatherModel.pressure));
        viewHolder.mHumanity.setText(String.format("%.1f", weatherModel.humidity));
        viewHolder.mDateView.setText(new SimpleDateFormat("EEEE dd",
                Locale.getDefault()).format(weatherModel.day));

        if (weatherModel.mWeatherIconUrl != null)
            new DownloadIcons(viewHolder.mIconView).execute(weatherModel.mWeatherIconUrl);
        return convertView;
    }

    static class ViewHolder {
        TextView mTemp;
        TextView mDateView;
        TextView mPressure;
        TextView mHumanity;
        ImageView mIconView;
    }
}
