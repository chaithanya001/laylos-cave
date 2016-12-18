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

    private static final int MAX_JINI_KIRK_DIST_X = 18;
    private static final int MAX_JINI_KIRK_DIST_Y = 23;
    private static final int DESTINATION_CONFIRM_LIMIT = 4;
    private static final int DESTINATION_CHECK_LIMIT = 21;
    private static final Random RANDOM = new Random();
    private static final float MAX_WANDER_TIMER = 3;
    private final ParticleEffect jiniAromaEffect = new ParticleEffect();
    private final PlayScreen screen;

    private boolean isTeleporting;
    private float rotation;
    private float restartWanderTimer = -0.1f;
    private float wanderTimer = MAX_WANDER_TIMER;

    private CharacterState.Jini currentCharacterState;
    private CharacterState.Jini previousCharacterState;

    private int dodgeCounter;
    private TileVector[] dodgeVectors;
    private int[][] tileIDSet;
    private boolean isDoubleJumpImpulse = false;
    private boolean isDodging;
    private boolean hasTeleported;

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
        jiniAromaEffect.setPosition(body.getPosition().x, body.getPosition().y);
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

        if(Math.abs(body.getLinearVelocity().x) > 2.3f
                || Math.abs(body.getLinearVelocity().y) > 2.3f)
            body.setLinearVelocity(new Vector2(0, 0));

        if(isDodging){
            isFacingRight = dodgeVectors[0].y() < dodgeVectors[dodgeVectors.length-1].y();
            body.setTransform(GameApp.tileVectorToWorldPosition(dodgeVectors[dodgeCounter]), 0);

            if(dodgeCounter == dodgeVectors.length-1)
                isDodging = false;
            else
                dodgeCounter++;

            body.setActive(false);
        }
        else{
            body.setActive(true);
            isFacingRight = kirkPosition.x > jiniPosition.x;

            if(wanderTimer > 0)
                wanderTimer -= dt;
            if(restartWanderTimer > 0)
                restartWanderTimer -= dt;

            if(restartWanderTimer <= 0){
                int xDir = RANDOM.nextInt(2) == 0 ?1 :-1;
                int yDir = RANDOM.nextInt(2) == 0 ?1 :-1;
                body.applyLinearImpulse(new Vector2(xDir*0.075f, yDir*0.075f), body.getWorldCenter(), true);
                wanderTimer = MAX_WANDER_TIMER;
            }

            if(wanderTimer <= 0) {
                body.setLinearVelocity(new Vector2(0, 0));
                restartWanderTimer = MAX_WANDER_TIMER;
            }
        }

        if(Math.abs(GameApp.worldPositionToTileVector(kirkPosition).y() - GameApp.worldPositionToTileVector(jiniPosition).y()) > MAX_JINI_KIRK_DIST_Y
                || Math.abs(GameApp.worldPositionToTileVector(kirkPosition).x() - GameApp.worldPositionToTileVector(jiniPosition).x()) > MAX_JINI_KIRK_DIST_X) {
            body.setLinearVelocity(new Vector2(0, 0));
            teleportJini();
            wanderTimer = MAX_WANDER_TIMER;
            restartWanderTimer = -0.1f;
        }
    }

    private void handleTeleporting(){
        if(isTeleporting){
            float halfTimer = animationPackager.getFrameSpeeds()[AnimationKey.Jini.TELEPORTING.index][0]
                    * animationPackager.getFrameSequences()[AnimationKey.Jini.TELEPORTING.index].length / 2;
            isDoubleJumpImpulse = false;

            if(animations[AnimationKey.Jini.TELEPORTING.index].isAnimationFinished(animationStateTimer)) {
                isTeleporting = false;
                animationStateTimer = 0;
                hasTeleported = false;
            }

            if (animationStateTimer > halfTimer && !hasTeleported) {
                TileVector gameCamPos = screen.getKirk().getCurrentPosition();
                body.setTransform(GameApp.tileVectorToWorldPosition(chooseFreeSpace(getPotentialTargetPositions(gameCamPos))), 0);
                hasTeleported = true;
            }
        }
    }

    private void handleDoubleJump(){
        if(isDoubleJumpImpulse){
            if(animations[AnimationKey.Jini.DOUBLE_JUMP.index].isAnimationFinished(animationStateTimer))
                isDoubleJumpImpulse = false;
        }
    }

    @Override
    protected void initialiseWorldValues(){
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
        jiniDetector.setRadius(GameApp.toPPM(GameApp.TILE_LENGTH*4));
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
        hasTeleported = false;
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

        if(!isDodging) {
            TileVector[][] positionsToCheck = getPotentialTargetPositions(currentPosition);
            targetPosition = chooseFreeSpace(positionsToCheck);
            dodgeVectors = AStar.calculateMapVectorPath(currentPosition, targetPosition);
            dodgeCounter = 0;
            isDodging = true;
            wanderTimer = MAX_WANDER_TIMER;
            restartWanderTimer = -0.1f;
        }
        else
            teleportJini();
    }

    private void teleportJini(){
        if(!isTeleporting){
            isTeleporting = true;
            resetAnimationStateTimer();
        }
    }

    private TileVector[][] getPotentialTargetPositions(TileVector middlePoint){
        TileVector[][] potentialTargetPositions = new TileVector[DESTINATION_CHECK_LIMIT][DESTINATION_CHECK_LIMIT];
        int displacement = potentialTargetPositions.length / 2;

        for (int x = -displacement; x <= displacement; x++) {
            for (int y = -displacement; y <= displacement; y++)
                potentialTargetPositions[x + displacement][y + displacement] = new TileVector(middlePoint.x() + x, middlePoint.y() + y);
        }

        return potentialTargetPositions;
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
}
