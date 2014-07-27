package org.PortalStick.listeners;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.PortalStick.CheckWireTask;
import org.PortalStick.Grill;
import org.PortalStick.PortalStick;
import org.PortalStick.Region;
import org.PortalStick.fallingblocks.FrozenSand;
import org.PortalStick.fallingblocks.FrozenSandFactory;
import org.PortalStick.fallingblocks.FlyingBlockMoveEvent;
import org.PortalStick.util.BlockStorage;
import org.PortalStick.util.RegionSetting;
import org.PortalStick.util.V10Location;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
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
import org.bukkit.material.Directional;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import com.bergerkiller.bukkit.common.events.EntityMoveEvent;
import com.bergerkiller.bukkit.common.events.EntityRemoveEvent;
import com.bergerkiller.bukkit.common.utils.EntityUtil;
import com.bergerkiller.bukkit.common.utils.FaceUtil;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;

public class PortalStickEventListener implements Listener {

    private final ArrayList<V10Location> wire = new ArrayList<V10Location>();
    private final HashMap<UUID, V10Location> buttonsToPlayer = new HashMap<UUID, V10Location>();
	public final HashMap<V10Location, FrozenSand> buttons = new HashMap<V10Location, FrozenSand>();
	public final HashMap<V10Location, UUID> cubes = new HashMap<V10Location, UUID>();
	public final HashMap<V10Location, UUID> cubesPlayer = new HashMap<V10Location, UUID>();
	private final ArrayList<BlockStorage> cubesFallen = new ArrayList<BlockStorage>();
	public final HashMap<V10Location, ItemStack> cubesPlayerItem = new HashMap<V10Location, ItemStack>();
	public final HashSet<UUID> respawnCubes = new HashSet<UUID>();
	private final PortalStick plugin;
	private final ArrayList<FallingBlock> blockMap = new ArrayList<FallingBlock>();
	public final HashMap<V10Location, V10Location> cubesign = new HashMap<V10Location, V10Location>();
	public final HashMap<BukkitTask, V10Location> hatches = new HashMap<BukkitTask, V10Location>();
	public final HashMap<V10Location, FrozenSand> flyingBlocks = new HashMap<V10Location, FrozenSand>();
	final BlockFace[] blockfaces = new BlockFace[] { BlockFace.WEST,
			BlockFace.NORTH_WEST, BlockFace.NORTH, BlockFace.NORTH_EAST,
			BlockFace.EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST,
			BlockFace.SOUTH_EAST };
	public PortalStickEventListener(PortalStick portalStick) {
		plugin = portalStick;
	}
	@EventHandler 
	public void flyingBlockMoveEvent(final FlyingBlockMoveEvent event) {
		if (!flyingBlocks.containsValue(event.getEntity())) return;
		Block under = event.getTo().getBlock().getRelative(BlockFace.DOWN);
		BlockFace face = FaceUtil.getDirection(event.getVelocity());
		Vector half = FaceUtil.faceToVector(face).multiply(0.5);
		Block to = event.getTo().clone().add(half).getBlock();
		Block from = event.getFrom().getBlock();
		Iterator<Entry<V10Location, FrozenSand>> fb = flyingBlocks.entrySet().iterator();
		V10Location respawnLoc = null;
		Entry<V10Location, FrozenSand> e;
		while (fb.hasNext()) {
			e = fb.next();
			if (e.getValue() == event.getEntity()) {
				respawnLoc = e.getKey();
				break;
			}
		}
		
		byte useGel = plugin.gelManager.useGelCube(event.getEntity(), new V10Location(event.getTo()), event.getVelocity(), under);
		Region region = plugin.regionManager.getRegion(new V10Location(to));
		String rg = region.getString(RegionSetting.RED_GEL_BLOCK);
		if (plugin.util.isSolid(to.getType())) {
			if (region.getBoolean(RegionSetting.ENABLE_RED_GEL_BLOCKS) && plugin.blockUtil.compareBlockToString(from.getRelative(BlockFace.DOWN), rg)){

				for (BlockFace rface : FaceUtil.getFaces(face)) {
					if (!plugin.util.isSolid(from.getRelative(rface).getType())) {
						event.setCancelled(true);
						event.getEntity().setVelocity(FaceUtil.faceToVector(rface));
						return;
					}
				}
			} else {
				event.setCancelled(true);
			}

			return;
		}
		if (region.getBoolean(RegionSetting.ENABLE_RED_GEL_BLOCKS) && plugin.blockUtil.compareBlockToString(under, rg)) {
			event.setVelocity(event.getVelocity().multiply(0.9));
			return;
		}

		if (useGel != -1){

			if (useGel == 0) {
				Iterator<FrozenSand> it = flyingBlocks.values().iterator();
				while (it.hasNext()) {
					if (it.next() == event.getEntity()) {
						it.remove();
					}
				}
				event.setCancelled(true);
				event.getEntity().clearAllPlayerViews();
				Location fl = event.getTo();
				fl.setY(fl.getBlockY()+1);
				FallingBlock f = to
						.getWorld()
						.spawnFallingBlock(
								fl,
								event.getEntity().getMaterial(),
								event.getEntity().getData());
				final UUID uuid = f.getUniqueId();
				cubes.put(respawnLoc, uuid);
				plugin.gelManager.ignore.add(uuid);
				f.setDropItem(false);
				Vector v = event.getVelocity().clone();
				v.setY(region.getDouble(RegionSetting.BLUE_GEL_MIN_VELOCITY));
				f.setVelocity(v);
				
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() { public void run() { plugin.gelManager.ignore.remove(uuid); }}, 20L);
			}
			return;
		}

