package com.matejdro.bukkit.portalstick;

import java.util.HashSet;
import java.util.LinkedHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.surgedev.util.SurgeLocation;

public class Bridge {
	final PortalStick plugin;
	
	public final LinkedHashMap<SurgeLocation, Integer> bridgeBlocks = new LinkedHashMap<SurgeLocation, Integer>();
	public final HashSet<Portal> involvedPortals = new HashSet<Portal>();
	HashSet<SurgeLocation> bridgeMachineBlocks = new HashSet<SurgeLocation>();
	SurgeLocation startBlock;
	public SurgeLocation creationBlock;
	BlockFace facingSide;

	Bridge(PortalStick plugin, SurgeLocation creationBlock, SurgeLocation startingBlock, BlockFace face, HashSet<SurgeLocation> machineBlocks)
	{
		this.plugin = plugin;
		startBlock = startingBlock;
		facingSide = face;
		bridgeMachineBlocks = machineBlocks;
		this.creationBlock = creationBlock;
	}
	/*
	public Block getCreationBlock()
	{
		return creationBlock;
	}
	*/
	public void activate()
	{
		//deactivate first for cleanup
		deactivate();
		
		BlockFace face = facingSide;
		SurgeLocation nextSurgeLocation = startBlock;
		Block nextBlock = nextSurgeLocation.getHandle().getBlock();
		Portal portal;
		while(true)
		{			
			portal = null;
			if(plugin.portalManager.insideBlocks.containsKey(nextSurgeLocation))
			{
			  portal = plugin.portalManager.insideBlocks.get(nextSurgeLocation);
			  if(portal.open)
			  {
				Portal destP = portal.getDestination();
				if(destP.horizontal || portal.inside[0].equals(nextSurgeLocation))
				  nextSurgeLocation = destP.teleport[0];
				else
				  nextSurgeLocation = destP.teleport[1];
			  }
			  else
				return;
			}
			else if(plugin.portalManager.borderBlocks.containsKey(nextSurgeLocation))
			{
			  portal = plugin.portalManager.borderBlocks.get(nextSurgeLocation);
			  if(portal.open)
				nextSurgeLocation = new SurgeLocation(portal.getDestination().teleport[0].getHandle().getBlock().getRelative(BlockFace.DOWN));
			  else
				return;
			}
			if (portal != null)
			{
				nextBlock = nextSurgeLocation.getHandle().getBlock();
				face = portal.getDestination().teleportFace.getOppositeFace();
				
				involvedPortals.add(portal);
				plugin.funnelBridgeManager.involvedPortals.put(portal, this);
				continue;
			}
			else if (nextBlock.getY() > nextBlock.getWorld().getMaxHeight() - 1 ||
					(!nextBlock.isLiquid() && nextBlock.getType() != Material.AIR))
			  return;
			
			nextBlock.setType(Material.GLASS);
			bridgeBlocks.put(nextSurgeLocation, 0);
			plugin.funnelBridgeManager.bridgeBlocks.put(nextSurgeLocation, this);
			
			if(!nextBlock.getWorld().isChunkLoaded(((int)nextSurgeLocation.getX() + face.getModX()) / 16,((int)nextSurgeLocation.getZ() + face.getModX()) / 16))
			  return;
			
			nextBlock = nextBlock.getRelative(face);
			nextSurgeLocation = new SurgeLocation(nextBlock);
		}
	}
	
	public void deactivate()
	{
		for (SurgeLocation b : bridgeBlocks.keySet())
		{
			b.getHandle().getBlock().setType(Material.AIR);
			plugin.funnelBridgeManager.bridgeBlocks.remove(b);
		}
		bridgeBlocks.clear();
		for (Portal p: involvedPortals)
			plugin.funnelBridgeManager.involvedPortals.remove(p);
		involvedPortals.clear();
	}
	
	public void delete()
	{
		deactivate();
		for (SurgeLocation b: bridgeMachineBlocks)
			plugin.funnelBridgeManager.bridgeMachineBlocks.remove(b);
	}
	
	public boolean isBlockNextToBridge(SurgeLocation check)
	{
		BlockFace[] faces = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};
		for (SurgeLocation b : bridgeBlocks.keySet())
			for (BlockFace face : faces)
				if (new SurgeLocation(b.getHandle().getBlock().getRelative(face)).equals(check)) return true;
		return false;
	}
	
	public String getStringLocation()
	{
		Location loc = creationBlock.getHandle();
		return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
	}
}
