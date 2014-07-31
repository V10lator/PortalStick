package org.PortalStick.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;

import org.PortalStick.Portal;
import org.PortalStick.PortalStick;
import org.PortalStick.Region;
import org.PortalStick.User;
import org.PortalStick.fallingblocks.FrozenSand;
import org.PortalStick.util.BlockStorage;
import org.PortalStick.util.RegionSetting;
import org.PortalStick.util.UpdatePlayerView;
import org.PortalStick.util.V10Location;
import org.PortalStick.util.Config.Sound;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

import com.bergerkiller.bukkit.common.utils.EntityUtil;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;




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
							plugin
							.
							util
							.
							sendMessage
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
						plugin.util.sendMessage(player, plugin.i18n.getString("CannotPlacePortal", player.getName()));
						plugin.util.playSound(Sound.PORTAL_CANNOT_CREATE, new V10Location(b));
						return;
					}
					else if (b.getType() == Material.TRAP_DOOR && (b.getData() & 4) == 0)
					{
						plugin.util.sendMessage(player, plugin.i18n.getString("CannotPlacePortal", player.getName()));
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
					plugin.util.sendMessage(player, plugin.i18n.getString("RegionPointTwoSet", player.getName()));
					event.setCancelled(true);
					break;
				case LEFT_CLICK_BLOCK:
					user.pointOne = new V10Location(event.getClickedBlock());
					plugin.util.sendMessage(player, plugin.i18n.getString("RegionPointOneSet", player.getName()));
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

			    plugin.util.sendMessage(player, plugin.i18n.getString("SwitchedPortalColor", player.getName(), color1, color2));
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
			} else {
			    // Take cube
				
				ArrayList<Entry<V10Location, FrozenSand>> sandToRemove = new ArrayList<Entry<V10Location, FrozenSand>>();
				Iterator<Entry<V10Location, FrozenSand>> it = plugin.cubeManager.flyingBlocks.entrySet().iterator();
				while (it.hasNext()) {
					Entry<V10Location, FrozenSand> e = it.next();
					
					if (plugin.util.compareLocation(e.getValue().getLocation().getBlock().getLocation(), loc.getHandle().getBlock().getLocation())) {
						
						FrozenSand fb = e.getValue();
		                plugin.cubeManager.cubesPlayer.put(e.getKey(), player.getUniqueId());
		                ItemStack item = new ItemStack(Material.getMaterial(Integer.parseInt(fb.id.split(":")[0])), 1, (byte)Integer.parseInt(fb.id.split(":")[1]));
		                plugin.cubeManager.cubesPlayerItem.put(e.getKey(), item);

		                player.getInventory().addItem(item);
		                plugin.util.doInventoryUpdate(player, plugin);
		               
		                V10Location middle;
		                if (plugin.cubeManager.buttons.containsValue(fb)) {
		                    Iterator<Entry<V10Location, FrozenSand>> iter = plugin.cubeManager.buttons.entrySet().iterator();
		                    while (iter.hasNext()) {
		                        Entry<V10Location, FrozenSand> enbt = iter.next();
		                        if (enbt.getValue() == fb) {
		                            middle = enbt.getKey();
		                            plugin.util.changeBtn(middle, false);
		                                iter.remove();
		                        }
		                    }
		                }
		                fb.remove();
		                sandToRemove.add(e);
		                
					}
					
				}
				plugin.cubeManager.flyingBlocks.entrySet().removeAll(sandToRemove);
			}
		    
		}

	}
 	    
	@EventHandler(ignoreCancelled = false)
	public void onPlayerMove(PlayerMoveEvent event)
	{
	  if(plugin.config.DisabledWorlds.contains(event.getPlayer().getLocation().getWorld().getName()))
		return;
	  for (Entry<FrozenSand, ArrayList<UUID>> e : plugin.frozenSandManager.playerMap.entrySet())
	  {
		  if (e.getKey().getLocation().distance(event.getTo()) < 30) {
			  if (!e.getValue().contains(event.getPlayer().getUniqueId())) {
				  e.getKey().shownc(event.getPlayer());
			  }
		  } else {
			  if (e.getValue().contains(event.getPlayer().getUniqueId())) {
				  e.getValue().remove(event.getPlayer().getUniqueId());
			  }
		  }
	  }
		Location to = plugin.entityManager.onEntityMove(event.getPlayer(), event.getFrom(), event.getTo(), false);
	  if(to != null) {
		event.setTo(to);
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
	                    plugin.cubeManager.cubes.put(entry.getKey(), f.getUniqueId());
	                    plugin.cubeManager.cubesPlayer.remove(entry.getKey());

	                }

	                return;
	            }
	        }
	    }

	}
	@EventHandler
	public void quit (PlayerQuitEvent event) {
		for (Entry<FrozenSand, ArrayList<UUID>> e : plugin.frozenSandManager.playerMap.entrySet())
		  {
				  if (e.getValue().contains(event.getPlayer().getUniqueId())) {
					  e.getValue().remove(event.getPlayer().getUniqueId());
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
	                    FallingBlock f = loc
	                            .getWorld()
	                            .spawnFallingBlock(
	                                    loc,
	                                    plugin.cubeManager.cubesPlayerItem.get(entry.getKey())
	                                    .getTypeId(),
	                                    (byte) plugin.cubeManager.cubesPlayerItem
	                                    .get(entry.getKey()).getData()
	                                    .getData());
	                    f.setDropItem(false);
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

	                    plugin.cubeManager.cubes.put(entry.getKey(), f.getUniqueId());
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
	    for (final FrozenSand f: plugin.frozenSandManager.fakeBlocks) {  
	        if (f.entityId+2 == entityID) {
	        	final EntityUseAction action = packet.getEntityUseActions().read(0);
	           
					onPlayerInteract(new PlayerInteractEvent(event.getPlayer(), action == EntityUseAction.INTERACT?Action.RIGHT_CLICK_BLOCK:Action.LEFT_CLICK_BLOCK, event.getPlayer().getItemInHand(), f.getLocation().getBlock(), null));
	
	        	
	            return;
	        }
		}
	    
	    Iterator<Entry<V10Location, UUID>> it = plugin.cubeManager.cubes.entrySet().iterator();
	   while (it.hasNext()) {
		   Entry<V10Location, UUID> entry = it.next();
	    	Entity en =EntityUtil.getEntity(event.getPlayer().getWorld(), entry.getValue());
	    	if (en == null) break;
	    	
	    	if (en.getEntityId() == entityID) {
	    		plugin.cubeManager.cubesPlayer.put(entry.getKey(), event.getPlayer().getUniqueId());
	            FallingBlock b = (FallingBlock) en;
	            ItemStack item = new ItemStack(b.getMaterial(), 1,
	                    b.getBlockData());
	            plugin.cubeManager.cubesPlayerItem.put(entry.getKey(), item);

	            event.getPlayer().getInventory().addItem(item);
	            plugin.util.doInventoryUpdate(event.getPlayer(), plugin);
	            en.remove();
	            it.remove();
	            break;
	    	}
	    }
			}});
	}
}
