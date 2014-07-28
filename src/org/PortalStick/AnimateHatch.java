/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.PortalStick;

import org.PortalStick.util.V10Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

public class AnimateHatch extends BukkitRunnable {
	private V10Location blk;
	private boolean on;
	private PortalStick plugin;

	public AnimateHatch(Block blk, boolean on, PortalStick plugin) {
		this.blk = new V10Location(blk);
		this.on = on;
		this.plugin = plugin;
	}

	public void run() {
		// What you want to schedule goes here
	    this.blk.getHandle().getBlock().setType(on ? Material.GLASS : Material.AIR);
	    cancel();

	}
	
	public void cancel() {
		plugin.cubeManager.hatches.remove(this);
	}
	
}
