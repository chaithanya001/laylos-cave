package com.heynaveed.layloscave.utils.maps;

import java.util.ArrayList;
import java.util.Arrays;


final class PathSegment {

    private final TileVector[] tileVectors;
    private final int segmentLength;
    private final TileVector workingPosition;
    private final PathDirection.Hub direction;

    PathSegment(PathDirection.Hub direction, int segmentLength, TileVector workingPosition) {
        this.direction = direction;
        this.segmentLength = segmentLength;
        this.workingPosition = workingPosition;
        tileVectors = createPathSegment();
    }

    private TileVector[] createPathSegment(){

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
