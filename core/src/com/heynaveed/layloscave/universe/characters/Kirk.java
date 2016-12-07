package com.heynaveed.layloscave.universe.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.heynaveed.layloscave.GameApp;
import com.heynaveed.layloscave.screens.PlayScreen;
import com.heynaveed.layloscave.states.CharacterState;
import com.heynaveed.layloscave.keys.SpriteKeys;
import com.heynaveed.layloscave.keys.ControlKey;
import com.heynaveed.layloscave.states.PlatformState;
import com.heynaveed.layloscave.universe.Portal;
import com.heynaveed.layloscave.utils.AnimationPackager;
import com.heynaveed.layloscave.utils.RoundTo;
import com.heynaveed.layloscave.keys.AnimationKey;
import com.heynaveed.layloscave.universe.Character;
import com.heynaveed.layloscave.utils.maps.CavernBlock;
import com.heynaveed.layloscave.utils.maps.TileVector;

import java.util.ArrayList;


public final class Kirk extends Character {

    private static final float KIRK_STRAIGHT_JUMP_ROTATION_SPEED = 25.0f;
    private static final float MAXIMUM_VELOCITY = 15.0f;
    private static final float CAMERA_WEST_LIMIT = 15.0f;
    private static final float CAMERA_EAST_LIMIT = 177.0f;
    private static final float CAMERA_NORTH_LIMIT = 119.0f;
    private static final float CAMERA_SOUTH_LIMIT = 8.5f;
    private static final float FIXTURE_WIDTH = GameApp.toPPM(48);
    private static final float FIXTURE_HEIGHT = GameApp.toPPM(92);
    private static final float HEAD_DISPLACEMENT = GameApp.toPPM(30);
    private static final float MAX_SLIDE_TIMER = 0.6f;
    private static final float MAX_BOUNCE_TIMER = 0.3f;
    private static final float CAMERA_PORTAL_SPEED = 75.0f;
    private final ParticleEffect cellularDisintegrationEffect = new ParticleEffect();
    private Vector2 cavernGameCamPosition;

    private float xDisplacement;
    private float yDisplacement;
    private float xPath = 0;
    private float yPath = 0;
    private Portal sourcePortal;
    private Portal targetPortal;
    private boolean isPortalLocked;
    private final PlayScreen screen;
    private float xVelocity;
    private float slideTimer;
    private float bounceTimer;
    private float maxJumpVelocity;
    private float dt;
    private boolean isBounceJump;
    private boolean isSliding;
    private boolean isControlDisabled;
    private boolean isStraightJumping;
    private CharacterState.Kirk currentCharacterState;
    private CharacterState.Kirk previousCharacterState;
    private PlatformState currentPlatformState;
    private boolean hasCellularEffectStarted = false;
    private ArrayList<Vector2> cavernBlockMidpoints = new ArrayList<Vector2>();

    public Kirk(PlayScreen screen) {
        super(screen);
        this.screen = screen;
        animationPackager = new AnimationPackager(SpriteKeys.KIRK);
        setTexture(animationPackager.getTexture());
        setOrigin(GameApp.toPPM(GameApp.TILE_LENGTH * 1.5f), GameApp.toPPM(GameApp.TILE_LENGTH * 0.75f));
        frameSequences = animationPackager.getFrameSequences();
        frameSpeeds = animationPackager.getFrameSpeeds();
        animations = animationPackager.getAnimations();
        currentPlatformState = PlatformState.NONE;
        cellularDisintegrationEffect.load(Gdx.files.internal("particle-effects/cellularDisintegration"), Gdx.files.internal("particle-effects"));
        cellularDisintegrationEffect.setDuration(0);
        initialiseWorldValues();
        initialiseTimers();
        initialiseBody();
        initialiseMapLogic();
        initialiseFixtures();
        initialiseStates();
    }

    @Override
    public void update(float dt) {
        this.dt = dt;

        switch(screen.getCurrentMapState()){
            case HUB:
                panHubCamera(dt);
                break;
            case CAVERN:
                panCavernCamera();
                break;
            case TUNNEL:
                panTunnelCamera(dt);
                break;
        }

        capVelocities();
        handleEffects();
        handlePlatformInteractions();
        handleJiniImpulseRotations();
        setPosition(body.getPosition().x - getWidth() / 2, body.getPosition().y - getHeight() / 2);
        setRegion(updateAnimationFrame(dt));
    }

