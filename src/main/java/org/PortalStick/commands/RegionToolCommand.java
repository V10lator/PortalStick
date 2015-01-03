package org.PortalStick.commands;

import org.PortalStick.PortalStick;
import org.PortalStick.components.User;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RegionToolCommand extends BaseCommand {

	public RegionToolCommand(PortalStick plugin) {
		super(plugin, "regiontool", 0, "<- enable/disable region selection mode", true);
	}
	
	public boolean execute() {
		User user = plugin.userManager.getUser(player);
		if (user.usingTool) {
			plugin.util.sendMessage(sender, plugin.i18n.getString("RegionToolDisabled", playerName));
		}
		else {
			plugin.util.sendMessage(sender, plugin.i18n.getString("RegionToolEnabled", playerName));
			if (!player.getInventory().contains(plugin.config.RegionTool))
					player.getInventory().addItem(new ItemStack(plugin.config.RegionTool, 1));
		}
		user.usingTool = !user.usingTool;
		return true;
	}
	
	public boolean permission(Player player) {
		return plugin.hasPermission(player, plugin.PERM_ADMIN_REGIONS);
	}

}
