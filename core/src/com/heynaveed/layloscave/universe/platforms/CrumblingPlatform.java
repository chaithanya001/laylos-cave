package com.heynaveed.layloscave.universe.platforms;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.heynaveed.layloscave.GameApp;
import com.heynaveed.layloscave.universe.Platform;

import java.util.ArrayList;
import java.util.Random;


public class CrumblingPlatform extends Platform {

    private static final Random random = new Random();
    private static final Texture texture = new Texture(Gdx.files.internal("maps/tileMapGutter.png"));
    private static final TextureRegion block1 = new TextureRegion(texture, GameApp.TILE_LENGTH*0, GameApp.TILE_LENGTH*1, GameApp.TILE_LENGTH, GameApp.TILE_LENGTH);
    private static final TextureRegion block2 = new TextureRegion(texture, GameApp.TILE_LENGTH*1, GameApp.TILE_LENGTH*1, GameApp.TILE_LENGTH, GameApp.TILE_LENGTH);
    private static final TextureRegion block3 = new TextureRegion(texture, GameApp.TILE_LENGTH*2, GameApp.TILE_LENGTH*1, GameApp.TILE_LENGTH, GameApp.TILE_LENGTH);
    private static final TextureRegion block4 = new TextureRegion(texture, GameApp.TILE_LENGTH*3, GameApp.TILE_LENGTH*1, GameApp.TILE_LENGTH, GameApp.TILE_LENGTH);
    private static final TextureRegion block5 = new TextureRegion(texture, GameApp.TILE_LENGTH*4, GameApp.TILE_LENGTH*1, GameApp.TILE_LENGTH, GameApp.TILE_LENGTH);

    private final ArrayList<ArrayList<CrumblingBlock>> blocks;
    private final int blocksWidth;
    private final int blocksHeight;
    private final int offsetX;
    private final int offsetY;
    private float deathCountdown;
    private boolean isTouched;
    private boolean isDead;

    public CrumblingPlatform(World world, MapObject object) {
        super(world, object);
        blocks = new ArrayList<ArrayList<CrumblingBlock>>();
        blocksWidth = (int) width / GameApp.TILE_LENGTH;
        blocksHeight = (int) height / GameApp.TILE_LENGTH;
        offsetX = calculateOffset(blocksWidth);
        offsetY = calculateOffset(blocksHeight);
        floorFixtureDef.friction = 1.0f;
        deathCountdown = (random.nextInt(300) + 200)/100;
        isTouched = false;
        isDead = false;
        createPlatform(GameApp.CRUMBLING_WALL_BIT);
        createBlocks();
    }

    private void createBlocks(){
        for(int i = 0; i < blocksHeight; i++) {
            blocks.add(new ArrayList<CrumblingBlock>());
            for(int j = 0; j < blocksWidth; j++)
                blocks.get(i).add(new CrumblingBlock(body.getPosition(), (-blocksWidth / 2 + j), (-blocksHeight / 2 + i), offsetX, offsetY));
        }
    }

    @Override
    public void update(float delta) {
        if(isTouched)
            deathCountdown -= delta;

        if(deathCountdown < 0)
            isDead = true;
    }

    public ArrayList<ArrayList<CrumblingBlock>> getBlocks(){
        return blocks;
    }

    public boolean getIsDead(){
        return isDead;
    }

    public void setIsTouched(boolean isTouched){
        this.isTouched = isTouched;
    }

    public class CrumblingBlock extends Sprite {

        public CrumblingBlock(Vector2 bodyPos, float xPos, float yPos, int offsetX, int offsetY){
            setBounds(0, 0, GameApp.toPPM(GameApp.TILE_LENGTH), GameApp.toPPM(GameApp.TILE_LENGTH));
            setRegion(decideRegion());
            setPosition(bodyPos.x - getWidth()/2 + GameApp.toPPM(xPos* GameApp.TILE_LENGTH + offsetX), bodyPos.y - getHeight()/2 + GameApp.toPPM(yPos* GameApp.TILE_LENGTH + offsetY));
        }

        private TextureRegion decideRegion(){
            switch(random.nextInt(5)){
                case 0:
                    return block1;
                case 1:
                    return block2;
                case 2:
                    return block3;
                case 3:
                    return block4;
                case 4:
                    return block5;
            }
            return null;
        }
    }
}
