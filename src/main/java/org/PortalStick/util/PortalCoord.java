package org.PortalStick.util;

import com.sanjay900.nmsUtil.fallingblocks.FrozenSand;
import com.sanjay900.nmsUtil.util.V10Location;

import org.bukkit.block.BlockFace;

public class PortalCoord {
	public final V10Location[] inside = new V10Location[2];
	public final FrozenSand[] insideFrozen = new FrozenSand[2];
	public final V10Location[] behind = new V10Location[2];
	public V10Location[] teleport = new V10Location[2]; // destLoc
	public BlockFace teleportFace;
	public boolean finished = false;
	public boolean horizontal;
}