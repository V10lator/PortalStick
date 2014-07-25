package org.PortalStick.listeners;

import java.util.HashSet;
import java.util.List;

import org.PortalStick.Portal;
import org.PortalStick.PortalStick;
import org.PortalStick.Region;
import org.PortalStick.User;
import org.PortalStick.util.RegionSetting;
import org.PortalStick.util.V10Location;
import org.PortalStick.util.Config.Sound;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;




public class PortalStickPlayerListener implements Listener {
	private final PortalStick plugin;
	
	public PortalStickPlayerListener(PortalStick plugin)
	{
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
		else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && player.getItemInHand().getTypeId() == 0 && event.getClickedBlock().getType() == Material.WOOL)
		{
			V10Location loc = new V10Location(event.getClickedBlock());
			Portal portal = plugin.portalManager.borderBlocks.get(loc);
			if (portal == null) portal = plugin.portalManager.insideBlocks.get(loc);
			if (portal == null && plugin.config.CompactPortal) portal = plugin.portalManager.behindBlocks.get(loc);
			if (portal == null) return;
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
 	    
	@EventHandler(ignoreCancelled = false)
	public void onPlayerMove(PlayerMoveEvent event)
	{
	  if(plugin.config.DisabledWorlds.contains(event.getPlayer().getLocation().getWorld().getName()))
		return;
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
}
