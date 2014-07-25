package com.sanjay900.PortalStick.Util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockIterator;

import com.bergerkiller.bukkit.common.utils.EntityUtil;
import com.sanjay900.PortalStick.AnimateHatch;
import com.sanjay900.fallingblocks.FrozenSand;

import de.V10lator.PortalStick.PortalStick;

public class Util {

	BlockFace[] blockfaces = new BlockFace[] { BlockFace.WEST,
		BlockFace.NORTH_WEST, BlockFace.NORTH, BlockFace.NORTH_EAST,
		BlockFace.EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST,
		BlockFace.SOUTH_EAST };
	public boolean compareLocation(Location l, Location l2) {
		return (l.getWorld().equals(l2.getWorld())
		        && l.getX() == l2.getX())
				&& (l.getY() == l2.getY())
				&& (l.getZ() == l2.getZ());

	}
	public Entity getTarget(final Player player) {

		BlockIterator iterator = new BlockIterator(player.getWorld(), player
				.getLocation().toVector(), player.getEyeLocation()
				.getDirection(), 0, 100);
		Entity target = null;
		Block item;
		List<Entity> entityList = null;
		if(iterator.hasNext())
		    entityList = player.getNearbyEntities(100, 100, 100);
		while (iterator.hasNext()) {
			item = iterator.next();
			for (Entity entity : entityList) {
				int acc = 1;
				for (int x = -acc; x < acc; x++)
					for (int z = -acc; z < acc; z++)
						for (int y = -acc; y < acc; y++)
							if (entity.getLocation().getBlock()
									.getRelative(x, y, z).equals(item)) {
								return target = entity;
							}
			}
		}
		return target;
	}
	public Entry<Block, FrozenSand> getTargetFlying(final Player player, PortalStick plugin) {

		BlockIterator iterator = new BlockIterator(player.getWorld(), player
				.getLocation().toVector(), player.getEyeLocation()
				.getDirection(), 0, 2);
		Block item;
		while (iterator.hasNext()) {
			item = iterator.next();
			for (Entry<Block, FrozenSand> fb : plugin.eventListener.FlyingBlocks.entrySet()) {
				int acc = 2;
				for (int x = -acc; x < acc; x++)
					for (int z = -acc; z < acc; z++)
						for (int y = -acc; y < acc; y++) {
							
							if (fb.getValue().getLocation().getBlock()
									.getRelative(x, y, z).equals(item)) {
								return fb;
							}
						}
			}
		}
		return null;
	}
	/**
	 * Removes a item from a inventory
	 * 
	 * @param inventory
	 *            The inventory to remove from.
	 * @param mat
	 *            The material to remove .
	 * @param amount
	 *            The amount to remove.
	 * @param damage
	 *            The data value or -1 if this does not matter.
	 */
	
	public void remove(Inventory inv, Material type, int amount,
			short damage) {
		ItemStack[] items = inv.getContents();
		for (int i = 0; i < items.length; i++) {
			ItemStack is = items[i];
			if (is != null && is.getType() == type
					&& is.getData().getData() == damage) {
				int newamount = is.getAmount() - amount;
				if (newamount > 0) {
					is.setAmount(newamount);
					break;
				} else {
					items[i] = null;
					amount = -newamount;
					if (amount == 0)
						break;
				}
			}
		}
		inv.setContents(items);

	}

	/**
	 * Checks weather the inventory contains a item or not.
	 * 
	 * @param inventory
	 *            The inventory to check..
	 * @param mat
	 *            The material to check .
	 * @param amount
	 *            The amount to check.
	 * @param damage
	 *            The data value or -1 if this does not matter.
	 * @return The amount of items the player has not. If this return 0 then the
	 *         check was successfull.
	 */
	public int contains(Inventory inventory, Material mat, int amount,
			short damage) {
		int searchAmount = 0;
		for (ItemStack item : inventory.getContents()) {

			if (item == null || !item.getType().equals(mat)) {
				continue;
			}

			if (damage != -1 && item.getDurability() == damage) {
				continue;
			}

			searchAmount += item.getAmount();
		}
		return searchAmount - amount;
	}
	
