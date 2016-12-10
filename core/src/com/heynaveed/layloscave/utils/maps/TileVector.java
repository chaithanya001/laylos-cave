package com.heynaveed.layloscave.utils.maps;


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

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }
}