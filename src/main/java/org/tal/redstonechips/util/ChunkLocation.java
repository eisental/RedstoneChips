package org.tal.redstonechips.util;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Used for storing a chunk location.
 *
 * @author Tal Eisenberg
 */
public class ChunkLocation {
    private int x, z;
    private World world;

    public ChunkLocation(int x, int z, World world) {
        this.x = x;
        this.z = z;
        this.world = world;
    }

    /**
     * Creates a ChunkLocation object for the parameter chunk.
     * @param chunk Chunk to extract location data from.
     * @return The ChunkLocation object pointing to the chunks location.
     */
    public static ChunkLocation fromChunk(Chunk chunk) {
        return new ChunkLocation(chunk.getX(), chunk.getZ(), chunk.getWorld());
    }

    public static ChunkLocation fromLocation(Location location) {
        return new ChunkLocation(location.getBlockX() >> 4, location.getBlockZ() >> 4, location.getWorld());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ChunkLocation)) return false;

        ChunkLocation that = (ChunkLocation)obj;
        return (that.x==this.x && that.z==this.z && that.world.getUID().equals(this.world.getUID()));
    }

    public int getX() { return x; }
    public int getZ() { return z; }
    public World getWorld() { return world; }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.x;
        hash = 29 * hash + this.z;
        hash = 29 * hash + (this.world != null ? this.world.hashCode() : 0);
        return hash;
    }

    public Chunk getChunk() {
        return world.getChunkAt(x, z);
    }

    public boolean isChunkLoaded() {
        return world.isChunkLoaded(x, z);
    }

    public boolean unloadChunk() {
        return world.unloadChunk(x, z);
    }

    public void loadChunk() {
        world.loadChunk(x, z, false);
    }

    @Override
    public String toString() {
        return "ChunkLocation:x=" + x + ",z=" + z + "world=" + world.getName();
    }
}
