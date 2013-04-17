package de.V10lator.PortalStick;

import java.util.HashSet;
import java.util.LinkedHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.libigot.LibigotLocation;

public class Bridge {
	final PortalStick plugin;
	
	public final LinkedHashMap<LibigotLocation, Integer> bridgeBlocks = new LinkedHashMap<LibigotLocation, Integer>();
	public final HashSet<Portal> involvedPortals = new HashSet<Portal>();
	HashSet<LibigotLocation> bridgeMachineBlocks = new HashSet<LibigotLocation>();
	LibigotLocation startBlock;
	public LibigotLocation creationBlock;
	BlockFace facingSide;

	Bridge(PortalStick plugin, LibigotLocation creationBlock, LibigotLocation startingBlock, BlockFace face, HashSet<LibigotLocation> machineBlocks)
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
		LibigotLocation nextV10Location = startBlock;
		Block nextBlock = nextV10Location.getHandle().getBlock();
		Portal portal;
		while(true)
		{			
			portal = null;
			if(plugin.portalManager.insideBlocks.containsKey(nextV10Location))
			{
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
			else if(plugin.portalManager.borderBlocks.containsKey(nextV10Location))
			{
			  portal = plugin.portalManager.borderBlocks.get(nextV10Location);
			  if(portal.open)
				nextV10Location = new LibigotLocation(portal.getDestination().coord.teleport[0].getHandle().getBlock().getRelative(BlockFace.DOWN));
			  else
				return;
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
			bridgeBlocks.put(nextV10Location, 0);
			plugin.funnelBridgeManager.bridgeBlocks.put(nextV10Location, this);
			
			if(!nextBlock.getWorld().isChunkLoaded(((int)nextV10Location.getX() + face.getModX()) / 16,((int)nextV10Location.getZ() + face.getModX()) / 16))
			  return;
			
			nextBlock = nextBlock.getRelative(face);
			nextV10Location = new LibigotLocation(nextBlock);
		}
	}
	
	public void deactivate()
	{
		for (LibigotLocation b : bridgeBlocks.keySet())
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
		for (LibigotLocation b: bridgeMachineBlocks)
			plugin.funnelBridgeManager.bridgeMachineBlocks.remove(b);
	}
	
	public boolean isBlockNextToBridge(LibigotLocation check)
	{
		BlockFace[] faces = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};
		for (LibigotLocation b : bridgeBlocks.keySet())
			for (BlockFace face : faces)
				if (new LibigotLocation(b.getHandle().getBlock().getRelative(face)).equals(check)) return true;
		return false;
	}
	
	public String getStringLocation()
	{
		Location loc = creationBlock.getHandle();
		return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
	}
}
