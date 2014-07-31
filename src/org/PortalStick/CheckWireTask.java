/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.PortalStick;

/**
 *
 * @author Sanjay
 */
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitRunnable;

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

		byte data, data1, data2, data3, data4, data5, data6, data7, data8;
		if(on) {
		    data1 = DyeColor.PINK.getData();
		    data2 = DyeColor.LIME.getData();
		    data3 = DyeColor.RED.getData();
		    data4 = DyeColor.GREEN.getData();
		    data5 = DyeColor.MAGENTA.getData();
		    data6 = DyeColor.YELLOW.getData();
		    data7 = DyeColor.PURPLE.getData();
		    data8= DyeColor.ORANGE.getData();
		} else {
		    data1 = DyeColor.LIME.getData();
		    data2 = DyeColor.PINK.getData();
		    data3 = DyeColor.GREEN.getData();
		    data4 = DyeColor.RED.getData();
		    data5 = DyeColor.YELLOW.getData();
		    data6 = DyeColor.MAGENTA.getData();
		    data7 = DyeColor.ORANGE.getData();
		    data8 = DyeColor.PURPLE.getData();
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

					
				} else if (lastblock.getData() == data5) {
					lastblock.setData(data6);
					new CheckWireTask(plugin, lastblock, blk, on)
							.runTaskLater(plugin, 0);

					
				}else if (lastblock.getData() == data7) {
					lastblock.setData(data8);
					new CheckWireTask(plugin, lastblock, blk, on)
							.runTaskLater(plugin, 0);

					
				} else if (lastblock.getData() == (byte)8 && !on) {
					lastblock.setType(Material.HARD_CLAY);
					new CheckWireTask(plugin, lastblock, blk, on)
							.runTaskLater(plugin, 0);

					
				}
				//diamond off, gold on
			} else if (mat == Material.DIAMOND_BLOCK && on) {
			    lastblock.setType(Material.GOLD_BLOCK);
			    new CheckWireTask(plugin, lastblock, blk, on)
                .runTaskLater(plugin, 0);
			    
			} else if (mat == Material.GOLD_BLOCK && !on){
			    lastblock.setType(Material.DIAMOND_BLOCK);
			    new CheckWireTask(plugin, lastblock, blk, on)
                .runTaskLater(plugin, 0);
			    
			    //hardened off, light_gray clay on
			} else if (mat == Material.HARD_CLAY && on) {
				lastblock.setType(Material.STAINED_CLAY);
				lastblock.setData((byte)8);
                new CheckWireTask(plugin, lastblock, blk, on)
                .runTaskLater(plugin, 0);
				
			}
			   else if (mat == Material.EMERALD_BLOCK && on) {
				lastblock.setType(Material.REDSTONE_BLOCK);

			} else if (mat == Material.REDSTONE_BLOCK && !on) {
				lastblock.setType(Material.EMERALD_BLOCK);
				

			} else {
				for (Entity e : lastblock.getWorld().getEntities()) {
					if (e instanceof ItemFrame && plugin.util.compareLocation(e.getLocation().getBlock().getLocation(), lastblock.getLocation())) {
						ItemFrame ifr = ((ItemFrame)e);
						ItemStack i = ifr.getItem();
		                if (i.getType() == Material.MAP) {
		                	if (i.getData().getData() == (byte)8 || i.getData().getData() == (byte)9) {
		                		i.setDurability((short) (on?8:9));
		                		ifr.setItem(i);
		                		
		                	}
		                }
					}
				}
			}
		}

	}

}
