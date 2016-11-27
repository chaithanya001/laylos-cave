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

/**
 * Created by naveed.shihab on 26/11/2016.
 */

public class Portal extends Sprite {

    private static int idCounter = 1;
    private final int id;
    private final AnimationPackager animationPackager;
    private final PlayScreen screen;
    private boolean isFacingRight;
    private boolean isFlipped = false;
    private Body body;
    private float animationStateTimer;

    public Portal(PlayScreen screen, boolean isFacingRight){
        animationPackager = new AnimationPackager(SpriteKeys.PORTAL);
        this.screen = screen;
        this.isFacingRight = isFacingRight;
        animationStateTimer = 0;
        id = idCounter++;
    }

    public Portal build(Vector2 position){
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

        if(isFacingRight && !isFlipped) {
            region.flip(true, false);
            isFlipped = true;
        }

        if(animationPackager.getAnimations()[0].isAnimationFinished(animationStateTimer))
            animationStateTimer = 0;

        animationStateTimer += dt;

        return region;
    }

    public int getId(){
        return id;
    }

    public boolean isFacingRight(){
        return isFacingRight;
    }

    public void dispose(){
        screen.dispose();;
    }
}
