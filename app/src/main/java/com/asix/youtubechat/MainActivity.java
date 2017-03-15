package com.asix.youtubechat;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.sendbird.android.GroupChannel;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private final String TAG = "MAIN";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);
        SendBird.init("1A49BA89-1050-4C5C-8E12-E37D8B337457", getApplicationContext());

        findViewById(R.id.button_host).setOnClickListener(this);
        findViewById(R.id.button_join).setOnClickListener(this);

        showAskUsernameDialog();
    }

    private void showAskUsernameDialog(){
        final Dialog usernameDialog = AsixUtils.createDialog(this, R.layout.dialog_askuser);
        usernameDialog.setCancelable(false);

        usernameDialog.findViewById(R.id.button_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = AsixUtils.getEditText_Text(usernameDialog.findViewById(R.id.edit_username));
                if(AsixUtils.doesStringExist(username)){
                    makeUser(username, usernameDialog);
                }else{
                    AsixUtils.makeToast(getApplicationContext(), "Please enter a username").show();
                }
            }
        });

        usernameDialog.show();
    }

    private void showAskRoomDialog(final boolean isHost){
        final Dialog loginDialog = AsixUtils.createDialog(this, R.layout.dialog_login);
        AsixUtils.setVisibility(loginDialog.findViewById(R.id.edit_youtube), isHost);

        loginDialog.findViewById(R.id.button_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String room = AsixUtils.getEditText_Text(loginDialog.findViewById(R.id.edit_roomname));
                //String ytLink = AsixUtils.getEditText_Text(loginDialog.findViewById(R.id.edit_youtube));

                if(AsixUtils.doesStringExist(room)){
                    if(isHost){
                        makeRoom(room, isHost);
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

    public void makeUser(final String username, final Dialog dialog){
        SendBird.connect(username, new SendBird.ConnectHandler() {
            @Override
            public void onConnected(User user, SendBirdException e) {
                if (e != null) {
                    // Error.
                    AsixUtils.makeToast(getApplicationContext(), "Register User Failed! Please try again.").show();
                    e.printStackTrace();
                }else{
                    dialog.dismiss();
                    MiruUser.initUser(user);
                    AsixUtils.makeToast(getApplicationContext(), "Welcome " + user.getUserId()).show();
                }
            }
        });
    }

    public void makeRoom(final String roomname, final boolean host){
        ArrayList<User> userList = new ArrayList<>();
        userList.add(MiruUser.getUser());
        GroupChannel.createChannel(userList, true, roomname, null, null, null, new GroupChannel.GroupChannelCreateHandler() {
            @Override
            public void onResult(GroupChannel groupChannel, SendBirdException e) {
                if (e != null) {
                    // Error.
                    AsixUtils.makeToast(getApplicationContext(), "Error making channel. Please try again").show();
                    e.printStackTrace();
                }else{
                    MiruUser.joinRoom(groupChannel, host);
                    AsixUtils.makeToast(getApplicationContext(), "Welcome to the " + groupChannel.getName() + " channel").show();
                    Log.i("TAG", groupChannel.getUrl());
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

                MiruUser.joinRoom(groupChannel, host);
                AsixUtils.makeToast(getApplicationContext(), "Welcome to the " + groupChannel.getName() + " channel").show();
                startActivity(new Intent(getApplicationContext(), ChatActivity.class));
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_host:
                showAskRoomDialog(true);
                break;
            case R.id.button_join:
                showAskRoomDialog(false);
                break;
            default:
                AsixUtils.makeToast(getApplicationContext(), "Button Inactive").show();
                break;
        }
    }
}
