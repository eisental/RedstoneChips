
package org.tal.redstonechips.util;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;

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
     * @return the squared distance
     */
    public static double distanceSquared(Location loc1, Location loc2) {
        double dx = loc1.getX() - loc2.getX();
        double dy = loc1.getY() - loc2.getY();
        double dz = loc1.getZ() - loc2.getZ();

        return dx*dx + dy*dy + dz*dz;
    }

}
