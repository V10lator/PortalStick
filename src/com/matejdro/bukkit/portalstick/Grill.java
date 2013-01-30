package com.matejdro.bukkit.portalstick;

import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.surgedev.util.SurgeLocation;

public class Grill {
	private final PortalStick plugin;
	
	final HashSet<SurgeLocation> border;
	private final HashSet<SurgeLocation> inside;
	final SurgeLocation firstBlock;
	public boolean disabled;
	
	public Grill(PortalStick plugin, HashSet<SurgeLocation> Border, HashSet<SurgeLocation> Inside, SurgeLocation FirstBlock)
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
		
		for (SurgeLocation b : border)
			plugin.grillManager.borderBlocks.remove(b);
	}
	
	public void deleteInside()
	{
		for (SurgeLocation b: inside)
		{
			b.getHandle().getBlock().setType(Material.AIR);
			plugin.grillManager.insideBlocks.remove(b);
		}
	}
	
	public void disable()
	{
		for (SurgeLocation b: inside)
		{
			b.getHandle().getBlock().setType(Material.AIR);
			disabled = true;
		}
	}
	
	public void enable()
	{
		for (SurgeLocation b: inside)
		{
			b.getHandle().getBlock().setType(Material.SUGAR_CANE_BLOCK);
			disabled = false;
		}
	}
		
	public boolean create()
	{
		boolean complete = true;
		Block rb;
		for (SurgeLocation b: inside)
    	{
			rb = b.getHandle().getBlock();
			plugin.grillManager.insideBlocks.put(b, this);
			if (rb.getType() != Material.SUGAR_CANE_BLOCK) {
				rb.setType(Material.SUGAR_CANE_BLOCK);
				complete = false;
			}
			
    	}
		for (SurgeLocation b : border)
			plugin.grillManager.borderBlocks.put(b, this);
		return complete;
	}
	
	public String getStringLocation()
	{
		Location loc = firstBlock.getHandle();
		return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
	}
}
