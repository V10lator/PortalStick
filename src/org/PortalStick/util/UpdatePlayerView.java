package org.PortalStick.util;

import java.util.UUID;

import org.PortalStick.PortalStick;
import org.PortalStick.User;
import org.PortalStick.fallingblocks.FrozenSand;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class UpdatePlayerView implements Runnable {
    private final PortalStick plugin;
    private final UUID uuid;
    
    public UpdatePlayerView(PortalStick plugin, UUID uuid) {
        this.plugin = plugin;
        this.uuid = uuid;
    }

    @Override
    public void run() {
        Player p = plugin.getServer().getPlayer(uuid);
        if(p == null)
            return;
        World world = p.getWorld();
        boolean disabled = plugin.config.DisabledWorlds.contains(world.getName());
        User user = plugin.userManager.getUser(p);
        String texturePack = disabled ? null : plugin.config.textureURL;
        if((user.hasDefaultTexture && texturePack != null) || (!user.hasDefaultTexture && texturePack == null)) {
            try {
                if(texturePack == null)
                    texturePack = plugin.config.defaultTextureURL;
                p.setResourcePack(texturePack);
            } catch(IllegalArgumentException e) {
                e.printStackTrace(); //TODO: Handle that.
            }
            user.hasDefaultTexture = !user.hasDefaultTexture;
        }
        if(!disabled) {
            for (FrozenSand h : plugin.frozenSandManager.fakeBlocks)
                if(world.equals(h.getLocation().getWorld()))
                    h.show(p);
        }
    }
}