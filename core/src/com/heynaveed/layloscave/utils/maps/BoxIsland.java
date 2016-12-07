package com.heynaveed.layloscave.utils.maps;

/**
 * Created by naveed.shihab on 05/12/2016.
 */

public final class BoxIsland {

    private final TileVector topLeft;
    private final TileVector bottomRight;
    private final TileVector topRight;
    private final TileVector bottomLeft;
    private final TileVector midPoint;

    public BoxIsland(TileVector topLeft, TileVector bottomRight){
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;
        this.topRight = new TileVector(topLeft.x, bottomRight.y);
        this.bottomLeft = new TileVector(topLeft.y, bottomRight.x);
        this.midPoint = calculateMidPoint();
    }

    public TileVector getTopLeft(){
        return topLeft;
    }

    public TileVector getBottomRight(){
        return bottomRight;
    }

    public TileVector getTopRight(){
        return topRight;
    }

    public TileVector getBottomLeft(){
        return bottomRight;
    }

    public TileVector getMidPoint(){
        return midPoint;
    }

    private TileVector calculateMidPoint(){

        int x;
        int y;

        if(topLeft.x > bottomRight.x) x = topLeft.x - bottomRight.x;
        else x = bottomRight.x - topLeft.x;
        if(topLeft.y > bottomRight.y) y = topLeft.y - bottomRight.y;
        else y = bottomRight.y - topLeft.y;

        return new TileVector(x, y);
    }
}
