/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sanjay900.PortalStick;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class SetFallLater extends BukkitRunnable {
	private Block blk;
	private Material mt;
	private byte data;
	private Vector vector;

	public SetFallLater(Block blk, Material mt, byte data, Vector vector) {
		this.blk = blk;
		this.mt = mt;
		this.data = data;
		this.vector = vector;
	}

	@SuppressWarnings("deprecation")
	public void run() {
		// What you want to schedule goes here
		this.blk.setType(Material.AIR);
		FallingBlock f2 = this.blk.getLocation()
				.getWorld()
				.spawnFallingBlock(
this.blk.getLocation(),
						this.mt,
						this.data);
		f2.setDropItem(false);
		f2.setVelocity(vector);
		

	}
}
