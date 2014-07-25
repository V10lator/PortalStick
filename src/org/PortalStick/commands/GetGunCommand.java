package org.PortalStick.commands;

import org.PortalStick.PortalStick;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GetGunCommand extends BaseCommand {

    public GetGunCommand(PortalStick plugin)
    {
      super(plugin, "gun", 0, "<- gives you the portal gun.", true);
    }
    
    public boolean execute() {
        ItemStack gun = plugin.util.createPortalGun();
        if(!player.getInventory().addItem(gun).isEmpty())
            player.getWorld().dropItemNaturally(player.getLocation(), gun);
        return true;
    }
    
    public boolean permission(Player player) {
        return plugin.hasPermission(player, plugin.PERM_GET_GUN);
    }
}
