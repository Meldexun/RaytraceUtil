package meldexun.raytraceutil;

public class RayTracingEngine {

	@FunctionalInterface
	public interface PositionPredicate {

		boolean isOpaque(int x, int y, int z);

	}

	private final RayTracingCache opacityCache;
	private int centerX;
	private int centerY;
	private int centerZ;
	private final PositionPredicate positionPredicate;

	public RayTracingEngine(int cacheSize, PositionPredicate positionPredicate) {
		this.opacityCache = new RayTracingCache(cacheSize);
		this.positionPredicate = positionPredicate;
	}

	public void setupCache(int x, int y, int z) {
		centerX = x;
		centerY = y;
		centerZ = z;
	}

	public void clearCache() {
		opacityCache.clearCache();
	}

	private boolean isOpaque(int x, int y, int z) {
		return opacityCache.getOrSetCachedValue(x - centerX, y - centerY, z - centerZ, () -> positionPredicate.isOpaque(x, y, z) ? 1 : 2) == 1;
	}

	public boolean raytraceThreshold(double startX, double startY, double startZ, double endX, double endY, double endZ, double threshold) {
		if (threshold <= 0.0D) {
			return raytrace(startX, startY, startZ, endX, endY, endZ);
		}

		double dirX = endX - startX;
		double dirY = endY - startY;
		double dirZ = endZ - startZ;

		if (dirX * dirX + dirY * dirY + dirZ * dirZ <= threshold * threshold) {
			return true;
		}

		int x = floor(startX);
		int y = floor(startY);
		int z = floor(startZ);
		int incX = signum(dirX);
		int incY = signum(dirY);
		int incZ = signum(dirZ);
		double dx = incX == 0 ? Double.MAX_VALUE : incX / dirX;
		double dy = incY == 0 ? Double.MAX_VALUE : incY / dirY;
		double dz = incZ == 0 ? Double.MAX_VALUE : incZ / dirZ;
		double percentX = dx * (incX > 0 ? 1.0D - frac(startX) : frac(startX));
		double percentY = dy * (incY > 0 ? 1.0D - frac(startY) : frac(startY));
		double percentZ = dz * (incZ > 0 ? 1.0D - frac(startZ) : frac(startZ));
		Axis axis;

		if (isOpaque(x, y, z)) {
			double d1 = Math.min(Math.min(Math.min(percentX, percentY), percentZ), 1.0D);
			double nextHitX = startX + dirX * d1;
			double nextHitY = startY + dirY * d1;
			double nextHitZ = startZ + dirZ * d1;

			threshold -= dist(startX, startY, startZ, nextHitX, nextHitY, nextHitZ);
			if (threshold <= 0.0D) {
				return false;
			}
		}

		while (percentX <= 1.0D || percentY <= 1.0D || percentZ <= 1.0D) {
			if (percentX < percentY) {
				if (percentX < percentZ) {
					x += incX;
					percentX += dx;
					axis = Axis.X;
				} else {
					z += incZ;
					percentZ += dz;
					axis = Axis.Z;
				}
			} else if (percentY < percentZ) {
				y += incY;
				percentY += dy;
				axis = Axis.Y;
			} else {
				z += incZ;
				percentZ += dz;
				axis = Axis.Z;
			}

			if (isOpaque(x, y, z)) {
				double d = Math.min(axis != Axis.X ? (axis != Axis.Y ? percentZ - dz : percentY - dy) : percentX - dx, 1.0D);
				double hitX = startX + dirX * d;
				double hitY = startY + dirY * d;
				double hitZ = startZ + dirZ * d;

				double d1 = Math.min(Math.min(Math.min(percentX, percentY), percentZ), 1.0D);
				double nextHitX = startX + dirX * d1;
				double nextHitY = startY + dirY * d1;
				double nextHitZ = startZ + dirZ * d1;

				threshold -= dist(hitX, hitY, hitZ, nextHitX, nextHitY, nextHitZ);
				if (threshold <= 0.0D) {
					return false;
				}
			}
		}

		return true;
	}

	public boolean raytrace(double startX, double startY, double startZ, double endX, double endY, double endZ) {
		int x = floor(startX);
		int y = floor(startY);
		int z = floor(startZ);

		if (isOpaque(x, y, z)) {
			return false;
		}

		double dirX = endX - startX;
		double dirY = endY - startY;
		double dirZ = endZ - startZ;
		int incX = signum(dirX);
		int incY = signum(dirY);
		int incZ = signum(dirZ);
		double dx = incX == 0 ? Double.MAX_VALUE : incX / dirX;
		double dy = incY == 0 ? Double.MAX_VALUE : incY / dirY;
		double dz = incZ == 0 ? Double.MAX_VALUE : incZ / dirZ;
		double percentX = dx * (incX > 0 ? 1.0D - frac(startX) : frac(startX));
		double percentY = dy * (incY > 0 ? 1.0D - frac(startY) : frac(startY));
		double percentZ = dz * (incZ > 0 ? 1.0D - frac(startZ) : frac(startZ));

		while (percentX <= 1.0D || percentY <= 1.0D || percentZ <= 1.0D) {
			if (percentX < percentY) {
				if (percentX < percentZ) {
					x += incX;
					percentX += dx;
				} else {
					z += incZ;
					percentZ += dz;
				}
			} else if (percentY < percentZ) {
				y += incY;
				percentY += dy;
			} else {
				z += incZ;
				percentZ += dz;
			}

			if (isOpaque(x, y, z)) {
				return false;
			}
		}

		return true;
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

	private static double dist(double x1, double y1, double z1, double x2, double y2, double z2) {
		x2 -= x1;
		y2 -= y1;
		z2 -= z1;
		return Math.sqrt(x2 * x2 + y2 * y2 + z2 * z2);
	}

}
