package com.heynaveed.layloscave.utils.maps.tools;

/**
 * Created by Naveed PC on 05/11/2016.
 */
public final class TileVector {

    protected final int x;
    protected final int y;

    public TileVector(int x, int y){
        this.x = x;
        this.y = y;
    }

    public int x(){
        return x;
    }

    public int y(){
        return y;
    }
}