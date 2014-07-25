/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.PortalStick;

import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class SetBlockLater extends BukkitRunnable {
	private Block blk;
	private Material mt;
	private byte data;
	private PortalStick plugin;

	public SetBlockLater(Block blk, Material mt, byte data, PortalStick plugin) {
		this.blk = blk;
		this.mt = mt;
		this.data = data;
		this.plugin = plugin;
	}

	public void run() {
		// What you want to schedule goes here
		this.blk.setType(mt);
		this.blk.setData(data);
		
		if (this.plugin.eventListener.paneltasks.containsKey(this)) {
			this.plugin.eventListener.paneltasks.remove(this);
			
		}

	}
}
