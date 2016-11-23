package com.heynaveed.layloscave.utils.maps.tools;

import com.heynaveed.layloscave.states.PathDirectionState;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by naveed.shihab on 16/11/2016.
 */

class Segment {

    private final TileVector[] tileVectors;
    private final PathDirectionState direction;

    Segment(){
        direction = PathMap.CURRENT_DIRECTION;
        tileVectors = createPathSegment(PathMap.SEGMENT_LENGTH, PathMap.WORKING_POSITION);
    }

    private TileVector[] createPathSegment(int segmentLength, TileVector workingPosition){

        TileVector[] temp = new TileVector[segmentLength];

        for(int i = 0; i < segmentLength; i++)
            temp[i] = new TileVector(workingPosition.x + (i*direction.x), workingPosition.y + (i*direction.y));

        return temp;
    }

    PathDirectionState getDirection(){
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
