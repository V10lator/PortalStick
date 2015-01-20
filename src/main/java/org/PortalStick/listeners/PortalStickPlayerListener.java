package org.PortalStick.listeners;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.PortalStick.PortalStick;
import org.PortalStick.components.Portal;
import org.PortalStick.components.Region;
import org.PortalStick.components.User;
import com.sanjay900.nmsUtil.fallingblocks.FrozenSand;
import org.PortalStick.util.Config.Sound;
import org.PortalStick.util.RegionSetting;
import org.PortalStick.util.UpdatePlayerView;
import com.sanjay900.nmsUtil.util.Utils;
import com.sanjay900.nmsUtil.util.V10Location;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;
import com.sanjay900.nmsUtil.EntityCubeImpl;

public class PortalStickPlayerListener extends PacketAdapter implements Listener {
	private final PortalStick plugin;

	public PortalStickPlayerListener(PortalStick plugin)
	{
		super(plugin,
				ListenerPriority.NORMAL, 
				PacketType.Play.Client.USE_ENTITY);
		this.plugin = plugin;
	}
	@EventHandler(ignoreCancelled = false)
	public void cubeInteractEvent(PlayerInteractEntityEvent event) {
		if (event.getRightClicked().getLocation().distance(event.getPlayer().getLocation())>2||event.getRightClicked().getLocation().distance(event.getPlayer().getEyeLocation())>2) return;
		Player player = event.getPlayer();
		EntityCubeImpl cube = plugin.util.nmsUtil.getCube(event.getRightClicked());
		if (cube != null) {
			Utils.doInventoryUpdate(event.getPlayer(), plugin);
			plugin.cubeManager.cubesPlayer.put(cube.<V10Location>getStored("respawnLoc"), event.getPlayer().getUniqueId());
			ItemStack item = new ItemStack(((FallingBlock)cube.getBukkitEntity()).getMaterial(), 1,
					((FallingBlock)cube.getBukkitEntity()).getBlockData());
			plugin.cubeManager.cubesPlayerItem.put(cube.<V10Location>getStored("respawnLoc"), item);

			player.getInventory().addItem(item);
			Utils.doInventoryUpdate(player, plugin);

			V10Location middle;
			if (plugin.cubeManager.buttonsToEntity.containsKey(cube.getUniqueID())) {
				middle = plugin.cubeManager.buttonsToEntity.get(cube.getUniqueID());
				plugin.cubeManager.buttonsToEntity.remove(cube.getUniqueID());
				if (!plugin.cubeManager.buttonsToEntity.containsValue(middle)) {
					plugin.util.changeBtn(middle, false);
				}	

				
			}
			cube.getBukkitEntity().remove();
		}
	}
	public void interactFunnelCube(Player player, FrozenSand fb) {
		if (fb.getLocation().distance(player.getLocation())>2||fb.getLocation().distance(player.getEyeLocation())>2) return;
		Utils.doInventoryUpdate(player, plugin);
		plugin.cubeManager.cubesPlayer.put(fb.<V10Location>getData("respawnLoc"), player.getUniqueId());
		ItemStack item = new ItemStack(fb.getMaterial(), 1,
				(short) fb.getData());
		plugin.cubeManager.cubesPlayerItem.put(fb.<V10Location>getData("respawnLoc"), item);

		player.getInventory().addItem(item);
		Utils.doInventoryUpdate(player, plugin);

		V10Location middle;
		if (plugin.cubeManager.buttonsToEntity.containsKey(fb.getUniqueID())) {
			middle = plugin.cubeManager.buttonsToEntity.get(fb.getUniqueID());
			plugin.cubeManager.buttonsToEntity.remove(fb.getUniqueID());
			if (!plugin.cubeManager.buttonsToEntity.containsValue(middle)) {
				plugin.util.changeBtn(middle, false);
			}	

			
		}
		fb.remove();
	}
	@EventHandler(ignoreCancelled = false)
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if(plugin.config.DisabledWorlds.contains(event.getPlayer().getLocation().getWorld().getName()))
			return;

		Player player = event.getPlayer();
		User user = plugin.userManager.getUser(player);

