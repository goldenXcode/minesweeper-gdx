package com.alex.games.minesweeper;
import com.badlogic.gdx.backends.jogl.JoglApplication;

public class MinesweeperDesktop {

	public static void main(String[] args) {
		new JoglApplication(new Minesweeper(), "Hello World", 480, 640, false);
	}

}
