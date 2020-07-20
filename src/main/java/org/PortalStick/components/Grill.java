package org.PortalStick.components;

import java.util.ArrayList;
import java.util.HashSet;

import org.PortalStick.PortalStick;
import com.sanjay900.nmsUtil.fallingblocks.FrozenSand;
import com.sanjay900.nmsUtil.fallingblocks.FrozenSandFactory;
import org.PortalStick.util.RegionSetting;
import com.sanjay900.nmsUtil.util.V10Location;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class Grill {
	private final PortalStick plugin;
	
	public final HashSet<V10Location> border;
	private final HashSet<V10Location> inside;
	public ArrayList<FrozenSand> blocks = new ArrayList<FrozenSand>();
	final V10Location firstBlock;
	public boolean disabled;
	
	public Grill(PortalStick plugin, HashSet<V10Location> Border, HashSet<V10Location> Inside, V10Location FirstBlock)
	{
		this.plugin = plugin;
		border = Border;
		inside = Inside;
		firstBlock = FirstBlock;
		disabled = false;
	}
	
	public void delete()
	{
		deleteInside();
		plugin.config.deleteGrill(getStringLocation());
		plugin.grillManager.grills.remove(this);
		plugin.config.saveAll();
		
		for (V10Location b : border)
			plugin.grillManager.borderBlocks.remove(b);
	}
	
	public void deleteInside()
	{
		for (V10Location b: inside)
		{
			plugin.grillManager.insideBlocks.remove(b);
		}
		for (FrozenSand b: blocks)
		{
			
			b.remove();
		}

	}
	
	public void disable()
	{
		disabled = true;
		for (FrozenSand b: blocks)
		{
			
			b.remove();
		}
	}
	
	public void enable()
	{
		for (V10Location b: inside)
		{
			Region region = plugin.regionManager.getRegion(b);
			FrozenSand hologram = new FrozenSandFactory(plugin, plugin.util.nmsUtil)
		    .withLocation(new Location(b.getHandle().getWorld(),b.getHandle().getBlockX(),b.getHandle().getBlockY(),b.getHandle().getBlockZ()))
		    .withText(region.getString(RegionSetting.GRILL_MATERIAL_INSIDE))
		    .build();
		blocks.add(hologram);
		disabled = false;
		}
		
	}
		
	public boolean create()
	{
		boolean complete = true;
		Block rb;
		for (V10Location b: inside)
    	{
			rb = b.getHandle().getBlock();
			plugin.grillManager.insideBlocks.put(b, this);
			Region region = plugin.regionManager.getRegion(b);
			FrozenSand hologram = new FrozenSandFactory(plugin, plugin.util.nmsUtil)
		    .withLocation(new Location(b.getHandle().getWorld(),b.getHandle().getBlockX(),b.getHandle().getBlockY(),b.getHandle().getBlockZ()))
		    .withText(region.getString(RegionSetting.GRILL_MATERIAL_INSIDE))
		    .build();
		blocks.add(hologram);
			
    	}
		for (V10Location b : border)
			plugin.grillManager.borderBlocks.put(b, this);
		return complete;
	}
	
	public String getStringLocation()
	{
		Location loc = firstBlock.getHandle();
		return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
	}
}
