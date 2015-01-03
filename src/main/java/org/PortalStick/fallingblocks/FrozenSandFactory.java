package org.PortalStick.fallingblocks;
import java.util.HashSet;
import java.util.UUID;

import org.PortalStick.PortalStick;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class FrozenSandFactory {
        private final PortalStick plugin;
	    private String worldName;
	    private double locX;
	    private double locY;
	    private double locZ;
	    private String saveId;
		private int blockid = 0;
		private int blockdata = 0;
		
		public FrozenSandFactory(PortalStick plugin) {
		    this.plugin = plugin;
		}
		
	    private FrozenSandFactory withCoords(double x, double y, double z) {
	        this.locX = x;
	        this.locY = y;
	        this.locZ = z;
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
	    public FrozenSandFactory withId(int id) {
	        this.blockid = id;
	        return this;
	    }
	    public FrozenSandFactory withData(int id) {
	        this.blockid = id;
	        return this;
	    }
	    public FrozenSand build() {
	        World world = Bukkit.getWorld(this.worldName);
	        if (world == null) {
	            if(plugin.config.debug)
	                plugin.getLogger().warning("Could not find valid world (" + this.worldName + ") for Hologram of ID " + this.saveId + ". Maybe the world isn't loaded yet?");
	            return null;
	        }
	        if (blockid == 0) {
	            if(plugin.config.debug)
	                plugin.getLogger().warning("The Hologram: " + this.saveId + ". is invalid as it has no id set!");
	            return null;
	        }
	        int id = plugin.frozenSandManager.getNextId();
	        FrozenSand hologram = new FrozenSand(plugin, id,this.worldName, this.locX, this.locY, this.locZ, blockid, blockdata);
	        plugin.frozenSandManager.fakeBlocks.put(hologram, new HashSet<UUID>());
	        for (Player e : world.getPlayers()) {
	            plugin.frozenSandManager.checkSight(e, null); 
	        }
	        return hologram;
	    }

		public FrozenSandFactory withText(String textid) {
			this.blockid = Integer.parseInt(textid.split(":")[0]);
			this.blockdata = textid.split(":").length == 1? 0: Integer.parseInt(textid.split(":")[1]);
			return this;
		}
	}

