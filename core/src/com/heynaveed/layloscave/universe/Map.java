package com.heynaveed.layloscave.universe;

import com.heynaveed.layloscave.states.MapState;
import com.heynaveed.layloscave.utils.maps.PathDirection;

import java.util.Random;

public abstract class Map {

    protected static Random RANDOM = new Random();
    protected static int[] CAVE_IDS = {6, 7, 8, 9, 10};
    protected final int width;
    protected final int height;
    protected MapState mapState;
    protected int[][] tileIDSet;

    protected Map(int height, int width){
        this.height = height;
        this.width = width;
        tileIDSet = new int[height][width];
    }

    protected static PathDirection.Hub[] truncateDirectionArray(PathDirection.Hub[] array) {
        PathDirection.Hub[] temp = new PathDirection.Hub[array.length - 1];
        for (int i = 0; i < temp.length; i++)
            temp[i] = array[i];
        return temp;
    }

    protected void smoothMap(int iterations){
        for(int i = 0; i < iterations; i++) {
            for (int j = 0; j < height; j++) {
                for (int k = 0; k < width; k++) {
                    int neighbourWallTileCount = calculateSurroundingWallCount(j, k);

                    if (neighbourWallTileCount > 4)
                        tileIDSet[j][k] = randomTileID(CAVE_IDS);
                    else if (neighbourWallTileCount < 4)
                        tileIDSet[j][k] = 0;
                }
            }
        }
    }

    private int calculateSurroundingWallCount(int gridX, int gridY){
        int wallCount = 0;

        for(int neighbourX = gridX - 1; neighbourX <= gridX + 1; neighbourX++){
            for(int neighbourY = gridY -1; neighbourY <= gridY +1; neighbourY++){
                if(neighbourX >= 0 && neighbourX < height && neighbourY >= 0 && neighbourY < width){
                    if(neighbourX != gridX || neighbourY != gridY)
                        wallCount += tileIDSet[neighbourX][neighbourY] == 0?0:1;
                }
                else
                    wallCount++;
            }
        }
        return wallCount;
    }

    protected int offsetRandom(int value){
        return RANDOM.nextInt(15) + value;
    }

    protected boolean isWallTile(int x, int y){
        return x < 6 || x > height -10 || y < 6 || y > width -8;
    }

    protected int randomTileID(int[] array){
        return array[RANDOM.nextInt(array.length)];
    }

    public int[][] getTileIDSet(){
        return tileIDSet;
    }

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return height;
    }

    public MapState getMapState(){
        return mapState;
    }
}

