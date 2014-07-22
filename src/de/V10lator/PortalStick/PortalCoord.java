package de.V10lator.PortalStick;

import java.util.HashSet;

import org.bukkit.block.BlockFace;

import de.V10lator.PortalStick.util.V10Location;

public class PortalCoord {
	public final HashSet<V10Location> border = new HashSet<V10Location>();
	public final V10Location[] inside = new V10Location[2];
	public final V10Location[] behind = new V10Location[2];
	public V10Location[] teleport = new V10Location[2]; // destLoc
	public BlockFace teleportFace;
	public boolean finished = false;
	public boolean horizontal;
}