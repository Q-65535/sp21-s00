package byow.Core;

import byow.TileEngine.*;

import static byow.Core.RandomUtils.*;
import java.util.*;

public class Utils {
	// @TODO: specify random seed by user.
	public static Random rm = new Random();

	public static void drawRecs(List<Rectangle> recs, TETile[][] world) {
		for (Rectangle rec : recs) {
			drawRec(rec, world);
		}
	}

	public static void drawRec(Rectangle rec, TETile[][] world) {
		for (int i = 0; i < rec.width; i++) {
			for (int j = 0; j < rec.height; j++) {
				// @Robustness: It seems better to try catch and index out of arry exception
				// then throw a more detailed error message.
				if (rec.getX() + i >= world.length || rec.getY() + j >= world[0].length) {
					throw new RuntimeException("The tile" + axisString(rec.getX(), rec.getY()) + "you want to draw is outside of the given map" + mapSizeString(world));
				}
				world[rec.getX() + i][rec.getY() + j] = rec.tile;
			}
		}
	}

	// Generate a path for connecting two points in the map with given tile.
	public static void drawPath(Coor from, Coor to, TETile tile, TETile[][] world) {
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

	public static Coor getRandomCoor(Rectangle rec) {
		int x = rec.getX() + uniform(rm, rec.width);
		int y = rec.getY() + uniform(rm, rec.height);
		return new Coor(x, y);
	}

	public static void fillWalls(TETile[][] world) {
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

	// Fill the map with empty space.
	public static void initWorld(TETile[][] world) {
		for (int i = 0; i < world.length; i++) {
			for (int j = 0; j < world[0].length; j++) {
				world[i][j] = Tileset.NOTHING;
			}
		}
	}

    public static String axisString(int x, int y) {
        return "(" + x + ", " + y + ")";
    }

    public static String mapSizeString(TETile[][] world) {
        return "(" + world.length + "X" + world[0].length + ")";
    }
}
