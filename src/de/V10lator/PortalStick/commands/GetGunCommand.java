package de.V10lator.PortalStick.commands;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.V10lator.PortalStick.PortalStick;

public class GetGunCommand extends BaseCommand {

    public GetGunCommand(PortalStick plugin)
    {
      super(plugin, "gun", 0, "<- gives you the portal gun.", false);
    }
    
    public boolean execute() {
        ItemStack gun = plugin.util.createPortalGun();
        if(!player.getInventory().addItem(gun).isEmpty())
            player.getWorld().dropItemNaturally(player.getLocation(), gun);
        return true;
    }
    
    public boolean permission(Player player) {
        return true;
    }
}
