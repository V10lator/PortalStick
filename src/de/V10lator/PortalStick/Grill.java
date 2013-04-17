package de.V10lator.PortalStick;

import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.libigot.LibigotLocation;

public class Grill {
	private final PortalStick plugin;
	
	final HashSet<LibigotLocation> border;
	private final HashSet<LibigotLocation> inside;
	final LibigotLocation firstBlock;
	public boolean disabled;
	
	public Grill(PortalStick plugin, HashSet<LibigotLocation> Border, HashSet<LibigotLocation> Inside, LibigotLocation FirstBlock)
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
		
		for (LibigotLocation b : border)
			plugin.grillManager.borderBlocks.remove(b);
	}
	
	public void deleteInside()
	{
		for (LibigotLocation b: inside)
		{
			b.getHandle().getBlock().setType(Material.AIR);
			plugin.grillManager.insideBlocks.remove(b);
		}
	}
	
	public void disable()
	{
		for (LibigotLocation b: inside)
		{
			b.getHandle().getBlock().setType(Material.AIR);
			disabled = true;
		}
	}
	
	public void enable()
	{
		for (LibigotLocation b: inside)
		{
			b.getHandle().getBlock().setType(Material.SUGAR_CANE_BLOCK);
			disabled = false;
		}
	}
		
	public boolean create()
	{
		boolean complete = true;
		Block rb;
		for (LibigotLocation b: inside)
    	{
			rb = b.getHandle().getBlock();
			plugin.grillManager.insideBlocks.put(b, this);
			if (rb.getType() != Material.SUGAR_CANE_BLOCK) {
				rb.setType(Material.SUGAR_CANE_BLOCK);
				complete = false;
			}
			
    	}
		for (LibigotLocation b : border)
			plugin.grillManager.borderBlocks.put(b, this);
		return complete;
	}
	
	public String getStringLocation()
	{
		Location loc = firstBlock.getHandle();
		return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
	}
}
