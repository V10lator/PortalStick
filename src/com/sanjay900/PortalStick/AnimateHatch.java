/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sanjay900.PortalStick;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

public class AnimateHatch extends BukkitRunnable {
	private Block blk;
	private boolean on;
	private EventListener el;

	public AnimateHatch(Block blk, boolean on, EventListener el) {
		this.blk = blk;
		this.on = on;
		this.el = el;
	}

	public void run() {
		// What you want to schedule goes here
		if (on) {
			this.blk.setType(Material.GLASS);
			el.hatches.remove(this);
		} else {
			this.blk.setType(Material.AIR);
			el.hatches.remove(this);
		}

	}
	
	public void cancel() {
		el.hatches.remove(this);
	}
	
}
