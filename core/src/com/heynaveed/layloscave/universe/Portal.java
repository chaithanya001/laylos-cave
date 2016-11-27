package com.heynaveed.layloscave.universe;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.heynaveed.layloscave.GameApp;
import com.heynaveed.layloscave.keys.SpriteKeys;
import com.heynaveed.layloscave.screens.PlayScreen;
import com.heynaveed.layloscave.utils.AnimationPackager;

import java.util.Random;

/**
 * Created by naveed.shihab on 26/11/2016.
 */

public class Portal extends Sprite {

    public static final int MAX_PORTAL_NUMBER = 10;
    public static final int RANDOM_PORTAL_BIT_ONE = 64;
    public static final int RANDOM_PORTAL_BIT_TWO = 128;
    private Vector2 position;
    private static final Random random = new Random();
    private static int idCounter = 0;
    private final int id;
    private final int partnerid;
    private final AnimationPackager animationPackager;
    private final PlayScreen screen;
    private boolean isFacingRight;
    private Body body;
    private float animationStateTimer;

    public Portal(PlayScreen screen, boolean isFacingRight){
        animationPackager = new AnimationPackager(SpriteKeys.PORTAL);
        this.screen = screen;
        this.isFacingRight = isFacingRight;
        animationStateTimer = random.nextInt(75) / 1000;

        if(idCounter < MAX_PORTAL_NUMBER) {
            id = idCounter;
            partnerid = MAX_PORTAL_NUMBER - 1 - id;
        }
        else if(idCounter == MAX_PORTAL_NUMBER){
            id = RANDOM_PORTAL_BIT_ONE;
            partnerid = RANDOM_PORTAL_BIT_ONE;
        }
        else{
            id = RANDOM_PORTAL_BIT_TWO;
            partnerid = RANDOM_PORTAL_BIT_TWO;
        }
        idCounter++;
    }

    public Portal build(Vector2 position){
        this.position = position;
        Body body;
        BodyDef bDef = new BodyDef();
        bDef.position.set(position);
        bDef.type = BodyDef.BodyType.StaticBody;
        body = screen.getWorld().createBody(bDef);
        body.setUserData(id);

        FixtureDef fDef = new FixtureDef();
        fDef.filter.categoryBits = GameApp.PORTAL_BIT;
        fDef.filter.maskBits = GameApp.KIRK_BIT;

        CircleShape mainBody = new CircleShape();
        mainBody.setRadius(GameApp.toPPM(64));
        if(!isFacingRight) {
            setPosition(position.x - GameApp.toPPM(192), position.y - GameApp.toPPM(256));
            mainBody.setPosition(new Vector2(0, -GameApp.toPPM(48)));
        }
        else {
            setPosition(position.x - GameApp.toPPM(128), position.y - GameApp.toPPM(256));
            mainBody.setPosition(new Vector2(GameApp.toPPM(64), -GameApp.toPPM(48)));
        }

        fDef.shape = mainBody;
        fDef.friction = 0;
        fDef.restitution = 0;
        fDef.isSensor = true;
        body.createFixture(fDef).setUserData(id);

        setTexture(animationPackager.getTexture());

        return this;
    }

    public void update(float dt) {
        setRegion(updateRegion(dt));
    }

    public TextureRegion updateRegion(float dt){
        TextureRegion region = animationPackager.getAnimations()[0].getKeyFrame(animationStateTimer);

        if(isFacingRight || !region.isFlipX())
            region.flip(true, false);

        if(animationPackager.getAnimations()[0].isAnimationFinished(animationStateTimer))
            animationStateTimer = 0;

        animationStateTimer += dt;

        return region;
    }

    public int getId(){
        return id;
    }

    public int getPartnerId(){
        return partnerid;
    }

    public boolean isFacingRight(){
        return isFacingRight;
    }

    public Vector2 getPosition(){
        return position;
    }

    public void dispose(){
        screen.dispose();;
    }
}
