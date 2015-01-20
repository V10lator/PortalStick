package org.PortalStick.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import org.PortalStick.PortalStick;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;

import com.sanjay900.nmsUtil.fallingblocks.FrozenSand;
import com.sanjay900.nmsUtil.util.V10Location;

public class Funnel extends Bridge {
	private boolean reversed = false;
	public boolean activated = true;

	public ArrayList<V10Location> throughPortal = new ArrayList<>();
	public Funnel(PortalStick plugin, V10Location CreationBlock, V10Location startingBlock, BlockFace face, HashSet<V10Location> machineBlocks) {
		super(plugin, CreationBlock, startingBlock, face, machineBlocks);
	}
	
	public void setReverse(boolean value)
	{
		reversed = value;
		//activate();
	}
	
	public BlockFace getDirection(Block block)
	{
		V10Location vb = new V10Location(block);
		BlockFace face =  bridgeBlocks.get(vb);		
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
	@Override
	public void activate()
	{
		activated = true;
		//deactivate first for cleanup
		if (portal) {
		deactivateThroughPortal();
		} else {
			deactivate();
		}
		
		BlockFace face = facingSide;
		V10Location nextLibigotLocation = startBlock;
		Block nextBlock = nextLibigotLocation.getHandle().getBlock();
		boolean inPortal = false;
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
			  inPortal = true;
			  continue;
			}
			else if (nextBlock.getY() > nextBlock.getWorld().getMaxHeight() - 1 || nextBlock.getY() < 1 || (!nextBlock.isLiquid() && nextBlock.getType() != Material.AIR))
			  break;
			
			if (!nextBlock.getWorld().isChunkLoaded(nextBlock.getChunk())) return;
			nextBlock.setType(Material.WATER);
			nextBlock.setData((byte)0);
			bridgeBlocks.put(nextLibigotLocation, face);
			if (inPortal) {
				throughPortal.add(nextLibigotLocation);
			}
			plugin.funnelBridgeManager.bridgeBlocks.put(nextLibigotLocation, this);
			
			nextBlock = nextBlock.getRelative(face);
			nextLibigotLocation = new V10Location(nextBlock);
		}
	}
	
	private void deactivateThroughPortal() {
		for (V10Location b : throughPortal)
		{
			b.getHandle().getBlock().setType(Material.AIR);
			plugin.funnelBridgeManager.bridgeBlocks.remove(b);
			Iterator<Entry<FrozenSand, Funnel>> it = plugin.funnelBridgeManager.cubeinFunnel.entrySet().iterator();
			while (it.hasNext()) {
				Entry<FrozenSand, Funnel> e = it.next();
				if (e.getValue() == this) {
					if (!new V10Location(e.getKey().getLocation().getBlock()).equals(b)) continue;
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
					plugin.util.nmsUtil.frozenSandManager.remove(e.getKey());
					it.remove();
				}
			}
		}
		throughPortal.clear();
		deactivate();
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
		Iterator<Entry<FrozenSand, Funnel>> it = plugin.funnelBridgeManager.cubeinFunnel.entrySet().iterator();
		if (!this.portal) { 
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
				plugin.util.nmsUtil.frozenSandManager.remove(e.getKey());
				it.remove();
			}
		}
		}
		involvedPortals.clear();
	}
}
