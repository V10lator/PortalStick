package org.PortalStick.components;

import java.util.HashSet;
import java.util.LinkedHashMap;

import org.PortalStick.PortalStick;

import com.sanjay900.nmsUtil.util.V10Location;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class Bridge {
	final PortalStick plugin;
	
	public final LinkedHashMap<V10Location, BlockFace> bridgeBlocks = new LinkedHashMap<V10Location, BlockFace>();
	public final HashSet<Portal> involvedPortals = new HashSet<Portal>();
	HashSet<V10Location> bridgeMachineBlocks = new HashSet<V10Location>();
	V10Location startBlock;
	public V10Location creationBlock;
	public BlockFace facingSide;

	public boolean portal = false;

	public Bridge(PortalStick plugin, V10Location creationBlock, V10Location startingBlock, BlockFace face, HashSet<V10Location> machineBlocks)
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
		V10Location nextV10Location = startBlock;
		Block nextBlock = nextV10Location.getHandle().getBlock();
		Portal portal;
		while(true)
		{			
			portal = null;
			if(plugin.portalManager.insideBlocks.containsKey(nextV10Location)) {
			  portal = plugin.portalManager.insideBlocks.get(nextV10Location);
			  if(portal.open)
			  {
				Portal destP = portal.getDestination();
				if(destP.coord.horizontal || portal.coord.inside[0].equals(nextV10Location))
				  nextV10Location = destP.coord.teleport[0];
				else
				  nextV10Location = destP.coord.teleport[1];
			  }
			  else
				return;
			}
			else {
			    V10Location loc2 = new V10Location(nextV10Location.getWorldName(), nextV10Location.getX(), nextV10Location.getY() + 1, nextV10Location.getZ());
			    if(plugin.portalManager.insideBlocks.containsKey(loc2))
			    {
			        portal = plugin.portalManager.insideBlocks.get(loc2);
			        if(portal.open)
			            nextV10Location = new V10Location(portal.getDestination().coord.teleport[0].getHandle().getBlock().getRelative(BlockFace.DOWN));
			        else
			            return;
			    }
			}
			if (portal != null)
			{
				nextBlock = nextV10Location.getHandle().getBlock();
				face = portal.getDestination().coord.teleportFace.getOppositeFace();
				
				involvedPortals.add(portal);
				plugin.funnelBridgeManager.involvedPortals.put(portal, this);
				continue;
			}
			else if (nextBlock.getY() > nextBlock.getWorld().getMaxHeight() - 1 ||
					(!nextBlock.isLiquid() && nextBlock.getType() != Material.AIR))
			  return;
			
			nextBlock.setType(Material.GLASS);
			bridgeBlocks.put(nextV10Location, face);
			plugin.funnelBridgeManager.bridgeBlocks.put(nextV10Location, this);
			
			if(!nextBlock.getWorld().isChunkLoaded(((int)nextV10Location.getX() + face.getModX()) / 16,((int)nextV10Location.getZ() + face.getModX()) / 16))
			  return;
			
			nextBlock = nextBlock.getRelative(face);
			nextV10Location = new V10Location(nextBlock);
			this.portal = false;
		}
	}
	
	public void deactivate()
	{
		for (V10Location b : bridgeBlocks.keySet())
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
		for (V10Location b: bridgeMachineBlocks)
			plugin.funnelBridgeManager.bridgeMachineBlocks.remove(b);
	}
	
	public boolean isBlockNextToBridge(V10Location check)
	{
		BlockFace[] faces = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};
		for (V10Location b : bridgeBlocks.keySet())
			for (BlockFace face : faces)
				if (new V10Location(b.getHandle().getBlock().getRelative(face)).equals(check)) return true;
		return false;
	}
	
	public String getStringLocation()
	{
		Location loc = creationBlock.getHandle();
		return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
	}
}
