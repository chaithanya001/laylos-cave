package com.heynaveed.layloscave.utils.maps;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by naveed.shihab on 05/12/2016.
 */

public class TunnelMap {

    private static final Random random = new Random();
    private static final int WALL_GIRTH = 3;
    private static final int TUNNEL_WIDTH = 15;
    private static final int MIN_X = 15;
    private static final int MIN_Y = 15;
    private static final int MAX_X = 185;
    private static final int MAX_Y = 285;
    private static final int[] TUNNEL_X_POINTS = {MIN_X, 100};
    private static final int MAIN_FLOOR_HEIGHT = 100;
    private int workingHeight = MAX_X;
    private int workingWidth = MIN_Y;

    private final ArrayList<BoxIsland> boxIslands;

    public TunnelMap(){
        boxIslands = new ArrayList<BoxIsland>();
        generateTunnelIslands();
    }

    private void generateTunnelIslands(){
        int randomYLength;
        for(int i = 0; i < TUNNEL_X_POINTS.length; i++) {
            for (int y = MIN_Y; y < MAX_Y; y++) {
                if (y < 240) {
                    BoxIsland island;
                    randomYLength = (random.nextInt(WALL_GIRTH) + WALL_GIRTH) * (random.nextInt(WALL_GIRTH) + WALL_GIRTH);
                    island = new BoxIsland(new TileVector(TUNNEL_X_POINTS[i], y), new TileVector(TUNNEL_X_POINTS[i]+60, y + randomYLength));
                    boxIslands.add(island);
                    y += (randomYLength - 1) + TUNNEL_WIDTH;
                } else break;
            }
        }

        int halfSize = boxIslands.size()/2+1;
        for(int i = 0; i < halfSize-1; i++){
            TileVector topRight = boxIslands.get(i+halfSize).getTopRight();
            if(random.nextInt(2) == 0){
                boxIslands.add(new BoxIsland(
                        new TileVector(topRight.x, topRight.y),
                        new TileVector(topRight.x+TUNNEL_WIDTH, topRight.y+TUNNEL_WIDTH)));
            }
        }
    }

    public ArrayList<BoxIsland> getBoxIslands(){
        return boxIslands;
    }
}
