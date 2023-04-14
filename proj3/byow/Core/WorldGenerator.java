package byow.Core;


import byow.TileEngine.*;
import java.util.*;

import static byow.Core.RandomUtils.*;
import static byow.Core.Utils.*;

public class WorldGenerator {
	public static final int WIDTH = 40;
	public static final int HEIGHT = 40;

	public static final int recCount = 15;

	private Random rm;
	private TETile[][] world;

	public WorldGenerator(long seed) {
		this.rm = new Random(seed);
		this.world = new TETile[WIDTH][HEIGHT];
	}

	// Fill the map with empty space.
	public void initWorld(TETile[][] world) {
		for (int i = 0; i < world.length; i++) {
			for (int j = 0; j < world[0].length; j++) {
				world[i][j] = Tileset.NOTHING;
			}
		}
	}

	public TETile[][] nextWorld() {
		initWorld(world);
		List<Rectangle> recs = new ArrayList<>();
		for (int i = 0; i < recCount; i++) {
			// @Note: The width and height here is of rectangle, not the map.
			int width = uniform(rm, 4, 10);
			int height = uniform(rm, 4, 10);

			int x = uniform(rm, 2, WIDTH-width - 2);
			int y = uniform(rm, 2, HEIGHT-height - 2);
			recs.add(new Rectangle(width, height, x, y, Tileset.FLOOR));
		}
		drawRecs(recs);

		// Two arrays: connected and unconnected.
		List<Rectangle> unconnected = new ArrayList<>();
		List<Rectangle> connected = new ArrayList<>();
		unconnected.addAll(recs);
		Rectangle firstConnected = unconnected.remove(0);
		connected.add(firstConnected);
		while (unconnected.size() > 0) {
			Rectangle recFrom = unconnected.remove(0);
			Rectangle recTo = connected.get(uniform(rm, connected.size()));
			drawPath(getRandomCoor(recFrom), getRandomCoor(recTo), Tileset.FLOOR);
			connected.add(recFrom);
		}
		drawWalls();
		return world;
	}

	public TETile[][] nextWorld(int width, int height) {
		this.world = new TETile[width][height];
		initWorld(world);
		List<Rectangle> recs = new ArrayList<>();
		for (int i = 0; i < recCount; i++) {
			// @Note: The width and height here is of rectangle, not the map.
			int recWidth = uniform(rm, 4, 10);
			int recHeight = uniform(rm, 4, 10);

			int x = uniform(rm, 2, width-recWidth - 2);
			int y = uniform(rm, 2, height-recHeight - 2);
			recs.add(new Rectangle(recWidth, recHeight, x, y, Tileset.FLOOR));
		}
		drawRecs(recs);

		// Two arrays: connected and unconnected.
		List<Rectangle> unconnected = new ArrayList<>();
		List<Rectangle> connected = new ArrayList<>();
		unconnected.addAll(recs);
		Rectangle firstConnected = unconnected.remove(0);
		connected.add(firstConnected);
		while (unconnected.size() > 0) {
			Rectangle recFrom = unconnected.remove(0);
			Rectangle recTo = connected.get(uniform(rm, connected.size()));
			drawPath(getRandomCoor(recFrom), getRandomCoor(recTo), Tileset.FLOOR);
			connected.add(recFrom);
		}
		drawWalls();
		return world;
	}

	private void drawRecs(List<Rectangle> recs) {
		for (Rectangle rec : recs) {
			drawRec(rec);
		}
	}

	private void drawRec(Rectangle rec) {
		for (int i = 0; i < rec.width; i++) {
			for (int j = 0; j < rec.height; j++) {
				// @Robustness: It seems better to try catch and index out of arry exception
				// then throw a more detailed error message.
				if (rec.getX() + i >= world.length || rec.getY() + j >= world[0].length) {
					throw new RuntimeException("The tile" + axisString(rec.getX(), rec.getY()) + "you want to draw is outside of the given map" + mapSizeString(world));
				}
				world[rec.getX() + i][rec.getY() + j] = Tileset.FLOOR;
			}
		}
	}

	private Coor getRandomCoor(Rectangle rec) {
		int x = rec.getX() + uniform(rm, rec.width);
		int y = rec.getY() + uniform(rm, rec.height);
		return new Coor(x, y);
	}

	// Generate a path for connecting two points in the map with given tile.
	private void drawPath(Coor from, Coor to, TETile tile) {
		// First draw horizontal path.
		int horizontalPathLen = Math.abs(from.x - to.x);
		int curX = from.x;
		int curY = from.y;
		int targetX = to.x;
		int targetY = to.y;

		if (curX < targetX) {
			for (int i = 0 ; i < horizontalPathLen; i++) {
				world[curX++][curY] = tile;
			}
		} else {
			for (int i = 0 ; i < horizontalPathLen; i++) {
				world[curX--][curY] = tile;
			}
		}
		// Then draw vertical path.
		int verticalPathLen = Math.abs(from.y - to.y);
		if (curY < targetY) {
			for (int i = 0 ; i < verticalPathLen; i++) {
				world[curX][curY++] = tile;
			}
		} else {
			for (int i = 0 ; i < verticalPathLen; i++) {
				world[curX][curY--] = tile;
			}
		}
	}

	public void drawWalls() {
		for (int i = 1; i < world.length - 1; i++) {
			for (int j = 1; j < world[0].length - 1; j++) {
				if (world[i][j] == Tileset.FLOOR) {
					continue;
				}
				if (world[i - 1][j] == Tileset.FLOOR || world[i + 1][j] == Tileset.FLOOR ||
					world[i][j - 1] == Tileset.FLOOR || world[i][j + 1] == Tileset.FLOOR) {
					world[i][j] = Tileset.WALL;
				}
			}
		}
	}


}
