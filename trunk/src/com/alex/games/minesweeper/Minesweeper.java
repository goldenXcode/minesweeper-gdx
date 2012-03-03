package com.alex.games.minesweeper;

import com.alex.games.minesweeper.screen.GameScreen;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Logger;


public class Minesweeper extends Game {

	public static final Logger Log = new Logger("Minesweeper");
	public static final String PREF_NAME = "Minesweeper";
// TODO Add ads
	@Override
	public void create() {
		setScreen(new GameScreen(this));
		Log.setLevel(Logger.DEBUG);
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
	}
}
