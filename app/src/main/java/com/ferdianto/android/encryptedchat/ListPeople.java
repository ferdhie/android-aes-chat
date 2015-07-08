package com.ferdianto.android.encryptedchat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ListPeople extends ActionBarActivity implements LoginFragment.LoginFragmentListener, ChatService.IncomingMessageListener {

    ListView contactList;
    List<String> contacts = new ArrayList<>();
    ArrayAdapter<String> adapter;
    ChatService chatService;
    LoginFragment loginFragment;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            chatService = ((ChatService.ChatBinder)service).getService();
            chatService.registerListener( ListPeople.this );
        }
        public void onServiceDisconnected(ComponentName className) {
            chatService.unregisterListener(ListPeople.this);
            chatService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, ChatService.class);
        startService(intent);

        bindService(new Intent(this, ChatService.class), serviceConnection, Context.BIND_AUTO_CREATE);
        setContentView(R.layout.list_people);

        try {
            contacts = Utility.loadContacts(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        adapter = new ArrayAdapter<String>( this, android.R.layout.simple_list_item_1, contacts );
        contactList = (ListView)findViewById(R.id.contactList);
        contactList.setAdapter(adapter);
        contactList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                doChat( position );
            }
        });

        contactList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                deleteContactAt(position);
                return false;
            }
        });

        loginFragment = new LoginFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.container, loginFragment).commit();

        contactList.setVisibility(View.INVISIBLE);
    }



    protected void doChat(int position) {

        Intent intent = new Intent(ListPeople.this, ChatActivity.class);
        intent.putExtra("name", adapter.getItem(position));
        startActivity(intent);

    }

    protected void addNewContact() {
        final EditText input = new EditText(ListPeople.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        new AlertDialog.Builder(ListPeople.this)
                .setTitle("Edit Contact")
                .setView(input)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newContact = input.getText().toString();
                        contacts.add(newContact);
                        adapter.notifyDataSetChanged();

                        try {
                            Utility.saveContacts(getApplicationContext(), contacts);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }

    protected void deleteContactAt(final int position) {
        new AlertDialog.Builder(ListPeople.this)
            .setTitle("Delete contact")
            .setMessage("Hapus kontak?")
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    String removed = contacts.remove(position);
                    adapter.notifyDataSetChanged();

                    try {
                        Utility.saveContacts(getApplicationContext(), contacts);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            })
            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            })
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        chatService.cleanup();
        unbindService(serviceConnection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list_people, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_new) {
            addNewContact();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void login(String username, String password, String server, String secret) throws Exception {
        chatService.connect(username, password, server, secret);
    }

    @Override
    public void loginSuccess() {
        getSupportFragmentManager().beginTransaction().remove(loginFragment).commit();
        findViewById(R.id.contactList).setVisibility(View.VISIBLE);
    }

    @Override
    public void onIncomingMessage(ChatService client, String msg) {
        Log.i("Chat", "incoming " + msg);
    }

}
