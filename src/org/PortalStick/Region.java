package org.PortalStick;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.PortalStick.util.RegionSetting;
import org.PortalStick.util.V10Location;
import org.bukkit.entity.Player;

public class Region extends User 
{
	public HashMap<RegionSetting, Object> settings = new HashMap<RegionSetting, Object>();
	
	private final PortalStick plugin;
	
	public V10Location min, max;
	
	public final String name;
	
	public Portal blueDestination; // Destination of the blue automated portal
	public Portal orangeDestination; // Destination of the orange automated portal
	
	Region(PortalStick plugin, String name)
	{
		super("§region§_"+name);
		this.plugin = plugin;
		this.name = name;
	}
	
	public boolean updateLocation(Player player) {
		String[] loc = getString(RegionSetting.LOCATION).split(":");
		V10Location min, max;
		if(this.name.equals("global"))
		  min = max = new V10Location((String)null, 0, 0, 0);
		else
		{
		  String[] loc1 = loc[1].split(",");
		  
		  int aX = Integer.parseInt(loc1[0]);
		  int aY = Integer.parseInt(loc1[1]);
		  int aZ = Integer.parseInt(loc1[2]);
		  
		  loc1 = loc[2].split(",");
		  int bX = Integer.parseInt(loc1[0]);
		  int bY = Integer.parseInt(loc1[1]);
		  int bZ = Integer.parseInt(loc1[2]);
		  
		  if(aX > bX)
		  {
			int tmp = aX;
			aX = bX;
			bX = tmp;
		  }
		  if(aY > bY)
		  {
			int tmp = aY;
			aY = bY;
			bY = tmp;
		  }
		  if(aZ > bZ)
		  {
			int tmp = aZ;
			aZ = bZ;
			bZ = tmp;
		  }
		  
		  ArrayList<V10Location> locs = new ArrayList<V10Location>(); 
		  for(int x = aX; x <= bX; x++)
			for(int y = aY; y <= bY; y++)
			  for(int z = aZ; z <= bZ; z++)
				locs.add(new V10Location(loc[0], x, y, z));
		  
		  min = new V10Location(loc[0], aX, aY, aZ);
		  max = new V10Location(loc[0], bX, bY, bZ);
		  
		  for(Region region: plugin.regionManager.regions.values())
			if(region != this && !region.name.equals("global"))
			  for(V10Location vLoc: locs)
				if(region.contains(vLoc))
				{
				  if(player != null)
					  plugin.util.sendMessage(player, plugin.i18n.getString("RegionsOverlap", player.getName(), name, region.name));
				  if(plugin.config.debug)
					  plugin.getLogger().info("Region \""+name+"\" overlaps with region \""+region.name+"\". Removing.");
				  return false;
				}
		}
		this.min = min;
		this.max = max;
		return true;
	}
	
	public boolean setLocation(Player player, V10Location one, V10Location two) {
		String old = (String)settings.get(RegionSetting.LOCATION);
		settings.put(RegionSetting.LOCATION, one.getWorldName() + ":" + one.getX()+","+one.getY()+","+one.getZ() + ":" + two.getX()+","+two.getY()+","+two.getZ());
		if(updateLocation(player))
		  return true;
		if(old == null)
		  settings.remove(RegionSetting.LOCATION);
		else
		  settings.put(RegionSetting.LOCATION, old);
		return false;
	}
	
	//Called when any portal in this region is deleted
	public void portalDeleted(Portal portal)
	{
		Region region;
		
		//We lost orange destination. Lets find new one.
		if (portal == orangeDestination)
		{
			orangeDestination = null;
			if (bluePortal != null)
			{
				for (Portal p: plugin.portalManager.portals)
				{
					if (p.orange && p.isRegionPortal())
					{
						region = plugin.regionManager.getRegion(p.coord.inside[0]);
						if(region == this)
						{
							orangeDestination = p;
							break;
						}
					}
				}
				if(orangeDestination == null)
				  for (Portal p: plugin.portalManager.portals)
				  {
					if (p.orange && !p.isRegionPortal())
					{
						region = plugin.regionManager.getRegion(p.coord.inside[0]);
						if(region == this)
						{
							orangeDestination = p;
							break;
						}
					}
				  }
				
				if (orangeDestination == null) //Close blue portals if there is no valid destinations.
					bluePortal.close();
			}
		}
		//We lost blue destination. Lets find new one
		else if (portal == blueDestination)
		{
			blueDestination = null;
			if (orangePortal != null)
			{
				for (Portal p: plugin.portalManager.portals)
				{
					if (!p.orange && p.isRegionPortal()) 
					{
						region = plugin.regionManager.getRegion(p.coord.inside[0]);
						if(region == this)
						{
							blueDestination = p;
							break;
						}
					}
				}
				if(blueDestination == null)
				  for (Portal p: plugin.portalManager.portals)
				  {
					if (!p.orange && !p.isRegionPortal())
					{
						region = plugin.regionManager.getRegion(p.coord.inside[0]);
						if(region == this)
						{
							blueDestination = p;
							break;
						}
					}
				  }
				
				if (blueDestination == null)
					orangePortal.close();
			}
		}
		
		
		//Close all connected portals if region portal is destroyed
		if (portal.isRegionPortal())
		{
			for (Portal p : plugin.portalManager.portals)
			{
				if (p.getDestination() == null && p.open)
					p.close();
			}
		}
	}
	
