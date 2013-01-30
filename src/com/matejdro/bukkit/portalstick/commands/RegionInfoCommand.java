package com.matejdro.bukkit.portalstick.commands;

import org.bukkit.entity.Player;
import org.surgedev.util.SurgeLocation;

import com.matejdro.bukkit.portalstick.PortalStick;
import com.matejdro.bukkit.portalstick.Region;

public class RegionInfoCommand extends BaseCommand {

	public RegionInfoCommand(PortalStick plugin) {
		super(plugin, "regioninfo", 0, "<- says the region you are in", true);
	}
	
	public boolean execute() {
		Region region = plugin.regionManager.getRegion(new SurgeLocation(player.getLocation()));
		plugin.util.sendMessage(sender, "&7- &c" + region.name + " &7- &c" + region.min.toString() + " &7-&c " + region.max.toString());
		return true;
	}
	
	public boolean permission(Player player) {
		return plugin.hasPermission(player, plugin.PERM_ADMIN_REGIONS);
	}

}