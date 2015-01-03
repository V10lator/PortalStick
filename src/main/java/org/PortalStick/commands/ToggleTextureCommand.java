package org.PortalStick.commands;

import org.PortalStick.PortalStick;
import org.PortalStick.util.UpdatePlayerView;
import org.bukkit.entity.Player;

public class ToggleTextureCommand extends BaseCommand
{
    public ToggleTextureCommand(PortalStick plugin)
    {
        super(plugin, "texture", 0, "<- toggles the texture", false);
    }
    
    public boolean execute()
    {
        boolean useTexture = plugin.config.toggleTextureURL(true);
        for(Player player: plugin.getServer().getOnlinePlayers())
            new UpdatePlayerView(plugin, player.getUniqueId()).run();
        plugin.util.sendMessage(sender, plugin.i18n.getString(useTexture ? "TextureEnabled" : "TextureDisabled", playerName));
        return true;
    }
    
    public boolean permission(Player player) {
        return plugin.hasPermission(player, plugin.PERM_TEXTURE);
    }
}