	//Called when any portal in this region is created
	public void portalCreated(Portal portal)
	{
		if (portal.isRegionPortal())
		{
			if (portal.orange)
			{
				orangeDestination = portal.getDestination();
				if(orangeDestination != null)
					return;
			}
			else
			{
				blueDestination = portal.getDestination();
				if(blueDestination != null)
					return;
			}
			
			Region region;
			for (Portal p: plugin.portalManager.portals)
			{
				if (p.orange == portal.orange || !p.isRegionPortal()) continue;
				//Loop through all portals to find destination
				region = plugin.regionManager.getRegion(p.coord.inside[0]);
				if(region != this)
				  continue;
				if (p.getDestination() == portal)
					p.open(); //This portal can lead to our new portal, so lets open it.						
				
				//This portal can be destination to our new portal
				if (p.orange)
				{
					if (blueDestination == null)
						blueDestination = p;
				}
				else if (orangeDestination == null)
					orangeDestination = p;
			}
			
			if((portal.orange && orangeDestination == null) || (!portal.orange && blueDestination == null))
			{
			  for (Portal p: plugin.portalManager.portals)
			  {
				if (p.orange == portal.orange || p.isRegionPortal()) continue;
				//Loop through all portals to find destination
				region = plugin.regionManager.getRegion(p.coord.inside[0]);
				if(region != this)
				  continue;
				if (p.getDestination() == portal)
					p.open(); //This portal can lead to our new portal, so lets open it.						
				
				//This portal can be destination to our new portal
				if (p.orange)
					blueDestination = p;
				else
					orangeDestination = p;
				break;
			  }
			}
			
			if (!portal.open && portal.getDestination() != null)
				portal.open();
		}
		else
		{
			if(portal.orange)
			{
				if(bluePortal != null && blueDestination == null)
				{
					blueDestination = portal;
					bluePortal.open();
				}
			}
			else
			{
				if(orangePortal != null && orangeDestination == null)
				{
					orangeDestination = portal;
					orangePortal.open();
				}
			}
		}
	}
		
	public boolean contains(V10Location loc) {
		return loc.getWorldName().equals(min.getWorldName()) &&
				loc.getX() >= min.getX() && loc.getX() <= max.getX() &&
				loc.getY() >= min.getY() && loc.getY() <= max.getY() &&
				loc.getZ() >= min.getZ() && loc.getZ() <= max.getZ();
	}
	
	public boolean getBoolean(RegionSetting setting) {
		return (Boolean)settings.get(setting);
	}
	public int getInt(RegionSetting setting) {
		return (Integer)settings.get(setting);
	}
	public List<?> getList(RegionSetting setting) {
		return (List<?>)settings.get(setting);
	}
	public String getString(RegionSetting setting) {
		Object ret = settings.get(setting);
		if(ret instanceof String)
		  return (String)ret;
		else if(ret instanceof Integer || ret instanceof Long)
		  return ""+ret;
		
		return ret.toString();
	}
	public double getDouble(RegionSetting setting) {
		return (Double)settings.get(setting);
	}
	
	public boolean validateRedGel()
	{
		if(getDouble(RegionSetting.RED_GEL_MAX_VELOCITY) > 1.0D)
		{
			settings.remove(RegionSetting.RED_GEL_MAX_VELOCITY);
			settings.put(RegionSetting.RED_GEL_MAX_VELOCITY, 1.0D);
			return false;
		}
		return true;
	}
	
	public boolean isException(String string) {
        return getList(RegionSetting.GRILL_REMOVE_EXCEPTIONS).contains(string);
    }
}
