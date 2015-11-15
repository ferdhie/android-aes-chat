package com.ferdianto.android.encryptedchat;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Base64;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ChatService extends Service {
    String user;
    String pass;
    String server;
    String secret;
    Socket socket;
    boolean isEncrypted;
    IncomingMessageListener incomingMessageListener;
    SecretKey secretKey;
    Receiver receiver;
    OutputStream outputStream;
    boolean connected = false;
    boolean loggedin = false;
    volatile boolean sync = false;

    List<IncomingMessageListener> listeners = new ArrayList<>();
    ArrayBlockingQueue<String> incomingQueue = new ArrayBlockingQueue<String>(100);

    public ChatService() {
    }

    public void registerListener(IncomingMessageListener l) {
        //ensure 1 listeners
        if ( !listeners.contains(l) )
            listeners.add(l);
    }

    public void unregisterListener(IncomingMessageListener l) {
        this.listeners.remove(l);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private final Binder  binder = new ChatBinder();

    class ChatBinder extends Binder {
        public ChatService getService() {
            return ChatService.this;
        }
    }

    public void connect(String user, String pass, String server, String secret, boolean isEncrypted) throws Exception {
        this.user=user;
        this.pass=pass;
        this.secret=secret;
        this.server=server;
        this.secretKey = getSecretKey(secret);
        this.isEncrypted=isEncrypted;

        connected=false;
        loggedin=false;

        socket = new Socket( server, 24281 );
        this.outputStream = socket.getOutputStream();

        this.incomingMessageListener =listener1;
        this.receiver=new Receiver();
        this.receiver.start();

        String resp = sendAndReceive("PING");
        if (!resp.startsWith("PONG")) {
            throw new Exception("Invalid server secret");
        }
        connected=true;

        resp = sendAndReceive("LOGIN " + user + " " + pass);
        if (!resp.startsWith("OK")) {
            throw new Exception("Invalid username/password");
        }
        loggedin=true;
    }

    public void cleanup() {
        if (socket!=null) try { socket.close(); } catch(Exception ex){}
    }

    private String receive(String line) throws Exception {
        byte[] decoded = Base64.decode(line.getBytes(), Base64.NO_WRAP);
        byte[] plainText = null;

        if (this.isEncrypted) {
            plainText = decrypt(secretKey, decoded);
        } else {
            plainText = decoded;
        }

        String incomingMessage = new String(plainText);
        return incomingMessage;
    }

    public void send(String line) throws Exception {


        byte[] encrypted = null;
        if (this.isEncrypted) {
            encrypted = encrypt(secretKey, line.getBytes());
        } else {
            encrypted = line.getBytes();
        }

        byte[] encoded = Base64.encode(encrypted, Base64.NO_WRAP);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(encoded);
        baos.write('\r');
        baos.write('\n');

        this.outputStream.write(baos.toByteArray());
        this.outputStream.flush();
    }

    public synchronized String sendAndReceive(String line) throws Exception {
        try {
            sync = true;
            send(line);
            return incomingQueue.take();
        } finally {
            sync = false;
        }
    }


    private void fireIncomingMessage(ChatService client, String msg) {
        for(IncomingMessageListener l: listeners) {
            l.onIncomingMessage(client, msg);
        }
    }

    IncomingMessageListener listener1 = new IncomingMessageListener() {
        @Override
        public void onIncomingMessage(ChatService client, String msg) {
            if (sync) {
                incomingQueue.offer(msg);
            }
            fireIncomingMessage(client, msg);
        }
    };

    static interface IncomingMessageListener {
        public void onIncomingMessage(ChatService client, String msg);
    }

    class Receiver implements Runnable {

        public void start() {
            Thread thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();
        }

        @Override
        public void run() {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String line=null;
                while(null != ( line = br.readLine() )) {
                    try {
                        String incoming = receive(line);
                        if (incomingMessageListener !=null)
                            incomingMessageListener.onIncomingMessage(ChatService.this, incoming);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Thread.yield();
                }
            } catch (IOException e) {
                if ( socket!=null ) try{socket.close();} catch(Exception e2){}
            } finally {
                if ( br!=null ) try{br.close();} catch(Exception e){}
            }
        }

    }
    public static SecretKey getSecretKey(String strKey) throws Exception {
        byte[] secret;
        try {
            secret = strKey.getBytes("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            secret = strKey.getBytes();
        }

        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] bkey = Arrays.copyOf(sha256.digest(secret), 128 / Byte.SIZE);
        SecretKey key = new SecretKeySpec(bkey, "AES");
        return key;
    }

    public static byte[] encrypt(SecretKey key, byte[] plain) throws Exception {
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] iv = new SecureRandom().generateSeed(16);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        c.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] encrypted =c.doFinal(plain);

        //add iv into the message
        byte[] encrypted_with_iv = new byte[ encrypted.length+16 ];
        System.arraycopy(iv, 0, encrypted_with_iv, 0, iv.length);
        System.arraycopy(encrypted, 0, encrypted_with_iv, 16, encrypted.length);

        return encrypted_with_iv;
    }

    public static byte[] decrypt(SecretKey key, byte[] encrypted) throws Exception {
        byte[] iv = new byte[16];
        System.arraycopy(encrypted, 0, iv, 0, iv.length);

        byte[] msg = new byte[encrypted.length-16];
        System.arraycopy(encrypted, 16, msg, 0, msg.length);

        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        c.init(Cipher.DECRYPT_MODE, key, ivSpec);
        byte[] plain = c.doFinal(msg);
        return plain;
    }
}
