package com.heynaveed.layloscave.universe;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.heynaveed.layloscave.keys.LayerKey;
import com.heynaveed.layloscave.universe.platforms.BouncyPlatform;
import com.heynaveed.layloscave.universe.platforms.CrumblingPlatform;
import com.heynaveed.layloscave.universe.platforms.MuddyPlatform;
import com.heynaveed.layloscave.universe.platforms.NormalPlatform;
import com.heynaveed.layloscave.universe.platforms.IcePlatform;
import com.heynaveed.layloscave.universe.platforms.RotationPlatform;
import com.heynaveed.layloscave.screens.PlayScreen;


public class Level {

    public static final int NUMBER_OF_LEVELS = 1;
    private static final String MAP_PATH = "maps/level";
    private static final String MAP_LEVEL = "1";
    private static final String MAP_FILE_EXTENSION = ".tmx";
    private static final TmxMapLoader mapLoader = new TmxMapLoader();

    private final PlayScreen screen;
    private final World world;
    private final TiledMap map;
    private final Array<NormalPlatform> groundPlatforms;
    private final Array<RotationPlatform> rotationPlatforms;
    private final Array<IcePlatform> icePlatforms;
    private final Array<MuddyPlatform> muddyPlatforms;
    private final Array<BouncyPlatform> bouncyPlatforms;
    private final Array<CrumblingPlatform> crumblingPlatforms;

    public Level(PlayScreen screen, int levelNumber){
        this.screen = screen;
        world = screen.getWorld();
        map = mapLoader.load(MAP_PATH + MAP_LEVEL + MAP_FILE_EXTENSION);
        groundPlatforms = new Array<NormalPlatform>();
        rotationPlatforms = new Array<RotationPlatform>();
        icePlatforms = new Array<IcePlatform>();
        muddyPlatforms = new Array<MuddyPlatform>();
        bouncyPlatforms = new Array<BouncyPlatform>();
        crumblingPlatforms = new Array<CrumblingPlatform>();

        createGroundPlatforms();
        createRotationPlatform();
        createIcePlatforms();
        createMuddyPlatforms();
        createBouncyPlatforms();
        createCrumblingPlatforms();
    }

    public boolean isCompleted(){
        return false;
    }

    private void createGroundPlatforms(){
        for(MapObject object : map.getLayers().get(LayerKey.GROUND.getKey()).getObjects().getByType(RectangleMapObject.class))
            groundPlatforms.add(new NormalPlatform(world, object));
    }

    private void createRotationPlatform(){
        for(MapObject object : map.getLayers().get(LayerKey.ROTATION.getKey()).getObjects().getByType(RectangleMapObject.class))
            rotationPlatforms.add(new RotationPlatform(world, object));
    }

    private void createIcePlatforms(){
        for(MapObject object : map.getLayers().get(LayerKey.ICE.getKey()).getObjects().getByType(RectangleMapObject.class))
            icePlatforms.add(new IcePlatform(world, object));
    }

    private void createMuddyPlatforms(){
        for(MapObject object : map.getLayers().get(LayerKey.MUDDY.getKey()).getObjects().getByType(RectangleMapObject.class))
            muddyPlatforms.add(new MuddyPlatform(world, object));
    }

    private void createBouncyPlatforms() {
        for (MapObject object : map.getLayers().get(LayerKey.BOUNCY.getKey()).getObjects().getByType(RectangleMapObject.class)) {
            bouncyPlatforms.add(new BouncyPlatform(world, object).applyLighting(screen));
        }
    }

    private void createCrumblingPlatforms() {
        for (MapObject object : map.getLayers().get(LayerKey.CRUMBLING.getKey()).getObjects().getByType(RectangleMapObject.class))
            crumblingPlatforms.add(new CrumblingPlatform(world, object));
    }

    public Array<RotationPlatform> getRotationPlatforms() {
        return rotationPlatforms;
    }

    public Array<CrumblingPlatform> getCrumblingPlatforms(){ return crumblingPlatforms; }

    public void dispose(){
        screen.dispose();
        world.dispose();
        map.dispose();
    }

    public TiledMap getMap(){
        return map;
    }
}
