package com.heynaveed.layloscave.universe.platforms;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.physics.box2d.World;
import com.heynaveed.layloscave.GameApp;
import com.heynaveed.layloscave.screens.PlayScreen;


public class BouncyPlatform extends com.heynaveed.layloscave.universe.Platform {

    private final int blocksWidth;
    private final int blocksHeight;

    private PlayScreen screen;

    public BouncyPlatform(World world, MapObject object) {
        super(world, object);
        blocksWidth = (int)width / GameApp.TILE_LENGTH;
        blocksHeight = (int)height / GameApp.TILE_LENGTH;
        floorFixtureDef.friction = 125;
        createPlatform(GameApp.BOUNCY_PLATFORM_BIT);
    }

    @Override
    public void update(float delta) {
    }

    public BouncyPlatform applyLighting(PlayScreen screen){
        this.screen = screen;
        return this;
    }
}
