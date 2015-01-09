package org.PortalStick.commands;

import org.PortalStick.PortalStick;
import com.sanjay900.nmsUtil.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class SayCommand extends BaseCommand {

	public SayCommand(PortalStick plugin) {
		super(plugin, "say", -1, "<- Say things however you like", false);
	}
	
	public boolean execute() {
		
	    if(args.length < 1) {
	        sendUsage();
	        return true;
	    }
		Player p = Bukkit.getPlayer(args[0]);
		if(p != null) {
		    StringBuilder sb = new StringBuilder(args[1]);
		    for (int i = 2; i < args.length ;i++ )
		        sb.append(' ').append(args[i]);
		    p.sendMessage(ChatColor.translateAlternateColorCodes('&', sb.toString()));
		} else
		    Utils.sendMessage(sender, plugin.i18n.getString("SayFailed", playerName, args[0]));
		
		return true;
	}
	
	public boolean permission(Player player) {
		return plugin.hasPermission(player, plugin.PERM_SAY);
	}

}