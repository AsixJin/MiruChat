package com.asix.youtubechat;

import com.sendbird.android.GroupChannel;
import com.sendbird.android.User;

public class MiruUser {

    private static User sendbirdUser = null;
    private static GroupChannel currentChannel = null;

    private static Boolean host = false;

    public static void initUser(User user){
        sendbirdUser = user;
    }

    public static void joinRoom(GroupChannel channel, boolean isHost){
        currentChannel = channel;
        host = isHost;
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
