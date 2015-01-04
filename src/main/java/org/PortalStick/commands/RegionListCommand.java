package org.PortalStick.commands;

import org.PortalStick.PortalStick;
import org.PortalStick.components.Region;
import com.sanjay900.nmsUtil.util.Utils;
import org.bukkit.entity.Player;

public class RegionListCommand extends BaseCommand {

	public RegionListCommand(PortalStick plugin) {
		super(plugin, "regionlist", 0, "<- list all portal regions", false);
	}
	
	public boolean execute() {
		Utils.sendMessage(sender, "&c---------- &7Portal Regions &c----------");
		for (Region region : plugin.regionManager.regions.values())
			Utils.sendMessage(sender, "&7- &c" + region.name + " &7- &c" + region.min.toString() + " &7-&c " + region.max.toString());
		return true;
	}
	
	public boolean permission(Player player) {
		return plugin.hasPermission(player, plugin.PERM_ADMIN_REGIONS);
	}

}