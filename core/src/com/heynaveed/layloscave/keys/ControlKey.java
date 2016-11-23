package com.heynaveed.layloscave.keys;


public enum ControlKey {

    LEFT(0), RIGHT(1), UP(2), JINI(3), DOWN(4);

    private final int key;

    ControlKey(int key){
        this.key = key;
    }

    public int getKey(){
        return key;
    }
}
