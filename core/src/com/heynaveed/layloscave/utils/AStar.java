package com.heynaveed.layloscave.utils;

import com.badlogic.gdx.math.Vector2;
import com.heynaveed.layloscave.GameApp;
import com.heynaveed.layloscave.utils.maps.TileVector;


public class AStar {

    private final TileVector tvSourcePos;
    private final TileVector tvTargetPos;
    private final Vector2 wcSourcePos;
    private final Vector2 wcTargetPos;

    public AStar(TileVector tvSourcePos, TileVector tvTargetPos){
        this.tvSourcePos = tvSourcePos;
        this.tvTargetPos = tvTargetPos;
        this.wcSourcePos = GameApp.tileVectorToWorldPosition(tvSourcePos);
        this.wcTargetPos = GameApp.tileVectorToWorldPosition(tvTargetPos);
    }

    public TileVector getTvSourcePos() {
        return tvSourcePos;
    }

    public TileVector getTvTargetPos() {
        return tvTargetPos;
    }

    public Vector2 getWcSourcePos() {
        return wcSourcePos;
    }

    public Vector2 getWcTargetPos() {
        return wcTargetPos;
    }
}
