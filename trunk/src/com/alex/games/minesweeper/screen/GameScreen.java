package com.alex.games.minesweeper.screen;

import java.util.TreeSet;

import com.alex.games.minesweeper.DieAnimation;
import com.alex.games.minesweeper.DieAnimation.Mine;
import com.alex.games.minesweeper.Minesweeper;
import com.alex.games.utils.Box;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;

public class GameScreen implements Screen {

	private static final int NEW = 0;
	private static final int LOSE = 1;
	private static final int WIN = 2;
	
	public static final float FRAME_TIME = 0.08f;
	public static final float EXPLOSION_FRAMES = 13;
	private static final long FrameTime = 33;
	private static final int MAX_SCORES = 6;
	
	private SpriteBatch spriteBatch;
	
	private final TextureRegion grass;
	private final TextureRegion mine;
	private final TextureRegion brokenMine;
	private final TextureRegion earth;
	private final TextureRegion flag;
	private final TextureRegion shovel;
	private final TextureRegion[] smiley;
	private final TextureRegion button;
	private final TextureRegion pressedButton;
	
	private final NinePatch dialog;
	
	private BitmapFont font;
	
	
	private int[][] matrix;
	private int cellHeight = 10;
	private int cellWidth = 10;
	private int initiallMines = 15;
	private boolean flagMode = false;
	
	// Coordinates for the objects
	private final int CellSize;
	private final int HalfCell;
	private final int ControlHeight; // Vertical space reserved for the game controls
	private final int baseX;
	private final int baseY;
	private final int gameWidth;
	private final int gameHeight;
	// Minefield box	
	private final Box mineField;
	private final Box flagBox;
	private final Box shovelBox;
	private final Box smileBox;
	private final Box counterBox;
	private final Box timerBox;
	private final Box yesBox;
	private final Box noBox;
	private final Box scoreBox;
	
	private int gameState = NEW;
	private int numMines;
	private int numSquares;
	private float time;
	private boolean isTimerRunning;
	private BitmapFont timeFont;
	private boolean displayExp;
	private Animation expAnimation;
	
	private DieAnimation dieAnimation;
	private float tFontPad;
	
	private boolean isPaused;
	
	private CharSequence quitText = "Are you sure you want to exit?";
	private NinePatch button2;
	private boolean showHighScores = true;
	TreeSet<Integer> scores = new TreeSet<Integer>();
	private Preferences prefs;
	
	public GameScreen(Game game) {
		int w = Gdx.app.getGraphics().getWidth();
		int h = Gdx.app.getGraphics().getHeight();
		//float aspectRatio = h/w;
		// Define cell size. This will define the resolution of the textures 
		// and the coordinates of everything in the game
		if (w/cellWidth >= 42 ) { 
			CellSize = 42;
		}
		else {
			CellSize = 32;
		}
		
		HalfCell = CellSize / 2;
		ControlHeight = CellSize*3;
		int boxSize = CellSize + HalfCell;
		// Load & prepare textures
		spriteBatch = new SpriteBatch();
		TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("data/textures/pack"), 
											  Gdx.files.internal("data/textures/"));
		grass = atlas.findRegion("grass2");
		
		grass.getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		button = atlas.findRegion("tile2");
		pressedButton = atlas.findRegion("pressedTile2");
		mine = atlas.findRegion("mine");
		brokenMine = atlas.findRegion("mine2");
		earth = atlas.findRegion("earth2");
		flag = atlas.findRegion("flag");
		shovel = atlas.findRegion("shovel");
		font = new BitmapFont(Gdx.files.internal("data/font16.fnt"), 
							  Gdx.files.internal("data/font16.png"), false);
		
		timeFont = new BitmapFont(false);
		timeFont.setColor(Color.RED);
		float scale = CellSize/32.f;
		font.setScale(scale);
		timeFont.setScale(scale);
		
		smiley = new TextureRegion[] { 
			atlas.findRegion("Smiley1"),
		    atlas.findRegion("Smiley3"),
			atlas.findRegion("Smiley4")
		};
			
