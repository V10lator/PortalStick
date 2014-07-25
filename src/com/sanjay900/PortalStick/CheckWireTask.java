/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sanjay900.PortalStick;

/**
 *
 * @author Sanjay
 */
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitRunnable;

import de.V10lator.PortalStick.PortalStick;

public class CheckWireTask extends BukkitRunnable {
    private final PortalStick plugin;
	private Block lblk;
	private Block blk;
	private boolean on;

	public CheckWireTask(PortalStick plugin, Block blk, Block lblk, boolean on) {
	    this.plugin = plugin;
		this.blk = blk;
		this.lblk = lblk;
		this.on = on;
	}

	
	public void run() {
		// What you want to schedule goes here
		Block lastblock = blk;

		byte data, data1, data2, data3, data4;
		if(on) {
		    data1 = DyeColor.PINK.getData();
		    data2 = DyeColor.LIME.getData();
		    data3 = DyeColor.RED.getData();
		    data4 = DyeColor.GREEN.getData();
		} else {
		    data1 = DyeColor.LIME.getData();
		    data2 = DyeColor.PINK.getData();
		    data3 = DyeColor.GREEN.getData();
		    data4 = DyeColor.RED.getData();
		}
		
		Material mat;
		for (BlockFace f : new BlockFace[] { BlockFace.NORTH, BlockFace.SOUTH,
				BlockFace.WEST, BlockFace.EAST, BlockFace.UP, BlockFace.DOWN }) {
			lastblock = blk.getRelative(f);

			if (lblk.getX() == lastblock.getX()
					&& lblk.getY() == lastblock.getY()
					&& lblk.getZ() == lastblock.getZ()) {

				continue;
			}
			mat = lastblock.getType();
			if (mat == Material.STAINED_CLAY) {

				if (lastblock.getData() == data1) {
					lastblock.setData(data2);
					new CheckWireTask(plugin, lastblock, blk, on)
							.runTaskLater(plugin, 0);

					
				} else if (lastblock.getData() == data3) {
					lastblock.setData(data4);
					new CheckWireTask(plugin, lastblock, blk, on)
							.runTaskLater(plugin, 0);

					
				}

			} else if (mat == Material.WOOL) {
			    data = lastblock.getData();
			    if(data == (byte)4 && on) {
			        lastblock.setData((byte)13);
	                new CheckWireTask(plugin, lastblock, blk, on)
	                .runTaskLater(plugin, 0);
			    } else if(data == (byte)13 && !on) {
			        lastblock.setData((byte)4);
	                new CheckWireTask(plugin, lastblock, blk, on)
	                .runTaskLater(plugin, 0);
			    }
			}else if (mat == Material.EMERALD_BLOCK && on) {
				lastblock.setType(Material.REDSTONE_BLOCK);

			} else if (mat == Material.REDSTONE_BLOCK && !on) {
				lastblock.setType(Material.EMERALD_BLOCK);
				

			}
		}

	}

}
