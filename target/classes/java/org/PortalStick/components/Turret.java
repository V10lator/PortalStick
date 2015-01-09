package org.PortalStick.components;

import java.util.ArrayList;
import java.util.UUID;

import org.PortalStick.PortalStick;
import org.PortalStick.util.BlockStorage;
import com.sanjay900.nmsUtil.util.V10Location;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Dispenser;
import org.bukkit.util.Vector;

public class Turret implements Runnable {

    private final PortalStick plugin;
    final V10Location location;
    final ArrayList<BlockStorage> blocksInSight = new ArrayList<BlockStorage>();
    public UUID target;
    private int id = -1;
    
    private double vectorX, vectorY, vectorZ;
    
    public Turret(PortalStick plugin, V10Location location) {
        this.plugin = plugin;
        this.location = location;
        calculateBlocksInSight();
    }
    
    public void checkPossibleTarget(UUID uuid, V10Location loc) {
        if(!isVisible(loc))
            return;
        updateVector(loc);
        target = uuid;
        id = Bukkit.getScheduler().runTaskTimer(plugin, this, 1L, 3L).getTaskId();
    }
    
    public void checkMovement(V10Location loc) {
        if(isVisible(loc)) {
            updateVector(loc);
            return;
        }
        target = null;
        vectorX = vectorY = vectorZ = 0.0D;
        Bukkit.getScheduler().cancelTask(id);
        id = -1;
    }
    
    private void updateVector(V10Location loc) {
        Vector vector = loc.getHandle().toVector().subtract(getShootingLocation().toVector());
        vectorX = vector.getX();
        vectorY = vector.getY();
        vectorZ = vector.getZ();
    }
    
    private boolean isVisible(V10Location loc) {
        for(BlockStorage block: blocksInSight)
            if(loc.equals(block.getLocation()))
                return true;
        return false;
    }
    
    public void calculateBlocksInSight() {
        blocksInSight.clear();
        // TODO 
    }
    
    private BlockFace getFace(Block block) {
        BlockState state = block.getState();
        if(!(state instanceof Dispenser)) {
            // TODO: Log the error
            return null;
        }
        return ((org.bukkit.material.Dispenser)state.getData()).getFacing();
    }
    
    private Location getShootingLocation() {
        Block block = location.getHandle().getBlock();
        BlockFace face = getFace(block);
        return face == null ? location.getHandle() : block.getRelative(face).getLocation();
    }
    
    public void run() {
        Location from = getShootingLocation();
        from.getWorld().spawnArrow(from, new Vector(vectorX, vectorY, vectorZ), 0.8F, 12.0F);
    }
}
