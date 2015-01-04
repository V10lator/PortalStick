package org.PortalStick.commands;

import org.PortalStick.PortalStick;
import com.sanjay900.nmsUtil.util.Utils;
import org.bukkit.entity.Player;

public class DeleteRegionCommand extends BaseCommand {

	public DeleteRegionCommand(PortalStick plugin) {
		super(plugin, "deleteregion", 1, "<name> <- deletes specified region", false);
	}
	
	public boolean execute() {
		if (args[0].equalsIgnoreCase("global"))
			Utils.sendMessage(sender, plugin.i18n.getString("CanNotDeleteGlobalRegion", playerName));
		else if (plugin.regionManager.getRegion(args[0]) != null) {
			plugin.regionManager.deleteRegion(args[0]);
			plugin.config.reLoad();
			Utils.sendMessage(sender, plugin.i18n.getString("RegionDeleted", playerName, args[0]));
		}
		else Utils.sendMessage(sender, plugin.i18n.getString("RegionNotFound", playerName, args[0]));
		return true;
	}
	
	public boolean permission(Player player) {
		return plugin.hasPermission(player, plugin.PERM_ADMIN_REGIONS);
	}

}