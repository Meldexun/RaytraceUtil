package meldexun.raytraceutil;

public class RayTracingEngine {

	@FunctionalInterface
	public interface PositionPredicate {

		boolean isOpaque(int x, int y, int z);

	}

	private final PositionPredicate positionPrediacte;

	public RayTracingEngine(PositionPredicate positionPrediacte) {
		this.positionPrediacte = positionPrediacte;
	}

	public MutableRayTraceResult rayTraceBlocks(double startX, double startY, double startZ, double endX, double endY, double endZ, boolean ignoreStart,
			double threshold, MutableRayTraceResult returnValue) {
		double dirX = endX - startX;
		double dirY = endY - startY;
		double dirZ = endZ - startZ;

		if (dirX * dirX + dirY * dirY + dirZ * dirZ <= threshold * threshold) {
			return null;
		}

		int incX = signum(dirX);
		int incY = signum(dirY);
		int incZ = signum(dirZ);
		double dx = incX == 0 ? Double.MAX_VALUE : incX / dirX;
		double dy = incY == 0 ? Double.MAX_VALUE : incY / dirY;
		double dz = incZ == 0 ? Double.MAX_VALUE : incZ / dirZ;
		double percentX = dx * (incX > 0 ? 1.0D - frac(startX) : frac(startX));
		double percentY = dy * (incY > 0 ? 1.0D - frac(startY) : frac(startY));
		double percentZ = dz * (incZ > 0 ? 1.0D - frac(startZ) : frac(startZ));
		Direction facingX = incX > 0 ? Direction.WEST : Direction.EAST;
		Direction facingY = incY > 0 ? Direction.DOWN : Direction.UP;
		Direction facingZ = incZ > 0 ? Direction.NORTH : Direction.SOUTH;

		int x = floor(startX);
		int y = floor(startY);
		int z = floor(startZ);
		Direction facing;

		if (ignoreStart) {
			if (!this.positionPrediacte.isOpaque(x, y, z)) {
				ignoreStart = false;
			}
		} else {
			if (this.positionPrediacte.isOpaque(x, y, z)) {
				facing = Direction.valueOf(dirX, dirY, dirZ).opposite();
				return returnValue.set(startX, startY, startZ, facing);
			}
		}

		boolean hasHitAnything = false;

		while (percentX <= 1.0D || percentY <= 1.0D || percentZ <= 1.0D) {
			if (percentX < percentY) {
				if (percentX < percentZ) {
					x += incX;
					percentX += dx;
					facing = facingX;
				} else {
					z += incZ;
					percentZ += dz;
					facing = facingZ;
				}
			} else if (percentY < percentZ) {
				y += incY;
				percentY += dy;
				facing = facingY;
			} else {
				z += incZ;
				percentZ += dz;
				facing = facingZ;
			}

			if (ignoreStart) {
				if (!this.positionPrediacte.isOpaque(x, y, z)) {
					ignoreStart = false;
				}
			} else {
				if (this.positionPrediacte.isOpaque(x, y, z)) {
					double d;
					if (facing == facingX) {
						d = percentX - dx;
					} else if (facing == facingY) {
						d = percentY - dy;
					} else {
						d = percentZ - dz;
					}
					double hitX = startX + dirX * d;
					double hitY = startY + dirY * d;
					double hitZ = startZ + dirZ * d;

					if (!hasHitAnything) {
						hasHitAnything = true;
						returnValue.set(hitX, hitY, hitZ, facing);
					}

					double d1;
					if (percentX < percentY) {
						if (percentX < percentZ) {
							d1 = percentX;
						} else {
							d1 = percentZ;
						}
					} else if (percentY < percentZ) {
						d1 = percentY;
					} else {
						d1 = percentZ;
					}
					double nextHitX = startX + dirX * d1;
					double nextHitY = startY + dirY * d1;
					double nextHitZ = startZ + dirZ * d1;

					threshold -= Math.sqrt(squareDist(hitX, hitY, hitZ, nextHitX, nextHitY, nextHitZ));
					if (threshold < 0.0D) {
						return returnValue;
					}
				}
			}
		}

		return null;
	}

	private static int signum(double x) {
		if (x == 0.0D) {
			return 0;
		}
		return x > 0.0D ? 1 : -1;
	}

	private static double frac(double number) {
		return number - floor(number);
	}

	private static int floor(double value) {
		int i = (int) value;
		return value < i ? i - 1 : i;
	}

	private static double squareDist(double x1, double y1, double z1, double x2, double y2, double z2) {
		x2 -= x1;
		y2 -= y1;
		z2 -= z1;
		return x2 * x2 + y2 * y2 + z2 * z2;
	}

	public static class MutableRayTraceResult {

		public double x;
		public double y;
		public double z;
		public Direction direction;

		public MutableRayTraceResult() {
			this(0.0D, 0.0D, 0.0D, Direction.NORTH);
		}

		public MutableRayTraceResult(double x, double y, double z, Direction direction) {
			this.set(x, y, z, direction);
		}

		public MutableRayTraceResult set(double x, double y, double z, Direction direction) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.direction = direction;
			return this;
		}

	}

}
