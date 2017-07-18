package com.asix.miruchat.chatlog;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.asix.miruchat.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ChatlogAdapter extends RecyclerView.Adapter<ChatlogAdapter.ChatHolder>{

    RecyclerView recyclerView;
    ArrayList<ChatItem> chatlog = new ArrayList<>();
    SimpleDateFormat timeStampFormat = new SimpleDateFormat("EEE h:mm aa");

    public ChatlogAdapter(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    @Override
    public ChatHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chatlog, null);
        ChatHolder holder = new ChatHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ChatHolder holder, int position) {
        ChatItem item = chatlog.get(position);

        holder.userName.setText(item.getUser());
        holder.userMessage.setText(item.getMessage());

        Calendar now = Calendar.getInstance();
        String time = timeStampFormat.format(now.getTime());
        holder.userTime.setText(time);
    }

    @Override
    public int getItemCount() {
        return chatlog.size();
    }

    public void addMessage(ChatItem item){
        chatlog.add(item);
        notifyDataSetChanged();
        recyclerView.scrollToPosition(chatlog.size()-1);
    }

    class ChatHolder extends RecyclerView.ViewHolder{
        public TextView userName;
        public TextView userMessage;
        public TextView userTime;

        public ChatHolder(View view) {
            super(view);
            userName = (TextView)view.findViewById(R.id.senderName);
            userMessage = (TextView)view.findViewById(R.id.chatMessage);
            userTime = (TextView)view.findViewById(R.id.messageTime);
        }
    }
}
