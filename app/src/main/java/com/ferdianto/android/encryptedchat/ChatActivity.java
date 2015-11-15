package com.ferdianto.android.encryptedchat;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class ChatActivity extends ActionBarActivity implements ChatService.IncomingMessageListener {

    String to;
    ChatService chatService;
    EditText message;
    ListView chatList;
    ChatListAdapter adapter;
    ArrayList<ChatMessage> messageList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bindService(new Intent(this, ChatService.class), serviceConnection, Context.BIND_AUTO_CREATE);

        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        to = intent.getStringExtra("name");

        setTitle("@"+to);

        Button button = (Button)findViewById(R.id.send);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendChat();
            }
        });

        message = (EditText)findViewById(R.id.editText_message);
        message.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                sendChat();
                return false;
            }
        });
        message.setImeActionLabel("Kirim", KeyEvent.KEYCODE_ENTER);

        adapter = new ChatListAdapter(ChatActivity.this, messageList);
        chatList = (ListView)findViewById(R.id.chatList);
        chatList.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    protected void sendChat() {
        String text = message.getText().toString().trim();

        if (text.length()>=256) {
            Toast.makeText(this, "Pesan terlalu panjang, panjang maksimal 256 char", Toast.LENGTH_LONG);
            return;
        }

        Log.i("ChatActivity", "sending chat to " + to + "; " + text);

        if (text.length()>0) {
            new AsyncTask<String, Void, Object>() {
                @Override
                protected Object doInBackground(String... params) {
                    String text = params[0];
                    try {
                        chatService.send("MSG " + to + " " + text);
                        return text;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return e;
                    }
                }

                @Override
                protected void onPostExecute(Object result) {
                    if (result instanceof Exception) {
                        Toast.makeText(getApplicationContext(), "Error: " + ((Exception)result).getMessage(), Toast.LENGTH_LONG);
                    } else {
                        ChatMessage chatMsg = new ChatMessage( chatService.user, (String)result, ChatMessage.DIRECTION_OUTGOING );
                        messageList.add(chatMsg);
                        adapter.notifyDataSetChanged();
                        message.setText("");
                    }
                }
            }.execute(text);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            chatService = ((ChatService.ChatBinder)service).getService();
            chatService.registerListener( ChatActivity.this );
        }
        public void onServiceDisconnected(ComponentName className) {
            chatService.unregisterListener( ChatActivity.this );
            chatService = null;
        }
    };

    @Override
    public void onIncomingMessage(ChatService client, String msg) {
        Log.i("ChatActivity", "incoming msg: " + msg);

        if (msg.startsWith("FROM")) {
            String[] args = msg.split(" ", 3);
            if (args.length>=2 && args[1].equalsIgnoreCase(to)) {

                String incomingMessage = args.length>=3 ? args[2] : "";
                ChatMessage chatMsg = new ChatMessage( to, incomingMessage, ChatMessage.DIRECTION_INCOMING );
                messageList.add(chatMsg);
                adapter.add(chatMsg);
                adapter.notifyDataSetChanged();

            }
        }

    }
}
