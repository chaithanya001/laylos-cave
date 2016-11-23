package com.heynaveed.layloscave.universe.platforms;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.physics.box2d.World;
import com.heynaveed.layloscave.GameApp;
import com.heynaveed.layloscave.universe.Platform;


public class MuddyPlatform extends Platform {

    public MuddyPlatform(World world, MapObject object) {
        super(world, object);
        floorFixtureDef.friction = 3.0f;
        createPlatform(GameApp.MUDDY_PLATFORM_BIT);
    }

    @Override
    public void update(float delta) {
    }
}
