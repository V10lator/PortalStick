package org.PortalStick.commands;

import org.PortalStick.PortalStick;
import org.PortalStick.Region;
import org.PortalStick.util.V10Location;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class RegionInfoCommand extends BaseCommand {

	public RegionInfoCommand(PortalStick plugin) {
		super(plugin, "regioninfo", 0, "<- says the region you are in", true);
	}
	
	public boolean execute() {
	    Location loc = player.getLocation();
		Region region = plugin.regionManager.getRegion(new V10Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
		plugin.util.sendMessage(sender, "&7- &c" + region.name + " &7- &c" + region.min.toString() + " &7-&c " + region.max.toString());
		return true;
	}
	
	public boolean permission(Player player) {
		return plugin.hasPermission(player, plugin.PERM_ADMIN_REGIONS);
	}

}