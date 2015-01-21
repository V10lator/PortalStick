package org.PortalStick.managers;

import java.util.ArrayList;
import java.util.Iterator;

import org.PortalStick.PortalStick;
import org.PortalStick.components.Wire;

import com.sanjay900.nmsUtil.util.Utils;
import com.sanjay900.nmsUtil.util.V10Location;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;

public class WireManager {
    private final PortalStick plugin;
    public final ArrayList<V10Location> wire = new ArrayList<V10Location>();

    public WireManager(PortalStick plugin) {
        this.plugin = plugin;
    }

    public void cleanUpWire() {
        byte data1, data2, data3, data4, data5, data6, data7, data8;
        Iterator<V10Location> iter = wire.iterator();
        Block blk;
        data1 = DyeColor.LIME.getData();
        data2 = DyeColor.PINK.getData();
        data3 = DyeColor.GREEN.getData();
        data4 = DyeColor.RED.getData();
        data5 = DyeColor.YELLOW.getData();
        data6 = DyeColor.MAGENTA.getData();
        data7 = DyeColor.ORANGE.getData();
        data8 = DyeColor.PURPLE.getData();
        while (iter.hasNext()) {
            blk = iter.next().getHandle().getBlock();
            if (blk.getData() == data1) {
                blk.setData(data2);
                Wire(plugin, blk, blk, false);
                return;
            } else if (blk.getData() == data3) {
                blk.setData(data4);
                Wire(plugin, blk, blk, false);
                return;
            }else if (blk.getData() == data5) {
                blk.setData(data6);
                Wire(plugin, blk, blk, false);
                return;
            }else if (blk.getData() == data7) {
                blk.setData(data8);
                Wire(plugin, blk, blk, false);
                return;
            }
            iter.remove();
        }
    }

	private void Wire(PortalStick plugin2, Block blk, Block lblk, boolean on) {
		Block lastblock = blk;

		byte data1, data2, data3, data4, data5, data6, data7, data8;
		if(on) {
		    data1 = DyeColor.PINK.getData();
		    data2 = DyeColor.LIME.getData();
		    data3 = DyeColor.RED.getData();
		    data4 = DyeColor.GREEN.getData();
		    data5 = DyeColor.MAGENTA.getData();
		    data6 = DyeColor.YELLOW.getData();
		    data7 = DyeColor.PURPLE.getData();
		    data8 = DyeColor.ORANGE.getData();
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
					Wire(plugin, lastblock, blk, on);

					
				} else if (lastblock.getData() == data3) {
					lastblock.setData(data4);
					Wire(plugin, lastblock, blk, on);

					
				} else if (lastblock.getData() == data5) {
					lastblock.setData(data6);
					Wire(plugin, lastblock, blk, on);

					
				}else if (lastblock.getData() == data7) {
					lastblock.setData(data8);
					Wire(plugin, lastblock, blk, on);

					
				} else if (lastblock.getData() == (byte)8 && !on) {
					lastblock.setType(Material.HARD_CLAY);
					Wire(plugin, lastblock, blk, on);

					
				}
				//diamond off, gold on
			} else if (mat == Material.DIAMOND_BLOCK && on) {
			    lastblock.setType(Material.GOLD_BLOCK);
			     Wire(plugin, lastblock, blk, on);
			    
			} else if (mat == Material.GOLD_BLOCK && !on){
			    lastblock.setType(Material.DIAMOND_BLOCK);
			    Wire(plugin, lastblock, blk, on);
			    //hardened off, light_gray clay on
			} else if (mat == Material.HARD_CLAY && on) {
				lastblock.setType(Material.STAINED_CLAY);
				lastblock.setData((byte)8);
                 Wire(plugin, lastblock, blk, on);
				
			}
			   else if (mat == Material.EMERALD_BLOCK && on) {
				lastblock.setType(Material.REDSTONE_BLOCK);

			} else if (mat == Material.REDSTONE_BLOCK && !on) {
				lastblock.setType(Material.EMERALD_BLOCK);
				

			} else {
				for (Entity e : lastblock.getWorld().getEntities()) {
					if (e instanceof ItemFrame && Utils.compareLocation(e.getLocation().getBlock().getLocation(), lastblock.getLocation())) {
						ItemFrame ifr = ((ItemFrame)e);
						ItemStack i = ifr.getItem();
		                if (i.getType() == Material.MAP) {
		                	if (i.getData().getData() == (byte)7 || i.getData().getData() == (byte)11) {
		                		i.setDurability((short) (on?11:7));
		                		ifr.setItem(i);
		                		
		                	}
		                }
					}
				}
			}
		}
	}
    
}
