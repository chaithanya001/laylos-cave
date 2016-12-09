package com.heynaveed.layloscave.utils.maps;


public final class Room {

    private final int roomNumber;
    private boolean isPathBlock = false;
    public static final int[] X_BLOCK_MIDPOINTS = {13, 13, 13, 13, 41, 41, 41, 41, 69, 69, 69, 69, 97, 97, 97, 97};
    public static final int[] Y_BLOCK_MIDPOINTS = {23, 71, 119, 167, 23, 71, 119, 167, 23, 71, 119, 167, 23, 71, 119, 167};
    private PathDirection.Stage direction = PathDirection.Stage.NONE;
    private final TileVector midPoint;

    public Room(int roomNumber){
        this.roomNumber = roomNumber;
        midPoint = new TileVector(X_BLOCK_MIDPOINTS[roomNumber -1], Y_BLOCK_MIDPOINTS[roomNumber -1]);
    }

    public PathDirection.Stage getDirection(){
        return direction;
    }

    public void setDirection(PathDirection.Stage direction){
        this.direction = direction;
    }

    public int getRoomNumber(){
        return roomNumber;
    }

    public boolean isPathBlock(){
        return isPathBlock;
    }

    public void setPathBlock(boolean isPathBlock){
        this.isPathBlock = isPathBlock;
    }

    TileVector getMidPoint(){
        return midPoint;
    }
}
