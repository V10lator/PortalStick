package com.matejdro.bukkit.portalstick;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Wool;
import org.libigot.LibigotLocation;
import org.libigot.block.BlockStorage;

import com.matejdro.bukkit.portalstick.util.Config.Sound;
import com.matejdro.bukkit.portalstick.util.RegionSetting;

public class PortalManager {
	private final PortalStick plugin;
	
	PortalManager(PortalStick plugin)
	{
		this.plugin = plugin;
	}
	
	public final ArrayList<Portal> portals = new ArrayList<Portal>();
	public final HashMap<LibigotLocation, Portal> borderBlocks = new HashMap<LibigotLocation, Portal>();
	public final HashMap<LibigotLocation, Portal> behindBlocks = new HashMap<LibigotLocation, Portal>();
	public final HashMap<LibigotLocation, Portal> insideBlocks = new HashMap<LibigotLocation, Portal>();
	final HashMap<LibigotLocation, Portal> awayBlocks = new HashMap<LibigotLocation, Portal>();
	final HashMap<LibigotLocation, Portal> awayBlocksY = new HashMap<LibigotLocation, Portal>();
	public final HashMap<LibigotLocation, BlockStorage> oldBlocks = new HashMap<LibigotLocation, BlockStorage>();

	public void checkEntityMove(Entity e, Region regionFrom, Region regionTo)
	{
	  if(!(e instanceof InventoryHolder))
		return;
	  
	  InventoryHolder ih = (InventoryHolder)e;
	  User user = plugin.userManager.getUser(e);
	  
	  if (user == null || user.usingTool)
		return;
	  if (!regionTo.name.equals(regionFrom.name)) {
		if(ih instanceof Player && (regionFrom.getBoolean(RegionSetting.DELETE_ON_EXITENTRANCE) || regionTo.getBoolean(RegionSetting.DELETE_ON_EXITENTRANCE)))
		  deletePortals(user);
		
		if (regionFrom.getBoolean(RegionSetting.UNIQUE_INVENTORY) || regionTo.getBoolean(RegionSetting.UNIQUE_INVENTORY))
		{
		  if (regionTo.name.equalsIgnoreCase("global"))
			user.revertInventory(ih);
		  else
		  {
			user.saveInventory(ih);
			setPortalInventory(ih, regionTo);
		  }
		}
	  }
	}
	
	private boolean checkPortal(PortalCoord portal)
	{
		Region region;
		int id;
		ArrayList<Portal> overlap = new ArrayList<Portal>();
		boolean ol;
		BlockStorage bh;
		Block block;
		for (LibigotLocation loc: portal.border)
		{
			if(borderBlocks.containsKey(loc))
			{
			  overlap.add(borderBlocks.get(loc));
			  ol = true;
			}
			else if(insideBlocks.containsKey(loc))
			{
			  overlap.add(insideBlocks.get(loc));
			  ol = true;
			}
			else if(behindBlocks.containsKey(loc))
			{
			  overlap.add(behindBlocks.get(loc));
			  ol = false;
			}
			else
			  ol = false;
			
			if(!ol)
			{
			  block = loc.getHandle().getBlock();
			  id = block.getTypeId();
			  region = plugin.regionManager.getRegion(loc);
			  if(!region.getBoolean(RegionSetting.ALL_BLOCKS_PORTAL))
			  {
				bh = new BlockStorage(block);
				if(plugin.gelManager.gelMap.containsKey(bh))
				{
				  bh = plugin.gelManager.gelMap.get(bh);
				  id = bh.getID();
				}
				if(!region.getList(RegionSetting.PORTAL_BLOCKS).contains(id))
				  return false;
			  }
			}
		}
		for (LibigotLocation loc: portal.inside)
		{
			if(loc == null)
			  continue;
			if(borderBlocks.containsKey(loc))
			{
			  overlap.add(borderBlocks.get(loc));
			  ol = true;
			}
			else if(insideBlocks.containsKey(loc))
			{
			  overlap.add(insideBlocks.get(loc));
			  ol = true;
			}
			else if(behindBlocks.containsKey(loc))
			{
			  overlap.add(behindBlocks.get(loc));
			  ol = false;
			}
			else
			  ol = false;
			
			if(!ol)
			{
			  block = loc.getHandle().getBlock();
			  id = block.getTypeId();
			  region = plugin.regionManager.getRegion(loc);
			  if(!region.getBoolean(RegionSetting.ALL_BLOCKS_PORTAL))
			  {
				bh = new BlockStorage(block);
				if(plugin.gelManager.gelMap.containsKey(bh))
				{
				  bh = plugin.gelManager.gelMap.get(bh);
				  id = bh.getID();
				}
				if(!region.getList(RegionSetting.PORTAL_BLOCKS).contains(id))
				  return false;
			  }
			}
		}
		for(Portal p: overlap)
		  p.delete();
		return true;
	}

