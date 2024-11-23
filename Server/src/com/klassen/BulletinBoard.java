package com.klassen;
import java.time.format.SignStyle;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class BulletinBoard {
    private static final int SIZE = 100; // bijvoorbeeld 100
    private final ArrayList<ConcurrentHashMap<String,String>> bulletinBoard;

    public BulletinBoard() {
        bulletinBoard = new ArrayList<>();
        for(int i = 0; i<SIZE; i++){
            bulletinBoard.add(i, new ConcurrentHashMap<>());
        }
    }

    public synchronized void add(int i, String v, String tag) {
        bulletinBoard.get(i).put(tag, v);
    }

    public synchronized String get(int i, String b) {

        //
        String tag = hash(b); // MOET NOG HASH VAN B WORDEN
        if(bulletinBoard.get(i).containsKey(tag)){
            String value=  bulletinBoard.get(i).get(tag); 
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
}
