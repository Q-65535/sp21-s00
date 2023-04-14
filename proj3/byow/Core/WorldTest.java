package byow.Core;

import java.util.*;
import byow.TileEngine.*;
import org.junit.Test;

import static byow.Core.RandomUtils.*;
import static byow.Core.Utils.*;

public class  WorldTest{
	public static final int WIDTH = 40;
	public static final int HEIGHT = 40;

    @Test
    public void testRandomWorld() throws InterruptedException {
        TETile[][] world = new TETile[WIDTH][HEIGHT];
		TERenderer ter = new TERenderer();
		ter.initialize(WIDTH, HEIGHT);
		initWorld(world);
		while (true) {
			List<Rectangle> recs = new ArrayList<>();
			for (int i = 0; i < 15; i++) {
				// @Note: The width and height here is of rectangle, not the map.
				int width = uniform(rm, 4, 10);
				int height = uniform(rm, 4, 10);

				int x = uniform(rm, 2, WIDTH-width - 2);
				int y = uniform(rm, 2, HEIGHT-height - 2);
				recs.add(new Rectangle(width, height, x, y, Tileset.FLOOR));
			}
			drawRecs(recs, world);
			// two arrays: connected and unconnected.
			List<Rectangle> unconnected = new ArrayList<>();
			List<Rectangle> connected = new ArrayList<>();
			unconnected.addAll(recs);
			Rectangle firstConnected = unconnected.remove(0);
			connected.add(firstConnected);
			while (unconnected.size() > 0) {
				Rectangle recFrom = unconnected.remove(0);
				Rectangle recTo = connected.get(uniform(rm, connected.size()));
				drawPath(getRandomCoor(recFrom), getRandomCoor(recTo), Tileset.FLOOR, world);
				connected.add(recFrom);
			}
			fillWalls(world);

			ter.renderFrame(world);
			initWorld(world);
			Thread.sleep(2000);
//			Thread.sleep(0);
		}
    }

	@Test
	public void testGenerator() throws InterruptedException {
		WorldGenerator gen = new WorldGenerator(1);
		TERenderer ter = new TERenderer();
		ter.initialize(WIDTH, HEIGHT);
		while (true) {
			TETile[][] world = gen.nextWorld();
			ter.renderFrame(world);
			Thread.sleep(2000);
		}
	}

	@Test
	public void testPath() throws InterruptedException {
		String dir = System.getProperty("user.dir");
		System.out.println(dir);
	}
}
