package de.V10lator.PortalStick;

import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import de.V10lator.PortalStick.util.BlockStorage;
import de.V10lator.PortalStick.util.V10Location;
import de.V10lator.PortalStick.util.RegionSetting;

public class Portal {
	private final PortalStick plugin;
	public final User owner;
	public final boolean orange;
	public boolean open = false;
	boolean disabled = false;
	public boolean transmitter = false;
	private final HashSet<V10Location> awayBlocks;
	final V10Location[] awayBlocksY = new V10Location[2];
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
		for (V10Location loc: coord.border)
		{
			if (plugin.portalManager.oldBlocks.containsKey(loc))
			{
				bh = plugin.portalManager.oldBlocks.get(loc);
				bh.set();
				if(plugin.gelManager.gelMap.containsKey(loc))
					plugin.gelManager.removeGel(bh);
				plugin.portalManager.oldBlocks.remove(loc);
			}
			plugin.portalManager.borderBlocks.remove(loc);
		}
		for (V10Location loc: coord.inside)
		{
		  if(loc == null)
			continue;
		  if (plugin.portalManager.oldBlocks.containsKey(loc))
			{
				bh = plugin.portalManager.oldBlocks.get(loc);
				bh.set();
				if(plugin.gelManager.gelMap.containsKey(loc))
					plugin.gelManager.removeGel(bh);
				plugin.portalManager.oldBlocks.remove(loc);
			}
		  plugin.portalManager.insideBlocks.remove(loc);
		}
		if (plugin.config.FillPortalBack > -1)
		{
			for (V10Location loc: coord.behind)
			{
				if (plugin.portalManager.oldBlocks.containsKey(loc))
				{
					bh = plugin.portalManager.oldBlocks.get(loc);
					bh.set();
					if(plugin.gelManager.gelMap.containsKey(loc))
						plugin.gelManager.removeGel(bh);
					plugin.portalManager.oldBlocks.remove(loc);
				}
				plugin.portalManager.behindBlocks.remove(loc);
			}
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
		Block b;
//		BlockStorage bh;
		for (V10Location loc: coord.inside)
    	{
		  if(loc == null)
			continue;
			b = loc.getHandle().getBlock();
//			bh = new BlockStorage(b);
//			if(plugin.gelManager.gelMap.containsKey(bh))
//			  plugin.gelManager.removeGel(bh);
			b.setType(Material.AIR);
    	}
		
		Portal destination = getDestination();
		if (placetorch || (destination != null && destination.transmitter))
		{
		    for (V10Location b2: coord.inside)
                if(b2 != null)
                  b2.getHandle().getBlock().setType(Material.REDSTONE_TORCH_ON);
			placetorch = false;
		}
		
		open = true;
		plugin.funnelBridgeManager.reorientBridge(this);
	}
	
	public void switchRedstoneTransmitter(boolean on) {
	    Portal destination = getDestination();
	    
	    transmitter = on;
	    if(destination == null)
	        return;
	    
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
		byte color;
		if (orange)
			color = (byte) plugin.util.getRightPortalColor(owner.colorPreset);
		else
			color = (byte) plugin.util.getLeftPortalColor(owner.colorPreset);
		int w = Material.WOOL.getId();
		for (V10Location b: coord.inside)
    	{
		  if(b != null)
		  {
    		b.getHandle().getBlock().setTypeIdAndData(w, color, true);
    		open = false;
		  }
    	}
		
		plugin.funnelBridgeManager.reorientBridge(this);
	}
	
	public void recreate()
	{
		byte color;
		if (orange)
			color = (byte) plugin.util.getRightPortalColor(owner.colorPreset);
		else
			color = (byte) plugin.util.getLeftPortalColor(owner.colorPreset);			
		
		for (V10Location b: coord.border)
    		b.getHandle().getBlock().setData(color);

		if (!open)
			for (V10Location b: coord.inside)
			  if(b != null)
	    		b.getHandle().getBlock().setData(color);
		
		if (plugin.config.CompactPortal)
			for (V10Location b: coord.behind)
	    		b.getHandle().getBlock().setData(color);
	}
	
	public void create()
	{
		byte color = (byte) (orange ? plugin.util.getRightPortalColor(owner.colorPreset) : plugin.util.getLeftPortalColor(owner.colorPreset));

		Block rb;
		BlockStorage bh;
		int wool = Material.WOOL.getId();
    	for (V10Location loc: coord.border)
    	{
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
    		rb.setTypeIdAndData(wool, color, false);
    		plugin.portalManager.borderBlocks.put(loc, this);
       	}
    	for (V10Location loc: coord.inside)
    	{
    	  if(loc != null && !plugin.portalManager.oldBlocks.containsKey(loc))
    	  {
    		rb = loc.getHandle().getBlock();
    		bh = new BlockStorage(rb);
    		if(plugin.gelManager.gelMap.containsKey(loc))
    		{
      		  bh = plugin.gelManager.gelMap.get(loc);
      		  plugin.gelManager.removeGel(bh);
    		}
    		plugin.portalManager.oldBlocks.put(loc, bh);
    	  }
    	}
    	byte data;
    	if (plugin.config.FillPortalBack > -1)
    	{
    		for (V10Location loc: coord.behind)
        	{
        		if (plugin.portalManager.borderBlocks.containsKey(loc))
        			plugin.portalManager.borderBlocks.get(loc).delete();
        		if (plugin.portalManager.insideBlocks.containsKey(loc))
        			plugin.portalManager.insideBlocks.get(loc).delete();

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
        		if (plugin.config.CompactPortal)
        		{
        			wool = Material.WOOL.getId();
            		data = color;
        		}
        		else
        		{
        			wool = plugin.config.FillPortalBack;
        			data = plugin.config.portalBackData;
        		}
        		rb.setTypeIdAndData(wool, data, false);
        		plugin.portalManager.behindBlocks.put(loc, this);
        	}
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
