package com.heynaveed.layloscave.utils.maps;


import java.util.ArrayList;
import java.util.Arrays;

final class Path {

    private final ArrayList<Node> nodes;
    private final TileVector[] pathVectors;

    Path(){
        nodes = new ArrayList<Node>();
        pathVectors = calculatePathway();
    }

    private TileVector[] calculatePathway(){
        return new TileVector[1];
    }

    ArrayList<Node> getNodes(){
        return nodes;
    }

    TileVector[] getPathVectors(){
        return pathVectors;
    }

    ArrayList<TileVector> getPathVectorsAsArrayList(){
        return new ArrayList<TileVector>(Arrays.asList(pathVectors));
    }
}
