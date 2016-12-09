package com.heynaveed.layloscave.utils.maps;

import com.heynaveed.layloscave.states.MapState;

import java.util.ArrayList;


final class TunnelMap extends Map implements MapBuilder {

    private static final int TUNNEL_WIDTH = 15;
    private static final int SLOT_HEIGHT = TUNNEL_WIDTH;
    private static final int MIN_X = 15;
    private static final int MIN_Y = 15;
    private static final int MAX_X = 185;
    private static final int MAX_Y = 285;
    private static final int STREAM_AREA_CUTOFF = 200;
    private static final int MIN_ISLAND_GIRTH = 10;
    private static final int TUNNEL_LENGTH = 69;
    private static final int TUNNEL_BORDER = 15;
    private static final int[] TUNNEL_X_POINTS = {MIN_X, 100};
    private static final ArrayList<BoxIsland> topBoxIslands = new ArrayList<BoxIsland>();
    private static final ArrayList<BoxIsland> bottomBoxIslands = new ArrayList<BoxIsland>();
    private static final ArrayList<BoxIsland> slotIslands = new ArrayList<BoxIsland>();
    private static final ArrayList<Node> tunnelEntranceNodes = new ArrayList<Node>();

    TunnelMap(int height, int width){
        super(height, width);
        mapState = MapState.TUNNEL;

        initialiseWorkingValues();
        initialiseTileIDSet();
        generateTerrain();
        populateTileIDSet();
    }

    @Override
    public void initialiseWorkingValues() {
        topBoxIslands.clear();
        bottomBoxIslands.clear();
        slotIslands.clear();
        tunnelEntranceNodes.clear();
    }

    @Override
    public void initialiseTileIDSet() {
        for(int x = 0; x < MapGenerator.TUNNEL_MAP_HEIGHT; x++){
            for(int y = 0; y < MapGenerator.TUNNEL_MAP_WIDTH; y++) {
                if(x < TUNNEL_BORDER || x > MapGenerator.TUNNEL_MAP_HEIGHT - TUNNEL_BORDER || y < TUNNEL_BORDER || y > MapGenerator.TUNNEL_MAP_WIDTH - TUNNEL_BORDER)
                    tileIDSet[x][y] = randomTileID(CAVE_IDS);
                else
                    tileIDSet[x][y] = 0;
            }
        }
    }

    @Override
    public void generateTerrain() {
        int randomYLength;
        int workingY = MIN_Y;

        while(workingY < STREAM_AREA_CUTOFF){
            BoxIsland island;
            randomYLength = RANDOM.nextInt(26)+MIN_ISLAND_GIRTH;

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
            randomYLength = RANDOM.nextInt(26)+MIN_ISLAND_GIRTH;

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

            if(RANDOM.nextInt(2) == 0)
                slotIslands.add(new BoxIsland(
                        new TileVector(topRight.x, topRight.y),
                        new TileVector(topRight.x + SLOT_HEIGHT, topRight.y + TUNNEL_WIDTH)));
        }

        for(int i = 0; i < topBoxIslands.size()-1; i++){
            TileVector bottomRight = topBoxIslands.get(i).getBottomRight();
            TileVector[] tileVectors = new TileVector[15];

            for(int j = 0; j < tileVectors.length; j++)
                tileVectors[j] = new TileVector(bottomRight.x + 8, bottomRight.y + j);

            tunnelEntranceNodes.add(new Node(tileVectors));
        }

        for(int i = 0; i < bottomBoxIslands.size()-1; i++){
            TileVector bottomRight = bottomBoxIslands.get(i).getBottomRight();
            TileVector[] tileVectors = new TileVector[15];

            for(int j = 0; j < tileVectors.length; j++)
                tileVectors[j] = new TileVector(bottomRight.x + 8, bottomRight.y + j);

            tunnelEntranceNodes.add(new Node(tileVectors));
        }
    }

    @Override
    public void populateTileIDSet() {

        ArrayList<BoxIsland> mainIslands = topBoxIslands;
        mainIslands.addAll(bottomBoxIslands);

        for(int i = 0; i < mainIslands.size(); i++){
            TileVector topLeft = mainIslands.get(i).getTopLeft();
            TileVector bottomRight = mainIslands.get(i).getBottomRight();
            for(int j = topLeft.x; j < bottomRight.x; j++){
                for(int k = topLeft.y; k < bottomRight.y; k++)
                    tileIDSet[j][k] = randomTileID(CAVE_IDS);
            }
        }

        for(int i = 0; i < slotIslands.size(); i++){
            TileVector topLeft = slotIslands.get(i).getTopLeft();
            TileVector bottomRight = slotIslands.get(i).getBottomRight();
            for(int x = topLeft.x; x < bottomRight.x; x++){
                for(int y = topLeft.y; y < bottomRight.y; y++)
                    tileIDSet[x][y] = randomTileID(CAVE_IDS);
            }
        }

        for(int i = 0; i < tunnelEntranceNodes.size(); i++){
            TileVector[] tileVectors = tunnelEntranceNodes.get(i).getTileVectorsAsArray();
            for(int j = 0; j < tileVectors.length; j++)
                tileIDSet[tileVectors[j].x][tileVectors[j].y] = randomTileID(CAVE_IDS);
        }
    }
}
