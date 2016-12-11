package com.heynaveed.layloscave.universe.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.heynaveed.layloscave.GameApp;
import com.heynaveed.layloscave.keys.SpriteKeys;
import com.heynaveed.layloscave.keys.AnimationKey;
import com.heynaveed.layloscave.screens.PlayScreen;
import com.heynaveed.layloscave.states.CharacterState;
import com.heynaveed.layloscave.utils.AStar;
import com.heynaveed.layloscave.utils.AnimationPackager;
import com.heynaveed.layloscave.universe.Character;
import com.heynaveed.layloscave.utils.maps.TileVector;

import java.util.Random;

public final class Jini extends Character {

    private static final int MAX_JINI_KIRK_DIST = 15;
    private static final int DESTINATION_CONFIRM_LIMIT = 5;
    private static final int DESTINATION_CHECK_LIMIT = 21;
    private static final Random RANDOM = new Random();
    private static final float MAX_KIRK_DISPLACEMENT = 3;
    private static final float MAX_WANDER_TIMER = 3;
    private final ParticleEffect jiniAromaEffect = new ParticleEffect();
    private final PlayScreen screen;

    private boolean isTeleporting;
    private boolean previousFacing;
    private float kirkDisplacement;
    private float rotation;
    private float restartWanderTimer = -0.1f;
    private float wanderTimer = MAX_WANDER_TIMER;
    private float maxXCamLimit;
    private float maxYCamLimit;

    private CharacterState.Jini currentCharacterState;
    private CharacterState.Jini previousCharacterState;

    private int dodgeCounter;
    private TileVector[] dodgeVectors;
    private int[][] tileIDSet;
    private boolean isDoubleJumpImpulse = false;
    private boolean isDodging;

    public Jini(PlayScreen screen){
        super(screen);
        this.screen = screen;
        animationPackager = new AnimationPackager(SpriteKeys.JINI);
        tileIDSet = screen.getCurrentTileIDSet();
        setTexture(animationPackager.getTexture());
        setOrigin(GameApp.toPPM(GameApp.NEW_TILE_LENGTH)/2, GameApp.toPPM(GameApp.NEW_TILE_LENGTH)/2);
        frameSequences = animationPackager.getFrameSequences();
        frameSpeeds = animationPackager.getFrameSpeeds();
        animations = animationPackager.getAnimations();
        jiniAromaEffect.load(Gdx.files.internal("particle-effects/JiniAroma"), Gdx.files.internal("particle-effects"));
        jiniAromaEffect.start();

        initialiseWorldValues();
        initialiseTimers();
        initialiseBody();
        initialiseFixtures();
        initialiseStates();
    }

    @Override
    public void update(float dt){
        followKirk();
        handleAromaEffect();
        handleTeleporting();
        handleDoubleJump();
        setPosition(body.getPosition().x - getWidth()/2, body.getPosition().y - getHeight()/2);
        currentPosition = GameApp.worldPositionToTileVector(body.getPosition());
        handleMovement(dt);
        setRegion(updateAnimationFrame(dt));
    }

    private void handleAromaEffect(){
        if(jiniAromaEffect.isComplete())
            jiniAromaEffect.reset();
    }

