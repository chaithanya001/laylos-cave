package com.heynaveed.layloscave.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.heynaveed.layloscave.GameApp;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		GameApp gameApp = new GameApp();
		GameApp.CONFIGURATION = "Desktop";
		new LwjglApplication(gameApp, config);
		config.title = GameApp.TITLE;
		config.height = GameApp.HEIGHT;
		config.width = GameApp.WIDTH;
		config.fullscreen = true;
	}
}
