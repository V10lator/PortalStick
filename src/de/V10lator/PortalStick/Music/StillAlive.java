package de.V10lator.PortalStick.Music;

import com.matejdro.bukkit.portalstick.PortalStick;

import de.V10lator.PortalStick.V10Location;

public class StillAlive extends IMusic
{
  protected Note[] stream;
  
  public StillAlive(PortalStick plugin, V10Location loc)
  {
	super(plugin, loc);
  }

  public long tempo()
  {
	return 2L;
  }
}