		dialog = new NinePatch(atlas.findRegion("dialog"),10,10,10,10);
		button2 = new NinePatch(atlas.findRegion("tile2"),5,5,5,5);
		loadExplosion();
		
		// Calculate decoration coordinates
		gameWidth = cellWidth * CellSize;
		gameHeight = cellHeight * CellSize;

		baseX = (w - gameWidth) / 2;
		baseY = (h - gameHeight - ControlHeight) / 2;
		mineField = new Box(baseX, baseX+gameWidth, baseY, baseY+gameHeight);
		
		counterBox = new Box(baseX + HalfCell, 
							 baseX + HalfCell + boxSize, 
							 mineField.ye + 15, 
							 mineField.ye + CellSize + 15);
		
		timerBox = new Box(mineField.xe - HalfCell - counterBox.w, 
							 mineField.xe - HalfCell, 
							 counterBox.yo, 
							 counterBox.ye);
		
		shovelBox = new Box(counterBox.xe + HalfCell, 
							 counterBox.xe + HalfCell + CellSize, 
							 counterBox.yo, 
							 counterBox.ye);
		
		flagBox = new Box(shovelBox.xe + 10, 
						  shovelBox.xe + 10 + CellSize, 
						  shovelBox.yo, 
						  shovelBox.ye);
		
		smileBox = new Box(flagBox.xe + CellSize, 
						   flagBox.xe + CellSize*2, 
						   shovelBox.yo, 
						   shovelBox.ye);
		
		yesBox = new Box(mineField.xo + 4*CellSize, 
						 mineField.xe - 4*CellSize,
						 mineField.yo + 4*CellSize,
						 mineField.yo + 5*CellSize);
		
		noBox = new Box(yesBox.xo, yesBox.xe, yesBox.ye + CellSize, yesBox.ye + CellSize + CellSize);
		
		scoreBox = new Box(mineField.xo + (int) (CellSize * 2.1), 
						   mineField.xe - (int) (CellSize * 2.1), 
						   mineField.yo + (int) (CellSize * 0.6), 
						   mineField.ye - (int) (CellSize * 0.6));
		
		tFontPad = (CellSize + timeFont.getCapHeight())/2;
		// TODO Reset or resume?
		clearGame();
		
		prefs = Gdx.app.getPreferences(Minesweeper.PREF_NAME);
		
