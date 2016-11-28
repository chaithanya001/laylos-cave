package com.heynaveed.layloscave.utils;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.heynaveed.layloscave.GameApp;
import com.heynaveed.layloscave.keys.SpriteKeys;

public final class AnimationPackager {

    private static final int KIRK_TOP_MARGIN_PADDING = 57;
    private static final int KIRK_LEFT_MARGIN_PADDING = 156;
    private static final int JINI_TOP_MARGIN_PADDING = 2;
    private static final int JINI_LEFT_MARGIN_PADDING = 2;
    private static final int BORDER_OFFSET = 2;
    private static final String TEXTURE_FILE_PATH = "sprite-sheets/";
    private static final String FILE_TYPE = ".png";
    private static final int[][] KIRK_FRAME_SEQUENCES = {{0, 1}, {0, 1, 0, 2}, {0, 1, 2}, {0, 1, 0, 2}, {0, 1}, {0}};
    private static final float[][] KIRK_FRAME_SPEEDS = {{0.4f}, {0.12f, 0.15f}, {0.2f}, {0.2f}, {0.4f}, {0}};
    private static final Animation[] KIRK_ANIMATIONS = new Animation[KIRK_FRAME_SEQUENCES.length];
    private static final int[][] JINI_FRAME_SEQUENCES = {{0, 1, 2, 3, 4, 5, 4, 3, 2, 1}, {0, 1, 2, 3}, {6, 5, 4, 3, 2, 1, 0, 1, 2, 3, 4, 5, 6}, {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14}};
    private static final float[][] JINI_FRAME_SPEEDS = {{0.1f}, {0.1f}, {0.03f}, {0.05f}};
    private static final Animation[] JINI_ANIMATIONS = new Animation[JINI_FRAME_SEQUENCES.length];
    private static final int[][] PORTAL_FRAME_SEQUENCES = {{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}};
    private static final float[][] PORTAL_FRAME_SPEEDS = {{0.075f}};
    private static final Animation[] PORTAL_ANIMATIONS = new Animation[PORTAL_FRAME_SEQUENCES.length];

    private final Texture texture;
    private int tileLengthWidth;
    private int tileLengthHeight;

    private int[][] frameSequences;
    private float[][] frameSpeeds;
    private Animation[] animations;

    public AnimationPackager(SpriteKeys character){
        texture = new Texture(Gdx.files.internal(TEXTURE_FILE_PATH + character.getKey() + FILE_TYPE));
        createPackage(character);
    }

    private void createPackage(SpriteKeys character){

        switch(character){
            case KIRK:
                frameSequences = KIRK_FRAME_SEQUENCES;
                frameSpeeds = KIRK_FRAME_SPEEDS;
                animations = KIRK_ANIMATIONS;
                tileLengthWidth = GameApp.TILE_LENGTH;
                tileLengthHeight = GameApp.TILE_LENGTH;
                initialiseAnimations(KIRK_TOP_MARGIN_PADDING, KIRK_LEFT_MARGIN_PADDING);
                break;
            case JINI:
                frameSequences = JINI_FRAME_SEQUENCES;
                frameSpeeds = JINI_FRAME_SPEEDS;
                animations = JINI_ANIMATIONS;
                tileLengthWidth = GameApp.NEW_TILE_LENGTH;
                tileLengthHeight = GameApp.NEW_TILE_LENGTH;
                initialiseAnimations(JINI_TOP_MARGIN_PADDING, JINI_LEFT_MARGIN_PADDING);
                break;
            case PORTAL:
                frameSequences = PORTAL_FRAME_SEQUENCES;
                frameSpeeds = PORTAL_FRAME_SPEEDS;
                animations = PORTAL_ANIMATIONS;
                tileLengthWidth = GameApp.NEW_TILE_LENGTH;
                tileLengthHeight = GameApp.NEW_TILE_LENGTH*2;
                initialiseAnimations(BORDER_OFFSET, BORDER_OFFSET);
        }
    }

    private void initialiseAnimations(int topPad, int leftPad){
        Array<TextureRegion> frames = new Array<TextureRegion>();

        for (int i = 0; i < frameSequences.length; i++) {
            for (int j = 0; j < frameSequences[i].length; j++)
                frames.add(new TextureRegion(texture, calculateXDisplacement(i, j, leftPad), calculateYDisplacement(i, topPad), tileLengthWidth, tileLengthHeight));
            animations[i] = new Animation(frameSpeeds[i][0], frames);
            frames.clear();
        }
    }

    private final int calculateXDisplacement(int i, int j, int leftPad){
        return leftPad + (frameSequences[i][j] * tileLengthWidth) + BORDER_OFFSET*(frameSequences[i][j]+1);
    }

    private final int calculateYDisplacement(int i, int topPad){
        return topPad + (tileLengthHeight*i)+(BORDER_OFFSET*(i+1));
    }

    public int[][] getFrameSequences(){
        return frameSequences;
    }

    public float[][] getFrameSpeeds(){
        return frameSpeeds;
    }

    public Animation[] getAnimations(){
        return animations;
    }

    public Texture getTexture(){
        return texture;
    }
}
