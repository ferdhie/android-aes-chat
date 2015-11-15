package com.ferdianto.android.encryptedchat;

/**
 * Created by ferdhie on 14-04-2015.
 * Represent one chat message
 */
public class ChatMessage {

    public static final int DIRECTION_OUTGOING = 1;
    public static final int DIRECTION_INCOMING = 0;


    public String text;
    public String from;
    public int direction;

    public ChatMessage(String text, String from, int direction) {
        this.text=text;
        this.from=from;
        this.direction=direction;
    }

}