	public void deletePortals(User user)
	{
		if (user.bluePortal != null) user.bluePortal.delete();
		if (user.orangePortal != null) user.orangePortal.delete();
	}

	private PortalCoord generateHorizontalPortal(LibigotLocation block, BlockFace face)
	{
		PortalCoord portal = generatePortal(block, face); // 0
		if(!checkPortal(portal))
		{
		  block = new LibigotLocation(block.getHandle().getBlock().getRelative(BlockFace.DOWN)); // -1
		  portal = generatePortal(block, face);
		  if(!checkPortal(portal))
		  {
			block = new LibigotLocation(block.getHandle().getBlock().getRelative(BlockFace.DOWN)); // -2 TODO: Doesn't work
			portal = generatePortal(block, face);
			if(!checkPortal(portal))
			{
			  block = new LibigotLocation(block.getHandle().getBlock().getRelative(BlockFace.UP, 3)); // 1 (-2 + 3)
			  portal = generatePortal(block, face);
			  if(!checkPortal(portal))
			  {
				block = new LibigotLocation(block.getHandle().getBlock().getRelative(BlockFace.UP)); // 2
				portal = generatePortal(block, face);
				if(!checkPortal(portal))
				  portal.finished = true;
			  }
			}
		  }
		}
		return portal;
	}

	private PortalCoord generatePortal(LibigotLocation block, BlockFace face)
	{
		PortalCoord portal = new PortalCoord();
		Block rb = block.getHandle().getBlock();
		
		switch(face)
		{
		  case DOWN:
		  case UP:
			if (!plugin.config.CompactPortal || plugin.config.FillPortalBack < 0)
			{
				portal.border.add(new LibigotLocation(rb.getRelative(BlockFace.NORTH)));
				if(!plugin.config.CompactPortal)
				{
				  portal.border.add(new LibigotLocation(rb.getRelative(BlockFace.NORTH_WEST))); 
				  portal.border.add(new LibigotLocation(rb.getRelative(BlockFace.WEST)));
				  portal.border.add(new LibigotLocation(rb.getRelative(BlockFace.SOUTH_WEST)));
				  portal.border.add(new LibigotLocation(rb.getRelative(BlockFace.SOUTH)));
				  portal.border.add(new LibigotLocation(rb.getRelative(BlockFace.SOUTH_EAST)));
				  portal.border.add(new LibigotLocation(rb.getRelative(BlockFace.EAST)));
				  portal.border.add(new LibigotLocation(rb.getRelative(BlockFace.NORTH_EAST)));
				}
			}
			
			portal.inside[0] = new LibigotLocation(rb);
	    	
	    	portal.destLoc[0] = new LibigotLocation(rb.getRelative(face));
	    	face = face.getOppositeFace();
			portal.behind[0] = new LibigotLocation(rb.getRelative(face));
	    	portal.tpFace = face;
	    	portal.vertical = true;
	    	return portal;
		  case NORTH:
		  case NORTH_EAST:
			face = BlockFace.SOUTH;
			break;
		  case EAST:
		  case SOUTH_EAST:
		    face = BlockFace.WEST;
		    break;
		  case SOUTH:
		  case SOUTH_WEST:
	    	face = BlockFace.NORTH;
	    	break;
		  default:
	    	face = BlockFace.EAST;
	    	break;
		}
	    
	    portal.tpFace = face;
	    
	    switch(face)
	    {
	      case NORTH:
	      case SOUTH:
	    	face = BlockFace.EAST;
	    	break;
	      default:
	    	face = BlockFace.NORTH;
	    }
	    
	    if (!plugin.config.CompactPortal || plugin.config.FillPortalBack < 0)
	    {
	      Block block2 = rb.getRelative(BlockFace.DOWN, 2);
	      portal.border.add(new LibigotLocation(block2));
	      
	      if(!plugin.config.CompactPortal)
	      {
	    	block2 = block2.getRelative(face);
	    	portal.border.add(new LibigotLocation(block2));
	    	for(int i = 0; i < 3; i++)
		    {
	    	  block2 = block2.getRelative(BlockFace.UP);
	    	  portal.border.add(new LibigotLocation(block2));
		    }
	    	face = face.getOppositeFace();
	    	for(int i = 0; i < 2; i++)
	    	{
	    	  block2 = block2.getRelative(face);
	    	  portal.border.add(new LibigotLocation(block2));
	    	}
	    	for(int i = 0; i < 3; i++)
	    	{
	    	  block2 = block2.getRelative(BlockFace.DOWN);
	    	  portal.border.add(new LibigotLocation(block2));
	    	}
	      }
	    }
	    
	    portal.inside[1] = block;
	    Block block2 = rb.getRelative(BlockFace.DOWN);
	    portal.inside[0] = new LibigotLocation(block2);
	    
	    Block block3 = block2.getRelative(portal.tpFace.getOppositeFace());
	    portal.destLoc[0] = new LibigotLocation(block3);
	    portal.destLoc[1] = new LibigotLocation(block3.getRelative(BlockFace.UP));
	    
	    portal.vertical = false;
	    
	    block2 = block2.getRelative(portal.tpFace);
	    portal.behind[0] = new LibigotLocation(block2);
	    portal.behind[1] = new LibigotLocation(block2.getRelative(BlockFace.UP));
	    
		return portal;
	}

