package byow.Core;

import byow.TileEngine.*;

public class Rectangle {
	public final Coor coor;
	public final int width;
	public final int height;
	public final TETile tile;

	public Rectangle(int width, int height, int x, int y, TETile tile) {
		this.width = width;
		this.height = height;
		this.coor = new Coor(x, y);
		this.tile = tile;
	}

	public int getX() {
		return coor.x;
	}

	public int getY() {
		return coor.y;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Rectangle rectangle = (Rectangle) o;

		if (width != rectangle.width) return false;
		if (height != rectangle.height) return false;
		return coor.equals(rectangle.coor);
	}

	@Override
	public int hashCode() {
		int result = coor.hashCode();
		result = 31 * result + width;
		result = 31 * result + height;
		return result;
	}
}
