package org.PortalStick.commands;

import org.PortalStick.PortalStick;
import org.PortalStick.util.Utils;
import org.bukkit.entity.Player;

public class LanguageCommand extends BaseCommand
{
	public LanguageCommand(PortalStick plugin)
	{
		super(plugin, "language", 1, "<- switches the language", false);
	}
	
	public boolean execute() {
		if(plugin.i18n.setLang(args[0]))
		{
		  Utils.sendMessage(sender, plugin.i18n.getString("LanguageChanged", playerName, args[0]));
		  plugin.config.lang = args[0];
		  plugin.config.saveAll();
		}
		else
		  Utils.sendMessage(sender, plugin.i18n.getString("LanguageNotChanged", playerName, args[0]));
		return true;
	}
	
	public boolean permission(Player player) {
		return plugin.hasPermission(player, plugin.PERM_LANGUAGE);
	}
}