    private void handleEffects() {
        if (isPortalLocked && !hasCellularEffectStarted) {
            cellularDisintegrationEffect.start();
            hasCellularEffectStarted = true;
        }
    }

    private void handleJiniImpulseRotations() {
        rotate(calculateJiniImpulseRotation());
    }

    private void capVelocities() {
        if (Math.abs(body.getLinearVelocity().x) > MAXIMUM_VELOCITY)
            body.setLinearVelocity(new Vector2(horizontalDirectionMultiplyer(MAXIMUM_VELOCITY), body.getLinearVelocity().y));
        if (body.getLinearVelocity().y < -MAXIMUM_VELOCITY && !isBounceJump)
            body.setLinearVelocity(new Vector2(body.getLinearVelocity().x, verticalDirectionMultiplyer(MAXIMUM_VELOCITY)));
    }

    private void panCavernCamera(){

        Vector2 closestMidPoint = cavernBlockMidpoints.get(0);

        for(int i = 1; i < cavernBlockMidpoints.size(); i++){
            if(distanceBetweenPoints(cavernBlockMidpoints.get(i), body.getPosition()) < distanceBetweenPoints(closestMidPoint, body.getPosition()))
                closestMidPoint = cavernBlockMidpoints.get(i);
        }

        gameCam.position.x = closestMidPoint.x;
        gameCam.position.y = closestMidPoint.y;
    }

    private void initialiseMapLogic() {
        switch(screen.getCurrentMapState()){
            case HUB:
                break;
            case CAVERN:
                for(int i = 0; i < CavernBlock.X_BLOCK_MIDPOINTS.length; i++)
                    cavernBlockMidpoints.add(screen.tileVectorToWorldPosition(
                            new TileVector(CavernBlock.X_BLOCK_MIDPOINTS[i], CavernBlock.Y_BLOCK_MIDPOINTS[i])));
                break;
            case TUNNEL:
                break;
        }
    }

    private float distanceBetweenPoints(Vector2 point1, Vector2 point2){
        return (float)Math.abs(Math.sqrt(Math.pow((point2.x - point1.x), 2) + Math.pow((point2.y - point1.y), 2)));
    }

    private void panTunnelCamera(float dt){

    }

    private void panHubCamera(float dt) {
        if (!isPortalLocked) {
            if (body.getPosition().x > CAMERA_WEST_LIMIT && body.getPosition().x < CAMERA_EAST_LIMIT)
                gameCam.position.x = RoundTo.RoundToNearest(body.getPosition().x, GameApp.toPPM(1));
            else if(body.getPosition().x < CAMERA_WEST_LIMIT)
                gameCam.position.x = CAMERA_WEST_LIMIT;
            else if(body.getPosition().x > CAMERA_EAST_LIMIT)
                gameCam.position.x = CAMERA_EAST_LIMIT;


            if (body.getPosition().y > CAMERA_SOUTH_LIMIT && body.getPosition().y < CAMERA_NORTH_LIMIT)
                gameCam.position.y = RoundTo.RoundToNearest(body.getPosition().y, GameApp.toPPM(1));
            else if(body.getPosition().y < CAMERA_SOUTH_LIMIT)
                gameCam.position.y = CAMERA_SOUTH_LIMIT;
            else if(body.getPosition().y > CAMERA_NORTH_LIMIT)
                gameCam.position.y = CAMERA_NORTH_LIMIT;
        }
        else {
            float xMovement = 0;
            float yMovement = 0;

            if (sourcePortal.getPosition().x > targetPortal.getPosition().x && xPath < xDisplacement) {
                xPath += CAMERA_PORTAL_SPEED*dt;
                xMovement = -CAMERA_PORTAL_SPEED*dt;
            }
            else if (sourcePortal.getPosition().x < targetPortal.getPosition().x && xPath < xDisplacement) {
                xPath += CAMERA_PORTAL_SPEED*dt;
                xMovement = CAMERA_PORTAL_SPEED*dt;
            }
            if (sourcePortal.getPosition().y > targetPortal.getPosition().y && yPath < yDisplacement) {
                yPath += CAMERA_PORTAL_SPEED*dt;
                yMovement = -CAMERA_PORTAL_SPEED*dt;
            }
            else if (sourcePortal.getPosition().y < targetPortal.getPosition().y && yPath < yDisplacement) {
                yPath += CAMERA_PORTAL_SPEED*dt;
                yMovement = CAMERA_PORTAL_SPEED*dt;
            }

            gameCam.position.x += xMovement;
            gameCam.position.y += yMovement;
            cellularDisintegrationEffect.setPosition(gameCam.position.x, gameCam.position.y);

            if (xPath >= xDisplacement && yPath >= yDisplacement) {
                isPortalLocked = false;
                hasCellularEffectStarted = false;
                cellularDisintegrationEffect.setDuration(0);
                xPath = 0;
                yPath = 0;
            }
        }
    }

