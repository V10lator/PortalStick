package org.PortalStick.components;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import org.PortalStick.PortalStick;

import com.sanjay900.nmsUtil.fallingblocks.FrozenSand;
import com.sanjay900.nmsUtil.util.V10Location;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;

public class Funnel extends Bridge {
	private boolean reversed = false;
	public boolean activated = true;
	
	public Funnel(PortalStick plugin, V10Location CreationBlock, V10Location startingBlock, BlockFace face, HashSet<V10Location> machineBlocks) {
		super(plugin, CreationBlock, startingBlock, face, machineBlocks);
	}
	
	public void setReverse(boolean value)
	{
		reversed = value;
		activate();
	}
	
	public BlockFace getDirection(Block block)
	{
		V10Location vb = new V10Location(block);
		if (!bridgeBlocks.containsKey(vb)) return null;
		
		int curnum = bridgeBlocks.get(vb);
		BlockFace face = null;
		for (BlockFace check : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN})
		{
			vb = new V10Location(block.getRelative(check));
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
	
	public int getCounter(V10Location block)
	{
		return bridgeBlocks.get(block);
	}
	
	@Override
	public void activate()
	{
		activated = true;
		//deactivate first for cleanup
		deactivate();
		
		BlockFace face = facingSide;
		V10Location nextLibigotLocation = startBlock;
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
			else {
                V10Location loc2 = new V10Location(nextLibigotLocation.getWorldName(), nextLibigotLocation.getX(), nextLibigotLocation.getY() + 1, nextLibigotLocation.getZ());
                if(plugin.portalManager.insideBlocks.containsKey(loc2))
                {
                    portal = plugin.portalManager.insideBlocks.get(loc2);
                    if(portal.open)
                        nextLibigotLocation = new V10Location(portal.getDestination().coord.teleport[0].getHandle().getBlock().getRelative(BlockFace.DOWN));
                    else
                        return;
                }
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
			nextBlock.setType(Material.WATER);
			nextBlock.setData((byte)0);
			/*
			if (counter < 0) counter = 8;
			if (counter > 0)
			{
				nextBlock.setType(Material.WATER);
				byte data;
				if(reversed)
				  data = (byte)(counter - 1);
				else
				  data = (byte)(8 - counter);
				if (face != BlockFace.UP && face != BlockFace.DOWN) nextBlock.setData((byte)0);
			}
			counter--;
			*/	
			bridgeBlocks.put(nextLibigotLocation, counter);
			plugin.funnelBridgeManager.bridgeBlocks.put(nextLibigotLocation, this);
			
			nextBlock = nextBlock.getRelative(face);
			nextLibigotLocation = new V10Location(nextBlock);
		}
	}
	
	@Override
	public void deactivate()
	{
		activated = false;
		for (V10Location b : bridgeBlocks.keySet())
		{
			b.getHandle().getBlock().setType(Material.AIR);
			plugin.funnelBridgeManager.bridgeBlocks.remove(b);
		}
		bridgeBlocks.clear();
		for (Portal p: involvedPortals)
			plugin.funnelBridgeManager.involvedPortals.remove(p);
		for (Entity e : plugin.funnelBridgeManager.glassBlocks.keySet())
			plugin.funnelBridgeManager.EntityExitsFunnel(e);
		Iterator<Entry<FrozenSand, Funnel>> it = plugin.funnelBridgeManager.cubeinFunnel.entrySet().iterator();
		while (it.hasNext()) {
			Entry<FrozenSand, Funnel> e = it.next();
			if (e.getValue() == this) {
				HashMap<String,Object> storedData = new HashMap<>();
				//Cubes don't have a null spawnLoc
				if (e.getKey().<V10Location>getData("respawnLoc") != null) {
				storedData.put("respawnLoc", e.getKey().<V10Location>getData("respawnLoc"));
				plugin.util.nmsUtil.createCube(e.getKey().getLocation(), e.getKey().getMaterial(), e.getKey().getData(), storedData);
				} else {
					//We are dealing with gel
					FallingBlock fb = e.getKey().getLocation().getWorld().spawnFallingBlock(e.getKey().getLocation(), e.getKey().blockId, (byte) e.getKey().blockData);
					fb.setDropItem(false);
					plugin.gelManager.flyingGels.put(fb.getUniqueId(), e.getKey().<V10Location>getData("dispenser"));
				}
				e.getKey().remove();
				it.remove();
			}
		}
		involvedPortals.clear();
	}
}
