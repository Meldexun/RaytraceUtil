package meldexun.raytraceutil;

public enum Direction {

	WEST,
	EAST,
	DOWN,
	UP,
	NORTH,
	SOUTH;

	static {
		WEST.opposite = EAST;
		EAST.opposite = WEST;
		DOWN.opposite = UP;
		UP.opposite = DOWN;
		NORTH.opposite = SOUTH;
		SOUTH.opposite = NORTH;
	}

	public static final Direction[] ALL = Direction.values();
	public static final Direction[] HORIZONTAL = {
			WEST,
			EAST,
			NORTH,
			SOUTH };
	public static final Direction[] VERTICAL = {
			DOWN,
			UP };

	private Direction opposite;

	public Direction opposite() {
		return this.opposite;
	}

	public static Direction valueOf(int ordinal) {
		return ALL[ordinal];
	}

	public static Direction valueOf(double x, double y, double z) {
		double x1 = Math.abs(x);
		double y1 = Math.abs(y);
		double z1 = Math.abs(z);
		if (x1 >= y1) {
			if (x1 >= z1) {
				return x < 0.0D ? WEST : EAST;
			} else {
				return z < 0.0D ? NORTH : SOUTH;
			}
		} else if (y1 >= z1) {
			return y < 0.0D ? DOWN : UP;
		} else {
			return z < 0.0D ? NORTH : SOUTH;
		}
	}

}
