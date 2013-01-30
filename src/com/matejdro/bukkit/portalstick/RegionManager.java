package com.matejdro.bukkit.portalstick;

import java.util.HashMap;

import org.bukkit.entity.Player;

import de.V10lator.PortalStick.V10Location;

public class RegionManager {
	private final PortalStick plugin;
	
	RegionManager(PortalStick plugin)
	{
		this.plugin = plugin;
	}
	
	public final HashMap<String, Region> regions = new HashMap<String, Region>();
	
	public Region loadRegion(String name, Player player, Region region) {
		name = name.toLowerCase();
		if(region == null)
		  region = getRegion(name);
		if(region == null)
		  region = new Region(plugin, name);
		if(plugin.config.loadRegionSettings(region, player))
		  regions.put(name, region);
		else
		{
		  region = null;
		  plugin.config.deleteRegion(name);
		}
		return region;
	}
	
	public void deleteRegion(String name) {
		Region region = getRegion(name);
		regions.remove(region.name);
		plugin.config.deleteRegion(name);
	}
	
	public boolean createRegion(Player player, String name, V10Location one, V10Location two) {
		name = name.toLowerCase();
		Region region = new Region(plugin, name);
		boolean ret = region.setLocation(player, one, two);
		if(ret)
		{
		  ret = (loadRegion(name, player, region) != null);
		  plugin.config.saveAll();
		}
		return ret;
	}
	
	public Region getRegion(V10Location location) {
		for (Region region : regions.values())
			if (region.contains(location) && !region.name.equals("global"))
				return region;
		return getRegion("global");
	}
	
	public Region getRegion(String name) {
		return regions.get(name.toLowerCase());
	}
	
}
