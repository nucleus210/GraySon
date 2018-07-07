package com.example.root.grayson;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.graphics.Bitmap;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<Bitmap> selected = new MutableLiveData<>();
    BluetoothActivityFragment bluetoothActivityFragment;

    Bitmap select(Bitmap thumbnail) {
        selected.setValue(thumbnail);
        return thumbnail;
    }

    LiveData<Bitmap> getSelected() {
        return selected;
    }

    LiveData<Bitmap> clearSelected() {
        selected.removeObservers(bluetoothActivityFragment);
        return null;
    }
    @SuppressWarnings("unused")
    public Bitmap uploadImageLiveData(Bitmap thumbnail) {
        selected.setValue(thumbnail);
        return thumbnail;
    }
    @SuppressWarnings("unused")
    public LiveData<Bitmap> sendLiveDataSocked() {
        return selected;
    }

    @SuppressWarnings("unused")
    public LiveData<Bitmap> uploadSelectedImageLiveData() {
        //Bitmap bmp = getSelected(selected);
        return selected;
    }
}











