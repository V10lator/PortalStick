package org.PortalStick;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.PortalStick.fallingblocks.FrozenSand;
import org.PortalStick.util.BlockStorage;
import org.PortalStick.util.V10Location;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public class CubeManager {
    public final ArrayList<V10Location> wire = new ArrayList<V10Location>();
    final HashMap<UUID, V10Location> buttonsToEntity = new HashMap<UUID, V10Location>();
    public final HashMap<V10Location, FrozenSand> buttons = new HashMap<V10Location, FrozenSand>();
    public final HashMap<V10Location, UUID> cubes = new HashMap<V10Location, UUID>();
    public final HashMap<V10Location, UUID> cubesPlayer = new HashMap<V10Location, UUID>();
    public final ArrayList<BlockStorage> cubesFallen = new ArrayList<BlockStorage>();
    public final HashMap<V10Location, ItemStack> cubesPlayerItem = new HashMap<V10Location, ItemStack>();
    public final HashSet<UUID> respawnCubes = new HashSet<UUID>();
    private final PortalStick plugin;
    public final HashSet<UUID> blockMap = new HashSet<UUID>();
    public final HashMap<V10Location, V10Location> cubesign = new HashMap<V10Location, V10Location>();
    public final HashMap<BukkitTask, V10Location> hatches = new HashMap<BukkitTask, V10Location>();
    public final HashMap<V10Location, FrozenSand> flyingBlocks = new HashMap<V10Location, FrozenSand>();
    final BlockFace[] blockfaces = new BlockFace[] { BlockFace.WEST,
            BlockFace.NORTH_WEST, BlockFace.NORTH, BlockFace.NORTH_EAST,
            BlockFace.EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST,
            BlockFace.SOUTH_EAST };
    
    public CubeManager(PortalStick portalStick) {
        plugin = portalStick;
    }
}
