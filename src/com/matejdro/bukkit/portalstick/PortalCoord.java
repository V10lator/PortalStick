package com.matejdro.bukkit.portalstick;

import java.util.HashSet;

import org.bukkit.block.BlockFace;
import org.surgedev.util.SurgeLocation;

class PortalCoord {
	public final HashSet<SurgeLocation> border = new HashSet<SurgeLocation>();
	public final SurgeLocation[] inside = new SurgeLocation[2];
	public final SurgeLocation[] behind = new SurgeLocation[2];
	public SurgeLocation[] destLoc = new SurgeLocation[2];
	public BlockFace tpFace;
	public boolean finished = false;
	public boolean vertical;
}