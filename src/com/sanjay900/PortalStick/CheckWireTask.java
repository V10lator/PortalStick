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

public class CheckWireTask extends BukkitRunnable {
	private Block lblk;
	private Block blk;
	private boolean on;

	public CheckWireTask(Block blk, Block lblk, boolean on) {
		this.blk = blk;
		this.lblk = lblk;
		this.on = on;
	}

	@SuppressWarnings("deprecation")
	public void run() {
		// What you want to schedule goes here
		Block lastblock = blk;

		for (BlockFace f : new BlockFace[] { BlockFace.NORTH, BlockFace.SOUTH,
				BlockFace.WEST, BlockFace.EAST, BlockFace.UP, BlockFace.DOWN }) {
			lastblock = blk.getRelative(f);

			if (lblk.getX() == lastblock.getX()
					&& lblk.getY() == lastblock.getY()
					&& lblk.getZ() == lastblock.getZ()) {

				continue;
			}
			if (lastblock.getType() == Material.STAINED_CLAY) {

				if (on) {

					if (lastblock.getState().getData().getData() == DyeColor.PINK
							.getData()) {
						lastblock.setData(DyeColor.LIME.getData());
						new CheckWireTask(lastblock, blk, on)
								.runTaskLater(Bukkit.getPluginManager()
										.getPlugin("PortalStick"), 0);

						
					} else if (lastblock.getState().getData().getData() == DyeColor.RED
							.getData()) {
						lastblock.setData(DyeColor.GREEN.getData());
						new CheckWireTask(lastblock, blk, on)
								.runTaskLater(Bukkit.getPluginManager()
										.getPlugin("PortalStick"), 0);

						
					}
				} else {
					if (lastblock.getState().getData().getData() == DyeColor.LIME
							.getData()) {
						lastblock.setData(DyeColor.PINK.getData());
						new CheckWireTask(lastblock, blk, on)
								.runTaskLater(Bukkit.getPluginManager()
										.getPlugin("PortalStick"), 0);
						
					} else if (lastblock.getState().getData().getData() == DyeColor.GREEN
							.getData()) {
						lastblock.setData(DyeColor.RED.getData());
						new CheckWireTask(lastblock, blk, on)
								.runTaskLater(Bukkit.getPluginManager()
										.getPlugin("PortalStick"), 0);
						
					}
				}

			} else if (lastblock.getType() == Material.WOOL && lastblock.getData() == (byte)4 && on) {
				lastblock.setData((byte)13);
				new CheckWireTask(lastblock, blk, on)
				.runTaskLater(Bukkit.getPluginManager()
						.getPlugin("PortalStick"), 0);

			} else if (lastblock.getType() == Material.WOOL && lastblock.getData() == (byte)13 && !on) {
				lastblock.setData((byte)4);
				new CheckWireTask(lastblock, blk, on)
				.runTaskLater(Bukkit.getPluginManager()
						.getPlugin("PortalStick"), 0);

			}else if (lastblock.getType() == Material.EMERALD_BLOCK && on) {
				lastblock.setType(Material.REDSTONE_BLOCK);

			} else if (lastblock.getType() == Material.REDSTONE_BLOCK && !on) {
				lastblock.setType(Material.EMERALD_BLOCK);
				

			}
		}

	}

}
