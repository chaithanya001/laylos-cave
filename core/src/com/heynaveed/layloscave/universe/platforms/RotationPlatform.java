package com.heynaveed.layloscave.universe.platforms;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.heynaveed.layloscave.GameApp;
import com.heynaveed.layloscave.universe.Platform;

import java.util.ArrayList;
import java.util.Random;


public class RotationPlatform extends Platform {

    private static final Texture texture = new Texture(Gdx.files.internal("maps/tileSet.png"));
    private static final TextureRegion textureReg = new TextureRegion(texture, GameApp.TILE_LENGTH*2, 0, GameApp.TILE_LENGTH, GameApp.TILE_LENGTH);

    private final Random random;
    private final ArrayList<ArrayList<RotationBlock>> blocks;
    private final int blocksWidth;
    private final int blocksHeight;
    private final int offsetX;
    private final int offsetY;

    private float blockRotation;
    private float previousAngle;
    private float restTime;
    private float rotationSpeed;
    private float restTimer;
    private boolean isRotating;

    public RotationPlatform(World world, MapObject object) {
        super(world, object);
        random = new Random();
        blocks = new ArrayList<ArrayList<RotationBlock>>();
        blockRotation = 0;
        previousAngle = 0;
        blocksWidth = (int)width / GameApp.TILE_LENGTH;
        blocksHeight = (int)height / GameApp.TILE_LENGTH;
        offsetX = calculateOffset(blocksWidth);
        offsetY = calculateOffset(blocksHeight);
        restTime = calculateRestTime();
        rotationSpeed = calculateRotationSpeed();
        restTimer = 0;
        isRotating = false;

        floorFixtureDef.friction = 1.0f;
        createPlatform(GameApp.GROUND_PLATFORM_BIT);
        createBlocks();
    }

    private void createBlocks(){
        for(int i = 0; i < blocksHeight; i++) {
            blocks.add(new ArrayList<RotationBlock>());
            for(int j = 0; j < blocksWidth; j++)
                blocks.get(i).add(new RotationBlock(body.getPosition(), (-blocksWidth / 2 + j), (-blocksHeight / 2 + i), offsetX, offsetY));
        }
    }

    private void rotateBlocks(float newAngle){
        for(ArrayList<RotationBlock> blockLists: blocks){
            for(RotationBlock blocksInList: blockLists)
                blocksInList.rotate(newAngle * MathUtils.radiansToDegrees);
        }
    }

    private int calculateDirection(){
        if(random.nextInt(2) == 0)
            return -1;
        else
            return 1;
    }

    private float calculateRestTime(){
        return (random.nextInt(600)+300)/100;
    }

    private float calculateRotationSpeed(){
        return (random.nextInt(30)+30)/10;
    }

    @Override
    public void update(float delta) {
        if(restTimer < restTime && !isRotating)
            restTimer += delta;
        else
            isRotating = true;

        if(isRotating && Math.abs(body.getAngularVelocity()) != rotationSpeed)
            body.setAngularVelocity(rotationSpeed * calculateDirection());

        if(isRotating){
            float newAngle = body.getAngle() - previousAngle;
            previousAngle = body.getAngle();
            blockRotation += newAngle;
            rotateBlocks(newAngle);
        }

        if(Math.abs(body.getAngle()) > 179 * MathUtils.degreesToRadians && isRotating){
            rotateBlocks((180*MathUtils.degreesToRadians) - blockRotation);
            body.setAngularVelocity(0);
            isRotating = false;
            restTimer = 0;
            blockRotation = 0;
            previousAngle = 0;
            restTime = calculateRestTime();
            rotationSpeed = calculateRotationSpeed();
            body.setTransform(body.getWorldCenter(), 0);
        }
    }

    public ArrayList<ArrayList<RotationBlock>> getBlocks() {
        return blocks;
    }

    public class RotationBlock extends Sprite {

        public RotationBlock(Vector2 bodyPos, float xPos, float yPos, int offsetX, int offsetY){
            setBounds(0, 0, GameApp.toPPM(GameApp.TILE_LENGTH), GameApp.toPPM(GameApp.TILE_LENGTH));
            setRegion(textureReg);
            setPosition(bodyPos.x - getWidth()/2 + GameApp.toPPM(xPos* GameApp.TILE_LENGTH + offsetX), bodyPos.y - getHeight()/2 + GameApp.toPPM(yPos* GameApp.TILE_LENGTH + offsetY));
            setOrigin(getWidth()/2 + GameApp.toPPM(-xPos* GameApp.TILE_LENGTH - offsetX), getHeight()/2 + GameApp.toPPM(-yPos* GameApp.TILE_LENGTH - offsetY));
        }
    }
}