		if (under.getType() == Material.WOOL
				&& (under.getData() == (byte) 15
				|| under.getData() == (byte) 14 || under
				.getData() == (byte) 5)) {

			Block middle = plugin.util.chkBtn(to.getLocation());
			if (middle != null) {
			    V10Location loc = new V10Location(middle);
					if(!buttons.containsKey(loc)) {

					    plugin.util.changeBtn(middle, true);
					    buttons.put(loc, event.getEntity());
					}
			}
		}

		event.setVelocity(event.getVelocity().multiply(0.2));

		if (event.getVelocity().length() < 0.00001)
		    event.setCancelled(true);
	}

	@EventHandler
	public void entityMoveEvent(final EntityMoveEvent event) {
		final Location from = new Location(event.getWorld(), event.getFromX(),
				event.getFromY(), event.getFromZ());
		final Location to = new Location(event.getWorld(), event.getToX(),
				event.getToY(), event.getToZ());
		if (from.getBlockX() == to.getBlockX()
				&& from.getBlockY() == to.getBlockY()
				&& from.getBlockZ() == to.getBlockZ()) {
			return;
		}

		checkPiston(to, event.getEntity());
	}

	@EventHandler
	public void onEntityDeath(EntityRemoveEvent event) {
		if (event.getEntity().getType().equals(EntityType.FALLING_BLOCK)) {
		    FallingBlock fb = (FallingBlock) event.getEntity();
		    if(blockMap.contains((FallingBlock) event.getEntity())) {
		        blockMap.remove((FallingBlock) event.getEntity());
		        return;
		    }
		    
		    UUID uuid = fb.getUniqueId();
		    
			FrozenSand fblock = null;
			for (Entry<V10Location, UUID> entry : cubes.entrySet()) {
				if (uuid.equals(entry.getValue())) {
				    if(!respawnCubes.contains(event.getEntity().getUniqueId())) {
				        String id = String.valueOf(((FallingBlock) event.getEntity())
				                .getMaterial().getId())+":"+String.valueOf(((FallingBlock) event.getEntity())
				                        .getBlockData());
				        fblock = new FrozenSandFactory(plugin).withLocation(event.getEntity().getLocation()).withText(id).build();

				        flyingBlocks.put(entry.getKey(), fblock);
				    } else {
				        respawnCubes.remove(event.getEntity().getUniqueId());
				        plugin.util.clear(entry.getKey().getHandle().getBlock(), true, fb.getBlockId(), fb.getBlockData(), cubesign.get(entry.getKey()).getHandle().getBlock());
				    }
					event.getEntity().remove();
					cubes.remove(entry.getKey());
					break;
				}
			}
			if (fblock == null) return;
			Block blockUnder = event.getEntity().getLocation().getBlock().getRelative(BlockFace.DOWN);
			if (blockUnder.getType() == Material.WOOL
					&& (blockUnder.getData() == (byte) 15
					|| blockUnder.getData() == (byte) 14 || blockUnder
					.getData() == (byte) 5)) {

				Block middle = plugin.util.chkBtn(event.getEntity().getLocation());
				if (middle != null) {
				    V10Location loc = new V10Location(middle);
				    if(!buttons.containsKey(loc)) {

				        plugin.util.changeBtn(middle, true);
				        buttons.put(loc, fblock);
				    }
				}
			} else if (blockUnder.getType() == Material.WOOL
					&& (blockUnder.getData() == (byte) 1)) {

				fblock.setVelocity(event.getEntity().getVelocity());
				return;
			} 
		}else {
			checkPiston(event.getEntity().getLocation(), event.getEntity());
		}
	}

	


	@EventHandler
	public void onPlayerAnimation(PlayerAnimationEvent event) {
		Entry<V10Location, FrozenSand> fb = plugin.util.getTargetFlying(event.getPlayer(),
				plugin);
		if (!(fb == null)) {
		    V10Location loc = fb.getKey();
			flyingBlocks.remove(loc);
			cubesPlayer.put(loc, event.getPlayer().getUniqueId());
			ItemStack item = new ItemStack(Material.getMaterial(Integer.parseInt(fb.getValue().id.split(":")[0])), 1, (byte)Integer.parseInt(fb.getValue().id.split(":")[1]));
			cubesPlayerItem.put(loc, item);

			event.getPlayer().getInventory().addItem(item);
			plugin.util.doInventoryUpdate(event.getPlayer(), plugin);
			fb.getValue().clearAllPlayerViews();
			V10Location middle;
			if (buttons.containsValue(fb.getValue())) {
				Iterator<Entry<V10Location, FrozenSand>> iter = buttons.entrySet().iterator();
				while (iter.hasNext()) {
					Entry<V10Location, FrozenSand> e = iter.next();
					if (e.getValue() == fb.getValue()) {
						middle = e.getKey();
						plugin.util.changeBtn(middle, false);
						iter.remove();
					}
				}
			}
			return;
		}


		Entity en = plugin.util.getTarget(event.getPlayer());
		if (en == null) return;
		for (Entry<V10Location, UUID> entry : cubes.entrySet()) {
			if (en.getUniqueId().equals(entry.getValue())) {
				cubesPlayer.put(entry.getKey(), event.getPlayer().getUniqueId());
				FallingBlock b = (FallingBlock) en;
				ItemStack item = new ItemStack(b.getMaterial(), 1,
						b.getBlockData());
				cubesPlayerItem.put(entry.getKey(), item);

				event.getPlayer().getInventory().addItem(item);
				plugin.util.doInventoryUpdate(event.getPlayer(), plugin);
				en.remove();

				break;

			}
		}





	}
	@EventHandler
	public void PlayerMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		Location to = e.getTo();
		UUID uuid = p.getUniqueId();
		if (!buttonsToPlayer.containsKey(uuid)) {
			Block middle = plugin.util.chkBtn(to);
			if (middle != null) {
			V10Location loc = new V10Location(middle);
			if (!buttonsToPlayer.containsValue(loc)
					&& !buttons.containsKey(loc)) {
				buttonsToPlayer.put(uuid, loc);
				plugin.util.changeBtn(middle, true);
				buttons.put(loc, null);
			}
			}
		} else {
			Block middle = plugin.util.chkBtn(to);
			if (middle == null) {

			    V10Location loc2 = buttonsToPlayer.get(uuid);
				Block middle2 = loc2.getHandle().getBlock();

				plugin.util.changeBtn(middle2,false);
				buttonsToPlayer.remove(uuid);
				buttons.remove(loc2);
			}
		}
		
		Location from =e.getFrom();
		for (FrozenSand s : flyingBlocks.values()) {
			if (plugin.util.compareLocation(e.getTo().getBlock().getLocation(), s.getLocation().getBlock().getLocation())) {
				
				s.setVelocity(FaceUtil.faceToVector(FaceUtil.getDirection(to.toVector().subtract(from.toVector()))));
			}
		}
		if (from.getBlockX() == to.getBlockX()
				&& from.getBlockY() == to.getBlockY()
				&& from.getBlockZ() == to.getBlockZ()) {
			return;
		}

		checkPiston(to, p);
	}


	@EventHandler
	public void death(PlayerDeathEvent event) {
		if (cubesPlayer.containsValue(event.getEntity().getUniqueId())) {
		    Location loc;
		    Block block;
			for (Entry<V10Location, UUID> entry : cubesPlayer.entrySet()) {
				if (event.getEntity().getUniqueId().equals(entry.getValue())) {
				    block = cubesign.get(entry.getKey()).getHandle().getBlock();
					if (block.isBlockPowered()
							|| block.isBlockIndirectlyPowered()) {
					    loc = entry.getKey().getHandle();
						FallingBlock f = loc
								.getWorld()
								.spawnFallingBlock(
										loc,
										cubesPlayerItem.get(entry.getKey())
										.getTypeId(),
										(byte) cubesPlayerItem
										.get(entry.getKey()).getData()
										.getData());
						f.setDropItem(false);
						Iterator<ItemStack> iter = event.getDrops().iterator();
						ItemStack drop;
						while (iter.hasNext()) {
						    drop = iter.next();
							if (drop.getType() == cubesPlayerItem.get(
									entry.getKey()).getType()
									&& drop.getData().getData() == cubesPlayerItem
									.get(entry.getKey()).getData()
									.getData()) {
								iter.remove();
								break;
							}

						}
						cubesPlayer.remove(entry.getKey());
						cubesPlayerItem.remove(entry.getKey());

						cubes.put(entry.getKey(), f.getUniqueId());
					}
				}
			}

			return;
		}
	}


	@EventHandler
	public void drop(PlayerDropItemEvent event) {
		if (cubesPlayer.containsValue(event.getPlayer().getUniqueId())) {

			for (Entry<V10Location, UUID> entry : cubesPlayer.entrySet()) {
				if (event.getPlayer().getUniqueId().equals(entry.getValue())) {
					if (cubesPlayerItem.get(entry.getKey()).getType() == event
							.getItemDrop().getItemStack().getType()
							&& cubesPlayerItem.get(entry.getKey()).getData()
							.getData() == event.getItemDrop()
							.getItemStack().getData().getData()) {

						FallingBlock f = entry
								.getKey()
								.getHandle()
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

						final UUID uuid = event.getPlayer().getUniqueId();
						Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,
								new Runnable() {
							@Override
							public void run() {
							    Player p = plugin.getServer().getPlayer(uuid);
							    if(p == null)
							        return;
								if (p.getItemInHand() != null &&
								        p.getItemInHand()
										.getAmount() - 1 > 1) {
									ItemStack hand = p.getItemInHand();
									hand.setAmount(p.getItemInHand()
											.getAmount() - 1);
									p.setItemInHand(
											hand);

								} else {
									p.setItemInHand(
											null);
								}
							}
						});
						cubes.put(entry.getKey(), f.getUniqueId());
						cubesPlayer.remove(entry.getKey());

					}

					return;
				}
			}
		}

	}


	@EventHandler
	public void blockBreak(PlayerInteractEvent event) {

		if (event.getAction() != Action.LEFT_CLICK_BLOCK
				|| cubesPlayer.containsValue(event.getPlayer()))
			return;
		V10Location loc = new V10Location(event.getClickedBlock());
		Iterator<BlockStorage> iter = cubesFallen.iterator();
		BlockStorage storage;
		while(iter.hasNext()) {
		    storage = iter.next();
		    if(storage.getLocation().equals(loc) &&
		            event.getClickedBlock().getTypeId() == storage.getID() &&
		            event.getClickedBlock().getData() == storage.getData()) {
		        
		        event.setCancelled(true);

		        ItemStack stack = new ItemStack(storage.getID(), storage.getData());
		        
                event.getPlayer().getInventory().addItem(stack);
                event.getPlayer().updateInventory();
                cubesPlayer.put(storage.getLocation(), event.getPlayer().getUniqueId());

                cubesPlayerItem.put(storage.getLocation(), stack);

                event.getClickedBlock().setType(Material.AIR);
                iter.remove();
                break;
		    }
		}
		Block middle = plugin.util.chkBtn(event.getClickedBlock().getLocation());

		if (middle != null) {
		    loc = new V10Location(middle);
		    if(buttons.containsKey(loc)) {

		        plugin.util.changeBtn(middle, false);
		        buttons.remove(loc);
		        return;
		    }
		}
		middle = plugin.util.chkBtnInner(event.getClickedBlock().getLocation());
		if (!(middle == null) && buttons.containsKey(middle)) {
		    loc = new V10Location(middle);
		    plugin.util.changeBtnInner(middle, !buttons.containsKey(loc));

		}

	}

	public boolean checkPiston(Location to, Entity entity) {

		BlockFace pistonBlockFace = null;

		Block orig = to.getBlock();
		Block pistonBlock = null;
		BlockFace[] BlockFaces = new BlockFace[] { BlockFace.UP,
				BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST,
				BlockFace.SOUTH, BlockFace.WEST };
		for (BlockFace bf : BlockFaces) {
			if (orig.getRelative(bf).getType() == Material.PISTON_BASE
					|| orig.getRelative(bf).getType() == Material.PISTON_STICKY_BASE) {

				pistonBlock = to.getBlock().getRelative(bf);
				pistonBlockFace = bf;
				break;
			}
		}
		if (pistonBlock == null) {
			return false;
		}
		BlockFace pistondir = ((Directional) pistonBlock.getState().getData())
				.getFacing().getOppositeFace();
		Block sBlock = pistonBlock.getRelative(pistondir);
		if (pistondir == pistonBlockFace && (sBlock.getType()
				.equals(Material.WALL_SIGN) || sBlock.getType()
				.equals(Material.SIGN_POST))) {
			Sign s = (Sign) sBlock.getState();

			double speed = 0.0D;
			double x = 0.0D;
			double y = 0.0D;
			double z = 0.0D;
			boolean pos = true;
			boolean ok = true;
			if (s.getLine(0).contains("direction")) {
				pos = false;
				y = entity.getLocation().getDirection().getY();
				double tmp = y;
				if (s.getLine(0).contains(",")) {

					String[] text = s.getLine(0).split(",");
					if (y < Double.parseDouble(text[1]))
					{
					    try {
					        y = Double.parseDouble(text[1]);
					    } catch (Exception nfe) {
					        y = tmp;
					    }
					}
				}
			} else {
				String[] text = s.getLine(0).split(",");
				try {
					x = Double.parseDouble(text[0]);
					y = Double.parseDouble(text[1]);
					z = Double.parseDouble(text[2]);
				} catch (Exception nfe) {
					ok = false;
				}
			}
			if(ok) {
			    try {
			        speed = Double.parseDouble(s.getLine(1));
			    } catch (NumberFormatException nfe) {
			        ok = false;
			    }

			    boolean pos2 = pos;
			    if (ok) {

			        if (entity instanceof Player) {
			            sBlock.setType(Material.REDSTONE_BLOCK);
			            if (entity.isInsideVehicle()) {
			                entity.getVehicle().eject();
			            }
			            Location playerloc = entity.getLocation()
			                    .getBlock().getLocation();
			            Vector vector = new Vector(0, 0, 0);
			            Location dest = null;
			            if (pos2) {

			                dest = new Location(playerloc
			                        .getWorld(), x, y, z);

			                vector = dest.toVector().subtract(
			                        playerloc.toVector());


			            } else {
			                vector = entity.getLocation().getDirection();
			            }

			            FallingBlock f = entity.getWorld()
			                    .spawnFallingBlock(playerloc, Material.GLASS, (byte)0);
			            blockMap.add(f);
			            f.setDropItem(false);
			            f.setPassenger(entity);

			            Vector v =vector.normalize()
			                    .multiply(speed);
			            if (!pos2) {
			                v.setY(y);
			            } else {
			                v.setY(vector.getY());
			            }
			            f.setVelocity(v);
			            ProtocolManager pm = ProtocolLibrary.getProtocolManager();
			            PacketContainer metadata = pm.createPacket(PacketType.Play.Server.ENTITY_METADATA);
			            WrappedDataWatcher dw = new WrappedDataWatcher();
			            dw.setObject(0, Byte.valueOf((byte) 0x20));

			            metadata.getIntegers().write(0, f.getEntityId());
			            metadata.getWatchableCollectionModifier().write(0, dw.getWatchableObjects());
			            for (Player pl : Bukkit.getOnlinePlayers()) {
			                try {
			                    pm.sendServerPacket(pl, metadata);
			                } catch (InvocationTargetException e) {
			                    // TODO Auto-generated catch block
			                    e.printStackTrace();
			                }
			            } 




			        }
			        Bukkit.getScheduler().scheduleSyncDelayedTask(
			                plugin,
			                new SignResetter(s),
			                2L);

			        if (entity.isInsideVehicle()) {
			            entity.getVehicle().eject();
			        }
			        Location playerloc = entity.getLocation().getBlock()
			                .getLocation();
			        Vector vector = new Vector(0, 0, 0);
			        if (pos2) {
			            Location dest = new Location(
			                    playerloc.getWorld(), x, y, z);

			            vector = dest.toVector().subtract(
			                    playerloc.toVector());

			        } else {
			            vector = entity.getLocation().getDirection();
			        }

			        FallingBlock f = entity.getWorld().spawnFallingBlock(
			                playerloc, Material.GLASS, (byte)0);
			        blockMap.add(f);
			        f.setDropItem(false);
			        f.setPassenger(entity);
			        f.setVelocity(vector.normalize().multiply(speed));
			        ProtocolManager pm = ProtocolLibrary.getProtocolManager();
			        PacketContainer metadata = pm.createPacket(PacketType.Play.Server.ENTITY_METADATA);
			        WrappedDataWatcher dw = new WrappedDataWatcher();
			        dw.setObject(0, Byte.valueOf((byte) 0x20));

			        metadata.getIntegers().write(0, f.getEntityId());
			        metadata.getWatchableCollectionModifier().write(0, dw.getWatchableObjects());
			        for (Player pl : Bukkit.getOnlinePlayers()) {
			            try {
			                pm.sendServerPacket(pl, metadata);
			            } catch (InvocationTargetException e) {
			                // TODO Auto-generated catch block
			                e.printStackTrace();
			            }
			        }
			        final V10Location loc = new V10Location(sBlock);
			        Bukkit.getScheduler().scheduleSyncDelayedTask(
			                plugin, new Runnable() {
			                    @Override
			                    public void run() {
			                        Block block = loc.getHandle().getBlock();
			                        Sign s = (Sign) block.getState();
			                        block.setType(
			                                Material.REDSTONE_BLOCK);
			                        Bukkit.getScheduler()
			                        .scheduleSyncDelayedTask(
			                                plugin,
			                                new SignResetter(s),
			                                2L);
			                    }
			                }, 2L);
			    }
			}
			return ok;
		}



		return false;
	}
		
	private class SignResetter implements Runnable {
	    final V10Location v10loc;
        final Material type;
        final String[] lines;
        final byte data;
        
        SignResetter(Sign s) {
            this.v10loc = new V10Location(s.getLocation());
            this.type = s.getType();
            this.data = s.getRawData();
            this.lines = s.getLines();
        }
	    public void run() {
	        Block block = v10loc.getHandle().getBlock();
            block.setType(type);
            block.setData(data);
            Sign newSign = (Sign) block.getState();
            for (int i = 0; i < 4; i++) {
                newSign.setLine(i, lines[i]);
            }
            newSign.update();
	    }
	}

	public void cleanUpWire() {
				byte data, data1, data2, data3, data4, data5, data6, data7, data8;
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
							new CheckWireTask(plugin, blk, blk, false).runTaskLater(
									plugin, 1L);
							return;
						} else if (blk.getData() == data3) {
							blk.setData(data4);
							new CheckWireTask(plugin, blk, blk, false).runTaskLater(
									plugin, 1L);
							return;
						}else if (blk.getData() == data5) {
							blk.setData(data6);
							new CheckWireTask(plugin, blk, blk, false).runTaskLater(
									plugin, 1L);
							return;
						}else if (blk.getData() == data7) {
							blk.setData(data8);
							new CheckWireTask(plugin, blk, blk, false).runTaskLater(
									plugin, 1L);
							return;
						}
						iter.remove();

					
				
			}
	}
	@EventHandler
	public void onBlockPhysics(BlockPhysicsEvent event) {

		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				byte data, data1, data2, data3, data4, data5, data6, data7, data8;
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
					if (!(blk.isBlockPowered() || blk
							.isBlockIndirectlyPowered())) {
						if (blk.getData() == data1) {
							blk.setData(data2);
							new CheckWireTask(plugin, blk, blk, false).runTaskLater(
									plugin, 1L);
							return;
						} else if (blk.getData() == data3) {
							blk.setData(data4);
							new CheckWireTask(plugin, blk, blk, false).runTaskLater(
									plugin, 1L);
							return;
						}else if (blk.getData() == data5) {
							blk.setData(data6);
							new CheckWireTask(plugin, blk, blk, false).runTaskLater(
									plugin, 1L);
							return;
						}else if (blk.getData() == data7) {
							blk.setData(data8);
							new CheckWireTask(plugin, blk, blk, false).runTaskLater(
									plugin, 1L);
							return;
						}
						iter.remove();

					}
				}
			}
		}, 2L);
		byte data1, data2, data3, data4, data5, data6, data7, data8;
	    data1 = DyeColor.PINK.getData();
	    data2 = DyeColor.LIME.getData();
	    data3 = DyeColor.RED.getData();
	    data4 = DyeColor.GREEN.getData();
	    data5 = DyeColor.MAGENTA.getData();
	    data6 = DyeColor.YELLOW.getData();
	    data7 = DyeColor.PURPLE.getData();
	    data8= DyeColor.ORANGE.getData();

		for (Block blk : plugin.blockUtil.getNearbyBlocks(event.getBlock()
				.getLocation(), 1)) {

			if (blk.getType() == Material.STAINED_CLAY){
				if (blk.isBlockPowered() || blk
							.isBlockIndirectlyPowered()) {

				if (blk.getData() == data1) {
					blk.setData(data2);
					new CheckWireTask(plugin, blk, blk, true).runTaskLater(plugin,
							1L);
					wire.add(new V10Location(blk));
					return;
				}
				if (blk.getData() == data3) {
				    blk.setData(data4);
				    new CheckWireTask(plugin, blk, blk, true).runTaskLater(plugin,
				            1L);
				    wire.add(new V10Location(blk));
				return;
				}
				if (blk.getData() == data5) {
					blk.setData(data6);
					new CheckWireTask(plugin, blk, blk, true).runTaskLater(plugin,
							1L);
					wire.add(new V10Location(blk));
					return;
				}
				if (blk.getData() == data7) {
				    blk.setData(data8);
				    new CheckWireTask(plugin, blk, blk, true).runTaskLater(plugin,
				            1L);
				    wire.add(new V10Location(blk));
				return;
				}
			} 
				
		} else if (blk.getType() == Material.WALL_SIGN) {
				Sign s = (Sign) blk.getState();
				if (s.getLine(0).equals("cube")) {
					Block attachedBlock = blk.getRelative(((org.bukkit.material.Sign) blk
							.getState().getData()).getAttachedFace());
					
					try {
						final V10Location hatchMiddleLoc = new V10Location(attachedBlock.getRelative(
								BlockFace.DOWN, 2));
						final int id;
						final int data;
						if (!s.getLine(1).isEmpty()) {
							try {
							    String[] split = s.getLine(1).split(":");
								id = Integer.parseInt(split[0]);
								data = split.length > 1 ? Integer.parseInt(split[1]) : 0;
							} catch (Exception nfe) {
								return;
							}
						} else {
							id = data = 0;
						}
						final V10Location loc = new V10Location(blk);
						Bukkit.getScheduler().runTaskLater(plugin, new Runnable(){
								@Override
							public void run() {
								Block blk = loc.getHandle().getBlock();
								if (blk.isBlockPowered()
                                        || blk.isBlockIndirectlyPowered()) {
								    Block next = blk.getRelative(((org.bukkit.material.Sign) blk
											.getState().getData()).getFacing());
									plugin.util.clear(hatchMiddleLoc.getHandle().getBlock(), next.isBlockPowered() || next.isBlockIndirectlyPowered(), id, data,
                                            blk);
								} 
							}}, 1l);
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}


	@EventHandler
	public void onEntityChangeBlock(EntityChangeBlockEvent event) {
		if (event.getEntity().getType().equals(EntityType.FALLING_BLOCK)) {
		    FallingBlock fb = (FallingBlock) event.getEntity();
		    if(blockMap.contains((FallingBlock) event.getEntity())) {
		        event.setCancelled(true);
		        blockMap.remove((FallingBlock) event.getEntity());
		        return;
		    }
		    
		    UUID uuid = fb.getUniqueId();
		    if(cubes.containsValue(uuid)) {
		        Block up = event.getBlock();
		        if (up.getType().name().contains("LAVA")) {
		            Location loc;
		            for (Entry<V10Location, UUID> entry: cubes.entrySet()) {
		                if (uuid.equals(entry.getValue())) {
		                    loc = entry.getKey().getHandle();
		                    FallingBlock f = loc
										.getWorld()
										.spawnFallingBlock(
												loc,
												((FallingBlock) event.getEntity()).getBlockId(),
												(byte)((FallingBlock) event.getEntity())
												.getBlockData());
								f.setDropItem(false);
								event.getEntity().remove();
								cubes.put(entry.getKey(), f.getUniqueId());
								event.setCancelled(true);
								return;
		                    
						}
					}
		        }
			}
			FrozenSand fblock = null;
			for (Entry<V10Location, UUID> entry : cubes.entrySet()) {
				if (uuid.equals(entry.getValue())) {
					event.setCancelled(true);
					String id = String.valueOf(((FallingBlock) event.getEntity())
							.getMaterial().getId())+":"+String.valueOf(((FallingBlock) event.getEntity())
									.getBlockData());
					fblock = new FrozenSandFactory(plugin).withLocation(event.getEntity().getLocation()).withText(id).build();

					flyingBlocks.put(entry.getKey(), fblock);
					event.getEntity().remove();
					cubes.remove(entry.getKey());
					break;
				}
			}
			if (fblock == null) return;
			Block blockUnder = event.getBlock().getRelative(BlockFace.DOWN);
			if (blockUnder.getType() == Material.WOOL
					&& (blockUnder.getData() == (byte) 15
					|| blockUnder.getData() == (byte) 14 || blockUnder
					.getData() == (byte) 5)) {

				Block middle = plugin.util.chkBtn(event.getBlock().getLocation());

				if (middle != null) {
				    V10Location loc = new V10Location(middle);
				    if(!buttons.containsKey(loc)) {

				        plugin.util.changeBtn(middle, true);
				        buttons.put(loc, fblock);
				    }
				}
			} else if (blockUnder.getType() == Material.WOOL
					&& (blockUnder.getData() == (byte) 1)) {

				fblock.setVelocity(event.getEntity().getVelocity());
				return;
			} 
		}else {
			checkPiston(event.getBlock().getLocation(), event.getEntity());
		}
	}
}


