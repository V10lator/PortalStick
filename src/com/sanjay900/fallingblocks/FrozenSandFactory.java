package com.sanjay900.fallingblocks;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class FrozenSandFactory {
	    private String worldName;
	    private double locX;
	    private double locY;
	    private double locZ;
	    private String saveId;
	    private Player ridePlayer = null;
	    private Player attachPlayer = null;
		private String tag;
	    private FrozenSandFactory withCoords(double x, double y, double z) {
	        this.locX = x;
	        this.locY = y;
	        this.locZ = z;
	        return this;
	    }
	    public FrozenSandFactory withPlayer(Player p) {
	        this.attachPlayer = p;
	        return this;
	    }
	    public FrozenSandFactory ridePlayer(Player p) {
	        this.ridePlayer = p;
	        return this;
	    }
	    private FrozenSandFactory withWorld(String worldName) {
	        this.worldName = worldName;
	        return this;
	    }
	    public FrozenSandFactory withLocation(Location location) {
	        this.withCoords(location.getX(), location.getY(), location.getZ());
	        this.withWorld(location.getWorld().getName());
	        return this;
	    }
	    public FrozenSandFactory withLocation(Vector vectorLocation, String worldName) {
	        this.withCoords(vectorLocation.getX(), vectorLocation.getY(), vectorLocation.getZ());
	        this.withWorld(worldName);
	        return this;
	    }
	    public FrozenSandFactory withText(String text) {
	        this.tag = text;
	        return this;
	    }
	    public FrozenSand build() {
	        if (Bukkit.getWorld(this.worldName) == null) {
	            Bukkit.getLogger().warning("Could not find valid world (" + this.worldName + ") for Hologram of ID " + this.saveId + ". Maybe the world isn't loaded yet?");
	            return null;
	        }
	        Integer id = FlyingBlocksAPI.getNextId();
	        FrozenSand hologram = new FrozenSand(id,this.worldName, this.locX, this.locY, this.locZ, this.attachPlayer,this.ridePlayer, tag);
	        FlyingBlocksAPI.fakeBlocks.add(hologram);
	        for (Player e : hologram.getLocation().getWorld().getPlayers()) {
	                hologram.show(e);
	        }
	        return hologram;
	    }
	}