    public void checkForPortalDisplacement() {
        if (isPortalLocked) {
            if (isFacingRight != targetPortal.isFacingRight()) {
                screen.getJini().setIsTeleporting(true);
                screen.getJini().resetAnimationStateTimer();
            }

            if (targetPortal.isFacingRight()) {
                body.setTransform(targetPortal.getPosition().x, targetPortal.getPosition().y+0.5f, 0);
                body.setLinearVelocity(new Vector2(15, 0));
            } else {
                body.setTransform(targetPortal.getPosition().x, targetPortal.getPosition().y+0.5f, 0);
                body.setLinearVelocity(new Vector2(-15, 0));
            }

            xDisplacement = Math.abs(sourcePortal.getPosition().x - targetPortal.getPosition().x);
            yDisplacement = Math.abs(sourcePortal.getPosition().y - targetPortal.getPosition().y);

            if (isFacingRight != targetPortal.isFacingRight())
                isFacingRight = targetPortal.isFacingRight();
        }
    }

    private void handlePlatformInteractions() {

        switch (currentPlatformState) {
            case NONE:
                interactWithNoPlatforms();
                break;
            case GROUND:
                interactWithNormalPlatform();
                break;
            case ICE:
                interactWithIcePlatform();
                break;
            case BOUNCY:
                interactWithBouncePlatform();
                break;
            case MUDDY:
                interactWithMuddyPlatform();
                break;
            case CRUMBLING:
                interactWithCrumblingPlatform();
                break;
            case ROTATING:
                interactWithRotatingPlatform();
                break;
        }
    }

    private void interactWithNoPlatforms() {
    }

    private void interactWithNormalPlatform() {
        xVelocity = 12.5f;
        maxJumpVelocity = 14.9f;
    }

    private void interactWithMuddyPlatform() {
        xVelocity = 8.0f;
        maxJumpVelocity = 12.0f;
    }

    private void interactWithRotatingPlatform() {
        xVelocity = 10.0f;
        maxJumpVelocity = 14.9f;
    }

    private void interactWithCrumblingPlatform() {
        xVelocity = 10.0f;
        maxJumpVelocity = 14.9f;
    }

    private void interactWithIcePlatform() {
        xVelocity = 7.5f;
        maxJumpVelocity = 12.0f;

        if (screen.getInputController().getControls()[ControlKey.LEFT.index] || screen.getInputController().getControls()[ControlKey.RIGHT.index])
            slideTimer += dt;
        else
            slideTimer = 0;

        if (body.getLinearVelocity().x == 0) {
            isControlDisabled = false;
            isSliding = false;
        } else if (isSliding)
            isControlDisabled = true;
    }

    private void interactWithBouncePlatform() {
        bounceTimer += dt;

        boolean left = screen.getInputController().getControls()[ControlKey.LEFT.index];
        boolean right = screen.getInputController().getControls()[ControlKey.RIGHT.index];

        if (left && !right)
            isFacingRight = false;
        else if (right && !left)
            isFacingRight = true;

        if (bounceTimer > MAX_BOUNCE_TIMER) {
            body.applyLinearImpulse(new Vector2(horizontalDirectionMultiplyer(6.0f), 10.5f), body.getWorldCenter(), true);
            isBounceJump = true;
        }
    }

    public boolean isAllowedToRun() {
        return Math.abs(body.getLinearVelocity().y) == 0 && currentPlatformState != PlatformState.BOUNCY;
    }

