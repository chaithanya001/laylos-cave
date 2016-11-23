package com.heynaveed.layloscave.utils.maps.tools;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Naveed PC on 04/11/2016.
 */
public final class Node {

    private final TileVector[] tileVectors;
    private boolean isRootNode;

    public Node(TileVector[] tileVectors){
        this.tileVectors = tileVectors;
    }

    public Node isRootNode(boolean isRootNode){
        this.isRootNode = isRootNode;
        return this;
    }

    public TileVector getLeftTilePos(){
        return tileVectors[0];
    }

    public TileVector getRightTilePos(){
        return tileVectors[tileVectors.length-1];
    }

    public TileVector[] getTileVectorsAsArray(){
        return tileVectors;
    }

    public ArrayList<TileVector> getTileVectorsAsList(){
        return new ArrayList<TileVector>(Arrays.asList(tileVectors));
    }
}
