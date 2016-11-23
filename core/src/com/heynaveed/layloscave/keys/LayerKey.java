package com.heynaveed.layloscave.keys;


public enum LayerKey {

    BACKGROUND(0), TERRAIN(1), GROUND(2), ROTATION(3),
    ICE(4), MUDDY(5), BOUNCY(6), CRUMBLING(7);

    private final int key;

    LayerKey(int key){
        this.key = key;
    }

    public int getKey(){
        return key;
    }
}
