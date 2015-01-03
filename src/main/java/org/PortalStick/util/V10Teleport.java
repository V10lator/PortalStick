package org.PortalStick.util;

import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * This holds a location + a velocity to give it across classes.
 * This isn't meant to be saved over time.
 * @author V10lator
 *
 */
public class V10Teleport
{
  public final Location to;
  public final Vector velocity;
  
  public V10Teleport(Location to, Vector velocity)
  {
	this.to = to;
	this.velocity = velocity;
  }
}
