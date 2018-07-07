# GraySon
Smart Mirror

Project name: Grayson

platform: Adnroid Things

Required Hardware: 

(1) Raspbarry Pi x 1 pc.

(2) External Usb Soundcard

(3) Microphone

First configure Raspberry Pi screen density with command:

$ adb shell wm density <correct_density_value>

Project features:

* Mobile Android application 
        --> remote Bluetooth control;
        --> send photos over Bluettoh usin RFCOMM protocol
        --> Create FreBase api account, verify, sing in, sing out
        --> Post messages online
        --> Send photos via Api

* Voice control using Android PocketSphinx;
        Phrases:
        --> "WekeUp"
        --> "Okay Gray"
        --> "start music,
             play next song,
             play previous song, 
             stop song, 
             play next video,
             stop video, 
             show day photo, 
             stop day photo, and a more...... from corpus.txt file"

* Music player
        --> integrated music player with media button and voice control;

* Video player
        --> integrated video player with media button and voice control;

* Bluetooth connection
        --> remote Bluetooth control;
        --> send photos over Bluettoh usin RFCOMM protocol;
        --> Audio A2DP profile - play music from phone;

* Weather API integrated;
        --> Show 5 days forecast. Data is fetch from https://openweathermap.org/ in JSON format. 
            Here was very interesting to understant how is working. Is not so hard to understant;

* Google Asssistant integrated;
        --> after "Ok Gray" phrase "Ok Google" with continious conversation. 
            After phrase "ok goodbye", "goodbye" or "system back" we back to main recognizer;
        
* Firebase Analytics API;
        --> Bug report in real time. This is very good practice and developer can track bug in code. 
            Thanks Google!!!
        
* Firebase Storage API;
        --> Great place to store photos. Here we only store photos and to update to the system is used Realtime Database. 
            Very Nice!!

* Firebase Real Time database;
        --> post messages via API in real time;
        --> send photos via API in real time;

* Google Account Sing In, Out;

// TODO :

* Light control;

* Play Youtube;

* Google Actions;

Thanks for visiting my git page. This project is created for DevLabs company in Varna, Bulgaria. 
