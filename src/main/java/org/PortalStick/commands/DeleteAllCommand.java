package org.PortalStick.commands;

import org.PortalStick.PortalStick;
import org.PortalStick.components.Portal;
import com.sanjay900.nmsUtil.util.Utils;
import org.bukkit.entity.Player;

public class DeleteAllCommand extends BaseCommand {
	public DeleteAllCommand(PortalStick plugin)
	{
		super(plugin, "deleteall", 0, "<- deletes all portals", false);
	}
	
	public boolean execute() {
		for(Portal p: plugin.portalManager.portals.toArray(new Portal[0]))
			p.delete();
		plugin.portalManager.portals.clear();
		Utils.sendMessage(sender, plugin.i18n.getString("AllPortalsDeleted", playerName));
		return true;
	}
	
	public boolean permission(Player player) {
		return plugin.hasPermission(player, plugin.PERM_DELETE_ALL);
	}

}