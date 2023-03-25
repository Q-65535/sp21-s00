package byow.lab12;
import org.junit.Test;
import static org.junit.Assert.*;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {
    private static final int WIDTH = 50;
    private static final int HEIGHT = 50;
	private static Random rm = new Random();

	public static void addHexgon(int size, int row, int col, TETile tile, TETile[][] world) {
		drawUpperShape(size, row, col, tile, world);
		drawLowerShape(size, row + size, col, tile, world);
	}

	private static void drawUpperShape(int size, int row, int col, TETile tile, TETile[][] world) {
		// We currently allow overlapping and out of bounds.
		int width = size + 2 *(size - 1);
		int height = size;
		// Imagine a regtangle surround the hexgon....
		// And the position is the upper-left corner of the hexgon.

		// Draw upper shape
		for (int i = 1; i <= height; i++) {
			for (int j = 1; j <= width; j++) {
				int tileRow = row + i - 1;
				int tileCol = col + j - 1;
				if (tileRow < 0 || tileRow >= world.length) {
					continue;
				}
				if (tileCol < 0 || tileCol >= world[0].length) {
					continue;
				}
				if (j <= size - i || j >= size * 2 - 1 + i) {
					continue;
				}
				world[tileRow][tileCol] = tile;
			}
		}
	}

	private static void drawLowerShape(int size, int row, int col, TETile tile, TETile[][] world) {
	// Imagine a regtangle surround the half down hexgon....
	// And the position is the upper-left corner of the hexgon.
		int width = size + 2 *(size - 1);
		int height = size;

		// Draw lower shape
		for (int i = 1; i <= height; i++) {
			for (int j = 1; j <= width; j++) {
				int tileRow = row + i - 1;
				int tileCol = col + j - 1;
				if (tileRow < 0 || tileRow >= world.length) {
					continue;
				}
				if (tileCol < 0 || tileCol >= world[0].length) {
					continue;
				}
				if (j < i || j > width + 1 - i) {
					continue;
				}
				world[tileRow][tileCol] = tile;
			}
		}

	}

    private static TETile randomTile() {
        int tileNum = rm.nextInt(6);
        switch (tileNum) {
            case 0: return Tileset.WALL;
            case 1: return Tileset.FLOWER;
            case 2: return Tileset.AVATAR;
			case 3: return Tileset.MOUNTAIN;
			case 4: return Tileset.SAND;
			case 5: return Tileset.WATER;
            default: return Tileset.NOTHING;
        }
    }

	public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);
        // initialize tiles
        TETile[][] world = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }

		
		for (int i = 0; i < 30; i++) {
			int size = rm.nextInt(8);
			int x = rm.nextInt(WIDTH - 10);
			int y = rm.nextInt(HEIGHT - 10);
			TETile randomTile = randomTile();
			addHexgon(size, y, x, randomTile, world);
		}

        ter.renderFrame(world);
	}
}
