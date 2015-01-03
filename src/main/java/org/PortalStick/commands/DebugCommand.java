package org.PortalStick.commands;

import org.PortalStick.PortalStick;
import org.bukkit.entity.Player;

public class DebugCommand extends BaseCommand
{
	public DebugCommand(PortalStick plugin)
	{
		super(plugin, "language", 1, "<- toggles debugging", false);
	}
	
	public boolean execute()
	{
		plugin.config.debug = !plugin.config.debug;
		plugin.util.sendMessage(sender, plugin.i18n.getString(plugin.config.debug ? "DebuggingEnabled" : "DebuggingDisabled", playerName));
		plugin.config.saveAll();
		return true;
	}
	
	public boolean permission(Player player) {
		return plugin.hasPermission(player, plugin.PERM_DEBUG);
	}
}
