package com.heynaveed.layloscave.utils.maps;

import java.util.ArrayList;
import java.util.Arrays;


public final class Node {

    private final TileVector[] tileVectors;
    private final PathDirection.Hub hubDirection;
    private final PathDirection.Stage stageDirection;
    private final PathDirection.Tunnel tunnelDirection;

    public Node(TileVector[] tileVectors){
        this.tileVectors = tileVectors;
        this.hubDirection = PathDirection.Hub.NONE;
        this.stageDirection = PathDirection.Stage.NONE;
        this.tunnelDirection = PathDirection.Tunnel.NONE;
    }

    public Node(TileVector[] tileVectors, PathDirection.Hub hubDirection){
        this.tileVectors = tileVectors;
        this.hubDirection = hubDirection;
        this.stageDirection = PathDirection.Stage.NONE;
        this.tunnelDirection = PathDirection.Tunnel.NONE;
    }

    Node(TileVector[] tileVectors, PathDirection.Stage stageDirection){
        this.tileVectors = tileVectors;
        this.hubDirection = PathDirection.Hub.NONE;
        this.stageDirection = stageDirection;
        this.tunnelDirection = PathDirection.Tunnel.NONE;
    }

    Node(TileVector[] tileVectors, PathDirection.Tunnel tunnelDirection){
        this.tileVectors = tileVectors;
        this.hubDirection = PathDirection.Hub.NONE;
        this.stageDirection = PathDirection.Stage.NONE;
        this.tunnelDirection = tunnelDirection;
    }

    public PathDirection.Hub getHubDirection(){
        return hubDirection;
    }

    PathDirection.Stage getStageDirection(){
        return stageDirection;
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

    public TileVector getEndTilePos(){
        return tileVectors[tileVectors.length-1];
    }

    public TileVector[] getTileVectorsAsArray(){
        return tileVectors;
    }

    ArrayList<TileVector> getTileVectorsAsList(){
        return new ArrayList<TileVector>(Arrays.asList(tileVectors));
    }
}
