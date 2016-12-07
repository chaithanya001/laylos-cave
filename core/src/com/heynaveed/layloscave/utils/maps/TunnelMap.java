package com.heynaveed.layloscave.utils.maps;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by naveed.shihab on 05/12/2016.
 */

public class TunnelMap {

    private static final Random random = new Random();
    private static final int TUNNEL_WIDTH = 15;
    private static final int SLOT_HEIGHT = TUNNEL_WIDTH;
    private static final int MIN_X = 15;
    private static final int MIN_Y = 15;
    private static final int MAX_X = 185;
    private static final int MAX_Y = 285;
    private static final int STREAM_AREA_CUTOFF = 200;
    private static final int MIN_ISLAND_GIRTH = 10;
    private static final int[] TUNNEL_X_POINTS = {MIN_X, 100};
    private static final int TUNNEL_LENGTH = 69;

    private final ArrayList<BoxIsland> topBoxIslands;
    private final ArrayList<BoxIsland> bottomBoxIslands;
    private final ArrayList<BoxIsland> slotIslands;

    public TunnelMap(){
        topBoxIslands = new ArrayList<BoxIsland>();
        bottomBoxIslands = new ArrayList<BoxIsland>();
        slotIslands = new ArrayList<BoxIsland>();
        generateTunnelIslands();
    }

    private void generateTunnelIslands(){

        int randomYLength;
        int workingY = MIN_Y;

        while(workingY < STREAM_AREA_CUTOFF){
            BoxIsland island;
            randomYLength = random.nextInt(26)+MIN_ISLAND_GIRTH;

            if(workingY + randomYLength > STREAM_AREA_CUTOFF)
                randomYLength = STREAM_AREA_CUTOFF - workingY;

            island = new BoxIsland(new TileVector(TUNNEL_X_POINTS[0], workingY), new TileVector(TUNNEL_X_POINTS[0]+TUNNEL_LENGTH, workingY + randomYLength));
            topBoxIslands.add(island);

            if(workingY + randomYLength + TUNNEL_WIDTH > STREAM_AREA_CUTOFF)
                workingY += randomYLength;
            else
                workingY += randomYLength + TUNNEL_WIDTH;
        }

        workingY = MIN_Y;

        while(workingY < STREAM_AREA_CUTOFF){
            BoxIsland island;
            randomYLength = random.nextInt(26)+MIN_ISLAND_GIRTH;

            if(workingY + randomYLength > STREAM_AREA_CUTOFF)
                randomYLength = STREAM_AREA_CUTOFF - workingY;

            island = new BoxIsland(new TileVector(TUNNEL_X_POINTS[1], workingY), new TileVector(TUNNEL_X_POINTS[1]+TUNNEL_LENGTH, workingY + randomYLength));
            bottomBoxIslands.add(island);

            if(workingY + randomYLength + TUNNEL_WIDTH > STREAM_AREA_CUTOFF)
                workingY += randomYLength;
            else
                workingY += randomYLength + TUNNEL_WIDTH;
        }

        for(int i = 0; i < bottomBoxIslands.size()-2; i++){
            BoxIsland referenceIsland = bottomBoxIslands.get(i);
            TileVector topRight = referenceIsland.getTopRight();

            if(random.nextInt(2) == 0)
                slotIslands.add(new BoxIsland(
                        new TileVector(topRight.x, topRight.y),
                        new TileVector(topRight.x + SLOT_HEIGHT, topRight.y + TUNNEL_WIDTH)));
        }
    }

    public ArrayList<BoxIsland> getBottomBoxIslands(){
        return bottomBoxIslands;
    }

    public ArrayList<BoxIsland> getTopBoxIslands(){
        return topBoxIslands;
    }

    public ArrayList<BoxIsland> getSlotIslands(){
        return slotIslands;
    }
}
