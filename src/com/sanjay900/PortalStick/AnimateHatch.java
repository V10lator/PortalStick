/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sanjay900.PortalStick;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import de.V10lator.PortalStick.util.V10Location;

public class AnimateHatch extends BukkitRunnable {
	private V10Location blk;
	private boolean on;
	private EventListener el;

	public AnimateHatch(Block blk, boolean on, EventListener el) {
		this.blk = new V10Location(blk);
		this.on = on;
		this.el = el;
	}

	public void run() {
		// What you want to schedule goes here
	    this.blk.getHandle().getBlock().setType(on ? Material.GLASS : Material.AIR);
	    cancel();

	}
	
	public void cancel() {
		el.hatches.remove(this);
	}
	
}
