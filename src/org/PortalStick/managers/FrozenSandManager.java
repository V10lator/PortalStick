package org.PortalStick.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

import org.PortalStick.PortalStick;
import org.PortalStick.fallingblocks.FrozenSand;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class FrozenSandManager {
    private final PortalStick plugin;
	public final HashMap<FrozenSand, HashSet<UUID>> fakeBlocks = new HashMap<FrozenSand, HashSet<UUID>>();
	public int lastId = 0;
	
	public FrozenSandManager(PortalStick plugin) {
	    this.plugin = plugin;
	}
	
	public int getNextId() {
		return ++lastId;
	}
	
	public void remove(FrozenSand sand) {
	    if (sand.velocitytask != null)
	        sand.velocitytask.cancel();
        int[] entityIDs = sand.getAllEntityIds();
        for (Player p : Bukkit.getOnlinePlayers()) {
                sand.clearTags(p, entityIDs);
        }
        if(plugin.cubeManager.flyingBlocks.containsValue(sand)) {
            Iterator<FrozenSand> iter = plugin.cubeManager.flyingBlocks.values().iterator();
            while(iter.hasNext())
                if(iter.next() == sand) {
                    iter.remove();
                    break;
                }
        }
        fakeBlocks.remove(sand);
	}
	
	public void checkSight(Player player, Location loc) {
	    int vd = plugin.getServer().getViewDistance();
	    for (Entry<FrozenSand, HashSet<UUID>> e : fakeBlocks.entrySet())
	    {
	        if (e.getKey().getLocation().distance(loc) < vd) {
	            if (!e.getValue().contains(player.getUniqueId()))
	                e.getKey().shownc(player);
	        } else
	            e.getValue().remove(player.getUniqueId());
	    }
	}
	
	public void clearFrozenSand(Player player) {
        for (Entry<FrozenSand, HashSet<UUID>> e : fakeBlocks.entrySet())
            e.getValue().remove(player.getUniqueId());
    }
}
