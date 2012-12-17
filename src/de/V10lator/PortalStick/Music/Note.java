package de.V10lator.PortalStick.Music;

import org.bukkit.Location;
import org.bukkit.Sound;

import de.V10lator.PortalStick.V10Location;

public class Note
{
  private Sound sound;
  private float volume, pitch;
  
  Note(Sound sound, float volume, float pitch)
  {
	this.sound = sound;
	this.volume = volume;
	this.pitch = pitch;
  }
  
  boolean play(V10Location loc)
  {
	Location to = loc.getHandle();
	if(to == null)
	  return false;
	to.getWorld().playSound(to, sound, volume, pitch);
	return true;
  }
}
