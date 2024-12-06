package com.klassen;
import java.time.format.SignStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.SealedObject;

public class BulletinBoard {
    private static final int SIZE = 100; // bijvoorbeeld 100
    private final ArrayList<HashMap<String,byte[]>> bulletinBoard; //ConcurrentHashMap<String,byte[]>

    public BulletinBoard() {
        bulletinBoard = new ArrayList<HashMap<String,byte[]>>(); //ConcurrentHashMap<String,byte[]>
        for(int i = 0; i<SIZE; i++){
            bulletinBoard.add(new HashMap<String,byte[]>()); //ConcurrentHashMap<String,byte[]>
        }
    }

    public synchronized void add(int i, byte[] v, String tag) {
        bulletinBoard.get(i).put(tag, v);
    }

    public synchronized byte[] get(int i, String b) {
        
        //
        String tag = hash(b); // MOET NOG HASH VAN B WORDEN
        if(bulletinBoard.get(i).containsKey(tag)){
            byte[] value=  bulletinBoard.get(i).get(tag); 
            //remove <t,v>
            bulletinBoard.get(i).remove(tag);
            return value;//reutrn v
        }
        return null;
    }

    private String hash(String b){
        //needs to be implemented
        //zelfde als bij clients
        return null;
    }

    //getter for the size
    public static int get_size(){
        return SIZE;
    }
}