	public void changeBtn(Block middle, boolean on) {
		Block under = middle.getRelative(BlockFace.DOWN);
		Block block;
		byte data;
		Material mat;
		if(on) {
		    data = 5;
		    mat = Material.EMERALD_BLOCK;
		} else {
		    data = 14;
		    mat = Material.REDSTONE_BLOCK;
		}
		for (BlockFace f : blockfaces)
		    middle.getRelative(f).setTypeIdAndData(Material.WOOL.getId(), data, true);
		
		under.getRelative(BlockFace.EAST, 2).setType(mat);
		under.getRelative(BlockFace.WEST, 2).setType(mat);
		under.getRelative(BlockFace.NORTH, 2).setType(mat);
		under.getRelative(BlockFace.SOUTH, 2).setType(mat);
	}
	
	public void changeBtnInner(Block middle, boolean on) {
	    Material mat;
	    byte data;
		if (on) {
		    mat = Material.EMERALD_BLOCK;
		    data = 5;
		} else {
		    mat = Material.REDSTONE_BLOCK;
		    data = 14;
		}
		    
		for (BlockFace f : blockfaces) {
			middle.getRelative(f).setType(Material.WOOL);
			middle.getRelative(f).setData(data);

		}
		middle.getRelative(BlockFace.DOWN).getRelative(BlockFace.EAST, 2)
		.setType(mat);
		middle.getRelative(BlockFace.DOWN).getRelative(BlockFace.WEST, 2)
		.setType(mat);
		middle.getRelative(BlockFace.DOWN).getRelative(BlockFace.NORTH, 2)
		.setType(mat);
		middle.getRelative(BlockFace.DOWN).getRelative(BlockFace.SOUTH, 2)
		.setType(mat);
	}
	public void doInventoryUpdate(final Player player, Plugin plugin) {
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			
			@Override
			public void run() {
				player.updateInventory();
			}
		}, 1L);
	}
	
	public void clear(Block hatchMiddle, boolean respawn, PortalStick plugin, int id, int data, Block sign) {
		if (plugin.eventListener.cubes.containsKey(hatchMiddle)) {
			if (EntityUtil.getEntity(hatchMiddle.getWorld(),plugin.eventListener.cubes.get(hatchMiddle)) != null)
				EntityUtil.getEntity(hatchMiddle.getWorld(),plugin.eventListener.cubes.get(hatchMiddle)).remove();
			plugin.eventListener.cubes.remove(hatchMiddle);
		} else if (plugin.eventListener.FlyingBlocks.containsKey(hatchMiddle)) {
			if (plugin.eventListener.buttons.containsValue(plugin.eventListener.FlyingBlocks.get(hatchMiddle))) {
				Iterator<Entry<Block, FrozenSand>> it = plugin.eventListener.buttons.entrySet().iterator();
				Entry<Block, FrozenSand> e;
				Block middle;
				while (it.hasNext()) {
					e = it.next();
					if (e.getValue() == plugin.eventListener.FlyingBlocks.get(hatchMiddle)) {
						middle = e.getKey();
						changeBtn(middle, !plugin.eventListener.buttons.containsKey(middle));
						it.remove();
					}
				}
			}
			plugin.eventListener.FlyingBlocks.get(hatchMiddle).clearAllPlayerViews();
			plugin.eventListener.FlyingBlocks.remove(hatchMiddle);
			

		} else if (plugin.eventListener.cubesPlayer.containsKey(hatchMiddle)) {

			// remove cubesPlayerItem.get(hatchMiddle)
			// from cubesPlayer.get(hatchMiddle)
			remove(plugin.eventListener.cubesPlayer.get(hatchMiddle)
					.getInventory(),
					plugin.eventListener.cubesPlayerItem.get(hatchMiddle)
					.getType(), 1, plugin.eventListener.cubesPlayerItem
					.get(hatchMiddle).getData()
					.getData());
			doInventoryUpdate(
					plugin.eventListener.cubesPlayer.get(hatchMiddle), plugin);

			plugin.eventListener.cubesPlayer.remove(hatchMiddle);

			plugin.eventListener.cubesPlayerItem.remove(hatchMiddle);

		}
		BlockFace[] blockfaces = new BlockFace[] {
				BlockFace.NORTH_EAST, BlockFace.EAST,
				BlockFace.SOUTH_EAST, BlockFace.SOUTH,
				BlockFace.SOUTH_WEST, BlockFace.WEST,
				BlockFace.NORTH_WEST, BlockFace.NORTH,
				BlockFace.SELF

		};
		Block blk = hatchMiddle.getRelative(BlockFace.DOWN,3);
		for (Entry<BukkitTask, Block> t : plugin.eventListener.hatches.entrySet()) {
			if (t.getValue() == blk) {
				t.getKey().cancel();
				plugin.eventListener.hatches.remove(t.getKey());
			}
		}
		/*if (respawn) {
			
			for (int i = 0; i < blockfaces.length; i++) {

				plugin.eventListener.hatches.put(
						new AnimateHatch(blk
								.getRelative(blockfaces[i]),
								false, plugin.eventListener).runTaskLater(
										plugin, 3L * i), blk);

			}

		} else {
			
			for (int i = 0; i < blockfaces.length; i++) {

				plugin.eventListener.hatches.put(
						new AnimateHatch(blk
								.getRelative(blockfaces[i]),
								true, plugin.eventListener).runTaskLater(
										plugin, 3L * i), blk);
			}

		}*/
		if (respawn) {
			
			FallingBlock f = hatchMiddle.getWorld()
					.spawnFallingBlock(
							hatchMiddle.getLocation(), id,
							(byte) data);
			f.setDropItem(false);
			plugin.eventListener.cubes.put(hatchMiddle, f.getUniqueId());
			plugin.eventListener.cubesign.put(hatchMiddle, sign);
		}
	}
	
	public Block chkBtn (Location l) {
		Block blockUnder = l.getBlock().getRelative(BlockFace.DOWN);
		if (!(blockUnder.getType()==Material.WOOL)) return null;
		Block middle = blockUnder;
		boolean error = false;
		// red wool - find closest black and check around
		if (blockUnder.getData() == (byte) 14) {
			for (BlockFace f : blockfaces) {
				if (blockUnder.getRelative(f).getType() == Material.WOOL
						&& blockUnder.getRelative(f).getData() == (byte) 15) {
					middle = blockUnder.getRelative(f);

				}
			}
		}
		// middle - already have relative
		else if (blockUnder.getData() == (byte) 15) {

			if (blockUnder.getRelative(BlockFace.NORTH).getType() == Material.WOOL
					&& blockUnder.getRelative(BlockFace.NORTH).getData() == (byte) 14||blockUnder.getRelative(BlockFace.NORTH).getData() == (byte) 5) {
				middle = blockUnder;
			}

		} else
			// lime
			if (blockUnder.getData() == (byte) 5) {
				for (BlockFace f : blockfaces) {
					if (blockUnder.getRelative(f).getType() == Material.WOOL
							&& blockUnder.getRelative(f).getData() == (byte) 15) {
						middle = blockUnder.getRelative(f);

					}
				}
			} else {
				error = true;
			}

		if(!error)
		    for (BlockFace f : blockfaces) {
		        Block rel = middle.getRelative(f);
		        if (rel.getType() == Material.WOOL
		                && (rel.getData() == (byte) 14 || rel.getData() == (byte) 5)) {

		        } else {
		            error = true;
		            break;
		        }
		    }
		return error ? null : middle;
	}
	public List<Block> getNearbyBlocks(Location location, int Radius) {
		List<Block> Blocks = new ArrayList<Block>();

		for (int X = location.getBlockX() - Radius; X <= location.getBlockX()
				+ Radius; X++) {
			for (int Y = location.getBlockY() - Radius; Y <= location
					.getBlockY() + Radius; Y++) {
				for (int Z = location.getBlockZ() - Radius; Z <= location
						.getBlockZ() + Radius; Z++) {
					Block block = location.getWorld().getBlockAt(X, Y, Z);
					if (!block.isEmpty()) {
						Blocks.add(block);
					}
				}
			}
		}

		return Blocks;
	}

	
	public Block chkBtnInner (Location l) {
		Block blockUnder = l.getBlock().getRelative(BlockFace.DOWN);
		if (!(blockUnder.getType()==Material.WOOL)) return null;
		Block middle = l.getBlock();
		boolean error = false;
		if (blockUnder.getData() == (byte) 15) {

			if (l.getBlock().getRelative(BlockFace.NORTH).getType() == Material.WOOL
					&& l.getBlock().getRelative(BlockFace.NORTH).getData() == (byte) 14 ||l.getBlock().getRelative(BlockFace.NORTH).getData() == (byte) 5) {
				middle = l.getBlock();
			} else {error = true;}

		} else {error = true;}

		if(!error) {
		    Block rel;
		    for (BlockFace f : blockfaces) {
		        rel = middle.getRelative(f);
		        if (rel.getType() == Material.WOOL
		                && (rel.getData() == (byte) 14 || rel.getData() == (byte) 5)) {

		        } else {
		            error = true;
		            break;
		        }
		    }	
		}
		return error ? null : middle;
	}
}
