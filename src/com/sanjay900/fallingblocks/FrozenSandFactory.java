package com.sanjay900.fallingblocks;
/*
 * This file is part of HoloAPI.
 *
 * HoloAPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * HoloAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HoloAPI.  If not, see <http://www.gnu.org/licenses/>.
 */


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 * A HologramFactory is responsible for creating an {@link com.dsh105.holoapi.api.Hologram} which can be managed by HoloAPI
 * <p/>
 * The HologramFactory implements a fluid hologram builder, allowing parameters to be set as an extension to the constructor
 */

public class FrozenSandFactory {

	    private Plugin owningPlugin;

	    private String worldName;
	    private double locX;
	    private double locY;
	    private double locZ;
	    private String saveId;
	    private Player ridePlayer = null;
	    private Player attachPlayer = null;
	    private boolean prepared = false;

	    private ArrayList<String> tags = new ArrayList<String>();
	    private int tagId;

		private String tag;

	    /**
	     * Constructs a HologramFactory
	     *
	     * @param owningPlugin plugin to register constructed holograms under
	     * @throws java.lang.IllegalArgumentException if the owning plugin is null
	     */
	    public FrozenSandFactory(Plugin owningPlugin) {
	        if (owningPlugin == null) {
	            throw new IllegalArgumentException("Plugin cannot be null");
	        }
	        this.owningPlugin = owningPlugin;
	    }


	    private FrozenSandFactory withCoords(double x, double y, double z) {
	        this.locX = x;
	        this.locY = y;
	        this.locZ = z;
	        this.prepared = true;
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

	    /**
	     * Sets the location for constructed Holograms
	     *
	     * @param location location for constructed Holograms
	     * @return This object
	     */
	    public FrozenSandFactory withLocation(Location location) {
	        this.withCoords(location.getX(), location.getY(), location.getZ());
	        this.withWorld(location.getWorld().getName());
	        return this;
	    }

	    /**
	     * Sets the location for constructed Holograms
	     *
	     * @param vectorLocation a {@link org.bukkit.util.Vector} representing the coordinates of constructed Holograms
	     * @param worldName      the world name to place constructed Hologram in
	     * @return This object
	     */
	    public FrozenSandFactory withLocation(Vector vectorLocation, String worldName) {
	        this.withCoords(vectorLocation.getX(), vectorLocation.getY(), vectorLocation.getZ());
	        this.withWorld(worldName);
	        return this;
	    }

	    /**
	     * Gets the emptiness state of the stored lines for constructed Holograms
	     *
	     * @return true if no tags exist
	     */
	    public boolean isEmpty() {
	        return this.tags.isEmpty();
	    }

	    /**
	     * Adds text to constructed Holograms
	     *
	     * @param text Text to add to constructed holograms
	     * @return This object
	     */
	    public FrozenSandFactory withText(String text) {
	        this.tag = text;
	        return this;
	    }

	  
	    /**
	     * Constructs an {@link com.dsh105.holoapi.api.Hologram} based on the settings stored in the factory
	     *
	     * @return The constructed Hologram
	     * @throws com.dsh105.holoapi.exceptions.HologramNotPreparedException if the lines are empty or the location is not initialised
	     */
	    public FrozenSand build() {
	       

	       

	        if (Bukkit.getWorld(this.worldName) == null) {
	            //HoloAPI.getManager().clearFromFile(this.saveId);
	            Bukkit.getLogger().warning("Could not find valid world (" + this.worldName + ") for Hologram of ID " + this.saveId + ". Maybe the world isn't loaded yet?");
	            return null;
	        }
	        Integer id = FlyingBlocksAPI.getNextId();
	        FrozenSand hologram = new FrozenSand(id,this.worldName, this.locX, this.locY, this.locZ, this.attachPlayer,this.ridePlayer, tag);
	        FlyingBlocksAPI.fakeBlocks.add(hologram);
	        for (Entity e : hologram.getLocation().getWorld().getEntities()) {
	            if (e instanceof Player) {
	                hologram.show((Player) e);
	            }
	        }
	        return hologram;
	    }
	}

