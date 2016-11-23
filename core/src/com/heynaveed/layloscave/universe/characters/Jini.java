package com.heynaveed.layloscave.universe.characters;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.heynaveed.layloscave.GameApp;
import com.heynaveed.layloscave.keys.CharacterKey;
import com.heynaveed.layloscave.keys.SpriteKey;
import com.heynaveed.layloscave.screens.PlayScreen;
import com.heynaveed.layloscave.states.CharacterState;
import com.heynaveed.layloscave.utils.AnimationPackager;
import com.heynaveed.layloscave.universe.Character;

public final class Jini extends Character {

    private static final float MAX_KIRK_DISPLACEMENT = 3;
    private static final float MAX_ROTATION = 15;
    private final PlayScreen screen;

    private boolean hasSwapped;
    private boolean isImpulsing;
    private boolean isTeleporting;
    private boolean previousFacing;
    private float kirkDisplacement;
    private float rotation;

    private CharacterState.Jini currentCharacterState;
    private CharacterState.Jini previousCharacterState;

    private boolean isLevitateImpulse = false;

    public Jini(PlayScreen screen){
        super(screen);
        this.screen = screen;
        animationPackager = new AnimationPackager(CharacterKey.JINI);
        setTexture(animationPackager.getTexture());
        setOrigin(GameApp.toPPM(GameApp.NEW_TILE_LENGTH)/2, GameApp.toPPM(GameApp.NEW_TILE_LENGTH)/2);
        frameSequences = animationPackager.getFrameSequences();
        frameSpeeds = animationPackager.getFrameSpeeds();
        animations = animationPackager.getAnimations();

        initialiseWorldValues();
        initialiseTimers();
        initialiseBody();
        initialiseFixtures();
        initialiseStates();
    }

    @Override
    public void update(float dt){
        followKirk();
        handleTeleporting();
        handleLevitating();
        setPosition(body.getPosition().x - getWidth()/2, body.getPosition().y - getHeight()/2);
        setRegion(updateAnimationFrame(dt));
    }

    private void handleTeleporting(){
        if(isTeleporting){
            isLevitateImpulse = false;
            if(animations[SpriteKey.Jini.TELEPORTING.getKey()].isAnimationFinished(animationStateTimer)) {
                isTeleporting = false;
                animationStateTimer = 0;
            }
        }
    }

    private void handleLevitating(){
        if(isLevitateImpulse){
            if(animations[SpriteKey.Jini.DOUBLE_JUMP.getKey()].isAnimationFinished(animationStateTimer))
                isLevitateImpulse = false;
        }
    }

    private void followKirk(){

        float halfTimer = animationPackager.getFrameSpeeds()[SpriteKey.Jini.TELEPORTING.getKey()][0]
                * animationPackager.getFrameSequences()[SpriteKey.Jini.TELEPORTING.getKey()].length/2;

        if(isTeleporting && (animationStateTimer > halfTimer)) {
            isFacingRight = screen.getKirk().isFacingRight();
            if (isFacingRight)
                kirkDisplacement = -MAX_KIRK_DISPLACEMENT;
            else
                kirkDisplacement = MAX_KIRK_DISPLACEMENT;

            if(previousFacing != isFacingRight)
                animationStateTimer = halfTimer;
        }

        previousFacing = isFacingRight;
        body.setTransform(screen.getKirk().getBody().getPosition().x + kirkDisplacement, screen.getKirk().getBody().getPosition().y, 0);
        setRotation(calculateRotation());
    }

    private float calculateRotation(){

        Vector2 kirkVelocity = screen.getKirk().getBody().getLinearVelocity();

        if(((kirkVelocity.y < 0 && isFacingRight) || (kirkVelocity.y > 0 && !isFacingRight)) && kirkVelocity.x != 0)
            rotation = -MAX_ROTATION;
        else if(((kirkVelocity.y > 0 && isFacingRight) || (kirkVelocity.y < 0 && !isFacingRight)) && kirkVelocity.x != 0)
            rotation = MAX_ROTATION;
        else
            rotation = 0;

        return rotation;
    }

