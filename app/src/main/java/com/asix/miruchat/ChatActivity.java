package com.asix.miruchat;

import android.app.Dialog;
import android.content.*;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.asix.miruchat.chatlog.ChatItem;
import com.asix.miruchat.chatlog.ChatlogAdapter;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.android.youtube.player.YouTubePlayerView;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.sendbird.android.AdminMessage;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.OpenChannel;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserMessage;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    private final String DEVELOPER_KEY = "AIzaSyCso2M5tx2eFVLikAWvtSaHLpjvFBaxVc0";
    private final String TAG = "CHAT";
    private final String SYSMSG = "SYSTEM";
    private GroupChannel channel;

    private PubNub pubnub;
    private YouTubePlayer player;
    private ChatlogAdapter adapter;

    @BindView(R.id.rcy_chatlog) RecyclerView rcyChatLog;
    @BindView(R.id.youtube_fragment) YouTubePlayerView tubePlayer;
    @BindView(R.id.edit_chatmsg) EditText chatMessage;
    @BindView(R.id.button_send) Button sendButton;
    @BindView(R.id.toolbar) Toolbar toolbar;

    private Handler handler = new Handler();
    private Runnable playbackRunnable = new Runnable() {
        @Override
        public void run() {
            pubnub.publish().channel(channel.getName()).message(generateInfo("play")).async(new PNCallback<PNPublishResult>() {
                @Override
                public void onResponse(PNPublishResult result, PNStatus status) {
                    if (status.isError()) {
                        System.out.println("error happened while publishing: " + status.toString());
                    } else {
                        System.out.println("message Published w/ timetoken " + result.getTimetoken());
                    }
                }
            });
            handler.postDelayed(this, 5000);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_chat);
        ButterKnife.bind(this);

        setActionBar(toolbar);

        adapter = new ChatlogAdapter(rcyChatLog);
        rcyChatLog.setAdapter(adapter);

        //Youtube Init
//        YouTubePlayerFragment youTubePlayerFragment = (YouTubePlayerFragment) getFragmentManager().findFragmentById(com.asix.miruchat.R.id.youtube_fragment);
//        youTubePlayerFragment.initialize(DEVELOPER_KEY, this);
        tubePlayer.initialize(DEVELOPER_KEY, this);

        //PubNub
        PNConfiguration pnConfiguration = new PNConfiguration();
        pnConfiguration.setSubscribeKey("sub-c-9a78b534-08d5-11e7-afb0-0619f8945a4f");
        if(MiruUser.isHost()){
            pnConfiguration.setPublishKey("pub-c-2bac26f2-a906-47f9-85aa-3e38a40d9846");
        }
        pnConfiguration.setSecure(false);
        pubnub = new PubNub(pnConfiguration);

        //Get Channel
        channel = MiruUser.getCurrentChannel();

        //region Hook up send button
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = chatMessage.getText().toString();
                if(AsixUtils.doesStringExist(message)){
                    sendMessage(message);
                }else{
                    AsixUtils.makeToast(getApplicationContext(), "Please type a message").show();
                }
            }
        });
        //endregion

        //region Add Channel Handler
        SendBird.addChannelHandler("channelHandler", new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
                if (baseMessage instanceof UserMessage) {
                    // message is a UserMessage
                    Log.i(TAG,"Message Received");
                    channel.markAsRead();
                    addMessageToLog(((UserMessage) baseMessage).getSender().getUserId(), ((UserMessage) baseMessage).getMessage());
                } else if (baseMessage instanceof FileMessage) {
                    // message is a FileMessage
                } else if (baseMessage instanceof AdminMessage) {
                    // message is an AdminMessage
                }
            }

            @Override
            public void onUserEntered(OpenChannel channel, User user) {
                addMessageToLog(SYSMSG, user.getUserId() + " has entered the channel!");
            }

            @Override
            public void onUserExited(OpenChannel channel, User user) {
                addMessageToLog(SYSMSG, user.getUserId() + " has exited the channel!");
            }

            @Override
            public void onUserJoined(GroupChannel channel, User user) {
                addMessageToLog(SYSMSG, user.getUserId() + " has joined the channel!");
            }

            @Override
            public void onUserLeft(GroupChannel channel, User user) {
                addMessageToLog(SYSMSG, user.getUserId() + " has left the channel!");
            }
        });
        //endregion

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {
                Log.i(TAG, status.toString());
            }

            @Override
            public void message(PubNub pubnub, PNMessageResult message) {
                if(!MiruUser.isHost()){
                    Log.i(TAG, message.getMessage().toString());
                    try{
                        syncVideo(message.getMessage().toString());
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {

            }
        });

        pubnub.subscribe()
                .channels(Collections.singletonList(channel.getName()))
                .execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOVERY_DIALOG_REQUEST) {
            // Retry initialization if user performed a recovery action
            getYouTubePlayerProvider().initialize(DEVELOPER_KEY, this);
        }
    }

    private SyncInfo generateInfo(String state){
        SyncInfo info = new SyncInfo();
        info.id = MiruUser.getYoutubeID();
        info.time = player.getCurrentTimeMillis();
        info.state = state;
        return info;
    }

    private void setupHost(){
        player.setPlaybackEventListener(new YouTubePlayer.PlaybackEventListener() {
            @Override
            public void onPlaying() {
                handler.postDelayed(playbackRunnable, 5000);
            }

            @Override
            public void onPaused() {
                handler.removeCallbacks(playbackRunnable);
                pubnub.publish().channel(channel.getName()).message(generateInfo("pause")).async(new PNCallback<PNPublishResult>() {
                    @Override
                    public void onResponse(PNPublishResult result, PNStatus status) {
                        if (status.isError()) {
                            System.out.println("error happened while publishing: " + status.toString());
                        } else {
                            System.out.println("message Published w/ timetoken " + result.getTimetoken());
                        }
                    }
                });;
            }

            @Override
            public void onStopped() {
                handler.removeCallbacks(playbackRunnable);
                pubnub.publish().channel(channel.getName()).message(generateInfo("stop")).async(new PNCallback<PNPublishResult>() {
                    @Override
                    public void onResponse(PNPublishResult result, PNStatus status) {
                        if (status.isError()) {
                            System.out.println("error happened while publishing: " + status.toString());
                        } else {
                            System.out.println("message Published w/ timetoken " + result.getTimetoken());
                        }
                    }
                });;
            }

            @Override
            public void onBuffering(boolean b) {
                handler.removeCallbacks(playbackRunnable);
            }

            @Override
            public void onSeekTo(int i) {
                handler.removeCallbacks(playbackRunnable);
            }
        });
    }

    private void syncVideo(String json) throws Exception{
        JSONObject object = new JSONObject(json);
        String state = object.getString("state");
        switch(state){
            case "play":
                if(player.isPlaying()){
                    int syncTime = object.getInt("time")/1000;
                    int playerTime = player.getCurrentTimeMillis()/1000;
                    int timeApart = syncTime-playerTime;
                    if(timeApart >= 10 || timeApart <= -10){
                        player.seekToMillis(object.getInt("time"));
                    }
                }else{
                    player.cueVideo(object.getString("id"));
                    player.seekToMillis(object.getInt("time"));
                    player.play();
                }
                break;
            case "pause":
                player.pause();
                break;
            case "stop":
                player.pause();
                break;
            default:
                break;
        }
    }

    private void showAskUsernameDialog(){
        final Dialog usernameDialog = AsixUtils.createDialog(this, com.asix.miruchat.R.layout.dialog_askuser);

        usernameDialog.findViewById(com.asix.miruchat.R.id.button_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usernameDialog.dismiss();
                String username = AsixUtils.getEditText_Text(usernameDialog.findViewById(com.asix.miruchat.R.id.edit_username), "");
                if(AsixUtils.doesStringExist(username)){
                    inviteUser(username);
                }else{
                    AsixUtils.makeToast(getApplicationContext(), "Please enter a username").show();
                }
            }
        });

        usernameDialog.show();
    }

    private void addMessageToLog(String user, String msg){
        ChatItem item = new ChatItem(user, msg);
        adapter.addMessage(item);
    }

    //region Menu Stuff
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(MiruUser.isHost()){
            getMenuInflater().inflate(R.menu.menu_invite, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id == R.id.invite){
            showAskUsernameDialog();
        }else if(id == R.id.send){
            AsixUtils.shareText(this, MiruUser.getCurrentChannel().getUrl());
        }
        return super.onOptionsItemSelected(item);
    }
    //endregion

    //region SendBird Methods
    private void sendMessage(String message){
        channel.sendUserMessage(message, new BaseChannel.SendUserMessageHandler() {
            @Override
            public void onSent(UserMessage userMessage, SendBirdException e) {
                if (e != null) {
                    // Error.
                    AsixUtils.makeToast(getApplicationContext(), "There was an issue. Message not sent").show();
                    e.printStackTrace();
                    return;
                }else{
                    channel.markAsRead();
                    addMessageToLog(userMessage.getSender().getUserId(), userMessage.getMessage());
                    chatMessage.setText("");
                }
            }
        });
    }

    private void inviteUser(String user){
        ArrayList<String> userIds = new ArrayList<>();
        userIds.add(user);
        channel.inviteWithUserIds(userIds, new GroupChannel.GroupChannelInviteHandler() {
            @Override
            public void onResult(SendBirdException e) {
                if (e != null) {
                    // Error.
                    AsixUtils.showToast(getApplicationContext(), "Invite User Failed! Try again later");
                    e.printStackTrace();
                    return;
                }

                AsixUtils.showToast(getApplicationContext(), "User Invite Successful");
            }
        });
    }
    //endregion

    //region Youtube Methods
    private static final int RECOVERY_DIALOG_REQUEST = 1;

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider,
                                        YouTubeInitializationResult errorReason) {
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show();
        } else {
            String errorMessage = String.format(getString(com.asix.miruchat.R.string.error_player), errorReason.toString());
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
        this.player = player;
        if(MiruUser.isHost()){
            player.cueVideo(MiruUser.getYoutubeID());
            player.play();
            setupHost();
        }
    }

    protected YouTubePlayer.Provider getYouTubePlayerProvider() {
        return (YouTubePlayerFragment) getFragmentManager().findFragmentById(com.asix.miruchat.R.id.youtube_fragment);
    }
    //endregion
}
