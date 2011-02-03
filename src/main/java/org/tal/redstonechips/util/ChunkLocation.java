/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.redstonechips.util;

import org.bukkit.Chunk;
import org.bukkit.World;

/**
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

    public static ChunkLocation fromChunk(Chunk chunk) {
        return new ChunkLocation(chunk.getX(), chunk.getZ(), chunk.getWorld());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ChunkLocation)) return false;

        ChunkLocation that = (ChunkLocation)obj;
        return (that.x==this.x && that.z==this.z && that.world.getId()==this.world.getId());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.x;
        hash = 29 * hash + this.z;
        hash = 29 * hash + (this.world != null ? this.world.hashCode() : 0);
        return hash;
    }
}
