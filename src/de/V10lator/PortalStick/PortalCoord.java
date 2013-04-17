package de.V10lator.PortalStick;

import java.util.HashSet;

import org.bukkit.block.BlockFace;
import org.libigot.LibigotLocation;

public class PortalCoord {
	public final HashSet<LibigotLocation> border = new HashSet<LibigotLocation>();
	public final LibigotLocation[] inside = new LibigotLocation[2];
	public final LibigotLocation[] behind = new LibigotLocation[2];
	public LibigotLocation[] teleport = new LibigotLocation[2]; // destLoc
	public BlockFace teleportFace;
	public boolean finished = false;
	public boolean horizontal;
}