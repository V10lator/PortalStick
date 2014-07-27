package org.PortalStick.commands;

import org.PortalStick.PortalStick;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class SayCommand extends BaseCommand {

	public SayCommand(PortalStick plugin) {
		super(plugin, "say", -1, "<- Say things however you like", false);
	}
	
	public boolean execute() {
		
		Player p = Bukkit.getPlayer(args[0]);
		String message = ChatColor.translateAlternateColorCodes("&".charAt(0), args[1]);
		for (int i = 2; i < args.length ;i++ ) {
			message += " " +ChatColor.translateAlternateColorCodes("&".charAt(0), args[i]);
		}
		p.sendMessage(message);
		
		return true;
	}
	
	public boolean permission(Player player) {
		return plugin.hasPermission(player, plugin.PERM_PLACE_PORTAL);
	}

}