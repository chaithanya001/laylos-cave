package com.heynaveed.layloscave.screens;

import com.badlogic.gdx.Gdx;
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
import com.heynaveed.layloscave.states.CharacterState;
import com.heynaveed.layloscave.utils.InputController;
import com.heynaveed.layloscave.utils.maps.tools.MapGenerator;
import com.heynaveed.layloscave.universe.characters.Jini;
import com.heynaveed.layloscave.universe.characters.Kirk;
import com.heynaveed.layloscave.universe.platforms.CrumblingPlatform;
import com.heynaveed.layloscave.universe.platforms.RotationPlatform;
import com.heynaveed.layloscave.universe.platforms.CrumblingPlatform.CrumblingBlock;
import com.heynaveed.layloscave.universe.platforms.RotationPlatform.RotationBlock;
import com.heynaveed.layloscave.utils.CollisionDetector;
import com.heynaveed.layloscave.universe.Level;
import com.heynaveed.layloscave.GameApp;

import java.io.IOException;
import java.util.ArrayList;


public class PlayScreen implements Screen {

    private static final int MAP_UNIT_SCALE = 1;
    private static final int SPRITE_SIZE = 2;
    private static final float MAX_WALK_JUMP_TIMER = 0.3f;
    private static final float DEFAULT_WORLD_GRAVITY = -40.0f;
    private static final float KIRK_STRAIGHT_JUMP_ROTATION_SPEED = 25.0f;
    private static final float KIRK_BOUNCE_JUMP_ROTATION_SPEED = 50.0f;
    private static final Array<Level> levels = new Array<Level>();
    private static final Box2DDebugRenderer debugRenderer = new Box2DDebugRenderer();
    private static OrthogonalTiledMapRenderer mapRenderer;

    private final GameApp gameApp;
    private final OrthographicCamera gameCam;
    private final Viewport viewport;
    private final World world;
    private final CollisionDetector contactListener;
    private final Kirk kirk;
    private final Jini jini;
    private final InputController inputController;

    private final int[][] workingTileIDSet;

    private TiledMap currentMap;
    private MapGenerator mapGenerator;
    private int currentLevel;
    private float kirkRotationAngle;
    private float dt;

    public PlayScreen(GameApp gameApp) throws IOException {
        this.gameApp = gameApp;
        gameCam = new OrthographicCamera();
//        mapGenerator = new MapGenerator().buildTreeMap();
        mapGenerator = new MapGenerator().buildPathMap();
        workingTileIDSet = mapGenerator.getWorkingTileIDSet();
        viewport = new FitViewport(GameApp.toPPM(GameApp.VIEWPORT_WIDTH), GameApp.toPPM(GameApp.VIEWPORT_HEIGHT), gameCam);
        gameCam.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
        currentLevel = 1;
        kirkRotationAngle = 0;
        world = new World(new Vector2(0, DEFAULT_WORLD_GRAVITY), true);
        loadLevels();
        kirk = new Kirk(this);
        jini = new Jini(this);
        kirk.setSize(SPRITE_SIZE, SPRITE_SIZE);
        jini.setSize(SPRITE_SIZE, SPRITE_SIZE);
        inputController = new InputController(this).setKirk(kirk).setJini(jini);
        contactListener = new CollisionDetector(this, kirk);
        contactListener.setMap(currentMap);
        currentMap = levels.get(levelNumberOffset()).getMap();
        mapRenderer = new OrthogonalTiledMapRenderer(currentMap, GameApp.toPPM(MAP_UNIT_SCALE));
        world.setContactListener(contactListener);
    }

    public void update(float dt) {
        this.dt = dt;
        world.step(1 / GameApp.FPS, 6, 2);
        gameCam.update();
        mapRenderer.setView(gameCam);
        inputController.update(dt);
        destroyMarkedBodies();
        updatePlatforms(dt);
        checkForNextLevel();
    }

    public void render(float dt) {
        update(dt);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mapRenderer.render();

        gameApp.batch.setProjectionMatrix(gameCam.combined);
        gameApp.batch.begin();
        kirk.draw(gameApp.batch);
        jini.draw(gameApp.batch);
        kirk.rotate(calculateJiniImpulseRotation());
        renderPlatforms();
        gameApp.batch.end();
//        debugRenderer.render(world, gameCam.combined);
    }

    private float calculateJiniImpulseRotation(){
        if(kirk.getCurrentCharacterState() == CharacterState.Kirk.JINI_IMPULSE) {
            float rotationSpeed = inputController.getBounceJumpImpulse() ? KIRK_BOUNCE_JUMP_ROTATION_SPEED : KIRK_STRAIGHT_JUMP_ROTATION_SPEED;
            kirkRotationAngle += kirk.horizontalDirectionMultiplyer(-rotationSpeed);
            return kirk.horizontalDirectionMultiplyer(-rotationSpeed);
        }
        else {
            float tempRotation = kirkRotationAngle;
            kirkRotationAngle = 0;
            return -tempRotation;
        }
    }

    private void loadLevels() {
        for (int i = 1; i <= Level.NUMBER_OF_LEVELS; i++)
            levels.add(new Level(this, i));
    }

    private void checkForNextLevel() {
        if (levels.get(levelNumberOffset()).isCompleted() && levelNumberOffset() != Level.NUMBER_OF_LEVELS) {
            currentLevel++;
            currentMap = levels.get(levelNumberOffset()).getMap();
            mapRenderer = new OrthogonalTiledMapRenderer(currentMap, GameApp.toPPM(MAP_UNIT_SCALE));
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
        Gdx.input.setInputProcessor(inputController);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
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
        currentMap.dispose();
        mapRenderer.dispose();
        for (Level level : levels)
            level.dispose();
        world.dispose();
        kirk.dispose();
        jini.dispose();
        debugRenderer.dispose();
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

    public Kirk getKirk(){
        return kirk;
    }

    public GameApp getGameApp(){
        return gameApp;
    }

    public int[][] getWorkingTileIDSet(){
        return workingTileIDSet;
    }

    public float dt(){
        return this.dt;
    }

    public Vector2 getRandomStartingPosition(){
        return mapGenerator.getRandomStartingPosition();
    }
}