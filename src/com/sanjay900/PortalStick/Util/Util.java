package com.sanjay900.PortalStick.Util;

import java.util.ArrayList;
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
import org.bukkit.util.BlockIterator;

import com.sanjay900.fallingblocks.FrozenSand;

import de.V10lator.PortalStick.PortalStick;

public class Util {

	static BlockFace[] blockfaces = new BlockFace[] { BlockFace.WEST,
		BlockFace.NORTH_WEST, BlockFace.NORTH, BlockFace.NORTH_EAST,
		BlockFace.EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST,
		BlockFace.SOUTH_EAST };

	public static Entity getTarget(final Player player) {

		BlockIterator iterator = new BlockIterator(player.getWorld(), player
				.getLocation().toVector(), player.getEyeLocation()
				.getDirection(), 0, 100);
		Entity target = null;
		while (iterator.hasNext()) {
			Block item = iterator.next();
			for (Entity entity : player.getNearbyEntities(100, 100, 100)) {
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
	public static Entry<Block, FrozenSand> getTargetFlying(final Player player, PortalStick plugin) {

		BlockIterator iterator = new BlockIterator(player.getWorld(), player
				.getLocation().toVector(), player.getEyeLocation()
				.getDirection(), 0, 100);
		while (iterator.hasNext()) {
			Block item = iterator.next();
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
	@SuppressWarnings("deprecation")
	public static void remove(Inventory inv, Material type, int amount,
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
					items[i] = new ItemStack(Material.AIR);
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
	public static int contains(Inventory inventory, Material mat, int amount,
			short damage) {
		ItemStack[] contents = inventory.getContents();
		int searchAmount = 0;
		for (ItemStack item : contents) {

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
	@SuppressWarnings("deprecation")
	public static void changeBtn(Block middle, boolean on) {
		Block under = middle.getRelative(BlockFace.DOWN);
		if (on) {
			for (BlockFace f : blockfaces) {
				middle.getRelative(f).setType(Material.WOOL);
				middle.getRelative(f).setData((byte) 5);

			}
			under.getRelative(BlockFace.EAST).getRelative(BlockFace.EAST)
			.setType(Material.EMERALD_BLOCK);
			under.getRelative(BlockFace.WEST).getRelative(BlockFace.WEST)
			.setType(Material.EMERALD_BLOCK);
			under.getRelative(BlockFace.NORTH).getRelative(BlockFace.NORTH)
			.setType(Material.EMERALD_BLOCK);
			under.getRelative(BlockFace.SOUTH).getRelative(BlockFace.SOUTH)
			.setType(Material.EMERALD_BLOCK);
		} else {
			for (BlockFace f : blockfaces) {
				middle.getRelative(f).setType(Material.WOOL);
				middle.getRelative(f).setData((byte) 14);

			}
			under.getRelative(BlockFace.EAST).getRelative(BlockFace.EAST)
			.setType(Material.REDSTONE_BLOCK);
			under.getRelative(BlockFace.WEST).getRelative(BlockFace.WEST)
			.setType(Material.REDSTONE_BLOCK);
			under.getRelative(BlockFace.NORTH).getRelative(BlockFace.NORTH)
			.setType(Material.REDSTONE_BLOCK);
			under.getRelative(BlockFace.SOUTH).getRelative(BlockFace.SOUTH)
			.setType(Material.REDSTONE_BLOCK);
		}
	}
	@SuppressWarnings("deprecation")
	public static void changeBtnInner(Block middle, boolean on) {
		if (on) {
			for (BlockFace f : blockfaces) {
				middle.getRelative(f).setType(Material.WOOL);
				middle.getRelative(f).setData((byte) 5);

			}
			middle.getRelative(BlockFace.DOWN).getRelative(BlockFace.EAST).getRelative(BlockFace.EAST)
			.setType(Material.EMERALD_BLOCK);
			middle.getRelative(BlockFace.DOWN).getRelative(BlockFace.WEST).getRelative(BlockFace.WEST)
			.setType(Material.EMERALD_BLOCK);
			middle.getRelative(BlockFace.DOWN).getRelative(BlockFace.NORTH).getRelative(BlockFace.NORTH)
			.setType(Material.EMERALD_BLOCK);
			middle.getRelative(BlockFace.DOWN).getRelative(BlockFace.SOUTH).getRelative(BlockFace.SOUTH)
			.setType(Material.EMERALD_BLOCK);
		} else {
			for (BlockFace f : blockfaces) {
				middle.getRelative(f).setType(Material.WOOL);
				middle.getRelative(f).setData((byte) 14);

			}
			middle.getRelative(BlockFace.DOWN).getRelative(BlockFace.EAST).getRelative(BlockFace.EAST)
			.setType(Material.REDSTONE_BLOCK);
			middle.getRelative(BlockFace.DOWN).getRelative(BlockFace.WEST).getRelative(BlockFace.WEST)
			.setType(Material.REDSTONE_BLOCK);
			middle.getRelative(BlockFace.DOWN).getRelative(BlockFace.NORTH).getRelative(BlockFace.NORTH)
			.setType(Material.REDSTONE_BLOCK);
			middle.getRelative(BlockFace.DOWN).getRelative(BlockFace.SOUTH).getRelative(BlockFace.SOUTH)
			.setType(Material.REDSTONE_BLOCK);
		}
	}
	public static void doInventoryUpdate(final Player player, Plugin plugin) {
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				player.updateInventory();
			}
		}, 1L);
	}
	@SuppressWarnings("deprecation")
	public static void clear(Block hatchMiddle, boolean respawn, PortalStick plugin, int id, int data, Block sign) {
		if (plugin.eventListener.cubes.containsKey(hatchMiddle)) {
			plugin.eventListener.cubes.get(hatchMiddle).remove();
			plugin.eventListener.cubes.remove(hatchMiddle);
		} else if (plugin.eventListener.FlyingBlocks.containsKey(hatchMiddle)) {
			plugin.eventListener.FlyingBlocks.get(hatchMiddle).clearAllPlayerViews();
			plugin.eventListener.FlyingBlocks.remove(hatchMiddle);

		} else if (plugin.eventListener.cubesFallen.containsKey(hatchMiddle)) {

			plugin.eventListener.cubesFallen.get(hatchMiddle).setType(
					Material.AIR);

			Block blockUnder = plugin.eventListener.cubesFallen.get(hatchMiddle)
					.getRelative(BlockFace.DOWN);

			if (blockUnder.getType() == Material.WOOL) {
				Block middle = chkBtn(plugin.eventListener.cubesFallen.get(hatchMiddle).getLocation());

				if (!(middle == null)
						&& plugin.eventListener.buttons.containsKey(middle)) {

					for (BlockFace f2 : blockfaces) {
						middle.getRelative(f2).setType(
								Material.WOOL);
						middle.getRelative(f2).setData(
								(byte) 14);

					}
					Block under = middle
							.getRelative(BlockFace.DOWN);
					changeBtn(under,
							!plugin.eventListener.buttons.containsKey(middle));
					plugin.eventListener.buttons.remove(middle);
				}
			}
			plugin.eventListener.cubesFallen.remove(hatchMiddle);

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
		if (respawn) {
			FallingBlock f = hatchMiddle.getWorld()
					.spawnFallingBlock(
							hatchMiddle.getLocation(), id,
							(byte) data);
			f.setDropItem(false);
			plugin.eventListener.cubes.put(hatchMiddle, f);
			plugin.eventListener.cubesign.put(hatchMiddle, sign);
		}
	}
	@SuppressWarnings("deprecation")
	public static Block chkBtn (Location l) {
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

		for (BlockFace f : blockfaces) {
			Block rel = middle.getRelative(f);
			if (rel.getType() == Material.WOOL
					&& (rel.getData() == (byte) 14 || rel.getData() == (byte) 5)) {

			} else {
				error = true;
			}
		}	
		if (!error) return middle;
		return null;
	}
	public static List<Block> getNearbyBlocks(Location location, int Radius) {
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

	@SuppressWarnings("deprecation")
	public static Block chkBtnInner (Location l) {
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

		for (BlockFace f : blockfaces) {
			Block rel = middle.getRelative(f);
			if (rel.getType() == Material.WOOL
					&& (rel.getData() == (byte) 14 || rel.getData() == (byte) 5)) {

			} else {
				error = true;
			}
		}	
		if (!error) return middle;
		return null;
	}
}
