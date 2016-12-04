package com.heynaveed.layloscave.utils.maps;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by naveed.shihab on 16/11/2016.
 */

final class HubSegment {

    private final TileVector[] tileVectors;
    private final PathDirection.Hub direction;

    HubSegment(){
        direction = HubPath.CURRENT_DIRECTION;
        tileVectors = createPathSegment(HubPath.SEGMENT_LENGTH, HubPath.WORKING_POSITION);
    }

    private TileVector[] createPathSegment(int segmentLength, TileVector workingPosition){

        TileVector[] temp = new TileVector[segmentLength];

        for(int i = 0; i < segmentLength; i++)
            temp[i] = new TileVector(workingPosition.x + (i*direction.x), workingPosition.y + (i*direction.y));

        return temp;
    }

    PathDirection.Hub getDirection(){
        return direction;
    }

    TileVector getStartTilePos(){
        return tileVectors[0];
    }

    TileVector getEndTilePos(){
        return tileVectors[tileVectors.length-1];
    }

    TileVector[] getTileVectorsAsArray(){
        return tileVectors;
    }

    ArrayList<TileVector> getTileVectorsAsList(){
        return new ArrayList<TileVector>(Arrays.asList(tileVectors));
    }
}
