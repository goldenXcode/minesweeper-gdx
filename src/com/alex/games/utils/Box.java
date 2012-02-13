package com.alex.games.utils;

public class Box {
	public final int xo;
	public final int yo;
	public final int xe;
	public final int ye;
	public final int w;
	public final int h;
	/**
	 * 
	 * @param xMin
	 * @param xMax
	 * @param yMin
	 * @param yMax
	 */
	public Box(int xMin, int xMax, int yMin, int yMax) {
		if (xMin <= xMax) {
			xo = xMin;
			xe = xMax;
		}
		else {
			xo = xMax;
			xe = xMin;
		}
		w = xe - xo;
		
		if (yMin <= yMax) {
			yo = yMin;
			ye = yMax;
		}
		else {
			yo = yMax;
			ye = yMin;
		}
		h = ye - yo;
	}
	
	/**
	 * Evaluates if the point is contained in this Box. Margins are considered in
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean contains(int x, int y) {
		return  x >= xo && x <= xe && y >= yo && y <= ye;
	}
	
	/**
	 * Evaluates if the point is contained in this Box. Strict contains is applied. Margins are considered out
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean subContains(int x, int y) {
		return  x > xo && x < xe && y > yo && y < ye;
	}
}
