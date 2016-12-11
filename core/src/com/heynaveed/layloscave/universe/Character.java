package com.heynaveed.layloscave.universe;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.heynaveed.layloscave.GameApp;
import com.heynaveed.layloscave.screens.PlayScreen;
import com.heynaveed.layloscave.utils.AnimationPackager;
import com.heynaveed.layloscave.utils.maps.MapGenerator;
import com.heynaveed.layloscave.utils.maps.TileVector;

public abstract class Character extends Sprite {

    protected final Screen screen;
    protected final OrthographicCamera gameCam;
    protected final World world;

    protected TileVector tileVectorPos;
    protected TileVector chosenPosition;
    protected AnimationPackager animationPackager;
    protected Body body;
    protected int[][] frameSequences;
    protected float[][] frameSpeeds;
    protected Animation[] animations;
    protected float animationStateTimer;
    protected boolean isFacingRight;

    public Character(PlayScreen screen) {
        this.screen = screen;
        gameCam = screen.getGameCam();
        world = screen.getWorld();
        animationStateTimer = 0;
        isFacingRight = true;
        tileVectorPos = new TileVector(0, 0);
    }

    protected abstract void update(float dt);

    protected abstract void initialiseWorldValues();

    protected abstract void initialiseTimers();

    protected abstract void initialiseBody();

    protected abstract void initialiseFixtures();

    protected abstract void initialiseStates();

    protected abstract void updateCharacterState();

    protected abstract TextureRegion updateAnimationFrame(float dt);

    protected abstract void dispose();

    public float horizontalDirectionMultiplyer(float velocity) {
        return !isFacingRight ? -1 * velocity : 1 * velocity;
    }

    public float verticalDirectionMultiplyer(float velocity) {
        return body.getLinearVelocity().y < 0 ? -1 * velocity : 1 * velocity;
    }

    public Body getBody() {
        return body;
    }

    public boolean isFacingRight() {
        return isFacingRight;
    }

    protected TileVector calculateTileVectorPos() {
        Vector2 bodyPosition = body.getPosition();
        int x = GameApp.fromPPM(bodyPosition.x / 64);
        int y = MapGenerator.workingHeight - GameApp.fromPPM(bodyPosition.y / 64);
        return new TileVector(y, x);
    }

    public TileVector getTileVectorPos() {
        return tileVectorPos;
    }

}
