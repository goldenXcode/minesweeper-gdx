package com.alex.games.minesweeper;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.alex.games.minesweeper.screen.GameScreen;

public class DieAnimation {
	private LinkedList<Mine> mines;
	private int numMines = 1;
	private static final float MAX_TIME = GameScreen.FRAME_TIME * GameScreen.EXPLOSION_FRAMES;
	private float updateTime = 0.8f;
	private float currentTime = 0f;
	private int[][] matrix;
	
	public class Mine {
		public final int x;
		public final int y;	
		public float timeState;
		public Mine(int x, int y) { this.x = x; this.y = y; }
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("X:").append(x).append(" Y:").append(y);
			return sb.toString();
		}
	}
	
	public DieAnimation(int[][] matrix, int mineX, int mineY) {
		mines = new LinkedList<Mine>();
		mines.add(new Mine(mineX, mineY));
		findMines(mineX, mineY, matrix);
		matrix[mineY][mineX] = -1;
		this.matrix = matrix;
	}

	/**
	 * @param col
	 * @param row
	 * @param matrix
	 */
	private void findMines(int col, int row, int[][] matrix) {
		int left = col - 1;
		int right = col + 1;
		int top = row + 1;
		int bottom = row - 1;
		
		boolean useLeft = left >= 0;
		boolean useRight = right < matrix[0].length;
		boolean useTop = top < matrix.length;
		boolean useBottom = bottom >= 0;
		
		if (!useLeft) left = 0;
		if (!useRight) right = matrix[0].length-1;
		if (!useBottom) bottom = 0;
		if (!useTop) top = matrix.length;
		
		// While at least one side is still in
		while (useLeft || useRight || useTop || useBottom) {
			for (int i=left; i<=right; i++) {
				if (useTop && matrix[top][i] == 9) {
					mines.add(new Mine(i,top));
				}
				if (useBottom && matrix[bottom][i] == 9) {
					mines.add(new Mine(i, bottom));
				}
			}
			
			int low = useBottom? bottom+1:bottom;
			for (int i=low; i<top; i++) {
				if (useLeft && matrix[i][left] == 9) {
					mines.add(new Mine(left, i));
				}
				if (useRight && matrix[i][right] == 9) {
					mines.add(new Mine(right, i));
				}
			}
						
			if (useLeft) {
				left--;
				if (left < 0) {
					left = 0;
					useLeft = false;
				}
			}
			if (useRight) {
				right++;
				if (right >= matrix[0].length) {
					useRight = false;
					right = matrix[0].length-1;
				}
			}
			if (useTop) {
				top++;
				if (top >= matrix.length) {
					top = matrix.length;
					useTop = false;
				}
			}
			if (useBottom) {
				bottom--;
				if (bottom < 0) {
					useBottom = false;
					bottom = 0;
				}
			}
		}
		/*
		System.out.println("-----------------------");
		for (Mine m:mines) {
			System.out.println(m);
		}
		System.out.println("-----------------------");
		*/
	}
	
	/*
	private void print(int left, int right, int top, int bottom) {
		System.out.println("L: "+left+" R:"+right+" T:"+top+" B:"+bottom );
		
	}
*/
	public void update(float deltaTime) {
		currentTime += deltaTime;
		if (currentTime >= updateTime) {
			currentTime = 0f;
			updateTime -= GameScreen.FRAME_TIME;
			numMines++;
		}
		
		int count = 0;
		Iterator<Mine> it = mines.iterator();
		while (it.hasNext() && count < numMines) {
			Mine m = it.next();
			m.timeState += deltaTime;
			count++;
			if (m.timeState > MAX_TIME) {
				it.remove();
			}
			matrix[m.y][m.x] = -1;
		}
	}
	
	public List<Mine> getMines() {
		List<Mine> result = new LinkedList<Mine>();
		int count = 0;
		for (Mine m:mines) {
			if (count < numMines) {
				result.add(m);
				count++;
			}
		}
		return result;
	}
	
	public boolean isDone() {
		return mines.isEmpty();
	}
}