	public boolean placePortal(LibigotLocation block, BlockFace face, Player player, boolean orange, boolean end)
	{
		//Check if player can place here
		Location loc = block.getHandle();
		Region region = plugin.regionManager.getRegion(block);
		if (region.getBoolean(RegionSetting.CHECK_WORLDGUARD) && plugin.worldGuard != null && !plugin.worldGuard.canBuild(player, loc))
			return false;
		
		boolean vertical = false;
		PortalCoord portalc;
		User owner = plugin.userManager.getUser(player);
		
		if (face == BlockFace.DOWN || face == BlockFace.UP)
		{
			vertical = true;
			portalc = generatePortal(block, face);
			if (!checkPortal(portalc))
			{
				if (end) plugin.util.sendMessage(player, plugin.i18n.getString("CannotPlacePortal", player.getName()));
				plugin.util.playSound(Sound.PORTAL_CANNOT_CREATE, block);
				return false;
			}
		}
		else
		{
			portalc = generateHorizontalPortal(block, face);
			if (portalc.finished)
			{
				if (end) plugin.util.sendMessage(player, plugin.i18n.getString("CannotPlacePortal", player.getName()));
				plugin.util.playSound(Sound.PORTAL_CANNOT_CREATE, block);
				return false;
			}
		}
		
		Portal portal = new Portal(plugin, portalc.destLoc, portalc.border, portalc.inside, portalc.behind, owner, orange, vertical, portalc.tpFace);
		
		
		if (orange)
		{
			if (owner.orangePortal != null)
			  owner.orangePortal.delete();
			owner.orangePortal = portal;
			plugin.util.playSound(Sound.PORTAL_CREATE_ORANGE, block);
			
		}
		else
		{
			if (owner.bluePortal != null)
			  owner.bluePortal.delete();
			owner.bluePortal = portal;
			plugin.util.playSound(Sound.PORTAL_CREATE_BLUE, block);
		}
		
		portals.add(portal);
		portal.create();
		
		return true;
		
	}
 
