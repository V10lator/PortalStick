package com.matejdro.bukkit.portalstick;

import java.util.HashSet;

import org.bukkit.block.BlockFace;
import org.libigot.LibigotLocation;

class PortalCoord {
	public final HashSet<LibigotLocation> border = new HashSet<LibigotLocation>();
	public final LibigotLocation[] inside = new LibigotLocation[2];
	public final LibigotLocation[] behind = new LibigotLocation[2];
	public LibigotLocation[] destLoc = new LibigotLocation[2];
	public BlockFace tpFace;
	public boolean finished = false;
	public boolean vertical;
}