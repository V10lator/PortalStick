package org.PortalStick.commands;

import org.PortalStick.PortalStick;
import com.sanjay900.nmsUtil.util.Utils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class HelpCommand extends BaseCommand {

	public HelpCommand(PortalStick plugin)
	{
	  super(plugin, "help", 0, "<- lists all PortalStick commands", false);
	}
	
	public boolean execute() {
		player.sendMessage(ChatColor.RED+"--------------------- "+ChatColor.GRAY+"PortalStick "+ChatColor.RED+"---------------------");
		for (BaseCommand cmd : plugin.commands)
			if ((player != null && cmd.permission(player)) || (player == null && !cmd.bePlayer))
				Utils.sendMessage(sender, "&7- /"+usedCommand+" &c" + cmd.name + " &7" + cmd.usage);
		return true;
	}
	
	public boolean permission(Player player) {
		return true;
	}

}