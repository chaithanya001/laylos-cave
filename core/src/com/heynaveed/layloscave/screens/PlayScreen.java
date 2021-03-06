package com.heynaveed.layloscave.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.heynaveed.layloscave.states.MapState;
import com.heynaveed.layloscave.universe.Map;
import com.heynaveed.layloscave.universe.Portal;
import com.heynaveed.layloscave.utils.InputController;
import com.heynaveed.layloscave.utils.maps.MapGenerator;
import com.heynaveed.layloscave.universe.characters.Jini;
import com.heynaveed.layloscave.universe.characters.Kirk;
import com.heynaveed.layloscave.universe.platforms.CrumblingPlatform;
import com.heynaveed.layloscave.universe.platforms.RotationPlatform;
import com.heynaveed.layloscave.universe.platforms.CrumblingPlatform.CrumblingBlock;
import com.heynaveed.layloscave.universe.platforms.RotationPlatform.RotationBlock;
import com.heynaveed.layloscave.utils.CollisionDetector;
import com.heynaveed.layloscave.universe.Level;
import com.heynaveed.layloscave.GameApp;
import com.heynaveed.layloscave.utils.maps.TileVector;

import java.io.IOException;
import java.util.ArrayList;


public class PlayScreen implements Screen {

    private static final ArrayList<Portal> portals = new ArrayList<Portal>();
    private static final int MAP_UNIT_SCALE = 1;
    private static final int SPRITE_SIZE = 2;
    private static final float DEFAULT_WORLD_GRAVITY = -40.0f;
    private static final Array<Level> levels = new Array<Level>();
    private static final Box2DDebugRenderer debugRenderer = new Box2DDebugRenderer();
    private static OrthogonalTiledMapRenderer mapRenderer;

    private final InputMultiplexer inputMultiplexer;
    private final GameApp gameApp;
    private final OrthographicCamera gameCam;
    private final Viewport viewport;
    private final World world;
    private final CollisionDetector contactListener;
    private final Kirk kirk;
    private final Jini jini;
    private final InputController inputController;

    private TiledMap currentTileMap;
    private Map currentMap;
    private MapGenerator mapGenerator;
    private int currentLevel;
    private int[][] currentTileIDSet;

    public PlayScreen(GameApp gameApp) throws IOException {
        this.gameApp = gameApp;
        gameCam = new OrthographicCamera();
        inputMultiplexer = new InputMultiplexer();
        mapGenerator = new MapGenerator();
//        currentMap = mapGenerator.newHubMap();
        currentMap = mapGenerator.newStageMap();
//        currentMap = mapGenerator.newTunnelMap();
        currentTileIDSet = currentMap.getTileIDSet();
        viewport = new FitViewport(GameApp.toPPM(GameApp.VIEWPORT_WIDTH), GameApp.toPPM(GameApp.VIEWPORT_HEIGHT), gameCam);
        gameCam.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
        currentLevel = 1;
        world = new World(new Vector2(0, DEFAULT_WORLD_GRAVITY), true);
        loadLevels();
        kirk = new Kirk(this);
        jini = new Jini(this);
        kirk.setSize(SPRITE_SIZE, SPRITE_SIZE);
        jini.setSize(SPRITE_SIZE, SPRITE_SIZE);
        inputController = new InputController(this).setKirk(kirk).setJini(jini);
        contactListener = new CollisionDetector(this);
        contactListener.setMap(currentTileMap);
        currentTileMap = levels.get(levelNumberOffset()).getMap();
        mapRenderer = new OrthogonalTiledMapRenderer(currentTileMap, GameApp.toPPM(MAP_UNIT_SCALE));
        world.setContactListener(contactListener);

        if(currentMap.getMapState().equals(MapState.HUB))
            initialisePortals();

        if(GameApp.CONFIGURATION == "Android")
            inputMultiplexer.addProcessor(inputController.getStage());
        inputMultiplexer.addProcessor(inputController);
    }

    public void update(float dt) {
        if(!kirk.isPortalLocked())
            world.step(1 / GameApp.FPS, 6, 2);
        kirk.checkForPortalDisplacement();
        gameCam.update();
        mapRenderer.setView(gameCam);
        inputController.update(dt);
        destroyMarkedBodies();
        updatePlatforms(dt);
        updatePortals(dt);
        checkForNextLevel();
    }

    public void render(float dt) {
        update(dt);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mapRenderer.render();

        Gdx.gl.glClearColor(0, 0, 0, 1);

        gameApp.batch.setProjectionMatrix(gameCam.combined);
        gameApp.batch.begin();

        jini.getJiniAromaEffect().draw(gameApp.batch, dt);

        if(!kirk.isPortalLocked())
            jini.draw(gameApp.batch);

        renderPortals();

        if(!kirk.isPortalLocked())
            kirk.draw(gameApp.batch);

        kirk.getCellularDisintegrationEffect().draw(gameApp.batch, dt);
        renderPlatforms();

        gameApp.batch.end();

        inputController.getStage().draw();

        if (jini.getJiniAromaEffect().isComplete())
            jini.getJiniAromaEffect().reset();
//        debugRenderer.render(world, gameCam.combined);
    }