    @Override
    protected void initialiseWorldValues(){
        kirkDisplacement = -MAX_KIRK_DISPLACEMENT;
        rotation = 0;
    }

    @Override
    protected void initialiseTimers() {
    }

    @Override
    protected void initialiseBody() {
        BodyDef bDef = new BodyDef();
        bDef.position.set(screen.getKirk().getBody().getPosition());
        bDef.type = BodyDef.BodyType.KinematicBody;
        body = world.createBody(bDef);
    }

    @Override
    protected void initialiseFixtures() {
        FixtureDef fDef = new FixtureDef();
        fDef.filter.categoryBits = GameApp.JINI_BIT;
        fDef.filter.maskBits = GameApp.GROUND_PLATFORM_BIT | GameApp.OBJECT_BIT;

        CircleShape mainBody = new CircleShape();
        mainBody.setRadius(GameApp.toPPM(48));
        mainBody.setPosition(new Vector2(0, 0));
        fDef.shape = mainBody;
        fDef.friction = 0;
        fDef.restitution = 0;
        fDef.isSensor = true;
        body.createFixture(fDef);
    }

    @Override
    protected void initialiseStates() {
        isImpulsing = false;
        isTeleporting = false;
        hasSwapped = false;
        previousFacing = true;
    }

    @Override
    protected void updateCharacterState() {

        if(isTeleporting)
            currentCharacterState = CharacterState.Jini.TELEPORTING;
        else if(isLevitateImpulse)
            currentCharacterState = CharacterState.Jini.DOUBLE_JUMP;
        else if(screen.getKirk().getCurrentCharacterState() == CharacterState.Kirk.RUNNING)
            currentCharacterState = CharacterState.Jini.FLYING;
        else if(screen.getKirk().getCurrentCharacterState() == CharacterState.Kirk.STANDING)
            currentCharacterState = CharacterState.Jini.FLOATING;
        else
            currentCharacterState = CharacterState.Jini.FLYING;
    }

    @Override
    protected TextureRegion updateAnimationFrame(float dt) {

        updateCharacterState();

        TextureRegion region;

        switch(currentCharacterState){
            case FLYING:
                region = animations[SpriteKey.Jini.FLYING.getKey()].getKeyFrame(animationStateTimer, true);
                break;
            case TELEPORTING:
                region = animations[SpriteKey.Jini.TELEPORTING.getKey()].getKeyFrame(animationStateTimer, false);
                break;
            case DOUBLE_JUMP:
                region = animations[SpriteKey.Jini.DOUBLE_JUMP.getKey()].getKeyFrame(animationStateTimer, false);
                break;
            case FLOATING:
            default:
                region = animations[SpriteKey.Jini.FLOATING.getKey()].getKeyFrame(animationStateTimer, true);
                break;
        }

        if (!isFacingRight && !region.isFlipX()) {
            region.flip(true, false);
            isFacingRight = false;
        } else if (isFacingRight && region.isFlipX()) {
            region.flip(true, false);
            isFacingRight = true;
        }

        animationStateTimer = currentCharacterState == previousCharacterState ? animationStateTimer + dt : 0;
        previousCharacterState = currentCharacterState;

        return region;
    }

    public void setIsTeleporting(boolean isTeleporting){
        this.isTeleporting = isTeleporting;
    }

    @Override
    public void dispose() {
        world.dispose();
        screen.dispose();
    }

    public boolean isTeleporting() {
        return isTeleporting;
    }

    public float getRotation(){
        return rotation;
    }

    public void resetAnimationStateTimer(){
        animationStateTimer = 0;
    }

    public void setisLevitateImpulse(boolean isLevitateImpulse){
        if(!isTeleporting)
            resetAnimationStateTimer();
        this.isLevitateImpulse = isLevitateImpulse;
    }
}
