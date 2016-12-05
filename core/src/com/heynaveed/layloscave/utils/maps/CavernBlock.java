package com.heynaveed.layloscave.utils.maps;


public final class CavernBlock {

    private final int blockNumber;
    private boolean isPathBlock = false;
    private boolean isStartBlock = false;
    private static final int[] X_BLOCK_MIDPOINTS = {13, 13, 13, 13, 41, 41, 41, 41, 69, 69, 69, 69, 97, 97, 97, 97};
    private static final int[] Y_BLOCK_MIDPOINTS = {23, 71, 119, 167, 23, 71, 119, 167, 23, 71, 119, 167, 23, 71, 119, 167};
    private PathDirection.Cavern direction = PathDirection.Cavern.NONE;
    private final TileVector midPoint;

    public CavernBlock(int blockNumber){
        this.blockNumber = blockNumber;
        midPoint = new TileVector(X_BLOCK_MIDPOINTS[blockNumber-1], Y_BLOCK_MIDPOINTS[blockNumber-1]);
    }

    public PathDirection.Cavern getDirection(){
        return direction;
    }

    public void setDirection(PathDirection.Cavern direction){
        this.direction = direction;
    }

    public int getBlockNumber(){
        return blockNumber;
    }

    public boolean isPathBlock(){
        return isPathBlock;
    }

    public void setPathBlock(boolean isPathBlock){
        this.isPathBlock = isPathBlock;
    }

    public CavernBlock setStartBlock(boolean isStartBlock){
        this.isStartBlock = isStartBlock;
        return this;
    }

    public TileVector getMidPoint(){
        return midPoint;
    }
}