    private void updatePortals(float dt) {
        for (int i = 0; i < portals.size(); i++)
            portals.get(i).update(dt);
    }

    private void renderPortals() {
        for (int i = 0; i < portals.size(); i++)
            portals.get(i).draw(gameApp.batch);
    }

    private void initialisePortals() {
        ArrayList<TileVector> portalPositions = mapGenerator.getPortalPositions();
        ArrayList<Boolean> portalFacing = mapGenerator.getPortalFacing();

        for (int i = 0; i < portalPositions.size(); i++) {
            for (int j = 0; j < portalPositions.size(); j++) {
                if (i != j) {
                    if (portalPositions.get(i).x() == portalPositions.get(j).x()
                            && portalPositions.get(i).y() == portalPositions.get(j).y()) {
                        portalPositions.remove(i);
                        portalFacing.remove(i);
                    }
                }
            }
        }

        for (int i = 0; i < portalPositions.size(); i++) {
            Portal portal = new Portal(this, portalFacing.get(i)).build(GameApp.tileVectorToWorldPosition(portalPositions.get(i)));
            portal.setSize(SPRITE_SIZE * 2, SPRITE_SIZE * 2);
            portals.add(portal);
//            System.out.println("ID: " + portals.get(i).getId() + ", Partner ID: " + portals.get(i).getPartnerId());
        }
    }

    private void loadLevels() {
        for (int i = 1; i <= Level.NUMBER_OF_LEVELS; i++)
            levels.add(new Level(this));
    }

    private void checkForNextLevel() {
        if (levels.get(levelNumberOffset()).isCompleted() && levelNumberOffset() != Level.NUMBER_OF_LEVELS) {
            currentLevel++;
            currentTileMap = levels.get(levelNumberOffset()).getMap();
            mapRenderer = new OrthogonalTiledMapRenderer(currentTileMap, GameApp.toPPM(MAP_UNIT_SCALE));
        }
    }

    private void updatePlatforms(float dt) {
        for (RotationPlatform platforms : levels.get(levelNumberOffset()).getRotationPlatforms())
            platforms.update(dt);

        for (CrumblingPlatform platforms : levels.get(levelNumberOffset()).getCrumblingPlatforms())
            platforms.update(dt);
    }

    private void renderPlatforms() {
        for (RotationPlatform platforms : levels.get(levelNumberOffset()).getRotationPlatforms()) {
            for (ArrayList<RotationBlock> blockLists : platforms.getBlocks()) {
                for (RotationBlock blocks : blockLists)
                    blocks.draw(gameApp.batch);
            }
        }

        for (CrumblingPlatform platforms : levels.get(levelNumberOffset()).getCrumblingPlatforms()) {
            for (ArrayList<CrumblingBlock> blockLists : platforms.getBlocks()) {
                for (CrumblingBlock blocks : blockLists)
                    blocks.draw(gameApp.batch);
            }
        }
    }

    private void destroyMarkedBodies() {

        Body b;

        Array<Body> bodiesForRemoval = contactListener.getBodiesToRemove();
        for (int i = 0; i < bodiesForRemoval.size; i++) {

            b = bodiesForRemoval.get(i);
            if (b != null) {
                if (((CrumblingPlatform) b.getUserData()).getIsDead()) {
                    levels.get(levelNumberOffset()).getCrumblingPlatforms().removeValue((CrumblingPlatform) b.getUserData(), true);
                    bodiesForRemoval.removeIndex(i);
                    world.destroyBody(b);
                }
            }
        }
    }

    private int levelNumberOffset() {
        return currentLevel - 1;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);

        if(GameApp.CONFIGURATION == "Android")
            inputController.getStage().getViewport().update(width, height);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        gameApp.dispose();
        currentTileMap.dispose();
        mapRenderer.dispose();
        for (Level level : levels)
            level.dispose();
        world.dispose();
        kirk.dispose();
        jini.dispose();
        debugRenderer.dispose();
        inputController.dispose();
        for(Portal portal : portals)
            portal.dispose();
    }

    public World getWorld() {
        return world;
    }

    public OrthographicCamera getGameCam() {
        return gameCam;
    }

    public InputController getInputController() {
        return inputController;
    }

    public Kirk getKirk() {
        return kirk;
    }

    public Vector2 getRandomStartingPosition() {
        return mapGenerator.getRandomStartingPosition();
    }

    public ArrayList<Portal> getPortals(){
        return portals;
    }

    public Jini getJini() {
        return jini;
    }

    public MapState getCurrentMapState(){
        return currentMap.getMapState();
    }

    public int[][] getCurrentTileIDSet(){
        return currentTileIDSet;
    }
}