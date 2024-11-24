package com.klassen;
import java.time.format.SignStyle;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.SealedObject;

public class BulletinBoard {
    private static final int SIZE = 100; // bijvoorbeeld 100
    private final ArrayList<ConcurrentHashMap<String,SealedObject>> bulletinBoard;

    public BulletinBoard() {
        bulletinBoard = new ArrayList<>();
        for(int i = 0; i<SIZE; i++){
            bulletinBoard.add(i, new ConcurrentHashMap<>());
        }
    }

    public synchronized void add(int i, SealedObject v, String tag) {
        bulletinBoard.get(i).put(tag, v);
    }

    public synchronized SealedObject get(int i, String b) {

        //
        String tag = hash(b); // MOET NOG HASH VAN B WORDEN
        if(bulletinBoard.get(i).containsKey(tag)){
            SealedObject value=  bulletinBoard.get(i).get(tag); 
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