	public void placePortal(LibigotLocation block, Player player, boolean orange)
	{
		
		float dir = (float)Math.toDegrees(Math.atan2(player.getLocation().getBlockX() - block.getX(), block.getZ() - player.getLocation().getBlockZ()));
		dir = dir % 360;
	    if(dir < 0)
	    	dir += 360;
	    
		//Try WEST/EAST
		if (dir < 90 || dir > 270)
		{
			if (placePortal(block, BlockFace.NORTH, player, orange, false))
			  return;
		}
		else if (placePortal(block, BlockFace.SOUTH, player, orange, false))
		  return;
		
		//Try NORTH/SOUTH
		if (dir < 180) 
		{
			if (placePortal(block, BlockFace.EAST, player, orange, false))
			  return;
		}
		else if (placePortal(block, BlockFace.WEST, player, orange, false))
		  return;
		
		//Try UP/DOWN
		if (player.getEyeLocation().getY() >= block.getY())
		{
			if (placePortal(block, BlockFace.UP, player, orange, false))
			  return;
		}
		else if (placePortal(block, BlockFace.DOWN, player, orange, true))
		  return;
	
	 }

	public void setPortalInventory(InventoryHolder ih, Region region)
	{
		if(region.getBoolean(RegionSetting.UNIQUE_INVENTORY))
		{
		  ItemStack item;
		  Inventory inv = ih.getInventory();
		  for (Object is : region.getList(RegionSetting.UNIQUE_INVENTORY_ITEMS))
		  {
			item = plugin.util.getItemData((String)is);
			inv.addItem(item);
		  }
		}
	}
	
	public void tryPlacingAutomatedPortal(Block rb)
	{
		//Check if wool is correct
		Wool wool = (Wool) Material.WOOL.getNewData(rb.getData());
		boolean orange;
		boolean black;
		switch(wool.getColor())
		{
		  case LIGHT_BLUE:
			orange = false;
			black = false;
			break;
		  case ORANGE:
			orange = true;
			black = false;
			break;
		  case BLACK:
			orange = false;
			black = true;
			break;
		  default:
			return;
		}
		
		//Find first iron:
		Block[] iron = new Block[4];
		BlockFace[] upDown = new BlockFace[] { BlockFace.UP, BlockFace.DOWN };
		for(BlockFace face: new BlockFace[] { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST })
		{
		  iron[0] = rb.getRelative(face, 2);
		  if(iron[0].getType() != Material.IRON_FENCE)
		  {
			for(BlockFace face2: upDown)
			{
			  iron[1] = iron[0].getRelative(face2);
			  if(iron[1].getType() == Material.IRON_FENCE)
			  {
				iron[0] = iron[1];
				break;
			  }
			}
		  }
		  if(iron[0].getType() == Material.IRON_FENCE)
			break;
		}
		if(iron[0].getType() != Material.IRON_FENCE)
		  return;
		
		//Find connected iron
		if(iron[0].getRelative(BlockFace.UP).getType() == Material.IRON_FENCE)
		  iron[1] = iron[0].getRelative(BlockFace.UP);
		else
		{
		  iron[1] = iron[0].getRelative(BlockFace.DOWN);
		  if(iron[1].getType() != Material.IRON_FENCE)
			return;
		  iron[2] = iron[0];
		  iron[0] = iron[1];
		  iron[1] = iron[2];
		}
		
		//Find iron at other side:
		BlockFace dir = null;
		for(BlockFace face: new BlockFace[] { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST })
		{
		  iron[2] = iron[0].getRelative(face, 2);
		  iron[3] = iron[1].getRelative(face, 2);
		  if(iron[2].getType() == Material.IRON_FENCE && iron[3].getType() == Material.IRON_FENCE)
		  {
			dir = face;
			break;
		  }
		}
		if(dir == null)
		  return;
		
		//Get possibe portal surface directions:
		BlockFace[] portalDirection = new BlockFace[2];
		switch(dir)
		{
		  case NORTH:
		  case SOUTH:
			portalDirection[0] = BlockFace.EAST;
			portalDirection[1] = BlockFace.WEST;
			break;
		  default:
			portalDirection[0] = BlockFace.NORTH;
			portalDirection[1] = BlockFace.SOUTH;
		}
		
		//Find portal surface:
		PortalCoord pc = null;
		Region region = plugin.regionManager.getRegion(new LibigotLocation(rb));
		boolean ap = region.getBoolean(RegionSetting.ALL_BLOCKS_PORTAL);
		List<?> pb = region.getList(RegionSetting.PORTAL_BLOCKS);
		ArrayList<Portal> oldPortals = new ArrayList<Portal>();
		boolean done = false;
		for(BlockFace face: portalDirection)
		{
		  pc = new PortalCoord();
		  oldPortals.clear();
		  pc.tpFace = face;
		  if(checkRelative(dir, face, iron[0], false, oldPortals, ap, pb, pc) &&
				checkRelative(dir, face, iron[1], true, oldPortals, ap, pb, pc))
		  {
			done = true;
			break;
		  }
		}
		if(!done)
		  return;
		
		//Delete old portals
		for(Portal p: oldPortals)
			p.delete();
		
		if(black)
		  return;
		
		Portal portal = new Portal(plugin, pc.destLoc, pc.border, pc.inside, pc.behind, region, orange, false, pc.tpFace);
		
		if (orange)
		{
			if (region.orangePortal != null) region.orangePortal.delete();
			region.orangePortal = portal;
		}
		else
		{
			if (region.bluePortal != null) region.bluePortal.delete();
			region.bluePortal = portal;
		}
		portals.add(portal);

		portal.create();
	}
	
