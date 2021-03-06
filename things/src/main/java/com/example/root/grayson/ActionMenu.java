package com.example.root.grayson;

public enum ActionMenu {
    INITIALIZING("INITIALIZING"),
    LISTENING_TO_KEYPHRASE("LISTENING_TO_KEYPHRASE"),
    CONFIRMING_KEYPHRASE("CONFIRMING_KEYPHRASE"),
    LISTENING_TO_ACTION("LISTENING_TO_ACTION"),
    CONFIRMING_ACTION("CONFIRMING_ACTION"),
    BT_DOWNLOAD("BT_DOWNLOAD"),

    LISTENING_TO_ASSISTANT("LISTENING_TO_ASSISTANT"),

    START_MUSIC_PLAYER("START_MUSIC_PLAYER"),
    PREVIOUS_SONG("PREVIOUS_SONG"),
    PLAY_SONG("PLAY_SONG"),
    NEXT_SONG("NEXT_SONG"),
    STOP_SONG("STOP_SONG"),
    STOP_MUSIC_PLAYER("STOP_MUSIC_PLAYER"),

    START_VIDEO_PLAYER("START_VIDEO_PLAYER"),
    PREVIOUS_VIDEO("PREVIOUS_VIDEO"),
    NEXT_VIDEO("NEXT_VIDEO"),
    STOP_VIDEO("STOP_VIDEO"),
    STOP_VIDEO_PLAYER("STOP_VIDEO_PLAYER"),

    FILE_OPERATION("FILE_OPERATION"),

    SHOW_MENU("SHOW_MENU"),
    MENU_NEXT("MENU_NEXT"),
    MENU_PREVIOUS("MENU_PREVIOUS"),
    MENU_SELECT("MENU_SELECT"),
    MENU_BACK("MENU_BACK"),
    HIDE_MENU("HIDE_MENU"),

    SING_IN_GOOGLE("SING_IN_GOOGLE"),
    SING_OUT_GOOGLE("SING_OUT_GOOGLE"),
    CHANGE_GOOGLE_ACCOUNT("CHANGE_GOOGLE_ACCOUNT"),
    CHANGE_WEATHER_CITY("CHANGE_WEATHER_CITY"),
    SING_IN_FIREBASE("SING_IN_FIREBASE"),


    CONFIRM_ASSISTANT("CONFIRM_ASSISTANT"),
    SHOW_DAY_PHOTO("SHOW_DAY_PHOTO"),
    STOP_DAY_PHOTO("STOP_DAY_PHOTO"),
    FIRE_BASE_STORAGE("FIRE_BASE_STORAGE"),
    PLAY_YOUTUBE("PLAY_YOUTUBE"),
    CLOUD_STORAGE("CLOUD_STORAGE"),
    SHOW_WEATHER("SHOW_WEATHER"),
    UPLOAD_IMAGE("UPLOAD_IMAGE"),
    DRIVE_STORAGE("DRIVE_STORAGE"),

    BLUETOOTH_ON("BLUETOOTH_ON"),
    BLUETOOTH_OFF("BLUETOOTH_OFF"),
    BLUETOOTH_DISCOVERY("BLUETOOTH_DISCOVERY"),
    BLUETOOTH_SCAN("BLUETOOTH_SCAN"),

    LIGHTS_ON("LIGHTS_ON"),
    GREEN_LIGHTING("GREEN_LIGHTING"),
    BLUE_LIGHTING("BLUE_LIGHTING"),
    RED_LIGHTING("RED_LIGHTING"),
    AUTO_LIGHTS("AUTO_LIGHTS"),
    DIM_LIGHTS("DIM_LIGHTS"),
    LIGHTS_OFF("LIGHTS_OFF"),

    SHOW_TIME("SHOW_TIME"),
    TURN_OFF("TURN_OFF"),
    TURN_ON("TURN_ON");


    private final String action;

    ActionMenu(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }
}


