package com.heynaveed.layloscave.keys;


public enum LayerKey {

    BACKGROUND(0), TERRAIN(1), GROUND(2), ROTATION(3),
    ICE(4), MUDDY(5), BOUNCY(6), CRUMBLING(7);

    public final int index;

    LayerKey(int index){
        this.index = index;
    }
}
