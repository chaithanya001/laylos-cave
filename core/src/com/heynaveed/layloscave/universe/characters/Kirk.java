package com.heynaveed.layloscave.universe.characters;

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
import com.heynaveed.layloscave.keys.CharacterKey;
import com.heynaveed.layloscave.keys.ControlKey;
import com.heynaveed.layloscave.states.PlatformState;
import com.heynaveed.layloscave.utils.AnimationPackager;
import com.heynaveed.layloscave.utils.RoundTo;
import com.heynaveed.layloscave.keys.SpriteKey;
import com.heynaveed.layloscave.universe.Character;


public final class Kirk extends Character {

    private static final float KIRK_STRAIGHT_JUMP_ROTATION_SPEED = 25.0f;
    private static final float MAXIMUM_VELOCITY = 15.0f;
    private static final float CAMERA_WEST_LIMIT = 12.0f;
    private static final float CAMERA_JUMP_THRESHOLD = 6.8f;
    private static final float FIXTURE_WIDTH = GameApp.toPPM(48);
    private static final float FIXTURE_HEIGHT = GameApp.toPPM(92);
    private static final float HEAD_DISPLACEMENT = GameApp.toPPM(30);
    private static final float MAX_SLIDE_TIMER = 0.6f;
    private static final float MAX_BOUNCE_TIMER = 0.3f;

    private final PlayScreen screen;
    private float xVelocity;
    private float slideTimer;
    private float bounceTimer;
    private float maxJumpVelocity;
    private float dt;
    private float kirkRotationAngle;
    private boolean isBounceJump;
    private boolean isSliding;
    private boolean isControlDisabled;
    private boolean isStraightJumping;
    private CharacterState.Kirk currentCharacterState;
    private CharacterState.Kirk previousCharacterState;
    private PlatformState currentPlatformState;

    public Kirk(PlayScreen screen) {
        super(screen);
        this.screen = screen;
        animationPackager = new AnimationPackager(CharacterKey.KIRK);
        setTexture(animationPackager.getTexture());
        setOrigin(GameApp.toPPM(GameApp.TILE_LENGTH * 1.5f), GameApp.toPPM(GameApp.TILE_LENGTH * 0.75f));
        frameSequences = animationPackager.getFrameSequences();
        frameSpeeds = animationPackager.getFrameSpeeds();
        animations = animationPackager.getAnimations();
        currentPlatformState = PlatformState.NONE;
        initialiseWorldValues();
        initialiseTimers();
        initialiseBody();
        initialiseFixtures();
        initialiseStates();
    }

    @Override
    public void update(float dt) {
        this.dt = dt;
        panCamera();
        capVelocities();
        handlePlatformInteractions();
        handleJiniImpulseRotations();
        setPosition(body.getPosition().x - getWidth() / 2, body.getPosition().y - getHeight() / 2);
        setRegion(updateAnimationFrame(dt));
    }

    private void handleJiniImpulseRotations(){
        rotate(calculateJiniImpulseRotation());
    }

    private void capVelocities() {
        if (Math.abs(body.getLinearVelocity().x) > MAXIMUM_VELOCITY)
            body.setLinearVelocity(new Vector2(horizontalDirectionMultiplyer(MAXIMUM_VELOCITY), body.getLinearVelocity().y));
        if (body.getLinearVelocity().y < -MAXIMUM_VELOCITY && !isBounceJump)
            body.setLinearVelocity(new Vector2(body.getLinearVelocity().x, verticalDirectionMultiplyer(MAXIMUM_VELOCITY)));
    }