		for (int i=0; i<MAX_SCORES; i++) {
			scores.add(prefs.getInteger("score"+i, 1000+i));
		}
	}
	
	@Override
	public void dispose() {
		// TODO Implement later
		Minesweeper.Log.debug("dispose");
	}

	@Override
	public void hide() {
		Minesweeper.Log.debug("Hide...");
	}

	@Override
	public void pause() {
		Minesweeper.Log.debug("Pause...");
		Gdx.input.setCatchBackKey(false);
	}

	@Override
	public void render(float delta) {
		long start = System.currentTimeMillis();
		update(delta);
		
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		spriteBatch.begin();
		
		dialog.draw(spriteBatch, mineField.xo - 5, mineField.yo - 5, mineField.w + 10, mineField.h + 10);
		for (int i=0; i<matrix.length; i++) {
			for (int j=0; j<matrix[0].length; j++) {
				int value = matrix[i][j];
				int x = j*CellSize + mineField.xo;
				int y = i*CellSize + mineField.yo;
				if (value > 9) {
					spriteBatch.draw(grass, x, y, CellSize, CellSize);
					
					if (value > 19) spriteBatch.draw(flag, x, y, CellSize, CellSize);
				}
				else {
					spriteBatch.draw(earth, x, y, CellSize, CellSize);
					
					if ( value == 9) {
						spriteBatch.draw(mine, x, y, CellSize, CellSize);
					}
					else if ( value == -1) {
						spriteBatch.draw(brokenMine, x, y, CellSize, CellSize);
					}
					else if (value != 0) {
						String text = Integer.toString(matrix[i][j]);
						font.draw(spriteBatch, text, x + CellSize/3, y + CellSize);
					}
				}
				
			}
		}
		
		if (displayExp) {
			for (Mine m:dieAnimation.getMines()) {
				spriteBatch.draw(expAnimation.getKeyFrame(m.timeState, false),
								 m.x*CellSize - HalfCell + mineField.xo, 
								 m.y*CellSize - HalfCell + mineField.yo);
			}
		}
		
		drawControls(delta);
		
		if (isPaused) {
			dialog.draw(spriteBatch, mineField.xo+CellSize, 
								  mineField.yo+CellSize, 
								  mineField.w-(CellSize*2), 
								  mineField.h-(CellSize*2));
		
			timeFont.setColor(Color.BLUE);
			timeFont.drawMultiLine(spriteBatch, quitText, mineField.xo + CellSize*2, 
							   mineField.ye - (CellSize*2), mineField.w - CellSize*4, 
							   BitmapFont.HAlignment.CENTER);
		
			button2.draw(spriteBatch, yesBox.xo, yesBox.yo, yesBox.w, yesBox.h);
		
			button2.draw(spriteBatch, noBox.xo, noBox.yo, noBox.w, noBox.h);
			timeFont.drawMultiLine(spriteBatch, "Yes", yesBox.xo + 5, 
					   				yesBox.ye - 10, yesBox.w - 10, 
					   				BitmapFont.HAlignment.CENTER);
			
			timeFont.drawMultiLine(spriteBatch, "No", noBox.xo + 5, 
	   								noBox.ye - 10, noBox.w - 10, 
	   								BitmapFont.HAlignment.CENTER);
			
			timeFont.setColor(Color.RED);
		}
		
		if (showHighScores) {
			drawHighScores();
		}
		spriteBatch.end();
		
		// Sleep to fix frame rate, save battery
		long renderTime = System.currentTimeMillis() - start;
		if (renderTime < FrameTime) {
			try {
				Thread.sleep(FrameTime-renderTime);
			} catch (InterruptedException e) {}
		}
	}

	@Override
	public void resize(int w, int h) {
		Minesweeper.Log.debug("Resize");
	}

	@Override
	public void resume() {
		Minesweeper.Log.debug("Resume...");
		Gdx.input.setCatchBackKey(true);
		Gdx.graphics.getGL10().glClearColor(.9f, .9f, .9f, 1f);
	}

	@Override
	public void show() {
		Minesweeper.Log.debug("Show");
		Gdx.input.setCatchBackKey(true);
		Gdx.graphics.getGL10().glClearColor(.9f, .9f, .9f, 1f);	
	}
	
	private void drawHighScores() {
		dialog.draw(spriteBatch, scoreBox.xo, scoreBox.yo, scoreBox.w, scoreBox.h);
		float y = scoreBox.ye - CellSize;
		timeFont.drawMultiLine(spriteBatch, "High Scores", scoreBox.xo, y, 
								scoreBox.w, HAlignment.CENTER);
		y -= CellSize;
		int pos = 1;
		for(Integer i:scores) {
			if (((int) time) == i) {
				dialog.draw(spriteBatch, 
							scoreBox.xo + 0.6f*CellSize, 
							y-(3*HalfCell)/2, 
							scoreBox.w - 1.2f*CellSize, 
							CellSize);
			}
			timeFont.drawMultiLine(spriteBatch, Integer.toString(pos++), 
					scoreBox.xo + 0.7f*CellSize, y, scoreBox.w, HAlignment.LEFT);
			
			timeFont.drawMultiLine(spriteBatch, i.toString(), 
					scoreBox.xo, y, scoreBox.w, HAlignment.CENTER);
			y -= CellSize;
		}
	}

	private void drawControls(float delta) {
		if (flagMode ) {
			spriteBatch.draw(pressedButton, flagBox.xo, flagBox.yo, CellSize, CellSize);
			button2.draw(spriteBatch, shovelBox.xo, shovelBox.yo, CellSize, CellSize);
			//spriteBatch.draw(button, shovelBox.xo, shovelBox.yo, CellSize, CellSize);
		} 
		else {
			spriteBatch.draw(button, flagBox.xo, flagBox.yo, CellSize, CellSize);
			spriteBatch.draw(pressedButton, shovelBox.xo, shovelBox.yo, CellSize, CellSize);
		}
		spriteBatch.draw(flag, flagBox.xo, flagBox.yo, CellSize, CellSize);		
		spriteBatch.draw(shovel, shovelBox.xo, shovelBox.yo, CellSize, CellSize);
		
		spriteBatch.draw(button, smileBox.xo, smileBox.yo, CellSize, CellSize);
		spriteBatch.draw(smiley[gameState], smileBox.xo, smileBox.yo, CellSize, CellSize);
		
		dialog.draw(spriteBatch, counterBox.xo, counterBox.yo, counterBox.w, counterBox.h);
		dialog.draw(spriteBatch, timerBox.xo, timerBox.yo, timerBox.w, timerBox.h);
		
		String text = Integer.toString(numMines);
		timeFont.drawMultiLine(spriteBatch, text, counterBox.xo + 10, 
							   counterBox.yo + tFontPad, counterBox.w - 20, 
							   BitmapFont.HAlignment.RIGHT);
		
		if (isTimerRunning && !isPaused) {
			time += delta;
		}
		text = Integer.toString((int) time);
		timeFont.drawMultiLine(spriteBatch, text, timerBox.xo + 10, 
							   timerBox.yo + tFontPad, timerBox.w - 20,
							   BitmapFont.HAlignment.RIGHT);
	}

	private void update(float delta) {
		Input in = Gdx.app.getInput();
		
		if (in.isKeyPressed(Input.Keys.BACK)) {
			isPaused = true;
			Minesweeper.Log.debug("Pause button");
			return;
		}
		
		int H = Gdx.app.getGraphics().getHeight();
		if (displayExp) { 
			dieAnimation.update(delta);
			if (dieAnimation.isDone()) displayExp = false;
		}
		
		
		if (in.justTouched()) {
			int x = in.getX();
			int y = H - in.getY();
			
			if (isPaused) {
				if (yesBox.contains(x, y)) {
					Gdx.app.exit();
				}
				else if (noBox.contains(x, y)) {
					isPaused = false;
				}
			}
			else if (smileBox.contains(x, y)) {
				clearGame();
			}
			else if (gameState == LOSE || gameState == WIN) {
				return;
			}
			else if (mineField.subContains(x,y)) {
				x = (x - mineField.xo) / CellSize;
				y = (y - mineField.yo) / CellSize;
				if (!isTimerRunning) {
					// First square touched!
					isTimerRunning = true;
					resetGame(x, y);
				}
				int value = matrix[y][x];
				if (!flagMode && value > 9 && value < 20) {
					value -= 10;
					matrix[y][x] = value;
					numSquares--;
					if (value == 0) {
						cleanNeighbours(x,y);
					}
					else if (value == 9) {
						die(x,y);
					}
					
					if (LOSE != gameState && numSquares <= 0) {
						gameState = WIN;
						isTimerRunning = false;
						if (scores.size() < MAX_SCORES || time < scores.last()) {
							scores.add((int)time);
							Integer[] values = scores.toArray(new Integer[0]);
							for (int i=0; i<6 && i<values.length; i++) {
								prefs.putInteger("score"+i, values[i]);
							}
							
							prefs.flush();
						}
						showHighScores = true;
					}
				}
				else if (flagMode && value > 9) {
					if (value >= 20) {
						value -= 10;
						numMines++;
					}
					else {
						value += 10;
						numMines--;
					}
					
					matrix[y][x] = value;
				}
			}
			else if ((!flagMode && flagBox.contains(x, y)) ||
					 ( flagMode && shovelBox.contains(x, y)) ) {
				flagMode = !flagMode;
			}
		}
	}

	private void clearGame() {
		matrix = new int[cellHeight][cellWidth];
		for (int row=0; row<cellHeight; row++) {
			for (int col=0; col<cellWidth; col++) {
				matrix[row][col] = 10; // All spaces are empty
			}
		}
		
		gameState = NEW;
		time = 0;
		isTimerRunning = false;
		displayExp = false;
		showHighScores = false;
	}
	
	/**
	 * Places the mines around the field avoiding the square that was just touched
	 * @param tx
	 * @param ty
	 */
	private void resetGame(int tx, int ty) {
		
		Minesweeper.Log.debug("Reset: "+tx+","+ty);
		int mines = initiallMines;
		
		while (mines > 0) {
			int x;
			int y;
			do { // Choose a cell not yet marked as a mine
				y = (int) (Math.random()*cellHeight);
				x = (int) (Math.random()*cellWidth);
			} while (matrix[y][x] == 19 || (x==tx && y==ty));
			matrix[y][x] = 19;
			Minesweeper.Log.debug("Mine: "+x+","+y);
			mines--;
		}
		
		
		for (int row=0; row<cellHeight; row++) {
			for (int col=0; col<cellWidth; col++) {
				matrix[row][col] = countMines(row,col) + 10;
				/* Debug
				if (matrix[row][col] == 19) {
					Minesweeper.Log.log("X:"+col+" Y:"+row);
				} */
			}
		}
		
		numMines = initiallMines;
		numSquares = cellHeight * cellWidth - numMines;
		
	}

	private void die(int mineX, int mineY) {
		gameState = LOSE;
		for (int i=0; i<matrix.length; i++) {
			for (int j=0; j<matrix[0].length; j++) {
				int value = matrix[i][j];
				if (value > 9 && value < 20) value -= 10;
				matrix[i][j] = value;
			}
		}
		displayExp = true;
		isTimerRunning = false;
			
		dieAnimation = new DieAnimation(matrix, mineX, mineY);
	}

	private void cleanNeighbours(int col, int row) {		
		int left = col-1 >= 0 ? col-1 : 0;
		int top = row-1 >= 0 ? row-1 : 0;
		int right = col+1 < cellWidth ? col+1 : col;
		int bottom = row+1 < cellHeight ? row+1 : row;
		
		for (int y=top; y<=bottom; y++) {
			for (int x=left; x<=right; x++) {
				int value = matrix[y][x]; 
				if (value >= 10 && value < 20) { 
					value -= 10;
					numSquares--;
					matrix[y][x] = value;
					if (value == 0) cleanNeighbours(x,y);
				}
			}
		}
	}
	
	/**
	 * Count the mines adjacent to the position given by row and col.
	 * PRE: ROW >= 0 && ROW < cellHeight && COL >= 0 && COL < cellWidth
	 * @param row
	 * @param col
	 * @return
	 */
	private int countMines(int row, int col) {
		if (matrix[row][col] == 19) return 9;
		
		int count = 0;
		
		int left = col-1 >= 0 ? col-1 : 0;
		int top = row-1 >= 0 ? row-1 : 0;
		int right = col+1 < cellWidth ? col+1 : col;
		int bottom = row+1 < cellHeight ? row+1 : row;
		
		for (int y=top; y<=bottom; y++) {
			for (int x=left; x<=right; x++) {
				if (matrix[y][x] == 19) 
					++count;
			}
		}
		return count;
	}

	private void loadExplosion() {
		TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("data/explosionMap"), Gdx.files.internal("data/"));
		TextureRegion[] textures = {
				atlas.findRegion("Explosion0"), 
				atlas.findRegion("Explosion1"), 
				atlas.findRegion("Explosion2"),
				atlas.findRegion("Explosion3"),
				atlas.findRegion("Explosion4"),
				atlas.findRegion("Explosion5"),
				atlas.findRegion("Explosion6"),
				atlas.findRegion("Explosion7"),
				atlas.findRegion("Explosion8"),
				atlas.findRegion("Explosion9"),
				atlas.findRegion("Explosion10"),
				atlas.findRegion("Explosion11"),
				atlas.findRegion("Explosion12"),
				atlas.findRegion("Explosion13"),
				//atlas.findRegion("Explosion14"),
				//atlas.findRegion("Explosion15"),
		};
		expAnimation = new Animation(FRAME_TIME, textures);
	}
}