    private void handleMovement(float dt){
        Vector2 kirkPosition = screen.getKirk().getBody().getPosition();
        Vector2 jiniPosition = body.getPosition();

        if(isDodging){
            isFacingRight = dodgeVectors[0].y() < dodgeVectors[dodgeVectors.length-1].y();
            body.setTransform(GameApp.tileVectorToWorldPosition(dodgeVectors[dodgeCounter]), 0);

            if(dodgeCounter == dodgeVectors.length-1)
                isDodging = false;
            else
                dodgeCounter++;
        }
        else{
            if(kirkPosition.x > jiniPosition.x) isFacingRight = true;
            else isFacingRight = false;

            if(wanderTimer > 0)
                wanderTimer -= dt;
            if(restartWanderTimer > 0)
                restartWanderTimer -= dt;

            if(restartWanderTimer <= 0){
                int xDir = RANDOM.nextInt(2) == 0 ?1 :-1;
                int yDir = RANDOM.nextInt(2) == 0 ?1 :-1;
                body.applyLinearImpulse(new Vector2(xDir*0.3f, yDir*0.25f), body.getWorldCenter(), true);
                wanderTimer = MAX_WANDER_TIMER;
            }

            if(wanderTimer <= 0) {
                body.setLinearVelocity(new Vector2(0, 0));
                restartWanderTimer = MAX_WANDER_TIMER;
            }
        }

        if(Math.abs(GameApp.worldPositionToTileVector(kirkPosition).y() - GameApp.worldPositionToTileVector(jiniPosition).y()) > MAX_JINI_KIRK_DIST
                || Math.abs(GameApp.worldPositionToTileVector(kirkPosition).x() - GameApp.worldPositionToTileVector(jiniPosition).x()) > 25) {
            body.setLinearVelocity(new Vector2(0, 0));

            if(isFacingRight)
                body.applyLinearImpulse(new Vector2(2.4f, 0), body.getWorldCenter(), true);
            else
                body.applyLinearImpulse(new Vector2(2.4f, 0), body.getWorldCenter(), true);
        }
    }

    private void handleTeleporting(){
        if(isTeleporting){
            isDoubleJumpImpulse = false;
            if(animations[AnimationKey.Jini.TELEPORTING.index].isAnimationFinished(animationStateTimer)) {
                isTeleporting = false;
                animationStateTimer = 0;
            }
        }
    }

    private void handleDoubleJump(){
        if(isDoubleJumpImpulse){
            if(animations[AnimationKey.Jini.DOUBLE_JUMP.index].isAnimationFinished(animationStateTimer))
                isDoubleJumpImpulse = false;
        }
    }

    private void followKirk() {

        float halfTimer = animationPackager.getFrameSpeeds()[AnimationKey.Jini.TELEPORTING.index][0]
                * animationPackager.getFrameSequences()[AnimationKey.Jini.TELEPORTING.index].length / 2;

        if (isTeleporting && (animationStateTimer > halfTimer)) {
            isFacingRight = screen.getKirk().isFacingRight();
            if (isFacingRight)
                kirkDisplacement = -MAX_KIRK_DISPLACEMENT;
            else
                kirkDisplacement = MAX_KIRK_DISPLACEMENT;

            if (previousFacing != isFacingRight)
                animationStateTimer = halfTimer;
        }

        previousFacing = isFacingRight;
//        body.setTransform(screen.getKirk().getBody().getPosition().x + kirkDisplacement, screen.getKirk().getBody().getPosition().y + 1, 0);
        jiniAromaEffect.setPosition(body.getPosition().x, body.getPosition().y);
    }

    @Override
    protected void initialiseWorldValues(){
        kirkDisplacement = -MAX_KIRK_DISPLACEMENT;
        rotation = 0;
        isDodging = false;
    }

    @Override
    protected void initialiseTimers() {
    }