    private void panCamera() {
        if (body.getPosition().x > CAMERA_WEST_LIMIT)
            gameCam.position.x = RoundTo.RoundToNearest(body.getPosition().x, GameApp.toPPM(1));
        else
            gameCam.position.x = CAMERA_WEST_LIMIT;

        if (body.getPosition().y > CAMERA_JUMP_THRESHOLD)
            gameCam.position.y = RoundTo.RoundToNearest(body.getPosition().y, GameApp.toPPM(1));
        else
            gameCam.position.y = CAMERA_JUMP_THRESHOLD;
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

        if (screen.getInputController().getControls()[ControlKey.LEFT.getKey()] || screen.getInputController().getControls()[ControlKey.RIGHT.getKey()])
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

        boolean left = screen.getInputController().getControls()[ControlKey.LEFT.getKey()];
        boolean right = screen.getInputController().getControls()[ControlKey.RIGHT.getKey()];

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

            if ((!screen.getInputController().getControls()[ControlKey.LEFT.getKey()] && !screen.getInputController().getControls()[ControlKey.RIGHT.getKey()]) || currentPlatformState == PlatformState.MUDDY) {
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
        kirkRotationAngle = 0;
    }

    @Override
    protected void initialiseTimers() {
        slideTimer = 0;
        bounceTimer = 0;
        dt = 0;
    }

    @Override
    protected void initialiseBody() {
        BodyDef bDef = new BodyDef();
        bDef.position.set(screen.getRandomStartingPosition());
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
    }

    @Override
    protected TextureRegion updateAnimationFrame(float dt) {

        updateCharacterState();

        TextureRegion region;
        switch (currentCharacterState) {
            case JUMPING:
                region = animations[SpriteKey.Kirk.JUMPING.getKey()].getKeyFrame(animationStateTimer);
                break;
            case RUNNING:
                int animationSpeedPosition = currentPlatformState != PlatformState.ICE ? 0 : 1;
                animations[SpriteKey.Kirk.RUNNING.getKey()].setFrameDuration(frameSpeeds[SpriteKey.Kirk.RUNNING.getKey()][animationSpeedPosition]);
                region = animations[SpriteKey.Kirk.RUNNING.getKey()].getKeyFrame(animationStateTimer, true);
                break;
            case JINI_IMPULSE:
                region = animations[SpriteKey.Kirk.BOUNCE_JUMP.getKey()].getKeyFrame(0);
                break;
            case FALLING:
                region = animations[SpriteKey.Kirk.JUMPING.getKey()].getKeyFrame(frameSpeeds[SpriteKey.Kirk.JUMPING.getKey()][0]
                        * frameSequences[SpriteKey.Kirk.JUMPING.getKey()][2]);
                break;
            case MUDDY_RUNNING:
                region = animations[SpriteKey.Kirk.MUDDY_RUNNING.getKey()].getKeyFrame(animationStateTimer, true);
                break;
            case BOUNCE_JUMP:
                if (currentPlatformState == PlatformState.BOUNCY)
                    region = animations[SpriteKey.Kirk.BOUNCE_JUMP.getKey()].getKeyFrame(frameSpeeds[SpriteKey.Kirk.BOUNCE_JUMP.getKey()][0]
                            * frameSequences[SpriteKey.Kirk.BOUNCE_JUMP.getKey()][0]);
                else
                    region = animations[SpriteKey.Kirk.BOUNCE_JUMP.getKey()].getKeyFrame(frameSpeeds[SpriteKey.Kirk.BOUNCE_JUMP.getKey()][0]
                            * frameSequences[SpriteKey.Kirk.BOUNCE_JUMP.getKey()][1]);
                break;
            case SLIDING:
                region = animations[SpriteKey.Kirk.SLIDING.getKey()].getKeyFrame(frameSpeeds[SpriteKey.Kirk.SLIDING.getKey()][0]
                        * frameSequences[SpriteKey.Kirk.SLIDING.getKey()][0]);
                break;
            case STANDING:
            default:
                region = animations[SpriteKey.Kirk.STANDING.getKey()].getKeyFrame(animationStateTimer, true);
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

    private float calculateJiniImpulseRotation(){
        if(currentCharacterState == CharacterState.Kirk.JINI_IMPULSE) {
            float rotationSpeed = KIRK_STRAIGHT_JUMP_ROTATION_SPEED;
            kirkRotationAngle += horizontalDirectionMultiplyer(-rotationSpeed);
            return horizontalDirectionMultiplyer(-rotationSpeed);
        }
        else {
            float tempRotation = kirkRotationAngle;
            kirkRotationAngle = 0;
            return -tempRotation;
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
    }
}