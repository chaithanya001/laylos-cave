package com.heynaveed.layloscave.universe.platforms;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.physics.box2d.World;
import com.heynaveed.layloscave.GameApp;


public class IcePlatform extends com.heynaveed.layloscave.universe.Platform {

    public IcePlatform(World world, MapObject object) {
        super(world, object);
        floorFixtureDef.friction = 0.004f;
        createPlatform(GameApp.ICE_PLATFORM_BIT);
    }

    @Override
    public void update(float delta) {

    }
}
