package org.PortalStick.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.UUID;

import org.PortalStick.fallingblocks.FrozenSand;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class FrozenSandManager {
	public int vd = 50;
	public final HashMap<FrozenSand, HashSet<UUID>> fakeBlocks = new HashMap<FrozenSand, HashSet<UUID>>();
	public int lastId = 0;
	public int getNextId() {
		return ++lastId;
	}
	private volatile int SHARED_ID = Short.MAX_VALUE;
	public int nextId() {
		int firstId = ++SHARED_ID;
		SHARED_ID += 4;
		return firstId;
	}

	public void remove(FrozenSand sand) {
		if (sand.velocitytask != null)
			sand.velocitytask.cancel();
		for (Player p : Bukkit.getOnlinePlayers()) {
			sand.clearTags(p);
		}

		fakeBlocks.remove(sand);
	}

	public void checkSight(Player player, Location loc) {

		if (loc == null) loc = player.getLocation();
		for (Entry<FrozenSand, HashSet<UUID>> e : fakeBlocks.entrySet())
		{
			if (e.getKey().getLocation().distance(loc) < vd) {
				if (!e.getValue().contains(player.getUniqueId())) {
					e.getKey().show(player);
					e.getValue().add(player.getUniqueId());
				}

			} else {
				e.getKey().clearTags(player);
				e.getValue().remove(player.getUniqueId());
			}
		}
	}

	public void clearFrozenSand(Player player) {
		for (Entry<FrozenSand, HashSet<UUID>> e : fakeBlocks.entrySet())
			e.getValue().remove(player.getUniqueId());
	}
}
