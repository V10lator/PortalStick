package org.PortalStick.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.PortalStick.util.V10Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public class CubeManager {
    public final HashMap<UUID, V10Location> buttonsToEntity = new HashMap<UUID, V10Location>();
    public final HashMap<V10Location, UUID> cubesPlayer = new HashMap<V10Location, UUID>();
    public final HashMap<V10Location, ItemStack> cubesPlayerItem = new HashMap<V10Location, ItemStack>();
    public final HashSet<UUID> blockMap = new HashSet<UUID>();
    public final HashMap<V10Location, V10Location> cubesign = new HashMap<V10Location, V10Location>();
    public final HashMap<BukkitTask, V10Location> hatches = new HashMap<BukkitTask, V10Location>();
}
