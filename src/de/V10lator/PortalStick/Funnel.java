package de.V10lator.PortalStick;

import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.libigot.LibigotLocation;

public class Funnel extends Bridge {
	private boolean reversed = false;
	
	Funnel(PortalStick plugin, LibigotLocation CreationBlock, LibigotLocation startingBlock, BlockFace face, HashSet<LibigotLocation> machineBlocks) {
		super(plugin, CreationBlock, startingBlock, face, machineBlocks);
	}
	
	public void setReverse(boolean value)
	{
		reversed = value;
		activate();
	}
	
	public BlockFace getDirection(Block block)
	{
		LibigotLocation vb = new LibigotLocation(block);
		if (!bridgeBlocks.containsKey(vb)) return null;
		
		int curnum = bridgeBlocks.get(vb);
		BlockFace face = null;
		for (BlockFace check : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN})
		{
			vb = new LibigotLocation(block.getRelative(check));
			if (bridgeBlocks.containsKey(vb) && (curnum - bridgeBlocks.get(vb) == 1 || bridgeBlocks.get(vb) > curnum + 1) )			{
				face = check;
				break;
			}
		}
		if (face == null) return null;
		
		if (reversed) face = face.getOppositeFace();
		
		return face;
	}
	
	public BlockFace getDirection(Entity entity)
	{
		Block eb = entity.getLocation().getBlock();
		BlockFace face = getDirection(eb);
		if (face == null)
		{
			for (BlockFace check : BlockFace.values())
			{
				face = getDirection(eb.getRelative(check));
				if (face != null) break;
			}
		}
		
		return face;
	}
	
	public int getCounter(LibigotLocation block)
	{
		return bridgeBlocks.get(block);
	}
	
	@Override
	public void activate()
	{
		//deactivate first for cleanup
		deactivate();
		
		BlockFace face = facingSide;
		LibigotLocation nextLibigotLocation = startBlock;
		Block nextBlock = nextLibigotLocation.getHandle().getBlock();
		int counter = reversed ? 1 : 8;
		while (true)
		{
			Portal portal = null;
			if(plugin.portalManager.insideBlocks.containsKey(nextLibigotLocation))
			{
			  portal = plugin.portalManager.insideBlocks.get(nextLibigotLocation);
			  if(portal.open)
			  {
				Portal destP = portal.getDestination();
				if(destP.coord.horizontal ||portal.coord.inside[0].equals(nextLibigotLocation))
				  nextLibigotLocation = destP.coord.teleport[0];
				else
				  nextLibigotLocation = destP.coord.teleport[1];
			  }
			  else
				return;
			}
			else if(plugin.portalManager.borderBlocks.containsKey(nextLibigotLocation))
			{
			  portal = plugin.portalManager.borderBlocks.get(nextLibigotLocation);
			  if(portal.open)
				nextLibigotLocation = new LibigotLocation(portal.getDestination().coord.teleport[0].getHandle().getBlock().getRelative(BlockFace.DOWN));
			  else
				return;
			}
			
			if(portal != null && portal.open)
			{
			  nextBlock = nextLibigotLocation.getHandle().getBlock();
			  
			  face = portal.getDestination().coord.teleportFace.getOppositeFace();
			  
			  involvedPortals.add(portal);
			  plugin.funnelBridgeManager.involvedPortals.put(portal, this);
			  continue;
			}
			else if (nextBlock.getY() > nextBlock.getWorld().getMaxHeight() - 1 || nextBlock.getY() < 1 || (!nextBlock.isLiquid() && nextBlock.getType() != Material.AIR))
			  break;
			
			if (!nextBlock.getWorld().isChunkLoaded(nextBlock.getChunk())) return;
			
			if (counter < 0) counter = 8;
			if (counter > 0)
			{
				nextBlock.setType(Material.WATER);
				byte data;
				if(reversed)
				  data = (byte)(counter - 1);
				else
				  data = (byte)(8 - counter);
				if (face != BlockFace.UP && face != BlockFace.DOWN) nextBlock.setData(data);
			}
			counter--;
				
			bridgeBlocks.put(nextLibigotLocation, counter);
			plugin.funnelBridgeManager.bridgeBlocks.put(nextLibigotLocation, this);
			
			nextBlock = nextBlock.getRelative(face);
			nextLibigotLocation = new LibigotLocation(nextBlock);
		}
	}
	
	@Override
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
//		for (Entity e : plugin.funnelBridgeManager.glassBlocks.keySet())
//			plugin.funnelBridgeManager.EntityExitsFunnel(e);
		
		involvedPortals.clear();
	}
}
