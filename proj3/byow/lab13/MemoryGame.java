package byow.lab13;

import byow.Core.RandomUtils;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.Color;
import java.awt.Font;
import java.util.Random;

public class MemoryGame {
    /** The width of the window of this game. */
    private int width;
    /** The height of the window of this game. */
    private int height;
    /** The current round the user is on. */
    private int round;
    /** The Random object used to randomly generate Strings. */
    private Random rand;
    /** Whether or not the game is over. */
    private boolean gameOver;
    /** Whether or not it is the player's turn. Used in the last section of the
     * spec, 'Helpful UI'. */
    private boolean playerTurn;
    /** The characters we generate random Strings from. */
    private static final char[] CHARACTERS = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    /** Encouraging phrases. Used in the last section of the spec, 'Helpful UI'. */
    private static final String[] ENCOURAGEMENT = {"You can do this!", "I believe in you!",
                                                   "You got this!", "You're a star!", "Go Bears!",
                                                   "Too easy for you!", "Wow, so impressive!"};

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please enter a seed");
            return;
        }

        long seed = Long.parseLong(args[0]);
        MemoryGame game = new MemoryGame(40, 40, seed);
        game.startGame();
    }

    public MemoryGame(int width, int height, long seed) {
        /* Sets up StdDraw so that it has a width by height grid of 16 by 16 squares as its canvas
         * Also sets up the scale so the top left is (0,0) and the bottom right is (width, height)
         */
        this.width = width;
        this.height = height;
        StdDraw.setCanvasSize(this.width * 16, this.height * 16);
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
		StdDraw.setPenRadius(0.01);
		StdDraw.setPenColor(230, 60, 57);
        StdDraw.setXscale(0, this.width);
        StdDraw.setYscale(0, this.height);
        StdDraw.clear(Color.GRAY);
        StdDraw.enableDoubleBuffering();

		
		this.rand = new Random(seed);
    }

    public String generateRandomString(int n) {
		StringBuilder sb = new StringBuilder(n);
		for (int i = 0; i < n; i++) {
			int index = rand.nextInt(CHARACTERS.length);
			sb.append(CHARACTERS[index]);
		}
        return sb.toString();
    }

    public void drawFrame(String s) {
		StdDraw.text(this.width/2, this.height/2, s);
		StdDraw.line(0, height * 0.92, width, height * 0.92);
		String middleText = null;
		middleText = playerTurn ? "请输入" : "注意看！";
		StdDraw.text(width/2, height * 0.95, middleText);
		StdDraw.text(width * 0.12, height * 0.95, "第" + String.valueOf(round) + "回合");
		StdDraw.show();
    }

    public void flashSequence(String letters) {
        for (int i = 0; i < letters.length(); i++) {
            // sleep(500);
            char nextChar = letters.charAt(i);
            drawFrame(String.valueOf(nextChar));
            sleep(1000);
            StdDraw.clear(Color.GRAY);
            StdDraw.show();
        }
    }

    public String solicitNCharsInput(int n) {
		int count = 0;
        StringBuilder sb = new StringBuilder();
		while (count < n) {
			if (StdDraw.hasNextKeyTyped()) {
                StdDraw.setPenColor(Color.GRAY);
                StdDraw.text(this.width/2, this.height/3, sb.toString());
				sb.append(StdDraw.nextKeyTyped());
                StdDraw.setPenColor(230, 60, 57);
                StdDraw.text(this.width/2, this.height/3, sb.toString());
                StdDraw.show();
				count++;
			}
		}
        sleep(200);
		return sb.toString();
    }

    public void startGame() {
		String letter = null;
		while (!gameOver) {
			if (!playerTurn) {
				round++;
				StdDraw.clear(Color.GRAY);
				StdDraw.show();
				letter = generateRandomString(round);
				flashSequence(letter);
				// sleep(500);
			} else {
				StdDraw.clear(Color.GRAY);
				StdDraw.show();
				drawFrame("轮到你啦！");
				String inputStr = solicitNCharsInput(round);
				if (!inputStr.equals(letter)) {
					gameOver = true;
				} else {
					StdDraw.clear(Color.GRAY);
					drawFrame("回答正确！");
					StdDraw.show();
					sleep(500);
				}
			}
			playerTurn = !playerTurn;
		}
        StdDraw.clear(Color.GRAY);
        StdDraw.show();
        StdDraw.text(this.width/2, this.height/2, "游戏结束:)");
        StdDraw.show();
        sleep(500);
		System.out.println("GAME OVER!");
    }

	public void sleep(long millisecond) {
		try {
			Thread.sleep(millisecond);
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
