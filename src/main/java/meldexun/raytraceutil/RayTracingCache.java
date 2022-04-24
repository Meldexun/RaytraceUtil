package meldexun.raytraceutil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.IntSupplier;

public class RayTracingCache {

	private final int radiusChunks;
	private final int sizeChunks;
	private final RayTracingCacheChunk[] chunks;
	private final List<RayTracingCacheChunk> dirtyChunks = new ArrayList<>();

	public RayTracingCache(int radiusChunks) {
		this.radiusChunks = radiusChunks;
		this.sizeChunks = this.radiusChunks * 2 + 1;
		this.chunks = new RayTracingCacheChunk[this.sizeChunks * this.sizeChunks * this.sizeChunks];

		for (int i = 0; i < this.chunks.length; i++) {
			this.chunks[i] = new RayTracingCacheChunk();
		}
	}

	public int getOrSetCachedValue(int x, int y, int z, IntSupplier function) {
		RayTracingCacheChunk chunk = this.getChunk(x, y, z);
		if (chunk == null) {
			return -1;
		}
		return chunk.getOrSetCachedValue(x & 15, y & 15, z & 15, function);
	}

	private RayTracingCacheChunk getChunk(int x, int y, int z) {
		x = (x >> 4) + this.radiusChunks;
		if (x < 0 || x >= this.sizeChunks) {
			return null;
		}
		y = (y >> 4) + this.radiusChunks;
		if (y < 0 || y >= this.sizeChunks) {
			return null;
		}
		z = (z >> 4) + this.radiusChunks;
		if (z < 0 || z >= this.sizeChunks) {
			return null;
		}
		return this.chunks[(z * this.sizeChunks + y) * this.sizeChunks + x];
	}

	public void clearCache() {
		for (Iterator<RayTracingCacheChunk> iter = this.dirtyChunks.iterator(); iter.hasNext();) {
			iter.next().clearChunk();
			iter.remove();
		}
	}

	private class RayTracingCacheChunk {

		private int[] cache = new int[256];
		private boolean dirty = false;

		/**
		 * @param x chunk relative
		 * @param y chunk relative
		 * @param z chunk relative
		 * @return the cached value
		 */
		public int getOrSetCachedValue(int x, int y, int z, IntSupplier function) {
			int index = (z << 4) | y;
			int offset = x << 1;
			int cachedSection = this.cache[index];
			int cachedValue = (cachedSection >>> offset) & 3;
			if (cachedValue != 0) {
				return cachedValue;
			}
			cachedValue = function.getAsInt();
			this.cache[index] = cachedSection | ((cachedValue & 3) << offset);
			this.markDirty();
			return cachedValue;
		}

		private void markDirty() {
			if (!this.dirty) {
				this.dirty = true;
				RayTracingCache.this.dirtyChunks.add(this);
			}
		}

		/**
		 * Checks if this chunk is dirty and cleans it.
		 */
		public void clearChunk() {
			if (this.dirty) {
				Arrays.fill(this.cache, 0);
				this.dirty = false;
			}
		}

	}

}
