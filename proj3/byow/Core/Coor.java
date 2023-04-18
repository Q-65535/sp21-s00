package byow.Core;

import static byow.Core.Utils.*;

public class Coor {
	public int x;
	public int y;

	public Coor(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString() {
		return axisString(this.x, this.y);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Coor coor = (Coor) o;

		if (x != coor.x) return false;
		return y == coor.y;
	}

	@Override
	public int hashCode() {
		int result = x;
		result = 31 * result + y;
		return result;
	}

	public static Coor parseCoor(String s) {
		StringBuilder xstr = new StringBuilder();
		StringBuilder ystr = new StringBuilder();
		int index = 0;
		boolean isFirstNumber = true;

		for (; index < s.length(); index++) {
			if (Character.isDigit(s.charAt(index))) {
				if (isFirstNumber) {
					xstr.append(s.charAt(index));
				} else {
					ystr.append(s.charAt(index));
				}
			} else if (s.charAt(index) == ',') {
				isFirstNumber = false;
			}
		}
		int x = Integer.parseInt(xstr.toString());
		int y = Integer.parseInt(ystr.toString());
		return new Coor(x, y);
	}
}
