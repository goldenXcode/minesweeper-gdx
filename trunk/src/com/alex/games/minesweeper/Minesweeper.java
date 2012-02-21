package com.alex.games.minesweeper;

import com.alex.games.minesweeper.screen.GameScreen;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Logger;


public class Minesweeper extends Game {

	public static final Logger Log = new Logger("Minesweeper");
// TODO add high scores
// TODO Manage application lifecycle in android (when quitting the state is lost)
// TODO Add ads
	private GameScreen gameScreen;
	private MenuScreen menuScreen;
	
	@Override
	public void create() {
		gameScreen = new GameScreen(this);
		menuScreen = new MenuScreen(this);
		gameScreen.setNextScreen(menuScreen);
		menuScreen.setNextScreen(gameScreen);
		setScreen(gameScreen);
		Log.setLevel(Logger.DEBUG);
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
	}
}
