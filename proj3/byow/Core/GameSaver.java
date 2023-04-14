package byow.Core;

import byow.TileEngine.*;
import java.util.*;
import java.io.*;

public class GameSaver {
	public static final File dir = new File(System.getProperty("user.dir"));

	TETile[][] world;

	public void save() {
		String info = null;
		try {
			File gameRecord = new File(dir, "record.txt");
			FileWriter fw = new FileWriter(gameRecord);
			fw.write(info);
			fw.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
