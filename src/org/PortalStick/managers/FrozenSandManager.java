package org.PortalStick.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

import org.PortalStick.PortalStick;
import org.PortalStick.fallingblocks.FrozenSand;
import org.PortalStick.util.V10Location;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class FrozenSandManager {
    private final PortalStick plugin;
	public final ArrayList<FrozenSand> fakeBlocks = new ArrayList<FrozenSand>();
	public final HashMap<FrozenSand, ArrayList<UUID>> playerMap = new HashMap<FrozenSand, ArrayList<UUID>>();
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
}