		//Portal tool
		if (plugin.util.isPortalGun(player.getItemInHand()) && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK))
		{
			if(event.getAction() == Action.RIGHT_CLICK_BLOCK)
			{
				Block block = event.getClickedBlock();
				Material mat = block.getType();
				if(mat == Material.STONE_BUTTON || mat == Material.WOOD_BUTTON || mat == Material.LEVER)
					return;
			}


			event.setCancelled(true);
			Location bloc = player.getLocation();
			Region region = plugin.regionManager.getRegion(new V10Location(bloc.getWorld(), bloc.getBlockX(), bloc.getBlockY(), bloc.getBlockZ()));
			HashSet<Byte> tb = new HashSet<Byte>();
			for (int i : region.getList(RegionSetting.TRANSPARENT_BLOCKS).toArray(new Integer[0]))
				tb.add((byte) i);


			if (region.getBoolean(RegionSetting.CHECK_WORLDGUARD) && plugin.worldGuard != null && !plugin.worldGuard.canBuild(player, player.getLocation().getBlock()))
				return;
			if (!plugin.hasPermission(player, plugin.PERM_PLACE_PORTAL))
				return;

			boolean orange = event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK;

			if((orange && !region.getBoolean(RegionSetting.ENABLE_ORANGE_PORTALS)) || !orange && !region.getBoolean(RegionSetting.ENABLE_BLUE_PORTALS))
				return;

			List<Block> targetBlocks = event.getPlayer().getLineOfSight(tb, 120);
			if (targetBlocks.size() < 1)
				return;

			V10Location loc;
			if (region.getBoolean(RegionSetting.PREVENT_PORTAL_THROUGH_PORTAL))
			{
				for (Block b : targetBlocks)
				{
					loc = new V10Location(b);
					for (Portal p : plugin.portalManager.portals)
					{
						for(int i = 0; i < 2; i++)
							if(p.coord.inside[i] != null && p.coord.inside[i].equals(loc))
							{
								Utils.sendMessage
								(
										player
										, 
										plugin.i18n.getString("CannotPlacePortal"
												, 
												player.getName()
												)
										);
								plugin.util.playSound(Sound.PORTAL_CANNOT_CREATE, loc);
								return;
							}
					}
				}
			}

			if (region.getBoolean(RegionSetting.PREVENT_PORTAL_CLOSED_DOOR))
			{
				for (Block b : targetBlocks)
				{
					if ((b.getType() == Material.IRON_DOOR_BLOCK || b.getType() == Material.WOODEN_DOOR) && ((b.getData() & 4) != 4) )
					{
						Utils.sendMessage(player, plugin.i18n.getString("CannotPlacePortal", player.getName()));
						plugin.util.playSound(Sound.PORTAL_CANNOT_CREATE, new V10Location(b));
						return;
					}
					else if (b.getType() == Material.TRAP_DOOR && (b.getData() & 4) == 0)
					{
						Utils.sendMessage(player, plugin.i18n.getString("CannotPlacePortal", player.getName()));
						plugin.util.playSound(Sound.PORTAL_CANNOT_CREATE, new V10Location(b));
						return;

					}
				}
			}

			if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_AIR || tb.contains((byte) event.getClickedBlock().getTypeId()))
			{
				Block b = targetBlocks.get(targetBlocks.size() - 1);
				loc = new V10Location(b);
				if (targetBlocks.size() < 2)
					plugin.portalManager.placePortal(loc, event.getPlayer(), orange);
				else
					plugin.portalManager.placePortal(loc, b.getFace(targetBlocks.get(targetBlocks.size() - 2)), event.getPlayer(), orange, true);
			}
			else
				plugin.portalManager.placePortal(new V10Location(event.getClickedBlock()), event.getBlockFace(), event.getPlayer(), orange, true);
		}
		//Region tool
		else if (user.usingTool && player.getItemInHand().getTypeId() == plugin.config.RegionTool)
		{
			switch (event.getAction()) {
			case RIGHT_CLICK_BLOCK:
				user.pointTwo = new V10Location(event.getClickedBlock());
				Utils.sendMessage(player, plugin.i18n.getString("RegionPointTwoSet", player.getName()));
				event.setCancelled(true);
				break;
			case LEFT_CLICK_BLOCK:
				user.pointOne = new V10Location(event.getClickedBlock());
				Utils.sendMessage(player, plugin.i18n.getString("RegionPointOneSet", player.getName()));
				event.setCancelled(true);
			default:
				break;
			}
		}
		//Flint and steel
		else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && player.getItemInHand().getType() == Material.FLINT_AND_STEEL) {
			{
				V10Location loc = new V10Location(event.getClickedBlock());
				if (plugin.grillManager.createGrill(player, loc) || plugin.funnelBridgeManager.placeGlassBridge(player, loc)) 
					event.setCancelled(true);
			}

		}
		//Color changing

		else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && !plugin.util.isPortalGun(player.getItemInHand()))
		{
			V10Location loc = new V10Location(event.getClickedBlock());
			Portal portal = plugin.portalManager.insideBlocks.get(loc);
			if (portal == null) portal = plugin.portalManager.behindBlocks.get(loc);
			if (portal != null) {
				if (portal.owner.name != player.getName()) return;


				int preset = user.colorPreset;
				if (preset == plugin.config.ColorPresets.size() - 1)
					preset = 0;
				else
					preset++;

				user.colorPreset = preset;
				user.recreatePortals();

				String color1 = DyeColor.values()[plugin.util.getLeftPortalColor(preset)].toString().replace("_", " ");
				String color2 = DyeColor.values()[plugin.util.getRightPortalColor(preset)].toString().replace("_", " ");

				Utils.sendMessage(player, plugin.i18n.getString("SwitchedPortalColor", player.getName(), color1, color2));
			} 
		}
		else if (event.getAction() == Action.LEFT_CLICK_BLOCK
				&& !plugin.cubeManager.cubesPlayer.containsValue(player))
		{
			V10Location loc = new V10Location(event.getClickedBlock());
			Portal portal = plugin.portalManager.insideBlocks.get(loc);
			if (portal == null) portal = plugin.portalManager.behindBlocks.get(loc);
			if (portal != null) {
				if (portal.owner.name != player.getName()) return;
				portal.delete();
			} 

		}

	}

	@EventHandler(ignoreCancelled = false)
	public void onPlayerMove(PlayerMoveEvent event)
	{
		if(plugin.config.DisabledWorlds.contains(event.getPlayer().getLocation().getWorld().getName()))
			return;
		Location to = plugin.entityManager.onEntityMove(event.getPlayer(), event.getFrom(), event.getTo(), true);
		if(to != null) {
			event.setCancelled(true);
		}
	}

	@EventHandler()
	public void noPickup(PlayerPickupItemEvent event)
	{
		Item item = event.getItem();
		if(plugin.config.DisabledWorlds.contains(item.getWorld().getName()))
			return;
		Location loc = item.getLocation();
		V10Location iloc = new V10Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		Region region = plugin.regionManager.getRegion(iloc);
		Player player = event.getPlayer();
		User user = plugin.userManager.getUser(player);
		if(!region.getBoolean(RegionSetting.GRILLS_REMOVE_ITEMS) || user.usingTool)
			return;
		ItemStack is = item.getItemStack();
		for(Object iss: region.getList(RegionSetting.GRILL_REMOVE_EXCEPTIONS))
		{
			if(is.getTypeId() == (Integer)iss)
				return;
		}
		loc = player.getLocation();
		V10Location ploc = new V10Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		int a, b;
		boolean x;
		if(ploc.getX() != iloc.getX())
		{
			a = (int)ploc.getX();
			b = (int)iloc.getX();
			x = true;
		}
		else if(ploc.getZ() != iloc.getZ())
		{
			a = (int)ploc.getZ();
			b = (int)iloc.getZ();
			x = false;
		}
		else
			return;
		if(a > b)
		{
			int tmp = a;
			a = b;
			b = tmp;
		}
		for(; a < b; a++)
		{
			if(x)
				iloc = new V10Location(iloc.getWorldName(), a, iloc.getY(), iloc.getZ());
			else
				iloc = new V10Location(iloc.getWorldName(), iloc.getX(), iloc.getY(), a);
			if(plugin.grillManager.insideBlocks.containsKey(iloc))
			{
				if(plugin.grillManager.insideBlocks.get(iloc).disabled)
					continue;
				event.setCancelled(true);
				item.remove();
				Location el = item.getLocation();
				if(x)
					el.setX(a);
				else
					el.setZ(a);
				plugin.grillManager.playGrillAnimation(el);
				return;
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void trackDrops(PlayerDropItemEvent event)
	{
		if(plugin.config.DisabledWorlds.contains(event.getPlayer().getLocation().getWorld().getName()))
			return;
		Player player = event.getPlayer();
		Location loc = player.getLocation();

		Region region = plugin.regionManager.getRegion(new V10Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));

		if(!region.getBoolean(RegionSetting.GRILLS_CLEAR_ITEM_DROPS))
			return;

		Item item = event.getItemDrop();
		ItemStack is = item.getItemStack();

		if(!region.getBoolean(RegionSetting.GRILL_GIVE_GUN_IF_NEEDED))
		{
			int id;
			for(Object iss: region.getList(RegionSetting.GRILL_REMOVE_EXCEPTIONS))
			{
				id = (Integer)iss;
				if(is.getTypeId() == id)
					return;
			}
		}
		plugin.userManager.getUser(player).droppedItems.add(item);
	}

	@EventHandler
	public void drop(PlayerDropItemEvent event) {
		if(plugin.config.DisabledWorlds.contains(event.getPlayer().getLocation().getWorld().getName()))
			return;
		if (plugin.cubeManager.cubesPlayer.containsValue(event.getPlayer().getUniqueId())) {

			for (Entry<V10Location, UUID> entry : plugin.cubeManager.cubesPlayer.entrySet()) {
				if (event.getPlayer().getUniqueId().equals(entry.getValue())) {
					if (plugin.cubeManager.cubesPlayerItem.get(entry.getKey()).getType() == event
							.getItemDrop().getItemStack().getType()
							&& plugin.cubeManager.cubesPlayerItem.get(entry.getKey()).getData()
							.getData() == event.getItemDrop()
							.getItemStack().getData().getData()) {
						HashMap<String,Object> storedData = new HashMap<>();
						storedData.put("respawnLoc", entry.getKey());
						EntityCubeImpl c = plugin.util.nmsUtil.createCube(event.getPlayer().getEyeLocation(), event.getItemDrop().getItemStack()
								.getTypeId(), event.getItemDrop().getItemStack()
								.getData().getData(), storedData);
						FallingBlock f = (FallingBlock) c.getBukkitEntity();
						f.setDropItem(false);
						f.setVelocity(event.getPlayer().getLocation()
								.getDirection().multiply(0.3));
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
										.getAmount() - 1 > 0) {
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
						plugin.cubeManager.cubesPlayer.remove(entry.getKey());

					}

					return;
				}
			}
		}

	}

	@EventHandler
	public void death(PlayerDeathEvent event) {
		if(plugin.config.DisabledWorlds.contains(event.getEntity().getLocation().getWorld().getName()))
			return;
		if (plugin.cubeManager.cubesPlayer.containsValue(event.getEntity().getUniqueId())) {
			Location loc;
			Block block;
			for (Entry<V10Location, UUID> entry : plugin.cubeManager.cubesPlayer.entrySet()) {
				if (event.getEntity().getUniqueId().equals(entry.getValue())) {
					block = plugin.cubeManager.cubesign.get(entry.getKey()).getHandle().getBlock();
					if (block.isBlockPowered()
							|| block.isBlockIndirectlyPowered()) {
						loc = entry.getKey().getHandle();
						plugin.util.clear(loc.getBlock(), true, plugin.cubeManager.cubesPlayerItem.get(entry.getKey())
								.getTypeId(), plugin.cubeManager.cubesPlayerItem
								.get(entry.getKey()).getData().getData(), block, false, null);

						Iterator<ItemStack> iter = event.getDrops().iterator();
						ItemStack drop;
						while (iter.hasNext()) {
							drop = iter.next();
							if (drop.getType() == plugin.cubeManager.cubesPlayerItem.get(
									entry.getKey()).getType()
									&& drop.getData().getData() == plugin.cubeManager.cubesPlayerItem
									.get(entry.getKey()).getData()
									.getData()) {
								iter.remove();
								break;
							}

						}
						plugin.cubeManager.cubesPlayer.remove(entry.getKey());
						plugin.cubeManager.cubesPlayerItem.remove(entry.getKey());
					}
				}
			}

			return;
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void login(PlayerJoinEvent event) {
		Bukkit.getScheduler().runTaskLater(plugin, new UpdatePlayerView(plugin, event.getPlayer().getUniqueId()),80L);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onTeleport(final PlayerTeleportEvent event) {
		if (event.getFrom().distance(event.getTo()) > 20)
			Bukkit.getScheduler().runTaskLater(plugin, new UpdatePlayerView(plugin, event.getPlayer().getUniqueId()),10L);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void worldMove (PlayerChangedWorldEvent event) {
		Bukkit.getScheduler().runTaskLater(plugin, new UpdatePlayerView(plugin, event.getPlayer().getUniqueId()), 10L);
	}

	@Override
	public void onPacketReceiving(final PacketEvent event) {
		final PacketContainer packet = event.getPacket();
		final int entityID = packet.getIntegers().read(0);
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){

			@Override
			public void run() {
				for (FrozenSand f: plugin.util.nmsUtil.frozenSandManager.fakeBlocks.keySet()) {  
					if (f.entityId+2 == entityID) {
						if (f.<V10Location>getData("respawnLoc") == null) {
						EntityUseAction action = packet.getEntityUseActions().read(0);
						onPlayerInteract(new PlayerInteractEvent(event.getPlayer(), action == EntityUseAction.INTERACT?Action.RIGHT_CLICK_BLOCK:Action.LEFT_CLICK_BLOCK, event.getPlayer().getItemInHand(), f.getLocation().getBlock(), null));
						} else {
							interactFunnelCube(event.getPlayer(),f);
						}
						return;
					}


				}
				for (Entity en : event.getPlayer().getWorld().getEntities()) {
					if (plugin.util.nmsUtil.getCube(en) != null && en.getEntityId() ==entityID) {
						cubeInteractEvent(new PlayerInteractEntityEvent(event.getPlayer(),en));
						return;
					}
				}
			}
		});
	}
}
