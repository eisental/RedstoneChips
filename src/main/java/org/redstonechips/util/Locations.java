
package org.redstonechips.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.MaterialData;

/**
 *
 * @author Tal Eisenberg
 */
public class Locations {

    /**
     * @param relativeTo A block face
     * @return The block face to the right when looking in the direction of the specified face.
     * @throws IllegalArgumentException when the block face is BlockFace.UP, BlockFace.DOWN or BlockFace.SELF.
     */
    public static BlockFace getLeftFace(BlockFace relativeTo) throws IllegalArgumentException {
        if (relativeTo==BlockFace.WEST) return BlockFace.SOUTH;
        else if (relativeTo==BlockFace.EAST) return BlockFace.NORTH;
        else if (relativeTo==BlockFace.SOUTH) return BlockFace.EAST;
        else if (relativeTo==BlockFace.NORTH) return BlockFace.WEST;
        else throw new IllegalArgumentException("Invalid block face: " + relativeTo);
    }

    /**
     * @param relativeTo A block face
     * @return The block face to the right when looking in the direction of the specified face.
     * @throws IllegalArgumentException when the block face is BlockFace.UP, BlockFace.DOWN or BlockFace.SELF.
     */
    public static BlockFace getRightFace(BlockFace relativeTo) throws IllegalArgumentException {
        if (relativeTo==BlockFace.WEST) return BlockFace.NORTH;
        else if (relativeTo==BlockFace.EAST) return BlockFace.SOUTH;
        else if (relativeTo==BlockFace.SOUTH) return BlockFace.WEST;
        else if (relativeTo==BlockFace.NORTH) return BlockFace.EAST;
        else throw new IllegalArgumentException("Invalid block face: " + relativeTo);
    }

    /**
     * @param loc Location object that represents the origin block.
     * @param face A face of the origin block.
     * @return A Location object pointing to the block attached to the origin's block specified face.
     */
    public static Location getFace(Location loc, BlockFace face) {
        return new Location(loc.getWorld(), loc.getBlockX()+face.getModX(), loc.getBlockY()+face.getModY(), loc.getBlockZ()+face.getModZ());
    }

    public static boolean isInRadius(Location origin, Location loc, float radius) {
        return Locations.distanceSquared(origin, loc) <= radius*radius;
    }

    /**
     * Get the squared distance between 2 locations
     *
     * @param loc1
     * @param loc2
     * @return the squared distance
     */
    public static double distanceSquared(Location loc1, Location loc2) {
        double dx = loc1.getX() - loc2.getX();
        double dy = loc1.getY() - loc2.getY();
        double dz = loc1.getZ() - loc2.getZ();

        return dx*dx + dy*dy + dz*dz;
    }

    public static final BlockFace[] cardinalFaces = new BlockFace[] { BlockFace.NORTH, BlockFace.WEST, BlockFace.EAST, BlockFace.SOUTH, BlockFace.UP, BlockFace.DOWN };

    /**
     * Looks for another block around origin of the same type and returns the direction between the two blocks.
     * 
     * @param origin Origin block.
     * @return BlockFace representing the direction to point from the origin to the direction block.
     * @throws IllegalArgumentException If zero or more than one block of the same type as origin is found.
     */
    public static BlockFace getDirectionBlock(Block origin) throws IllegalArgumentException {
        MaterialData type = origin.getState().getData();
        BlockFace ret = null;

        for (BlockFace face : cardinalFaces) {
            Block b = origin.getRelative(face);
            if (b.getType()==type.getItemType() && (b.getData()==type.getData() || type.getData()==-1)) {
                if (ret==null)
                    ret = face;
                else throw new IllegalArgumentException("Found more than one direction block.");
            }

        }

        if (ret==null)
            throw new IllegalArgumentException("Couldn't find a direction block.");
        return ret;
    }    
    
    public static Location getFaceCenter(Location l, BlockFace face) {
        return getFaceCenter(l.getWorld(), l.getBlockX(), l.getBlockY(), l.getBlockZ(), face);
    }
    
    public static Location getFaceCenter(World w, int x, int y, int z, BlockFace face) {    
        if (face==BlockFace.DOWN) {            
            return new Location(w, x+0.5, y, z+0.5);
        } else if (face==BlockFace.UP) {
            return new Location(w, x+0.5, y+1, z+0.5);
        } else if (face==BlockFace.NORTH) {
            return new Location(w, x, y+0.5, z-0.5);
        } else if (face==BlockFace.SOUTH) {
            return new Location(w, x+1, y+0.5, z+0.5);
        } else if (face==BlockFace.EAST) {
            return new Location(w, x+0.5, y+0.5, z);
        } else if (face==BlockFace.WEST) {
            return new Location(w, x-0.5, y+0.5, z+1);
        } else throw new IllegalArgumentException("Invalid direction: " + face.name());
    }

    /**
     * Finds the location of all 4 blocks at each side of the origin block at l.
     * @param l Origin block location.
     * @return an array of 4 elements containing the locations of the side blocks.
     */
    public static Location[] getSides(Location l) {
        return new Location[] 
        {
            l.clone().add(0, 0, 1),
            l.clone().add(1, 0, 0),
            l.clone().add(0, 0,-1),            
            l.clone().add(-1,0, 0)
        };
    }
}
