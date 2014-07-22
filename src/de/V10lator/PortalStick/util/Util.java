package de.V10lator.PortalStick.util;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.getspout.spoutapi.SpoutManager;






import de.V10lator.PortalStick.PortalStick;
import de.V10lator.PortalStick.util.Config.Sound;

public class Util {
	private final PortalStick plugin;
	private int maxLength = 105;
	
	public Util(PortalStick plugin)
	{
		this.plugin = plugin;
	}
	
	public void sendMessage(CommandSender player, String msg) {
		int i;
		String part;
		ChatColor lastColor = ChatColor.RESET;
		for (String line : msg.split("`n")) {
			i = 0;
			while (i < line.length()) {
				part = getMaxString(line.substring(i));
				if (i+part.length() < line.length() && part.contains(" "))
					part = part.substring(0, part.lastIndexOf(" "));
				part = lastColor + part;
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', part));
				lastColor = getLastColor(part);
				i = i + part.length() -1;
			}
		}
	}
	
	public Location getSimpleLocation(Location location) {
		location.setX((double)Math.round(location.getX() * 10) / 10);
		location.setY((double)Math.round(location.getY() * 10) / 10);
		location.setZ((double)Math.round(location.getZ() * 10) / 10);
		return location;
	}
	
	public ChatColor getLastColor(String str) {
		int i = 0;
		ChatColor lastColor = ChatColor.RESET;
		while (i < str.length()-2) {
			for (ChatColor color: ChatColor.values()) {
				if (str.substring(i, i+2).equalsIgnoreCase(color.toString()))
					lastColor = color;
			}
			i = i+2;
		}
		return lastColor;
	}
    
    private String getMaxString(String str) {
    	for (int i = 0; i < str.length(); i++) {
    		if (str.substring(0, i).length() == maxLength) {
    			if (str.substring(i, i+1) == "")
    				return str.substring(0, i-1);
    			else
    				return str.substring(0, i);
    		}
    	}
    	return str;
    }
    
    private void playNativeSound(Sound sound, V10Location loc)
    {
      boolean oldState = plugin.config.useSpoutSounds;
      plugin.config.useSpoutSounds = false;
      playSound(sound, loc);
      plugin.config.useSpoutSounds = oldState;
    }
    
    public void playSound(Sound sound, V10Location loc)
    {
      if (!plugin.regionManager.getRegion(loc).getBoolean(RegionSetting.ENABLE_SOUNDS))
    	return;
      
      Plugin spoutPlugin = plugin.getServer().getPluginManager().getPlugin("Spout");
      if(spoutPlugin == null || !plugin.config.useSpoutSounds)
      {
        if(plugin.config.useNativeSounds)
        {
          String raw = plugin.config.soundNative[sound.ordinal()];
          if(raw == null || raw.equals(""))
          {
        	if(plugin.config.debug)
        	  plugin.getLogger().info("Sound "+sound.toString()+" not found!");
        	return;
          }
          String[] split = raw.split(":");
          float volume = 1.0F;
          float pitch = volume;
          if(split.length > 1)
        	try
          	{
        	  volume = Float.parseFloat(split[1]);
          	}
          	catch(Exception e)
          	{
          	  if(plugin.config.debug)
          		  plugin.getLogger().info("Warning: Invalid volume \""+split[1]+"\" for sound "+split[0]);
          	  volume = 1.0F;
          	}
          if(split.length > 2)
          {
        	try
        	{
        	  pitch = Float.parseFloat(split[2]);
        	}
        	catch(Exception e)
          	{
        	  if(plugin.config.debug)
        		  plugin.getLogger().info("Warning: Invalid pitch \""+split[2]+"\" for sound "+split[0]);
          	  pitch = 1.0F;
          	}
          }
          try
          {
        	org.bukkit.Sound s = org.bukkit.Sound.valueOf(split[0]);
        	loc.getHandle().getWorld().playSound(loc.getHandle(), s, volume, pitch);
          }
          catch(IllegalArgumentException e)
          {
        	e.printStackTrace();
          }
        }
      }
      else
      {
    	String url = plugin.config.soundUrls[sound.ordinal()];
    	if(url != null && url.length() > 4 && url.length() < 257)
    	  SpoutManager.getSoundManager().playGlobalCustomSoundEffect(plugin, url, false, loc.getHandle(), plugin.config.soundRange);
    	else
    	  playNativeSound(sound, loc);
      }
    }
    
    public int getLeftPortalColor(int preset)
    {
    	return Integer.parseInt(plugin.config.ColorPresets.get(preset).split("-")[0]);
    }
    
    public int getRightPortalColor(int preset)
    {
    	return Integer.parseInt(plugin.config.ColorPresets.get(preset).split("-")[1]);
    }
    
    public ItemStack getItemData(String itemString)
    {
    	int num;
    	int id;
    	short data;
    	
    	String[] split = itemString.split(",");
    	if (split.length < 2)
    		num = 1;
    	else
    		num = Integer.parseInt(split[1]);
    	split = split[0].split(":");
    	if (split.length < 2)
    		data = 0;
    	else
    		data = Short.parseShort(split[1]);

    	id = Integer.parseInt(split[0]);
    	return new ItemStack(id, num, data);
    }
    
    public void setItemNameAndDesc(ItemStack item, String name, String desc) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if(desc != null)
            meta.setLore(Arrays.asList(desc.split("\n")));
        item.setItemMeta(meta);
    }
    
    public boolean isPortalGun(ItemStack item) {
        return item != null && item.getTypeId() == plugin.config.PortalTool &&
                item.getDurability() == plugin.config.portalToolData &&
                item.getItemMeta().getDisplayName() != null &&
                item.getItemMeta().getDisplayName().equals(plugin.config.portalToolName);
    }
    
    public ItemStack createPortalGun() {
        ItemStack gun = new ItemStack(plugin.config.PortalTool, 1, plugin.config.portalToolData);
        plugin.util.setItemNameAndDesc(gun, plugin.config.portalToolName, plugin.config.portalToolDesc);
        return gun;
    }
}
