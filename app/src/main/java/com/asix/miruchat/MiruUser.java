package com.asix.miruchat;

import android.support.annotation.Nullable;

import com.sendbird.android.GroupChannel;
import com.sendbird.android.User;

public class MiruUser {

    private static User sendbirdUser = null;
    private static GroupChannel currentChannel = null;

    private static Boolean host = false;
    private static String youtubeID = "";

    public static void initUser(User user){
        sendbirdUser = user;
    }

    public static void joinRoom(GroupChannel channel, boolean isHost, @Nullable String tubeID){
        currentChannel = channel;
        host = isHost;
        if(tubeID != null){
            youtubeID = tubeID;
        }
    }

    public static String getYoutubeID(){
        return youtubeID;
    }

    public static User getUser(){
        return sendbirdUser;
    }

    public static Boolean isHost() {
        return host;
    }

    public static GroupChannel getCurrentChannel(){
        return currentChannel;
    }

    public static boolean isUserInit(){
        if(sendbirdUser == null){
            return false;
        }else{
            return true;
        }
    }

}
