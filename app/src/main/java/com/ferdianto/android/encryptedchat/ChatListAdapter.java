package com.ferdianto.android.encryptedchat;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by ferdhie on 14-04-2015.
 * Adapter list for chat, incoming or outgoing
 */
public class ChatListAdapter extends ArrayAdapter<ChatMessage> {
    private final Activity context;
    private final ArrayList<ChatMessage> list;
    LayoutInflater inflator;

    public ChatListAdapter(Activity context, ArrayList<ChatMessage> list) {
        super(context, R.layout.list_layout, list);
        this.context = context;
        this.list = list;
        inflator = context.getLayoutInflater();
    }

    static class ViewHolder {
        protected TextView text;
        protected TextView sent_or_received;
        protected LinearLayout chat_row_lin;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = inflator.inflate(R.layout.list_chat_row, null);
            viewHolder = new ViewHolder();
            viewHolder.chat_row_lin = (LinearLayout) convertView.findViewById(R.id.chat_row_lin);
            viewHolder.text = (TextView) convertView.findViewById(R.id.person_name);
            viewHolder.sent_or_received = (TextView) convertView.findViewById(R.id.sent_or_received);
            viewHolder.text.setTextColor(Color.BLACK);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (list != null && list.size()>=position) {
            ChatMessage h = list.get(position);
            viewHolder.text.setText(h.text);
            viewHolder.sent_or_received.setText(h.from);
            if (h.direction == 1) {
                viewHolder.chat_row_lin.setGravity(Gravity.RIGHT);
            }
        }

        return convertView;
    }
}
