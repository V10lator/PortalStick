package org.PortalStick.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public class V10Location {

    private final int x, y, z;
    private final String world;
    
    public V10Location(Location location) {
        this(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
    
    public V10Location(String world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public V10Location(World world, int x, int y, int z) {
        this(world.getName(), x, y, z);
    }
    
    public V10Location(Block block) {
        this(block.getLocation());
    }
    
    public Location getHandle() {
        World world = Bukkit.getWorld(this.world);
        if(world == null)
            return null;
        return new Location(world, x, y, z);
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public int getZ() {
        return z;
    }
    
    public String getWorldName() {
        return world;
    }
    
    public V10Location clone() {
        return new V10Location(world, x, y, z);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((world == null) ? 0 : world.hashCode());
        long temp;
        temp = Double.doubleToLongBits(x);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(z);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj != null && obj instanceof V10Location) {
            V10Location other = (V10Location)obj;
            return (world == null && other.world == null) ||
                    (world != null && world.equals(other.world)) &&
                    x == other.x &&
                    y == other.y &&
                    z == other.z;
        }
        return false;
    }
}
