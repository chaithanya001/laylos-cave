package com.heynaveed.layloscave.utils.maps;

import java.util.ArrayList;
import java.util.Arrays;


final class Node {

    private final TileVector[] tileVectors;
    private final PathDirection.Hub hubDirection;
    private final PathDirection.Cavern cavernDirection;
    private final PathDirection.Tunnel tunnelDirection;

    Node(TileVector[] tileVectors){
        this.tileVectors = tileVectors;
        this.hubDirection = PathDirection.Hub.NONE;
        this.cavernDirection = PathDirection.Cavern.NONE;
        this.tunnelDirection = PathDirection.Tunnel.NONE;
    }

    Node(TileVector[] tileVectors, PathDirection.Hub hubDirection){
        this.tileVectors = tileVectors;
        this.hubDirection = hubDirection;
        this.cavernDirection = PathDirection.Cavern.NONE;
        this.tunnelDirection = PathDirection.Tunnel.NONE;
    }

    Node(TileVector[] tileVectors, PathDirection.Cavern cavernDirection){
        this.tileVectors = tileVectors;
        this.hubDirection = PathDirection.Hub.NONE;
        this.cavernDirection = cavernDirection;
        this.tunnelDirection = PathDirection.Tunnel.NONE;
    }

    Node(TileVector[] tileVectors, PathDirection.Tunnel tunnelDirection){
        this.tileVectors = tileVectors;
        this.hubDirection = PathDirection.Hub.NONE;
        this.cavernDirection = PathDirection.Cavern.NONE;
        this.tunnelDirection = tunnelDirection;
    }

    PathDirection.Hub getHubDirection(){
        return hubDirection;
    }

    PathDirection.Cavern getCavernDirection(){
        return cavernDirection;
    }

    PathDirection.Tunnel getTunnelDirection(){
        return tunnelDirection;
    }

    TileVector getLeftTilePos(){
        return tileVectors[0];
    }

    TileVector getRightTilePos(){
        return tileVectors[tileVectors.length-1];
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