    @Override
    protected void initialiseBody() {
        BodyDef bDef = new BodyDef();
        bDef.position.set(screen.getKirk().getBody().getPosition().x - 3, screen.getKirk().getBody().getPosition().y+2);
        bDef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bDef);
        body.setGravityScale(0);
    }

    @Override
    protected void initialiseFixtures() {
        FixtureDef fDef = new FixtureDef();
        fDef.filter.categoryBits = GameApp.JINI_BIT;
        fDef.filter.maskBits = GameApp.GROUND_PLATFORM_BIT | GameApp.OBJECT_BIT | GameApp.KIRK_BIT;

        CircleShape jiniBody = new CircleShape();
        jiniBody.setRadius(GameApp.toPPM(GameApp.TILE_LENGTH*1.5f));
        jiniBody.setPosition(new Vector2(0, 0));
        fDef.shape = jiniBody;
        fDef.friction = 0;
        fDef.restitution = 0;
        body.createFixture(fDef).setUserData("jiniBody");

        CircleShape jiniDetector = new CircleShape();
        jiniDetector.setRadius(GameApp.toPPM(GameApp.TILE_LENGTH*3));
        jiniDetector.setPosition(new Vector2(0, 0));
        fDef.shape = jiniDetector;
        fDef.friction = 0;
        fDef.restitution = 0;
        fDef.isSensor = true;
        body.createFixture(fDef).setUserData("jiniDetector");
    }

    @Override
    protected void initialiseStates() {
        isTeleporting = false;
        previousFacing = true;
    }

    @Override
    protected void updateCharacterState() {

        if(isTeleporting)
            currentCharacterState = CharacterState.Jini.TELEPORTING;
        else if(isDodging)
            currentCharacterState = CharacterState.Jini.FLYING;
        else if(isDoubleJumpImpulse)
            currentCharacterState = CharacterState.Jini.DOUBLE_JUMP;
        else
            currentCharacterState = CharacterState.Jini.FLOATING;
    }

    @Override
    protected TextureRegion updateAnimationFrame(float dt) {

        updateCharacterState();

        TextureRegion region;

        switch(currentCharacterState){
            case FLYING:
                region = animations[AnimationKey.Jini.FLYING.index].getKeyFrame(animationStateTimer, true);
                break;
            case TELEPORTING:
                region = animations[AnimationKey.Jini.TELEPORTING.index].getKeyFrame(animationStateTimer, false);
                break;
            case DOUBLE_JUMP:
                region = animations[AnimationKey.Jini.DOUBLE_JUMP.index].getKeyFrame(animationStateTimer, false);
                break;
            case FLOATING:
            default:
                region = animations[AnimationKey.Jini.FLOATING.index].getKeyFrame(animationStateTimer, true);
                break;
        }

        if (!isFacingRight && !region.isFlipX()) {
            region.flip(true, false);
            isFacingRight = false;
        } else if (isFacingRight && region.isFlipX()) {
            region.flip(true, false);
            isFacingRight = true;
        }

        if(!screen.getKirk().isPortalLocked()) {
            animationStateTimer = currentCharacterState == previousCharacterState ? animationStateTimer + dt : 0;
            previousCharacterState = currentCharacterState;
        }

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
        this.isDoubleJumpImpulse = isLevitateImpulse;
    }

    public ParticleEffect getJiniAromaEffect(){
        return jiniAromaEffect;
    }

    public void determineCheckSpace(){
        TileVector[][] positionsToCheck = new TileVector[DESTINATION_CHECK_LIMIT][DESTINATION_CHECK_LIMIT];
        int displacement = positionsToCheck.length/2;

        for(int x = -displacement; x <= displacement; x++){
            for(int y = -displacement; y <= displacement; y++)
                positionsToCheck[x + displacement][y + displacement] = new TileVector(currentPosition.x() + x, currentPosition.y() + y);
        }

        targetPosition = chooseFreeSpace(positionsToCheck);
        dodgeVectors = AStar.calculateMapVectorPath(currentPosition, targetPosition);
        dodgeCounter = 0;
        isDodging = true;
        wanderTimer = 1;
        restartWanderTimer = -1;
    }

    private TileVector chooseFreeSpace(TileVector[][] positionsToCheck){
        int randomX = RANDOM.nextInt(positionsToCheck.length);
        int randomY = RANDOM.nextInt(positionsToCheck.length);
        TileVector vectorToCheck = positionsToCheck[randomX][randomY];

        if (tileIDSet[vectorToCheck.x()][vectorToCheck.y()] == 0
                && !currentPosition.equals(screen.getKirk().getCurrentPosition())) {
            for(int i = -DESTINATION_CONFIRM_LIMIT; i <= DESTINATION_CONFIRM_LIMIT; i++){
                for(int j = -DESTINATION_CONFIRM_LIMIT; j <= DESTINATION_CONFIRM_LIMIT; j++){
                    if(tileIDSet[vectorToCheck.x() + i][vectorToCheck.y() + j] != 0
                            || (vectorToCheck.x() == screen.getKirk().getCurrentPosition().x() && (vectorToCheck.y()) == screen.getKirk().getCurrentPosition().y()))
                        vectorToCheck = chooseFreeSpace(positionsToCheck);
                }
            }
        }
        else vectorToCheck = chooseFreeSpace(positionsToCheck);

        return new TileVector(vectorToCheck.x(), vectorToCheck.y());
    }

    public boolean isDodging() {
        return isDodging;
    }
}
