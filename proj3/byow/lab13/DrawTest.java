package byow.lab13;

import java.awt.*;
import java.util.*;
import byow.TileEngine.*;
import edu.princeton.cs.introcs.StdDraw;
import org.junit.Test;

public class DrawTest {
	@Test
	public void testDraw() throws InterruptedException {
		while (true) {
			StdDraw.setPenRadius(0.5);
			StdDraw.setPenColor(191, 42, 100);
			StdDraw.setFont(new Font("Arial", Font.BOLD, 40));
			StdDraw.text(0.5, 0.5, "hello");
			Thread.sleep(1000);
			StdDraw.clear();
			Thread.sleep(500);
		}
	}

	@Test
	public void testGame() {
		MemoryGame game = new MemoryGame(30, 30, 1);
		 game.startGame();
	}
}