	private boolean checkRelative(BlockFace dir, BlockFace face, Block from, boolean up, ArrayList<Portal> oldPortals, boolean ap, List<?> pb, PortalCoord pc)
	{
	  Block block = from.getRelative(face);
	  if(!checkBlockType(block, oldPortals, pc, ap, pb, true, 0))
		return false;
	  
	  block = block.getRelative(dir);
	  if(!checkBlockType(block, oldPortals, pc, ap, pb, false, up ? 1 : 0))
		return false;
	  
	  block = block.getRelative(dir);
	  if(!checkBlockType(block, oldPortals, pc, ap, pb, true, 0))
		return false;
	  if(up)
		block = block.getRelative(BlockFace.UP);
	  else
		block = block.getRelative(BlockFace.DOWN);
	  if(!checkBlockType(block, oldPortals, pc, ap, pb, true, 0))
		return false;
	  
	  dir = dir.getOppositeFace();
	  for(int i = 1; i < 3; i++)
		if(!checkBlockType(block.getRelative(dir, i), oldPortals, pc, ap, pb, true, 0))
		  return false;
	  
	  return true;
	}
	
	private boolean checkBlockType(Block block, ArrayList<Portal> oldPortals, PortalCoord pc, boolean ap, List<?> pb, boolean border, int ii)
	{
	  Portal oldPortal;
	  LibigotLocation loc = new LibigotLocation(block);
	  if(borderBlocks.containsKey(loc))
		oldPortal = borderBlocks.get(loc);
	  else if(insideBlocks.containsKey(loc))
		oldPortal = insideBlocks.get(loc);
	  else
		oldPortal = null;
	  
	  if(oldPortal != null)
	  {
		if(!oldPortals.contains(oldPortal))
		  oldPortals.add(oldPortal);
		
		if(border)
		  pc.border.add(loc);
		else
		{
		  pc.inside[ii] = loc;
		  block = block.getRelative(pc.tpFace.getOppositeFace());
		  loc = new LibigotLocation(block);
		  pc.destLoc[ii] = loc;
		  block = block.getRelative(pc.tpFace, 2);
		  loc = new LibigotLocation(block);
		  pc.behind[ii] = loc;
		}
	  }
	  else if(ap || pb.contains(block.getTypeId()))
	  {
		if(border)
		  pc.border.add(loc);
		else
		{
		  pc.inside[ii] = loc;
		  block = block.getRelative(pc.tpFace.getOppositeFace());
		  loc = new LibigotLocation(block);
		  pc.destLoc[ii] = loc;
		  block = block.getRelative(pc.tpFace, 2);
		  loc = new LibigotLocation(block);
		  pc.behind[ii] = loc;
		}
	  }
	  else
		return false;
	  return true;
	}
}
