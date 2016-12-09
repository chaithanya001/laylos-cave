package com.heynaveed.layloscave.utils.maps;


public final class Room {

    private final int blockNumber;
    private boolean isPathBlock = false;
    public static final int[] X_BLOCK_MIDPOINTS = {13, 13, 13, 13, 41, 41, 41, 41, 69, 69, 69, 69, 97, 97, 97, 97};
    public static final int[] Y_BLOCK_MIDPOINTS = {23, 71, 119, 167, 23, 71, 119, 167, 23, 71, 119, 167, 23, 71, 119, 167};
    private PathDirection.Cavern direction = PathDirection.Cavern.NONE;
    private final TileVector midPoint;

    Room(int blockNumber){
        this.blockNumber = blockNumber;
        midPoint = new TileVector(X_BLOCK_MIDPOINTS[blockNumber-1], Y_BLOCK_MIDPOINTS[blockNumber-1]);
    }

    PathDirection.Cavern getDirection(){
        return direction;
    }

    void setDirection(PathDirection.Cavern direction){
        this.direction = direction;
    }

    int getBlockNumber(){
        return blockNumber;
    }

    boolean isPathBlock(){
        return isPathBlock;
    }

    void setPathBlock(boolean isPathBlock){
        this.isPathBlock = isPathBlock;
    }

    TileVector getMidPoint(){
        return midPoint;
    }
}
