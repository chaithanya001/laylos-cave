package com.heynaveed.layloscave.states;

/**
 * Created by naveed.shihab on 16/11/2016.
 */

public enum PathDirectionState {
    UP(-1, 0), DOWN(1, 0), LEFT(0, -1), RIGHT(0, 1), NONE(0, 0);

    public final int x;
    public final int y;

    PathDirectionState(int x, int y){
        this.x = x;
        this.y = y;
    }
}
