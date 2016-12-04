package com.heynaveed.layloscave.utils.maps;


public final class CavernBlock {

    private final int blockNumber;
    private boolean isPathBlock = false;
    private boolean isStartBlock = false;
    private PathDirection.Cavern direction = PathDirection.Cavern.NONE;

    public CavernBlock(int blockNumber){
        this.blockNumber = blockNumber;
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
}