    public void applyRunImpulse() {
        body.setLinearVelocity(new Vector2(horizontalDirectionMultiplyer(xVelocity), body.getLinearVelocity().y));
    }

    public void applySlide() {
        if (slideTimer > MAX_SLIDE_TIMER) {
            body.applyLinearImpulse(new Vector2(horizontalDirectionMultiplyer(1.0f), 0), body.getWorldCenter(), true);
            isSliding = true;
        } else
            body.setLinearVelocity(0, 0);
    }

    public void jump() {

        if (body.getLinearVelocity().y == 0) {

            body.setLinearVelocity(0, 0);

            if ((!screen.getInputController().getControls()[ControlKey.LEFT.index] && !screen.getInputController().getControls()[ControlKey.RIGHT.index]) || currentPlatformState == PlatformState.MUDDY) {
                body.applyLinearImpulse(new Vector2(0, maxJumpVelocity), body.getWorldCenter(), true);
                isStraightJumping = true;
            } else
                body.applyLinearImpulse(new Vector2(horizontalDirectionMultiplyer(9.0f), maxJumpVelocity), body.getWorldCenter(), true);

            currentCharacterState = CharacterState.Kirk.JUMPING;
        }
    }

    public void resetBounceTimer() {
        bounceTimer = 0;
    }

    @Override
    protected void initialiseWorldValues() {
        xVelocity = 10.0f;
        maxJumpVelocity = 7.5f;
    }

    @Override
    protected void initialiseTimers() {
        slideTimer = 0;
        bounceTimer = 0;
        dt = 0;
    }

