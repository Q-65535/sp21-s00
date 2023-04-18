package byow.Core;

import byow.TileEngine.*;
import edu.princeton.cs.introcs.StdDraw;
import java.awt.*;
import java.io.*;
import java.util.*;

public class Engine {
	enum GameState {
		INIT,
		READSEED,
		OPERATE,
		COLON,
		END,
	}

	public static final File dir = new File(System.getProperty("user.dir"));
    /* Feel free to change the width and height. */
    public static final int WIDTH = 40;
    public static final int HEIGHT = 40;


    TERenderer ter = new TERenderer();
	private Coor avatarPosition;
	private GameState gameState;
	private long seed;
	private TETile[][] world;
	StringBuilder seedStrBuilder = new StringBuilder();


	public Engine() {
		this.gameState = GameState.INIT;
	}

	// Maybe not necessary to have all these getters?
	public GameState getGameState() {
		return this.gameState;
	}

	public long getSeed() {
		return this.seed;
	}

	public Coor getAvatarPosition() {
		return this.avatarPosition;
	}

	public TETile[][] getWorld() {
		return this.world;
	}

	@Override
	public String toString() {
		if (world != null) {
			return TETile.toString(world);
		} else {
			return "the world does not exist yet, cannot get the string representation.";
		}
	}

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
		ter.initialize(WIDTH, HEIGHT);
		StdDraw.setPenColor(230, 60, 57);
		while (gameState != GameState.END) {
			// State may transit if the user press a key.
			if (StdDraw.hasNextKeyTyped()) {
				char typed = StdDraw.nextKeyTyped();
				stateTransit(typed);
			}
			// Whatever happens, the content must be displayed.
			displayContent();
		}
		System.exit(0);
	}

	// State transit according to the user input char.
	private void stateTransit(char typed) {
		switch(gameState) {
		case INIT: {
			if (typed == 'N' || typed == 'n') {
				gameState = GameState.READSEED;
			} else if (typed == 'L' || typed == 'l') {
				load();
				gameState = GameState.OPERATE;
			}
			break;
		}
		case READSEED: {
			if (Character.isDigit(typed)) {
				seedStrBuilder.append(typed);
			} else if (typed == 'S' || typed == 's') {
				String seedStr = seedStrBuilder.toString();
				this.seed = Long.parseLong(seedStr);
				WorldGenerator gen = new WorldGenerator(seed);
				world = gen.nextWorld(WIDTH, HEIGHT);
				initAvatar();
				gameState = GameState.OPERATE;
			} else {
				throw new RuntimeException("the seed input is invalid");
			}
			break;
		}
		case OPERATE: {
			if (typed == ':') {
				gameState = GameState.COLON;
			} else {
				move(typed);
			}
			break;
		}
		case COLON: {
			if (typed == 'Q' || typed == 'q') {
				gameState = GameState.END;
				save();
			} else {
				gameState = GameState.OPERATE;
			}
			break;
		}
		}
	}

	// Display what should be displayed in current game state.
	public void displayContent() {
		StdDraw.clear(new Color(0, 0, 0));
		if (world != null) {
			ter.drawFrame(world);
		}
		StdDraw.setPenColor(230, 60, 57);
		drawHUD();
		switch(gameState) {
		case INIT: {
			StdDraw.text(WIDTH*0.5, HEIGHT*0.8, "Welcome to my game!!!");
			StdDraw.text(WIDTH/2, HEIGHT/2, "Press N to start inputing a seed");
			StdDraw.text(WIDTH/2, HEIGHT/2.2, "Press L to load the previous game");

			break;
		}
		case READSEED: {
			StdDraw.text(WIDTH*0.5, HEIGHT*0.8, "Enter the seed number (ends with S):");
			StdDraw.text(WIDTH/2, HEIGHT/2, seedStrBuilder.toString());
			break;
		}
		case OPERATE: {
			StdDraw.text(WIDTH*0.1, HEIGHT*0.03, "operating");
			break;
		}
		case COLON: {
			StdDraw.text(WIDTH*0.1, HEIGHT*0.03, "ready to quit (press q)?");
			break;
		}
		}
		StdDraw.show();
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
					world[avatarPosition.x][avatarPosition.y] = Tileset.AVATAR;
					return;
				}
			}
		}
	}

	private void drawHUD() {
		int mouseX = (int)StdDraw.mouseX();
		int mouseY = (int)StdDraw.mouseY();
		if (world != null) {
			// Prevent out of bounds.
			if (mouseX < WIDTH &&mouseY < HEIGHT) {
				StdDraw.text(WIDTH*0.1, HEIGHT*0.9, world[mouseX][mouseY].description());
			}
			StdDraw.text(WIDTH*0.12, HEIGHT*0.95, "(" + StdDraw.mouseX() + ", " + StdDraw.mouseY() + ")");
		}
	}

	private void clear(int x, int y, TETile tile) {
		world[x][y] = tile;
	}

	private void save() {
		try {
			// Save world state.
			File worldStateRecord = new File(dir, "world.txt");
			FileWriter fw = new FileWriter(worldStateRecord);
			BufferedWriter bfw = new BufferedWriter(fw);
			bfw.append(TETile.toString(world));
			bfw.close();

			// Save metadata.
			// @Repetition: too many repetitions?
			File metaRecord = new File(dir, "meta.txt");
			fw = new FileWriter(metaRecord);
			bfw = new BufferedWriter(fw);
			// Save game seed.
			bfw.append("seed: ");
			bfw.append(String.valueOf(seed));
			bfw.append("\n");
			// Save avatar position.
			bfw.append("avatar position: ");
			bfw.append(avatarPosition.toString());
			bfw.append("\n");
			bfw.close();
		} catch (IOException e) {
			System.out.println("There was a problem when saving");
			System.out.println(e);
		}
	}

	private void load() {
		try {
			// Read metadata.
			File worldStateRecord = new File(dir, "world.txt");
			File metaRecord = new File(dir, "meta.txt");
			if (!worldStateRecord.exists() || !metaRecord.exists()) {
				System.exit(0);
			}
			Scanner sc = new Scanner(metaRecord);
			while (sc.hasNextLine()) {
				String record = sc.nextLine();
				retrieveRecord(record);
			}
			WorldGenerator gen = new WorldGenerator(seed);
			world = gen.nextWorld(WIDTH, HEIGHT);
			world[avatarPosition.x][avatarPosition.y] = Tileset.AVATAR;

			// Read world.
			sc = new Scanner(worldStateRecord);
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				for (int i = 0; i < line.length(); i++) {
					
				}
			}


		} catch (IOException e) {
			System.out.println("There was a problem when loading");
			System.out.println(e);
		}
	}

	private void retrieveRecord(String record) {
		String[] split = record.split(": ");
		if (split.length != 2) {
			throw new RuntimeException("The meta data format is incorrect");
		}
		String identifier = split[0];
		String data = split[1];
		switch (identifier) {
		case "seed": {
			seed = Long.parseLong(data);
			break;
		}
		case "avatar position": {
			this.avatarPosition = Coor.parseCoor(data);
			break;
		}
		}
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
		for (int i = 0; i < input.length(); i++) {
			if (gameState != GameState.END) {
				stateTransit(input.charAt(i));
			} else {
				break;
			}
		}
		return world;

		// char firstChar = input.charAt(0);
		// char lastChar = input.charAt(input.length() - 1);
		// if ((firstChar != 'N' && firstChar != 'n')
		// 	|| lastChar != 'S' && lastChar != 's') {
		// 	throw new RuntimeException("The input string is not valid");
		// }
		// String numberStr = input.substring(1, input.length() - 1);
        // long seed = Long.parseLong(numberStr);
		// WorldGenerator gen = new WorldGenerator(seed);
        // TETile[][] finalWorldFrame = gen.nextWorld();
        // return finalWorldFrame;
    }
}
