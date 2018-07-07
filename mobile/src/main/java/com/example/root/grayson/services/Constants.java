package com.example.root.grayson.services;

public interface Constants {
    /**
     * Defines several constants used between {@link BluetoothConnectionService} and the UI.
     */
    // Message types sent from the BluetoothChatService Handler
    int MESSAGE_STATE_CHANGE = 1;
    int MESSAGE_READ = 2;
    int MESSAGE_WRITE = 3;
    int MESSAGE_TOAST = 4;
    int MESSAGE_DEVICE_NAME = 5;
    // Key names received from the BluetoothChatService Handler
    String DEVICE_NAME = "device_name";
    String TOAST = "toast";
}
