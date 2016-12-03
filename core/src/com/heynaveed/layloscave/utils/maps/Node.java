package com.heynaveed.layloscave.utils.maps;

import java.util.ArrayList;
import java.util.Arrays;


final class Node {

    private final TileVector[] tileVectors;
    private boolean isRootNode;

    Node(TileVector[] tileVectors){
        this.tileVectors = tileVectors;
    }

    Node isRootNode(boolean isRootNode){
        this.isRootNode = isRootNode;
        return this;
    }

    TileVector getLeftTilePos(){
        return tileVectors[0];
    }

    TileVector getRightTilePos(){
        return tileVectors[tileVectors.length-1];
    }

    TileVector[] getTileVectorsAsArray(){
        return tileVectors;
    }

    ArrayList<TileVector> getTileVectorsAsList(){
        return new ArrayList<TileVector>(Arrays.asList(tileVectors));
    }
}
