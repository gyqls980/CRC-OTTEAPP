package com.example.otte.base;

import java.util.ArrayList;

/**
 * Created by kmuvcl_laptop_dell on 2016-07-05.
 */
public class Constants {
    public static long beforClickTime = 0;
    public static String beforSignal = "00";

    public static String basicColor = "#ff4BADAC";

    public static final int LOCATION_UPDATE_GPS_TIME_MILLISEC = 60000;
    public static final int LOCATION_UPDATE_GPS_DIST_METER = 10;
    public static final int LOCATION_UPDATE_NETWORK_TIME_MILLISEC = 60000;
    public static final int LOCATION_UPDATE_NETWORK_DIST_METER = 10;

    public static final int REQUEST_CONNECT_DEVICE = 2;


    public static final int CAMERA_CAPTURE = 12;

    // CLICK INDEX
    public static final int CLICK_MUSIC_PLAY = 0;
    public static final int CLICK_CAMERA = 1;
    public static final int CLICK_EMERGENCY = 2;
    public static final int CLICK_MANNER_MODE = 3;
    public static final int CLICK_FIND_PHONE = 4;
    public static final int CLICK_LIGHT_CONTROL = 5;
    public static final int CLICK_RECORD_VOICE =6;
    public static int clickIndex = CLICK_MUSIC_PLAY;

    public static final int CLICK_SINGLE = 0;
    public static final int CLICK_DOUBLE = 1;
    public static final int CLICK_HOLD = 2;

    public static int musicPlay = CLICK_SINGLE;
    public static int musicForward = CLICK_DOUBLE;
    public static int musicReverse = CLICK_HOLD;
    public static int emergencySendLocation = CLICK_SINGLE;
}
