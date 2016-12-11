package com.heynaveed.layloscave.utils;

import com.heynaveed.layloscave.utils.maps.TileVector;
import java.util.ArrayList;

public class AStar {

    public static void main(String[] args){
        TileVector startPos = new TileVector(91, 23);
        TileVector targetPos = new TileVector(94, 20);
        TileVector[] tileVectors = AStar.calculateMapVectorPath(startPos, targetPos);

        System.out.println("========== Start: (" + startPos.x() + ", " + startPos.y() + ") - Target: (" + targetPos.x() + ", " + targetPos.y() + ") ==========");

        System.out.println("\nPositions\n========================================================");

        for(int i = 0; i < tileVectors.length; i++)
            System.out.println("(" + tileVectors[i].x() + ", " + tileVectors[i].y() + ")");

        System.out.println("\n========== Start: (" + startPos.x() + ", " + startPos.y() + ") - Target: (" + targetPos.x() + ", " + targetPos.y() + ") ==========");
    }

    public static TileVector[] calculateMapVectorPath(TileVector startPos, TileVector targetPos){

        TileVector[] vectors;
        ArrayList<TileVector> list = new ArrayList<TileVector>();
        int xDir, yDir, x = startPos.x(), y = startPos.y();

        if(startPos.x() < targetPos.x()) xDir = 1; else xDir = -1;
        if(startPos.y() < targetPos.y()) yDir = 1; else yDir = -1;


        do{
            list.add(new TileVector(x, y));
            list.add(new TileVector(x, y));

            if(x != targetPos.x()) x+= xDir;
            if(y != targetPos.y()) y+= yDir;

        } while(x != targetPos.x() || y != targetPos.y());

        list.add(targetPos);

        vectors = new TileVector[list.size()];

        for(int i = 0; i < vectors.length; i++)
            vectors[i] = new TileVector(list.get(i).x(), list.get(i).y());

        return vectors;
    }
}
