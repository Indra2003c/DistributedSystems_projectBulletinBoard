package com.klassen;

import java.security.MessageDigest;
import java.time.format.SignStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Base64;
import javax.crypto.SealedObject;

public class BulletinBoard {
    private static final int SIZE = 100; // bijvoorbeeld 100
    private final ArrayList<HashMap<String, byte[]>> bulletinBoard; // ConcurrentHashMap<String,byte[]>
    private MessageDigest sha;

    public BulletinBoard() {
        bulletinBoard = new ArrayList<HashMap<String, byte[]>>(); // ConcurrentHashMap<String,byte[]>
        for (int i = 0; i < SIZE; i++) {
            bulletinBoard.add(new HashMap<String, byte[]>()); // ConcurrentHashMap<String,byte[]>
        }
        try {
            sha = MessageDigest.getInstance("SHA-256");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public synchronized void add(int i, byte[] v, String tag) {
        bulletinBoard.get(i).put(tag, v);
    }

    public synchronized byte[] get(int idx, String b) {

        //
        String tag = hash(b);
        if (bulletinBoard.get(idx).containsKey(tag)) {
            byte[] value = bulletinBoard.get(idx).get(tag);
            // remove <t,v>
            bulletinBoard.get(idx).remove(tag);
            return value;// reutrn v
        }
        return null;
    }

    private String hash(String b) {
        // zelfde als bij clients
        try {
            byte[] b_bytes = b.getBytes();
            sha.update(b_bytes);
            byte[] hash_b = sha.digest();
            String hash_b_string = Base64.getEncoder().encodeToString(hash_b);
            return hash_b_string;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // getter for the size
    public static int get_size() {
        return SIZE;
    }


}
