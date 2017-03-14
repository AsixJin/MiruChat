package com.asix.youtubechat;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.sendbird.android.SendBird;

public class Main extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        SendBird.init("1A49BA89-1050-4C5C-8E12-E37D8B337457", getApplicationContext());

        findViewById(R.id.button_host).setOnClickListener(this);
        findViewById(R.id.button_join).setOnClickListener(this);
    }

    private void showLoginDialog(boolean isHost){
        Dialog loginDialog = AsixUtils.createDialog(this, R.layout.login_dialog);
        AsixUtils.setVisibility(loginDialog.findViewById(R.id.edit_youtube), isHost);

        loginDialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_host:
                showLoginDialog(true);
                break;
            case R.id.button_join:
                showLoginDialog(false);
                break;
            default:
                AsixUtils.makeToast(getApplicationContext(), "Button Inactive");
                break;
        }
    }
}
