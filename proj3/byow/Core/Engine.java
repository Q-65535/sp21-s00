package byow.Core;

import byow.TileEngine.*;
import edu.princeton.cs.introcs.StdDraw;
import java.awt.*;

public class Engine {
	enum GameState {
		INIT,
		READSEED,
		OPERATE,
	}

    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 80;


	private Coor avatarPosition;
	private GameState gameState;
	private TETile[][] world;


	public Engine() {
		this.gameState = GameState.INIT;
	}

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
		StringBuilder seedStrBuilder = new StringBuilder();
		ter.initialize(WIDTH, HEIGHT);
		StdDraw.setPenRadius(0.02);
		StdDraw.setPenColor(230, 60, 57);
		StdDraw.text(WIDTH/2, HEIGHT/2, "Press N to start inputing a seed");
		StdDraw.show();
		while (true) {
			if (!StdDraw.hasNextKeyTyped()) {
				continue;
			}
			char typed =  StdDraw.nextKeyTyped();
			switch(gameState) {
			case INIT: {
				if (typed == 'N') {
					gameState = GameState.READSEED;
					StdDraw.clear(new Color(0, 0, 0));
					StdDraw.text(WIDTH*0.5, HEIGHT*0.8, "Enter the seed number (ends with S):");
					StdDraw.show();
				}
				break;
			}
			case READSEED: {
				if (Character.isDigit(typed)) {
					seedStrBuilder.append(typed);
					StdDraw.clear(new Color(0, 0, 0));
					StdDraw.text(WIDTH*0.5, HEIGHT*0.8, "Enter the seed number (ends with S):");
					StdDraw.text(WIDTH/2, HEIGHT/2, seedStrBuilder.toString());
					StdDraw.show();
				} else if (typed == 'S') {
					String seedStr = seedStrBuilder.toString();
					long seed = Long.parseLong(seedStr);
					WorldGenerator gen = new WorldGenerator(seed);
					world = gen.nextWorld(WIDTH, HEIGHT);
					initAvatar();
					world[avatarPosition.x][avatarPosition.y] = Tileset.AVATAR;
					gameState = GameState.OPERATE;
					ter.renderFrame(world);
				} else {
					throw new RuntimeException("the seed input is invalid");
				}
				break;
			}
			case OPERATE: {
				move(typed);
				ter.renderFrame(world);
				break;
			}
			}
		}
    }

	private void move(char action) {
		if (world == null) {
			throw new RuntimeException("Cannot move, the world is not initialized yet");
		}
		clear(avatarPosition.x, avatarPosition.y, Tileset.FLOOR);
		switch (action) {
		case 'w': {
			if (world[avatarPosition.x][avatarPosition.y + 1] == Tileset.FLOOR) {
				avatarPosition.y++;
			}
			break;
		}
		case 's': {
			if (world[avatarPosition.x][avatarPosition.y - 1] == Tileset.FLOOR) {
				avatarPosition.y--;
			}
			break;
		}
		case 'd': {
			if (world[avatarPosition.x + 1][avatarPosition.y] == Tileset.FLOOR) {
				avatarPosition.x++;
			}
			break;
		}
		case 'a': {
			if (world[avatarPosition.x - 1][avatarPosition.y] == Tileset.FLOOR) {
				avatarPosition.x--;
			}
			break;
		}
		}
		world[avatarPosition.x][avatarPosition.y] = Tileset.AVATAR;
	}

	private void initAvatar() {
		if (world == null) {
			throw new RuntimeException("Cannot initialize avatar, the world is not initialized yet");
		}
		for (int i = 0; i < WIDTH; i++) {
			for (int j = 0; j < HEIGHT; j++) {
				if (world[i][j] == Tileset.FLOOR) {
					avatarPosition = new Coor(i, j);
				}
			}
		}
	}

	private void clear(int x, int y, TETile tile) {
		world[x][y] = tile;
	}


    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        // TODO: Fill out this method so that it run the engine using the input
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        //
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for many different input types.
		if (input.charAt(0) != 'N' || input.charAt(input.length() - 1) != 'S'
			|| input.charAt(0) != 'n' || input.charAt(input.length() - 1) != 's') {
			throw new RuntimeException("The input string is not valid");
		}
		String numberStr = input.substring(1, input.length() - 1);
        int seed = Integer.parseInt(numberStr);
		WorldGenerator gen = new WorldGenerator(seed);
        TETile[][] finalWorldFrame = gen.nextWorld();
        return finalWorldFrame;
    }
}
