package org.PortalStick.components;

import java.util.HashSet;

import org.PortalStick.PortalStick;
import com.sanjay900.nmsUtil.fallingblocks.FrozenSand;
import com.sanjay900.nmsUtil.fallingblocks.FrozenSandFactory;
import org.PortalStick.util.BlockStorage;
import org.PortalStick.util.PortalCoord;
import org.PortalStick.util.RegionSetting;
import com.sanjay900.nmsUtil.util.V10Location;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class Portal {
	private final PortalStick plugin;
	public final User owner;
	public final boolean orange;
	public boolean open = false;
	public boolean disabled = false;
	public boolean transmitter = false;
	private final HashSet<V10Location> awayBlocks;
	public final V10Location[] awayBlocksY = new V10Location[2];
	private boolean placetorch = false;
	public final PortalCoord coord;
	
	public Portal(PortalStick plugin, User owner, boolean orange, PortalCoord coord)
	{
		this.plugin = plugin;
		this.orange = orange;
		this.owner = owner;
		if(coord.horizontal)
		  awayBlocks = new HashSet<V10Location>();
		else
		  awayBlocks = null;
		this.coord = coord;
	}
	
	public void delete()
	{
		BlockStorage bh;
		for (int i = 0; i < 2; i++)
		{
		  if(coord.inside[i] == null)
			continue;
		  if(coord.insideFrozen[i] != null) {
		      coord.insideFrozen[i].remove();
		      coord.insideFrozen[i] = null;
		  }
		  if (plugin.portalManager.oldBlocks.containsKey(coord.inside[i]))
			{
				bh = plugin.portalManager.oldBlocks.get(coord.inside[i]);
				bh.set();
				if(plugin.gelManager.gelMap.containsKey(coord.inside[i]))
					plugin.gelManager.removeGel(bh);
				plugin.portalManager.oldBlocks.remove(coord.inside[i]);
			}
		  plugin.portalManager.insideBlocks.remove(coord.inside[i]);
		}
		if(coord.horizontal)
		{
		  for(V10Location l: awayBlocks)
			plugin.portalManager.awayBlocks.remove(l);
		  plugin.portalManager.awayBlocksY.remove(awayBlocksY[0]);
		  plugin.portalManager.awayBlocksY.remove(awayBlocksY[1]);
		}
		
		if (orange)
			owner.orangePortal = null;
		else
			owner.bluePortal = null;
			
		plugin.portalManager.portals.remove(this);
		plugin.regionManager.getRegion(coord.inside[0]).portalDeleted(this);
		
		Portal oldDestination = getDestination();
		if(oldDestination != null)
		  if (oldDestination.getDestination() == null) oldDestination.close();

   	}
	
	public void open()
	{
	    Location loc;
	    Portal destination = getDestination();
	    boolean receiver = placetorch || (destination != null && destination.transmitter);
		for (int i = 0; i < 2; i++)
    	{
		    if(coord.inside[i] == null)
		        continue;
		    loc = coord.inside[i].getHandle();
		    if(receiver)
		        loc.getBlock().setType(Material.REDSTONE_TORCH_ON);
		    else
		        loc.getBlock().setType(Material.AIR);
		    coord.insideFrozen[i] = new FrozenSandFactory(plugin, plugin.util.nmsUtil).withLocation(loc).withText("95:"+(orange ? plugin.util.getRightPortalColor(owner.colorPreset) : plugin.util.getLeftPortalColor(owner.colorPreset))).build();
    	}
		if(receiver)
		    placetorch = false;
		open = true;
		plugin.funnelBridgeManager.reorientBridge(this);
	}
	
	public void switchRedstoneTransmitter(boolean on) {
	    Portal destination = getDestination();
	    
	    transmitter = on;
	    if(destination == null)
	        return;
	    
	    if(destination.transmitter) {
	        transmitter = false;
	        return;
	    }
	    
	    if(!destination.open) {
	        destination.placetorch = on;
	        return;
	    }
	    
	    int mat1, mat2;
	    boolean create;
	    if (on)
	    {
	        mat1 = Material.REDSTONE_TORCH_ON.getId();
	        mat2 = Material.AIR.getId();
	        create = true;
	    }
	    else
	    {
	        mat1 = Material.AIR.getId();
	        mat2 = Material.REDSTONE_TORCH_ON.getId();
	        create = false;
	    }
	    Block block;
	    for (V10Location b: destination.coord.inside)
	    {
	        if(b != null)
	        {
	            block = b.getHandle().getBlock();
	            if(block.getTypeId() == mat2)
	            {
	                if(create) {
	                    plugin.portalManager.torches.add(b);
	                }
	                block.setTypeIdAndData(mat1, (byte)0, false);
	            }
	            if(!create) {
	                plugin.portalManager.torches.remove(b);
	            }
	        }
	    }
	}
	
	public void close()
	{
		byte color = (byte) (orange ? plugin.util.getRightPortalColor(owner.colorPreset) : plugin.util.getLeftPortalColor(owner.colorPreset));
		int w = Material.WOOL.getId();
		for (int i = 0; i < 2; i++)
		{
		    if(coord.inside[i] != null)
		    {
		        if(coord.insideFrozen[i] != null) {
		            coord.insideFrozen[i].remove();
		            coord.insideFrozen[i] = null;
		        }
		        coord.inside[i].getHandle().getBlock().setTypeIdAndData(w, color, true);
		    }
		}
		open = false;
		plugin.funnelBridgeManager.reorientBridge(this);
	}
	
	public void recreate()
	{
	    if(open) {
	        for(FrozenSand sand: coord.insideFrozen)
	            if(sand != null)
	                sand.remove();
	            
	        open();
	    }
	    else
	        close();
	}
	
	public void create()
	{
		Block rb;
		BlockStorage bh;
    	for (V10Location loc: coord.inside)
    	{
    	    if(loc == null)
    	        continue;
    		if (plugin.portalManager.insideBlocks.containsKey(loc))
    			plugin.portalManager.insideBlocks.get(loc).delete();
    		if (plugin.portalManager.behindBlocks.containsKey(loc))
    			plugin.portalManager.behindBlocks.get(loc).delete();
    		
    		rb = loc.getHandle().getBlock();
    		if(!plugin.portalManager.oldBlocks.containsKey(loc))
    		{
    		  bh = new BlockStorage(rb);
    		  if(plugin.gelManager.gelMap.containsKey(loc))
    		  {
    			bh = plugin.gelManager.gelMap.get(loc);
    			plugin.gelManager.removeGel(bh);
    		  }
    		  plugin.portalManager.oldBlocks.put(loc, bh);
    		}
    		plugin.portalManager.insideBlocks.put(loc, this);
    	}
    	
    	Region region = plugin.regionManager.getRegion(coord.inside[0]);
    	
    	if (region.getBoolean(RegionSetting.ENABLE_REDSTONE_TRANSFER))
    	{
    	    for(int i = 0; i < 2; i++) {
    	        if(coord.inside[i] == null)
    	            continue;
    	        
    	        if (coord.inside[i].getHandle().getBlock().getBlockPower() > 0) {
    	            switchRedstoneTransmitter(true);
    	            break;
    	        }
    	    }
    	}
    	
    	Portal dest = getDestination();
    	if (dest == null)
    		close();
    	else
    	{
    		open();
    		if(!dest.open)
    		  dest.open(); //TODO: I think we double-open portals now. Need research where the other open is...
    	}
    	
    	
    	V10Location oloc;
    	V10Location loc;
    	int i;
    	oloc = coord.inside[0].clone();
    	plugin.portalManager.insideBlocks.put(coord.inside[0], this);
    	if(coord.inside[1] != null)
    	  plugin.portalManager.insideBlocks.put(coord.inside[1], this);
    	
    	if(coord.horizontal)
    	{
    	  for (int y = -1;y<2;y++)
    	  {
    		if(y != 0)
    		{
    		  loc = new V10Location(oloc.getWorldName(), oloc.getX(), oloc.getY() + y, oloc.getZ());
    		  plugin.portalManager.awayBlocksY.put(loc, this);
    		  if(y < 1)
    			i = 0;
    		  else
    			i = 1;
    		  awayBlocksY[i] = loc;
    		}
    		for (int x = -1;x<2;x++)
    		{
    		  for (int z = -1;z<2;z++)
    		  {
    			loc = new V10Location(oloc.getWorldName(), oloc.getX() + x, oloc.getY() + y, oloc.getZ() + z);
    			plugin.portalManager.awayBlocks.put(loc, this);
    			awayBlocks.add(loc);
    		  }
    		}
    	  }
    	} else {
    	    for(int i1 = 0; i1 < 1; i1++)
    	        if(coord.teleport[i1] != null) {
    	            plugin.portalManager.awayBlocks.put(coord.teleport[i1], this);
    	            awayBlocksY[i1] =coord.teleport[i1];
    	        }
    	}
    	
    	plugin.regionManager.getRegion(coord.inside[0]).portalCreated(this);
	}
	
	public Portal getDestination()
	{
		Region region = plugin.regionManager.getRegion(coord.inside[0]);
		
		if (orange)
		{
			if (owner.bluePortal != null) 
				return owner.bluePortal;
			else if (!isRegionPortal())
				return region.bluePortal;
			else
				return region.orangeDestination;
		}
		else
		{
			if (owner.orangePortal != null) 
				return owner.orangePortal;
			else if (!isRegionPortal())
				return region.orangePortal;
			else
				return region.blueDestination;

		}
	}
	
	public boolean isRegionPortal()
	{
		return owner.name.startsWith("§region§_");
	}
}