    @Override
    protected void initialiseBody() {
        Vector2 randomStartingPosition = screen.getRandomStartingPosition();
        cavernGameCamPosition = randomStartingPosition;
        BodyDef bDef = new BodyDef();
        bDef.position.set(randomStartingPosition);
//        bDef.position.set(GameApp.toPPM(MapGenerator.WIDTH/2)*64, GameApp.toPPM(MapGenerator.HUB_HEIGHT/2)*64);
        bDef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bDef);
    }

    @Override
    protected void initialiseFixtures() {
        FixtureDef fDef = new FixtureDef();
        fDef.filter.categoryBits = GameApp.KIRK_BIT;
        fDef.filter.maskBits = GameApp.GROUND_PLATFORM_BIT | GameApp.OBJECT_BIT |
                GameApp.SIDE_WALL_BIT | GameApp.BOUNCY_PLATFORM_BIT |
                GameApp.CRUMBLING_WALL_BIT | GameApp.MUDDY_PLATFORM_BIT | GameApp.ICE_PLATFORM_BIT | GameApp.PORTAL_BIT;

        PolygonShape mainBody = new PolygonShape();
        mainBody.setAsBox(FIXTURE_WIDTH / 2.5f, FIXTURE_HEIGHT / 2.5f + FIXTURE_HEIGHT / 10, new Vector2(0, -FIXTURE_HEIGHT / 1.75f), 0);
        fDef.shape = mainBody;
        fDef.friction = 10.0f;
        body.createFixture(fDef).setUserData("kirk_body");

        EdgeShape leftFrictor = new EdgeShape();
        leftFrictor.set(new Vector2(-FIXTURE_WIDTH / 2.4f, -FIXTURE_HEIGHT * 1.05f), new Vector2(-FIXTURE_WIDTH / 2.4f, -FIXTURE_HEIGHT / 4));
        fDef.shape = leftFrictor;
        fDef.restitution = 0;
        fDef.friction = 0;
        body.createFixture(fDef).setUserData("leftFrictor");

        EdgeShape rightFrictor = new EdgeShape();
        rightFrictor.set(new Vector2(FIXTURE_WIDTH / 2.4f, -FIXTURE_HEIGHT * 1.05f), new Vector2(FIXTURE_WIDTH / 2.4f, -FIXTURE_HEIGHT / 4));
        fDef.shape = rightFrictor;
        fDef.restitution = 0;
        body.createFixture(fDef).setUserData("rightFrictor");

        EdgeShape walkJumpSensor = new EdgeShape();
        walkJumpSensor.set(new Vector2(FIXTURE_WIDTH * 0.8f, -FIXTURE_HEIGHT / 2), new Vector2(-FIXTURE_WIDTH * 0.8f, -FIXTURE_HEIGHT / 2));
        fDef.shape = walkJumpSensor;
        fDef.restitution = 0;
        fDef.friction = 0;
        fDef.isSensor = true;
        body.createFixture(fDef).setUserData("walkJumpSensor");

        CircleShape head = new CircleShape();
        head.setRadius(FIXTURE_WIDTH);
        head.setPosition(new Vector2(0, HEAD_DISPLACEMENT));
        fDef.shape = head;
        fDef.friction = 0;
        fDef.restitution = 0;
        fDef.isSensor = false;
        body.createFixture(fDef).setUserData("kirk_head");

        EdgeShape leftDiagonalFrictor = new EdgeShape();
        leftDiagonalFrictor.set(new Vector2(-FIXTURE_WIDTH, FIXTURE_WIDTH / 2), new Vector2(-FIXTURE_WIDTH / 3, -FIXTURE_WIDTH));
        fDef.restitution = 0;
        fDef.shape = leftDiagonalFrictor;
        fDef.shape = leftDiagonalFrictor;
        fDef.isSensor = false;

        body.createFixture(fDef).setUserData("diagonal");

        EdgeShape rightDiagonalFrictor = new EdgeShape();
        rightDiagonalFrictor.set(new Vector2(FIXTURE_WIDTH, FIXTURE_WIDTH / 2), new Vector2(FIXTURE_WIDTH / 3, -FIXTURE_WIDTH));
        fDef.restitution = 0;
        fDef.shape = rightDiagonalFrictor;
        fDef.shape = leftDiagonalFrictor;
        fDef.isSensor = false;

        body.createFixture(fDef).setUserData("diagonal");
    }

    @Override
    protected void initialiseStates() {
        isBounceJump = false;
        isSliding = false;
        isControlDisabled = false;
        isStraightJumping = false;
        isPortalLocked = false;
    }

    @Override
    protected TextureRegion updateAnimationFrame(float dt) {

        updateCharacterState();

        TextureRegion region;
        switch (currentCharacterState) {
            case JUMPING:
                region = animations[AnimationKey.Kirk.JUMPING.index].getKeyFrame(animationStateTimer);
                break;
            case RUNNING:
                int animationSpeedPosition = currentPlatformState != PlatformState.ICE ? 0 : 1;
                animations[AnimationKey.Kirk.RUNNING.index].setFrameDuration(frameSpeeds[AnimationKey.Kirk.RUNNING.index][animationSpeedPosition]);
                region = animations[AnimationKey.Kirk.RUNNING.index].getKeyFrame(animationStateTimer, true);
                break;
            case JINI_IMPULSE:
                region = animations[AnimationKey.Kirk.BOUNCE_JUMP.index].getKeyFrame(0);
                break;
            case FALLING:
                region = animations[AnimationKey.Kirk.JUMPING.index].getKeyFrame(frameSpeeds[AnimationKey.Kirk.JUMPING.index][0]
                        * frameSequences[AnimationKey.Kirk.JUMPING.index][2]);
                break;
            case MUDDY_RUNNING:
                region = animations[AnimationKey.Kirk.MUDDY_RUNNING.index].getKeyFrame(animationStateTimer, true);
                break;
            case BOUNCE_JUMP:
                if (currentPlatformState == PlatformState.BOUNCY)
                    region = animations[AnimationKey.Kirk.BOUNCE_JUMP.index].getKeyFrame(frameSpeeds[AnimationKey.Kirk.BOUNCE_JUMP.index][0]
                            * frameSequences[AnimationKey.Kirk.BOUNCE_JUMP.index][0]);
                else
                    region = animations[AnimationKey.Kirk.BOUNCE_JUMP.index].getKeyFrame(frameSpeeds[AnimationKey.Kirk.BOUNCE_JUMP.index][0]
                            * frameSequences[AnimationKey.Kirk.BOUNCE_JUMP.index][1]);
                break;
            case SLIDING:
                region = animations[AnimationKey.Kirk.SLIDING.index].getKeyFrame(frameSpeeds[AnimationKey.Kirk.SLIDING.index][0]
                        * frameSequences[AnimationKey.Kirk.SLIDING.index][0]);
                break;
            case STANDING:
            default:
                region = animations[AnimationKey.Kirk.STANDING.index].getKeyFrame(animationStateTimer, true);
                break;
        }

        if (!isFacingRight && !region.isFlipX()) {
            region.flip(true, false);
            isFacingRight = false;
        } else if (isFacingRight && region.isFlipX()) {
            region.flip(true, false);
            isFacingRight = true;
        }

        if (!isPortalLocked) {
            animationStateTimer = currentCharacterState == previousCharacterState ? animationStateTimer + dt : 0;
            previousCharacterState = currentCharacterState;
        }

        return region;
    }

    private float calculateJiniImpulseRotation() {
        if (currentCharacterState == CharacterState.Kirk.JINI_IMPULSE) {
            float rotationSpeed = KIRK_STRAIGHT_JUMP_ROTATION_SPEED;
            return horizontalDirectionMultiplyer(-rotationSpeed);
        } else {
            setRotation(0);
            return 0;
        }
    }

    @Override
    protected void updateCharacterState() {

        if (screen.getInputController().getDoubleJumpImpulse() || screen.getInputController().getBounceJumpImpulse()
                || screen.getInputController().getIceBurstImpulse())
            currentCharacterState = CharacterState.Kirk.JINI_IMPULSE;
        else if ((body.getLinearVelocity().y > 0 && currentCharacterState == CharacterState.Kirk.JUMPING) ||
                (body.getLinearVelocity().y < 0 && previousCharacterState == CharacterState.Kirk.JUMPING) && !isBounceJump && !isStraightJumping)
            currentCharacterState = CharacterState.Kirk.JUMPING;
        else if (body.getLinearVelocity().y < 0 && !isBounceJump)
            currentCharacterState = CharacterState.Kirk.FALLING;
        else if (body.getLinearVelocity().x != 0 && currentPlatformState == PlatformState.ICE && isSliding)
            currentCharacterState = CharacterState.Kirk.SLIDING;
        else if (body.getLinearVelocity().x != 0 && currentPlatformState == PlatformState.MUDDY)
            currentCharacterState = CharacterState.Kirk.MUDDY_RUNNING;
        else if (currentPlatformState == PlatformState.BOUNCY || isBounceJump)
            currentCharacterState = CharacterState.Kirk.BOUNCE_JUMP;
        else if (body.getLinearVelocity().x != 0)
            currentCharacterState = CharacterState.Kirk.RUNNING;
        else
            currentCharacterState = CharacterState.Kirk.STANDING;
    }

    @Override
    public void dispose() {
        world.dispose();
        screen.dispose();
    }

    public PlatformState getCurrentPlatformState() {
        return currentPlatformState;
    }

    public boolean isControlDisabled() {
        return isControlDisabled;
    }

    public boolean isBounceJumping() {
        return isBounceJump;
    }

    public boolean isSliding() {
        return isSliding;
    }

    public CharacterState.Kirk getCurrentCharacterState() {
        return currentCharacterState;
    }

    public void setCurrentPlatformState(PlatformState state) {
        this.currentPlatformState = state;
    }

    public void setFacingRight(boolean isFacingRight) {
        this.isFacingRight = isFacingRight;
    }

    public void setStraightJumping(boolean isStraightJumping) {
        this.isStraightJumping = isStraightJumping;
    }

    public void setBounceJump(boolean isBounceJump) {
        this.isBounceJump = isBounceJump;
    }

    public void setControlDisabled(boolean isControlDisabled) {
        this.isControlDisabled = isControlDisabled;
    }

    public void setSliding(boolean sliding) {
        this.isSliding = sliding;
    }

    public void glideDown() {
        body.setLinearVelocity(new Vector2(0, -20.0f));
        setRotation(0);
        screen.getInputController().setDoubleJumpImpulse(false);
    }

    public void setSourcePortal(Portal sourcePortal) {
        this.sourcePortal = sourcePortal;
    }

    public void setTargetPortal(Portal targetPortal) {
        this.targetPortal = targetPortal;
    }

    public void setPortalLocked(boolean isPortalLocked) {
        this.isPortalLocked = isPortalLocked;
    }

    public boolean isPortalLocked() {
        return isPortalLocked;
    }

    public ParticleEffect getCellularDisintegrationEffect(){
        return cellularDisintegrationEffect;
    }
}
