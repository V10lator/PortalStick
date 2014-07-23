package com.sanjay900.PortalStick;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import com.bergerkiller.bukkit.common.events.EntityRemoveEvent;
import com.sanjay900.PortalStick.Util.Util;
import com.sanjay900.fallingblocks.FrozenSand;
import com.sanjay900.fallingblocks.FrozenSandFactory;

import de.V10lator.PortalStick.PortalStick;

public class EventListener implements Listener {

	public ArrayList<Block> wire = new ArrayList<Block>();
	public HashMap<Player, Block> buttonsToPlayer = new HashMap<>();
	public HashMap<Block, Block> buttons = new HashMap<>();
	public HashMap<Block, Block> panels = new HashMap<>();
	public HashMap<BukkitTask, Block> paneltasks = new HashMap<>();
	public HashMap<Block, FallingBlock> cubes = new HashMap<Block, FallingBlock>();
	public HashMap<Block, Player> cubesPlayer = new HashMap<Block, Player>();
	public HashMap<Block, Block> cubesFallen = new HashMap<Block, Block>();
	public HashMap<Block, ItemStack> cubesPlayerItem = new HashMap<Block, ItemStack>();
	public PortalStick plugin;
	public HashMap<Block, Block> cubesign = new HashMap<>();
	public HashMap<BukkitTask, Block> hatches = new HashMap<>();
	public HashMap<Block, FrozenSand> FlyingBlocks = new HashMap<>();
	BlockFace[] blockfaces = new BlockFace[] { BlockFace.WEST,
			BlockFace.NORTH_WEST, BlockFace.NORTH, BlockFace.NORTH_EAST,
			BlockFace.EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST,
			BlockFace.SOUTH_EAST };
	public EventListener(PortalStick portalStick) {

		portalStick.getServer().getPluginManager()
		.registerEvents(this, portalStick);
		plugin = portalStick;
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onEntityDeath(final EntityRemoveEvent event) {
		if (event.getEntity().getType() == EntityType.FALLING_BLOCK) {
			final FallingBlock fb = ((FallingBlock) event.getEntity());
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,
					new Runnable() {
				@Override
				public void run() {
					if (cubes.containsValue((FallingBlock) event
							.getEntity())) {
						for (Entry<Block, FallingBlock> entry : cubes
								.entrySet()) {
							if (((FallingBlock) event.getEntity())
									.equals(entry.getValue())) {
								FallingBlock f = entry
										.getKey()
										.getWorld()
										.spawnFallingBlock(
												fb.getLocation(),
												fb.getMaterial()
												.getId(),
												(byte) fb
												.getBlockData());
								f.setDropItem(false);
								f.setVelocity(event.getEntity()
										.getVelocity());
								event.getEntity().remove();
								cubes.remove(entry.getKey());
								cubes.put(entry.getKey(), f);

							}
						}
					}
				}
			}, 1L);

		}

	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerAnimation(PlayerAnimationEvent event) {
		Entry<Block, FrozenSand> fb = Util.getTargetFlying(event.getPlayer(),
				plugin);
		if (!(fb == null)) {
			FlyingBlocks.remove(fb.getKey());
			cubesPlayer.put(fb.getKey(), event.getPlayer());
			ItemStack item = new ItemStack(Material.getMaterial(Integer.parseInt(fb.getValue().id.split(":")[0])), 1, (byte)Integer.parseInt(fb.getValue().id.split(":")[1]));
			cubesPlayerItem.put(fb.getKey(), item);

			event.getPlayer().getInventory().addItem(item);
			Util.doInventoryUpdate(event.getPlayer(), plugin);
			fb.getValue().clearAllPlayerViews();;
			return;
		}
		for (FallingBlock e : cubes.values()) {
			Entity en = Util.getTarget(event.getPlayer());
			if (en == e) {

				for (Entry<Block, FallingBlock> entry : cubes.entrySet()) {
					if (en.equals(entry.getValue())) {
						cubes.remove(entry.getKey());
						cubesPlayer.put(entry.getKey(), event.getPlayer());
						FallingBlock b = (FallingBlock) en;
						ItemStack item = new ItemStack(b.getMaterial(), 1,
								b.getBlockData());
						cubesPlayerItem.put(entry.getKey(), item);

						event.getPlayer().getInventory().addItem(item);
						Util.doInventoryUpdate(event.getPlayer(), plugin);
						en.remove();
						break;

					}
				}

			}

		}

	}
	@EventHandler
	public void PlayerMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		Location to = e.getTo();
		if (!buttonsToPlayer.containsKey(p)) {
			Block middle = Util.chkBtn(to);
			if (!(middle == null) && !buttonsToPlayer.containsValue(middle)
					&& !buttons.containsKey(middle)) {
				buttonsToPlayer.put(p, middle);
				Util.changeBtn(middle, !buttons.containsKey(middle));
				buttons.put(middle, null);
			}
		} else {
			Block middle = Util.chkBtn(to);
			if (middle == null) {
				
				Block middle2 = buttonsToPlayer.get(p);

				Util.changeBtn(middle2, !buttons.containsKey(middle2));
				buttonsToPlayer.remove(p);
				buttons.remove(middle2);
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void death(PlayerDeathEvent event) {
		if (cubesPlayer.containsValue(event.getEntity())) {

			for (Entry<Block, Player> entry : cubesPlayer.entrySet()) {
				if (event.getEntity().equals(entry.getValue())) {
					if (cubesign.get(entry.getKey()).isBlockPowered()
							|| cubesign.get(entry.getKey())
							.isBlockIndirectlyPowered()) {

						FallingBlock f = entry
								.getKey()
								.getWorld()
								.spawnFallingBlock(
										entry.getKey().getLocation(),
										cubesPlayerItem.get(entry.getKey())
										.getTypeId(),
										(byte) cubesPlayerItem
										.get(entry.getKey()).getData()
										.getData());
						f.setDropItem(false);
						ArrayList<ItemStack> remove = new ArrayList<>();
						for (ItemStack drop : event.getDrops()) {
							// You can add a check for item types here. Like
							// if(drop.getType() == Material.TNT) ...
							if (drop.getType() == cubesPlayerItem.get(
									entry.getKey()).getType()
									&& drop.getData().getData() == cubesPlayerItem
									.get(entry.getKey()).getData()
									.getData()) {
								remove.add(drop);
								break;
							}

						}

						for (ItemStack is : remove) {
							event.getDrops().remove(is);
						}
						cubesPlayer.remove(entry.getKey());
						cubesPlayerItem.remove(entry.getKey());

						cubes.put(entry.getKey(), f);
					}
				}
			}

			return;
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void drop(final PlayerDropItemEvent event) {
		if (cubesPlayer.containsValue(event.getPlayer())) {

			for (Entry<Block, Player> entry : cubesPlayer.entrySet()) {
				if (event.getPlayer().equals(entry.getValue())) {
					if (cubesPlayerItem.get(entry.getKey()).getType() == event
							.getItemDrop().getItemStack().getType()
							&& cubesPlayerItem.get(entry.getKey()).getData()
							.getData() == event.getItemDrop()
							.getItemStack().getData().getData()) {

						FallingBlock f = entry
								.getKey()
								.getWorld()
								.spawnFallingBlock(
										event.getPlayer().getEyeLocation(),
										event.getItemDrop().getItemStack()
										.getTypeId(),
										event.getItemDrop().getItemStack()
										.getData().getData());
						f.setDropItem(false);
						f.setVelocity(event.getPlayer().getLocation()
								.getDirection().multiply(1));
						event.setCancelled(true);

						Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,
								new Runnable() {
							@Override
							public void run() {
								if (event.getPlayer().getItemInHand()
										.getAmount() - 1 > 1) {
									ItemStack hand = event.getPlayer()
											.getItemInHand();
									hand.setAmount(event.getPlayer()
											.getItemInHand()
											.getAmount() - 1);
									event.getPlayer().setItemInHand(
											hand);

								} else {
									event.getPlayer().setItemInHand(
											null);
								}
							}
						});
						cubes.put(entry.getKey(), f);
						cubesPlayer.remove(entry.getKey());

					}

					return;
				}
			}
		}

	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void blockBreak(PlayerInteractEvent event) {

		if (event.getAction() != Action.LEFT_CLICK_BLOCK
				|| cubesPlayer.containsValue(event.getPlayer()))
			return;
		if (cubesFallen.containsValue(event.getClickedBlock())) {
			ItemStack stack = new ItemStack(event.getClickedBlock().getType(),
					1, event.getClickedBlock().getData());
			for (Entry<Block, Block> entry : cubesFallen.entrySet()) {
				if (event.getClickedBlock().equals(entry.getValue())) {

					event.setCancelled(true);

					event.getPlayer().getInventory().addItem(stack);
					event.getPlayer().updateInventory();
					cubesPlayer.put(entry.getKey(), event.getPlayer());

					cubesPlayerItem.put(entry.getKey(), stack);

					event.getClickedBlock().setType(Material.AIR);
					cubesFallen.remove(entry.getKey());

				}
			}

		}
		Block middle = Util.chkBtn(event.getClickedBlock().getLocation());

		if (!(middle == null) && buttons.containsKey(middle)) {

			Util.changeBtn(middle, !buttons.containsKey(middle));
			buttons.remove(middle);
		} else {
			middle = Util.chkBtnInner(event.getClickedBlock().getLocation());
			if (!(middle == null) && buttons.containsKey(middle)) {

				Util.changeBtnInner(middle, !buttons.containsKey(middle));

			} else {
				// vertical in wall block
				Block block = event.getClickedBlock();
				if (event.getClickedBlock().getRelative(BlockFace.DOWN).getType() == Material.WOOL
						&& event.getClickedBlock().getRelative(BlockFace.UP)
						.getType() == Material.WOOL) {
					if (event.getClickedBlock().getRelative(BlockFace.WEST)
							.getType() == Material.WOOL
							&& event.getClickedBlock().getRelative(BlockFace.EAST)
							.getType() == Material.WOOL) {
						Block[] blocks = new Block[] {
								block.getRelative(BlockFace.UP),
								block.getRelative(BlockFace.DOWN),
								block.getRelative(BlockFace.WEST),
								block.getRelative(BlockFace.EAST),
								block.getRelative(BlockFace.UP).getRelative(
										BlockFace.WEST),
										block.getRelative(BlockFace.UP).getRelative(
												BlockFace.EAST),
												block.getRelative(BlockFace.DOWN).getRelative(
														BlockFace.WEST),
														block.getRelative(BlockFace.DOWN).getRelative(
																BlockFace.EAST) };
						boolean error2 = false;
						for (Block block2 : blocks) {
							if (!(block2.getType() == Material.WOOL && (block2
									.getData() == (byte) 5 || block2.getData() == (byte) 14))) {
								error2 = true;
							}
						}
						BlockFace face = BlockFace.SELF;
						if (block.getRelative(BlockFace.NORTH).getType() == Material.WOOL) {
							face = BlockFace.NORTH;
						}
						if (block.getRelative(BlockFace.SOUTH).getType() == Material.WOOL) {
							face = BlockFace.SOUTH;
						}
						if (!error2 && buttons.containsKey(block)
								&& face != BlockFace.SELF) {

							buttons.remove(block);
							for (Block block2 : blocks) {
								block2.setType(Material.WOOL);
								block2.setData((byte) 14);

							}
							for (Block block2 : blocks) {
								block2.getRelative(face).setType(
										Material.EMERALD_BLOCK);

							}

						} else if (!event.getPlayer().hasPermission("portal.place")) {
							event.setCancelled(true);
						}

					} else if (event.getClickedBlock().getRelative(BlockFace.NORTH)
							.getType() == Material.WOOL
							&& event.getClickedBlock().getRelative(BlockFace.SOUTH)
							.getType() == Material.WOOL) {
						Block[] blocks = new Block[] {
								block.getRelative(BlockFace.UP),
								block.getRelative(BlockFace.DOWN),
								block.getRelative(BlockFace.NORTH),
								block.getRelative(BlockFace.SOUTH),
								block.getRelative(BlockFace.UP).getRelative(
										BlockFace.NORTH),
										block.getRelative(BlockFace.UP).getRelative(
												BlockFace.SOUTH),
												block.getRelative(BlockFace.DOWN).getRelative(
														BlockFace.NORTH),
														block.getRelative(BlockFace.DOWN).getRelative(
																BlockFace.SOUTH) };
						boolean error2 = false;
						for (Block block2 : blocks) {
							if (!(block2.getType() == Material.WOOL && (block2
									.getData() == (byte) 5 || block2.getData() == (byte) 14))) {
								error2 = true;
							}
						}
						BlockFace face = BlockFace.SELF;
						if (block.getRelative(BlockFace.EAST).getType() == Material.WOOL) {
							face = BlockFace.EAST;
						}
						if (block.getRelative(BlockFace.WEST).getType() == Material.WOOL) {
							face = BlockFace.WEST;
						}
						if (!error2 && buttons.containsKey(block)
								&& face != BlockFace.SELF) {

							buttons.remove(block);
							for (Block block2 : blocks) {
								block2.setType(Material.WOOL);
								block2.setData((byte) 14);

							}
							for (Block block2 : blocks) {
								block2.getRelative(face).setType(
										Material.EMERALD_BLOCK);

							}

						} else if (!event.getPlayer().hasPermission("portal.place")) {
							event.setCancelled(true);
						}
					}
				}
			}
		}

	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void blockPlace(BlockPlaceEvent event) {
		boolean cube = false;
		if (cubesPlayer.containsValue(event.getPlayer())) {

			for (Entry<Block, Player> entry : cubesPlayer.entrySet()) {
				if (event.getPlayer().equals(entry.getValue())
						&& cubesPlayerItem.get(entry.getKey()).getType() == event
						.getBlockPlaced().getType()
						&& cubesPlayerItem.get(entry.getKey()).getData()
						.getData() == event.getBlockPlaced().getData()) {

					cubesPlayer.remove(entry.getKey());
					cubesPlayerItem.remove(entry.getKey());
					cubesFallen.put(entry.getKey(), event.getBlockPlaced());
					cube = true;
					break;
				}
			}

		}
		Block middle = Util.chkBtn(event.getBlock().getLocation());

		if (!(middle == null) && !buttons.containsKey(middle) && cube) {

			Util.changeBtn(middle, !buttons.containsKey(middle));
			buttons.put(middle, event.getBlock());
		} else {
			middle = Util.chkBtnInner(event.getBlock().getLocation());
			if (!(middle == null) && !buttons.containsKey(middle) && cube) {

				Util.changeBtnInner(middle, !buttons.containsKey(middle));
				buttons.put(middle, event.getBlock());
			} else {
				// vertical in wall block
				Block block = event.getBlockPlaced();
				if (event.getBlockPlaced().getRelative(BlockFace.DOWN).getType() == Material.WOOL
						&& event.getBlockPlaced().getRelative(BlockFace.UP)
						.getType() == Material.WOOL) {
					if (event.getBlockPlaced().getRelative(BlockFace.WEST)
							.getType() == Material.WOOL
							&& event.getBlockPlaced().getRelative(BlockFace.EAST)
							.getType() == Material.WOOL) {
						Block[] blocks = new Block[] {
								block.getRelative(BlockFace.UP),
								block.getRelative(BlockFace.DOWN),
								block.getRelative(BlockFace.WEST),
								block.getRelative(BlockFace.EAST),
								block.getRelative(BlockFace.UP).getRelative(
										BlockFace.WEST),
										block.getRelative(BlockFace.UP).getRelative(
												BlockFace.EAST),
												block.getRelative(BlockFace.DOWN).getRelative(
														BlockFace.WEST),
														block.getRelative(BlockFace.DOWN).getRelative(
																BlockFace.EAST) };

						boolean error2 = false;
						for (Block block2 : blocks) {
							if (!(block2.getType() == Material.WOOL && (block2
									.getData() == (byte) 5 || block2.getData() == (byte) 14))) {
								error2 = true;
							}
						}
						BlockFace face = BlockFace.SELF;
						if (block.getRelative(BlockFace.NORTH).getType() == Material.WOOL) {
							face = BlockFace.NORTH;
						}
						if (block.getRelative(BlockFace.SOUTH).getType() == Material.WOOL) {
							face = BlockFace.SOUTH;
						}
						if (!error2 && !buttons.containsKey(block)
								&& face != BlockFace.SELF) {

							buttons.put(block, event.getBlock());
							for (Block block2 : blocks) {
								block2.setType(Material.WOOL);
								block2.setData((byte) 5);

							}
							for (Block block2 : blocks) {
								block2.getRelative(face).setType(
										Material.REDSTONE_BLOCK);

							}

						} else if (!event.getPlayer().hasPermission("portal.place")) {
							event.setCancelled(true);
						}

					} else if (event.getBlockPlaced().getRelative(BlockFace.NORTH)
							.getType() == Material.WOOL
							&& event.getBlockPlaced().getRelative(BlockFace.SOUTH)
							.getType() == Material.WOOL) {
						Block[] blocks = new Block[] {
								block.getRelative(BlockFace.UP),
								block.getRelative(BlockFace.DOWN),
								block.getRelative(BlockFace.NORTH),
								block.getRelative(BlockFace.SOUTH),
								block.getRelative(BlockFace.UP).getRelative(
										BlockFace.NORTH),
										block.getRelative(BlockFace.UP).getRelative(
												BlockFace.SOUTH),
												block.getRelative(BlockFace.DOWN).getRelative(
														BlockFace.NORTH),
														block.getRelative(BlockFace.DOWN).getRelative(
																BlockFace.SOUTH) };

						boolean error2 = false;
						for (Block block2 : blocks) {
							if (!(block2.getType() == Material.WOOL && (block2
									.getData() == (byte) 5 || block2.getData() == (byte) 14))) {
								error2 = true;
							}
						}
						BlockFace face = BlockFace.SELF;
						if (block.getRelative(BlockFace.EAST).getType() == Material.WOOL) {
							face = BlockFace.EAST;
						}
						if (block.getRelative(BlockFace.WEST).getType() == Material.WOOL) {
							face = BlockFace.WEST;
						}
						if (!error2 && !buttons.containsKey(block)
								&& face != BlockFace.SELF) {

							buttons.put(block, event.getBlock());
							for (Block block2 : blocks) {
								block2.setType(Material.WOOL);
								block2.setData((byte) 5);

							}
							for (Block block2 : blocks) {
								block2.getRelative(face).setType(
										Material.REDSTONE_BLOCK);

							}

						} else if (!event.getPlayer().hasPermission("portal.place")) {
							event.setCancelled(true);
						}
					}
				}

			}

		}
	}

	@SuppressWarnings({ "deprecation" })
	@EventHandler
	public void onBlockPhysics(BlockPhysicsEvent event) {

		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				ArrayList<Block> BlockToRemove = new ArrayList<>();
				for (Block blk : wire) {
					if (!(blk.isBlockPowered() || blk
							.isBlockIndirectlyPowered())) {
						if (blk.getState().getData().getData() == DyeColor.LIME
								.getData()) {
							blk.setData(DyeColor.PINK.getData());
							new CheckWireTask(blk, blk, false).runTaskLater(
									plugin, 1L);
							return;
						} else if (blk.getState().getData().getData() == DyeColor.GREEN
								.getData()) {
							blk.setData(DyeColor.RED.getData());
							new CheckWireTask(blk, blk, false).runTaskLater(
									plugin, 1L);
							return;
						}
						BlockToRemove.add(blk);

					}
				}
				for (Block blk : BlockToRemove) {
					wire.remove(blk);
				}
				BlockToRemove.clear();
			}
		}, 2L);

		if (event.getBlock().getWorld().getName().toLowerCase()
				.contains("portal")||event.getBlock().getWorld().getName().toLowerCase()
				.contains("lobby")) {
			for (final Block blk : Util.getNearbyBlocks(event.getBlock()
					.getLocation(), 1)) {

				if (blk.getType() == Material.STAINED_CLAY
						&& (blk.isBlockPowered() || blk
								.isBlockIndirectlyPowered())) {

					if (blk.getState().getData().getData() == DyeColor.PINK
							.getData()) {
						blk.setData(DyeColor.LIME.getData());
						new CheckWireTask(blk, blk, true).runTaskLater(plugin,
								1L);
						wire.add(blk);
						return;
					} else if (blk.getState().getData().getData() == DyeColor.RED
							.getData()) {
						blk.setData(DyeColor.GREEN.getData());
						new CheckWireTask(blk, blk, true).runTaskLater(plugin,
								1L);
						wire.add(blk);
						return;
					}
				} else if (blk.getType() == Material.WALL_SIGN) {
					final Sign s = (Sign) blk.getState();
					if (s.getLine(0).equals("hatch")) {

						org.bukkit.material.Sign sm = (org.bukkit.material.Sign) blk
								.getState().getData();
						Block attachedBlock = blk.getRelative(sm
								.getAttachedFace());
						Block hatchMiddle = null;
						try {
							hatchMiddle = attachedBlock.getRelative(
									BlockFace.DOWN,
									Integer.parseInt(s.getLine(2)) + 3);
						} catch (Exception e) {
							e.printStackTrace();
							return;
						}
						BlockFace[] blockfaces = new BlockFace[] {
								BlockFace.NORTH_EAST, BlockFace.EAST,
								BlockFace.SOUTH_EAST, BlockFace.SOUTH,
								BlockFace.SOUTH_WEST, BlockFace.WEST,
								BlockFace.NORTH_WEST, BlockFace.NORTH,
								BlockFace.SELF

						};
						for (Entry<BukkitTask, Block> t : hatches.entrySet()) {
							if (t.getValue() == blk) {
								t.getKey().cancel();
								hatches.remove(t.getKey());
							}
						}
						if (blk.isBlockPowered()
								|| blk.isBlockIndirectlyPowered()) {

							for (int i = 0; i < blockfaces.length; i++) {

								hatches.put(
										new AnimateHatch(hatchMiddle
												.getRelative(blockfaces[i]),
												false, this).runTaskLater(
														plugin, 3L * i), blk);

							}

						} else {

							for (int i = 0; i < blockfaces.length; i++) {

								hatches.put(
										new AnimateHatch(hatchMiddle
												.getRelative(blockfaces[i]),
												true, this).runTaskLater(
														plugin, 3L * i), blk);
							}

						}
					} else if (s.getLine(0).equals("block")) {

						org.bukkit.material.Sign sm = (org.bukkit.material.Sign) blk
								.getState().getData();
						Block attachedBlock = blk.getRelative(sm
								.getAttachedFace());
						Block hatchMiddle = null;
						try {
							hatchMiddle = attachedBlock.getRelative(
									BlockFace.DOWN, 2);

							int id = 0;
							int data = 0;
							if (!s.getLine(1).isEmpty()) {
								try {
									id = Integer.parseInt(s.getLine(1).split(
											":")[0]);
									data = Integer.parseInt(s.getLine(1).split(
											":")[1]);
								} catch (NumberFormatException
										| IndexOutOfBoundsException nfe) {
									return;
								}
							}
							boolean powered = blk.isBlockPowered()
									|| blk.isBlockIndirectlyPowered();

							Util.clear(hatchMiddle, powered, plugin, id, data,
									blk);

						} catch (Exception e) {
							e.printStackTrace();
							return;
						}

					
					}
				}
			}
		}

	}

	@SuppressWarnings({ "deprecation" })
	@EventHandler
	public void onEntityChangeBlock(final EntityChangeBlockEvent event) {
		if ((event.getEntity().getType().equals(EntityType.FALLING_BLOCK))
				&& cubes.containsValue((FallingBlock) event.getEntity())) {
			
			if (event.getBlock().getRelative(BlockFace.UP).getType() == Material.LAVA
					|| event.getBlock().getRelative(BlockFace.UP).getType() == Material.STATIONARY_LAVA) {
				if (cubes.containsValue((FallingBlock) event.getEntity())) {
					for (Entry<Block, FallingBlock> entry : cubes.entrySet()) {
						if (((FallingBlock) event.getEntity()).equals(entry
								.getValue())) {

							if (cubesign.get(entry.getKey()).isBlockPowered()
									|| cubesign.get(entry.getKey())
									.isBlockIndirectlyPowered()) {
								FallingBlock f = entry
										.getKey()
										.getWorld()
										.spawnFallingBlock(
												entry.getKey().getLocation(),
												entry.getValue().getBlockId(),
												(byte) entry.getValue()
												.getBlockData());
								f.setDropItem(false);
								event.getEntity().remove();
								cubes.remove(entry.getKey());
								cubes.put(entry.getKey(), f);
								event.setCancelled(true);
								return;
							}
						}
					}

				}
			}
			Block blockUnder = event.getBlock().getRelative(BlockFace.DOWN);
			if (blockUnder.getType() == Material.WOOL
					&& (blockUnder.getData() == (byte) 15
					|| blockUnder.getData() == (byte) 14 || blockUnder
					.getData() == (byte) 5)) {

				Block middle = Util.chkBtn(event.getBlock().getLocation());

				if (!(middle == null) && !buttons.containsKey(middle)) {

					Util.changeBtn(middle, !buttons.containsKey(middle));
					buttons.put(middle, event.getBlock());
				}
				for (Entry<Block, FallingBlock> entry : cubes.entrySet()) {
					if (((FallingBlock) event.getEntity()).equals(entry
							.getValue())) {
						cubesFallen.put(entry.getKey(), event.getBlock());
						cubes.remove(entry.getKey());
						return;
					}
				}
			} else if (blockUnder.getType() == Material.WOOL
					&& (blockUnder.getData() == (byte) 1)) {

				for (Entry<Block, FallingBlock> entry : cubes.entrySet()) {
					if (((FallingBlock) event.getEntity()).equals(entry
							.getValue())) {
						event.setCancelled(true);
						String id = String.valueOf(((FallingBlock) event.getEntity())
								.getMaterial().getId())+":"+String.valueOf(((FallingBlock) event.getEntity())
										.getBlockData());
						FrozenSand fblock = new FrozenSandFactory(plugin).withLocation(event.getEntity().getLocation()).withText(id).build();
						fblock.setVelocity(event.getEntity().getVelocity());
						FlyingBlocks.put(entry.getKey(), fblock);
						event.getEntity().remove();
						cubes.remove(entry.getKey());
						return;
					}
				}

			} else {
			
						for (Entry<Block, FallingBlock> entry : cubes.entrySet()) {
							if (((FallingBlock) event.getEntity()).equals(entry
									.getValue())) {
								cubesFallen.put(entry.getKey(), event.getBlock());
								cubes.remove(entry.getKey());
								event.getEntity().remove();
								return;
							}
						}
					

			}
		}



	}

}


