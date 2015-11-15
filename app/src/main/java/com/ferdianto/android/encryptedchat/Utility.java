package com.ferdianto.android.encryptedchat;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Utility classes, for misc functions
 * Created by ferdhie on 14-04-2015.
 */
public class Utility {

    public static void saveContacts(Context context, List<String> contacts) throws IOException {
        File file = new File(context.getFilesDir(), "contacts.txt");
        BufferedWriter br=null;
        try {
            br = new BufferedWriter(new FileWriter(file, false));
            for (String contact : contacts) {
                String con = contact.replace("\r", "").replace("\n", "").trim();
                if (con.length()>0)
                    br.write(contact.replace("\r", "").replace("\n", "") + "\n");
            }
        } finally {
            if (br!=null) try{br.close();} catch(Exception e){}
        }

    }

    public static List<String> loadContacts(Context context) throws IOException {

        File file = new File(context.getFilesDir(), "contacts.txt");
        List<String> contacts=new ArrayList<>();
        if (file.exists()) {
            BufferedReader br = null;
            br = new BufferedReader(new FileReader(file));
            try {
                for(String s=""; null!=s; s=br.readLine()) {
                    s=s.trim();
                    if (s.length()>0)
                        contacts.add(s);
                }
            } finally {
                if (br!=null) try { br.close(); } catch (Exception e) {}
            }
        }
        return contacts;

    }
//
//    public static SharedPreferences getPreferences(Context context) {
//        return context.getSharedPreferences(((ActionBarActivity) context).getClass().getSimpleName(), Context.MODE_PRIVATE);
//    }
//
//    public static void savePreferences(Context context, String key, String value) {
//        final SharedPreferences prefs = getPreferences(context);
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.putString(key, value);
//        editor.commit();
//    }
//
//    public static String getFromPreferences(Context context, String key) {
//        final SharedPreferences prefs = getPreferences(context);
//        String value = prefs.getString(key, "");
//        if (value.isEmpty()) {
//            return "";
//        }
//        return value;
//    }
//
//    public static SecretKey getSecretKey(String strKey) throws Exception {
//        final int AES_KEY_SIZE = 128;
//        byte[] secret;
//        try {
//            secret = strKey.getBytes("UTF-8");
//        } catch (UnsupportedEncodingException ex) {
//            secret = strKey.getBytes();
//        }
//
//        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
//        byte[] bkey = Arrays.copyOf(sha256.digest(secret), AES_KEY_SIZE / Byte.SIZE);
//        SecretKey key = new SecretKeySpec(bkey, "AES");
//        return key;
//    }
//
//    public static byte[] encrypt(SecretKey key, byte[] plain) throws Exception {
//        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
//        byte[] iv = new SecureRandom().generateSeed(16);
//        IvParameterSpec ivSpec = new IvParameterSpec(iv);
//        c.init(Cipher.ENCRYPT_MODE, key, ivSpec);
//        byte[] encrypted =c.doFinal(plain);
//
//        //add iv into the message
//        byte[] encrypted_with_iv = new byte[ encrypted.length+16 ];
//        System.arraycopy(iv, 0, encrypted_with_iv, 0, iv.length);
//        System.arraycopy(encrypted, 0, encrypted_with_iv, 16, encrypted.length);
//
//        return encrypted_with_iv;
//    }
//
//    public static byte[] decrypt(SecretKey key, byte[] encrypted) throws Exception {
//        byte[] iv = new byte[16];
//        System.arraycopy(encrypted, 0, iv, 0, iv.length);
//
//        byte[] msg = new byte[encrypted.length-16];
//        System.arraycopy(encrypted, 16, msg, 0, msg.length);
//
//        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
//        IvParameterSpec ivSpec = new IvParameterSpec(iv);
//        c.init(Cipher.DECRYPT_MODE, key, ivSpec);
//        byte[] plain = c.doFinal(msg);
//        return plain;
//    }
//
//
}
