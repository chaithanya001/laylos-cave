package com.heynaveed.layloscave;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.heynaveed.layloscave.screens.PlayScreen;
import com.heynaveed.layloscave.utils.maps.MapGenerator;
import com.heynaveed.layloscave.utils.maps.TileVector;

import java.io.IOException;

public class GameApp extends Game {

	public static final String TITLE = "Laylos Cave";
	public static final int WIDTH = 1200;
	public static final int HEIGHT = 675;
	public static final int VIEWPORT_WIDTH = 3000;
	public static final int VIEWPORT_HEIGHT = 1687;
	public static final int TILE_LENGTH = 64;
	public static final int NEW_TILE_LENGTH = 128;
	public static final short KIRK_BIT = 1;
	public static final short OBJECT_BIT = 2;
	public static final short GROUND_PLATFORM_BIT = 4;
	public static final short SIDE_WALL_BIT = 8;
	public static final short BOUNCY_PLATFORM_BIT = 16;
	public static final short CRUMBLING_WALL_BIT = 32;
	public static final short MUDDY_PLATFORM_BIT = 64;
	public static final short ICE_PLATFORM_BIT = 128;
	public static final short JINI_BIT = 256;
	public static final short PORTAL_BIT = 512;
	public static final float FPS = 60;
	public static String CONFIGURATION;
    private static final float PPM = 100;

	public SpriteBatch batch;

	@Override
	public void create () {
		batch = new SpriteBatch();
		try {
			setScreen(new PlayScreen(this));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    public static float toPPM(int value){
        return value / PPM;
    }

    public static float toPPM(float value){
        return value / PPM;
    }

	public static int fromPPM(float value){
		return (int)(value * PPM);
	}

	public static Vector2 tileVectorToWorldPosition(TileVector position) {
		return new Vector2(toPPM(position.y()) * 64, toPPM(MapGenerator.workingHeight - position.x()) * 64);
	}

	public static TileVector worldPositionToTileVector(Vector2 position) {
		int x = fromPPM(position.x / 64);
		int y = MapGenerator.workingHeight - fromPPM(position.y / 64);
		return new TileVector(y, x);
	}

	@Override
	public void render () {
		super.render();
	}

	@Override
	public void dispose () {
		batch.dispose();
	}
}
