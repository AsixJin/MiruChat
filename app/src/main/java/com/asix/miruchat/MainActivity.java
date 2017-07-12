package com.asix.miruchat;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import com.sendbird.android.GroupChannel;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MAIN";
    private SharedPreferences _prefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.asix.miruchat.R.layout.layout_main);
        //User ButterKnife to bind views
        ButterKnife.bind(this);
        //Init SendBird API
        SendBird.init(getString(R.string.sendBird_AppId), getApplicationContext());
        //Get Shared Prefs
        _prefs = AsixUtils.getSharedPrefs(this);
        //Get the name that the user will use in chat
        getUserName();
    }

    //region ButterKnife On Clicks
    @OnClick(R.id.button_host) public void buttonHostClick(){
        showAskRoomDialog(true);
    }

    @OnClick(R.id.button_join) public void buttonJoinClick(){
        showAskRoomDialog(false);
    }
    //endregion

    //region Username Methods
    private void getUserName(){
        String username = _prefs.getString(getString(R.string.userPrefKey), null);
        if(AsixUtils.doesStringExist(username)){
            makeUser(username, null);
        }else{
            showAskUsernameDialog();
        }
    }

    private void showAskUsernameDialog(){
        final Dialog usernameDialog = AsixUtils.createDialog(this, R.layout.dialog_askuser);
        usernameDialog.setCancelable(false);
        final CheckBox rememberCheck = (CheckBox) usernameDialog.findViewById(R.id.check_rememberMe);

        usernameDialog.findViewById(R.id.button_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = AsixUtils.getEditText_Text(usernameDialog.findViewById(R.id.edit_username));
                if(AsixUtils.doesStringExist(username)){
                    makeUser(username, usernameDialog);
                    if(rememberCheck.isChecked()){
                        _prefs.edit().putString(getString(R.string.userPrefKey), username).apply();
                    }
                }else{
                    AsixUtils.showToast(getApplicationContext(), "Please enter a username");
                }
            }
        });

        usernameDialog.show();
    }

    public void makeUser(final String username, @Nullable final Dialog dialog){
        SendBird.connect(username, new SendBird.ConnectHandler() {
            @Override
            public void onConnected(User user, SendBirdException e) {
                if (e != null) {
                    // Error.
                    AsixUtils.makeToast(getApplicationContext(), "Register User Failed! Please try again.").show();
                    e.printStackTrace();
                }else{
                    if(dialog != null){
                        dialog.dismiss();
                    }
                    MiruUser.initUser(user);
                    AsixUtils.makeToast(getApplicationContext(), "Welcome " + user.getUserId()).show();
                }
            }
        });
    }
    //endregion

    //region Room Methods
    private void showAskRoomDialog(final boolean isHost){
        final Dialog loginDialog = AsixUtils.createDialog(this, R.layout.dialog_login);
        AsixUtils.setVisibility(loginDialog.findViewById(R.id.edit_youtube), isHost);

        loginDialog.findViewById(R.id.button_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String room = AsixUtils.getEditText_Text(loginDialog.findViewById(R.id.edit_roomname));
                String ytLink = AsixUtils.getEditText_Text(loginDialog.findViewById(R.id.edit_youtube));

                if((!isHost && AsixUtils.doesStringExist(room)) || (isHost && AsixUtils.doesStringExist(room) && AsixUtils.doesStringExist(ytLink))){
                    if(isHost){
                        makeRoom(room, ytLink, isHost);
                    }else{
                        joinRoom(room, isHost);
                    }
                }else{
                    AsixUtils.makeToast(getApplicationContext(), "Please enter room name or link").show();
                }

            }
        });

        loginDialog.show();
    }

    public void makeRoom(final String roomname, final String youtubeID, final boolean host){
        ArrayList<User> userList = new ArrayList<>();
        userList.add(MiruUser.getUser());
        GroupChannel.createChannel(userList, true, roomname, null, null, null, new GroupChannel.GroupChannelCreateHandler() {
            @Override
            public void onResult(GroupChannel groupChannel, SendBirdException e) {
                if (e != null) {
                    // Error.
                    AsixUtils.showToast(getApplicationContext(), "Error making channel. Please try again");
                    e.printStackTrace();
                }else{
                    MiruUser.joinRoom(groupChannel, host, youtubeID);
                    AsixUtils.showToast(getApplicationContext(), "Welcome to the " + groupChannel.getName() + " channel");
                    Log.i(TAG, groupChannel.getUrl());
                    startActivity(new Intent(getApplicationContext(), ChatActivity.class));
                }
            }
        });
    }

    public void joinRoom(final String roomlink, final boolean host){
        GroupChannel.getChannel(roomlink, new GroupChannel.GroupChannelGetHandler() {
            @Override
            public void onResult(GroupChannel groupChannel, SendBirdException e) {
                if (e != null) {
                    // Error!
                    e.printStackTrace();
                    return;
                }

                MiruUser.joinRoom(groupChannel, host, null);
                AsixUtils.showToast(getApplicationContext(), "Welcome to the " + groupChannel.getName() + " channel");
                startActivity(new Intent(getApplicationContext(), ChatActivity.class));
            }
        });
    }
    //endregion

}
