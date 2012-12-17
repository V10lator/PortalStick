package de.V10lator.PortalStick.Music;

import com.matejdro.bukkit.portalstick.PortalStick;

import de.V10lator.PortalStick.V10Location;

public abstract class IMusic implements Runnable
{
  private final PortalStick plugin;
  
  protected Note[] stream;
  V10Location loc;
  private int c = 0;
  private final int pid;
  
  protected IMusic(PortalStick plugin, V10Location loc)
  {
	this.plugin = plugin;
	this.loc = loc;
	pid = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 0L, 1L);
  }
  
  public void run()
  {
	Note note = stream[c++];
	if(note != null && !stream[c++].play(loc))
	  stop();
	else if(c >= stream.length)
	  stop();
  }
  
  private void stop()
  {
	plugin.getServer().getScheduler().cancelTask(pid);
  }
  
  public abstract long tempo();
}
