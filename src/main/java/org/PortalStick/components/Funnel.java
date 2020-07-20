package org.PortalStick.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import org.PortalStick.PortalStick;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.material.MaterialData;

import com.sanjay900.nmsUtil.fallingblocks.FrozenSand;
import com.sanjay900.nmsUtil.fallingblocks.FrozenSandFactory;
import com.sanjay900.nmsUtil.util.V10Location;

public class Funnel extends Bridge {
	private boolean reversed = false;
	public boolean activated = false;
	public boolean recent = false;
	public V10Location[] lastUpdate = null;
	public final HashMap<FrozenSand, BlockFace> bridgeBlocks = new HashMap<FrozenSand, BlockFace>();
	public ArrayList<FrozenSand> throughPortal = new ArrayList<>();
	public Funnel(PortalStick plugin, V10Location CreationBlock, V10Location startingBlock, BlockFace face, HashSet<V10Location> machineBlocks) {
		super(plugin, CreationBlock, startingBlock, face, machineBlocks);
	}

	public void setReverse(boolean value)
	{
		if (reversed == value) return;
		reversed = value;
		//activate();
		HashMap<FrozenSand, BlockFace> add = new HashMap<>();
		Iterator<Entry<FrozenSand, BlockFace>> it =  bridgeBlocks.entrySet().iterator();
		while (it.hasNext()) {
			Entry<FrozenSand, BlockFace> entry = it.next();
			Location l = entry.getKey().getLocation();
			MaterialData md = getBlock(entry.getValue());
			FrozenSand f = new FrozenSandFactory(plugin, plugin.util.nmsUtil).withLocation(l).withId(md.getItemType().getId()).withData(md.getData()).build();
			plugin.util.nmsUtil.frozenSandManager.remove(entry.getKey());
			add.put(f, entry.getValue());
			it.remove();
		}
		for (Entry<FrozenSand, BlockFace> s:add.entrySet()) {
			bridgeBlocks.put(s.getKey(),s.getValue());
		}
	}
	public FrozenSand getFrozen(V10Location vb) {
		for (FrozenSand f: bridgeBlocks.keySet()) {
			if (new V10Location(f.getLocation().getBlock()).equals(vb)) {
				return f;
			}
		}
		return null;
	}
	public BlockFace getDirection(Block block)
	{
		V10Location vb = new V10Location(block);
		BlockFace face =  bridgeBlocks.get(getFrozen(vb));		
		if (reversed) face = face.getOppositeFace();

		return face;
	}

	public BlockFace getDirection(Entity entity)
	{
		Block eb = entity.getLocation().getBlock();
		BlockFace face = getDirection(eb);
		if (face == null)
		{
			for (BlockFace check : BlockFace.values())
			{
				face = getDirection(eb.getRelative(check));
				if (face != null) break;
			}
		}

		return face;
	}
	@Override
	public void reorient(V10Location... b) {
		BlockFace face = facingSide;
		V10Location nextLibigotLocation = startBlock;
		Block nextBlock = nextLibigotLocation.getHandle().getBlock();
		boolean inPortal = false;
		clear();
		while (true)
		{
			Portal portal = null;
			if(plugin.portalManager.insideBlocks.containsKey(nextLibigotLocation))
			{
				portal = plugin.portalManager.insideBlocks.get(nextLibigotLocation);
				if(portal.open)
				{
					Portal destP = portal.getDestination();
					if(destP.coord.horizontal ||portal.coord.inside[0].equals(nextLibigotLocation))
						nextLibigotLocation = destP.coord.teleport[0];
					else
						nextLibigotLocation = destP.coord.teleport[1];
				}
				else
					return;
			}
			else {
				V10Location loc2 = new V10Location(nextLibigotLocation.getWorldName(), nextLibigotLocation.getX(), nextLibigotLocation.getY() + 1, nextLibigotLocation.getZ());
				if(plugin.portalManager.insideBlocks.containsKey(loc2))
				{
					portal = plugin.portalManager.insideBlocks.get(loc2);
					if(portal.open)
						nextLibigotLocation = new V10Location(portal.getDestination().coord.teleport[0].getHandle().getBlock().getRelative(BlockFace.DOWN));
					else
						return;
				}
			}

			if(portal != null && portal.open)
			{
				nextBlock = nextLibigotLocation.getHandle().getBlock();

				face = portal.getDestination().coord.teleportFace.getOppositeFace();

				involvedPortals.add(portal);
				plugin.funnelBridgeManager.involvedPortals.put(portal, this);
				inPortal = true;
				continue;
			}
			else if (nextBlock.getY() > nextBlock.getWorld().getMaxHeight() - 1 || nextBlock.getY() < 1 || (!nextBlock.isLiquid() && nextBlock.getType() != Material.AIR))
				break;

			if (!nextBlock.getWorld().isChunkLoaded(nextBlock.getChunk())) return;

			MaterialData md = getBlock(face);
			boolean exists = false;
			if (getFrozen(new V10Location(nextBlock))!=null) {
				FrozenSand s = getFrozen(new V10Location(nextBlock));
				if (s.blockId == md.getItemType().getId() && s.blockData == md.getData()) {
					exists = true;
				} else {
					plugin.util.nmsUtil.frozenSandManager.remove(s);
					bridgeBlocks.remove(s);
				}
			}
			if (!exists) {
				FrozenSand f = new FrozenSandFactory(plugin, plugin.util.nmsUtil).withLocation(nextBlock.getLocation()).withId(md.getItemType().getId()).withData(md.getData()).build();
				bridgeBlocks.put(f, face);
				if (inPortal) {
					throughPortal.add(f);
				}
			}
			plugin.funnelBridgeManager.bridgeBlocks.put(nextLibigotLocation, this);

			nextBlock = nextBlock.getRelative(face);
			nextLibigotLocation = new V10Location(nextBlock);
		}
	}
	@Override
	public void activate()
	{
		if (activated)
		deactivate();
		activated = true;
		//deactivate first for cleanup
		if (portal) {
			dropPortalGel();
		} else {
			dropGel();
		}

		BlockFace face = facingSide;
		V10Location nextLibigotLocation = startBlock;
		Block nextBlock = nextLibigotLocation.getHandle().getBlock();
		boolean inPortal = false;
		while (true)
		{
			Portal portal = null;
			if(plugin.portalManager.insideBlocks.containsKey(nextLibigotLocation))
			{
				portal = plugin.portalManager.insideBlocks.get(nextLibigotLocation);
				if(portal.open)
				{
					Portal destP = portal.getDestination();
					if(destP.coord.horizontal ||portal.coord.inside[0].equals(nextLibigotLocation))
						nextLibigotLocation = destP.coord.teleport[0];
					else
						nextLibigotLocation = destP.coord.teleport[1];
				}
				else
					return;
			}
			else {
				V10Location loc2 = new V10Location(nextLibigotLocation.getWorldName(), nextLibigotLocation.getX(), nextLibigotLocation.getY() + 1, nextLibigotLocation.getZ());
				if(plugin.portalManager.insideBlocks.containsKey(loc2))
				{
					portal = plugin.portalManager.insideBlocks.get(loc2);
					if(portal.open)
						nextLibigotLocation = new V10Location(portal.getDestination().coord.teleport[0].getHandle().getBlock().getRelative(BlockFace.DOWN));
					else
						return;
				}
			}

			if(portal != null && portal.open)
			{
				nextBlock = nextLibigotLocation.getHandle().getBlock();

				face = portal.getDestination().coord.teleportFace.getOppositeFace();

				involvedPortals.add(portal);
				plugin.funnelBridgeManager.involvedPortals.put(portal, this);
				inPortal = true;
				continue;
			}
			else if (nextBlock.getY() > nextBlock.getWorld().getMaxHeight() - 1 || nextBlock.getY() < 1 || (!nextBlock.isLiquid() && nextBlock.getType() != Material.AIR))
				break;

			if (!nextBlock.getWorld().isChunkLoaded(nextBlock.getChunk())) return;

			MaterialData md = getBlock(face);
			FrozenSand f = new FrozenSandFactory(plugin, plugin.util.nmsUtil).withLocation(nextBlock.getLocation()).withId(md.getItemType().getId()).withData(md.getData()).build();
			bridgeBlocks.put(f, face);
			if (inPortal) {
				throughPortal.add(f);
			}

			plugin.funnelBridgeManager.bridgeBlocks.put(nextLibigotLocation, this);

			nextBlock = nextBlock.getRelative(face);
			nextLibigotLocation = new V10Location(nextBlock);
		}
	}

	private void dropGel() {
		Iterator<Entry<FrozenSand, Funnel>> it = plugin.funnelBridgeManager.cubeinFunnel.entrySet().iterator();
		while (it.hasNext()) {
			Entry<FrozenSand, Funnel> e = it.next();
			if (e.getValue() == this) {
				HashMap<String,Object> storedData = new HashMap<>();
				//Cubes have a spawnLoc
				if (e.getKey().<V10Location>getData("respawnLoc") != null) {
					storedData.put("respawnLoc", e.getKey().<V10Location>getData("respawnLoc"));
					plugin.util.nmsUtil.createCube(e.getKey().getLocation(), e.getKey().getMaterial(), e.getKey().getData(), storedData);
				} else {
					//We are dealing with gel
					FallingBlock fb = e.getKey().getLocation().getWorld().spawnFallingBlock(e.getKey().getLocation(), e.getKey().blockId, (byte) e.getKey().blockData);
					fb.setDropItem(false);
					plugin.gelManager.flyingGels.put(fb.getUniqueId(), e.getKey().<V10Location>getData("dispenser"));
				}
				plugin.util.nmsUtil.frozenSandManager.remove(e.getKey());
				it.remove();
			}
		}
	}

	private void dropPortalGel() {
		for (FrozenSand b : throughPortal)
		{
			Iterator<Entry<FrozenSand, Funnel>> it = plugin.funnelBridgeManager.cubeinFunnel.entrySet().iterator();
			while (it.hasNext()) {
				Entry<FrozenSand, Funnel> e = it.next();
				if (e.getValue() == this) {
					if (!new V10Location(e.getKey().getLocation().getBlock()).equals(new V10Location(b.getLocation().getBlock()))) continue;
					HashMap<String,Object> storedData = new HashMap<>();
					//Cubes don't have a null spawnLoc
					if (e.getKey().<V10Location>getData("respawnLoc") != null) {
						storedData.put("respawnLoc", e.getKey().<V10Location>getData("respawnLoc"));
						plugin.util.nmsUtil.createCube(e.getKey().getLocation(), e.getKey().getMaterial(), e.getKey().getData(), storedData);
					} else {
						//We are dealing with gel
						FallingBlock fb = e.getKey().getLocation().getWorld().spawnFallingBlock(e.getKey().getLocation(), e.getKey().blockId, (byte) e.getKey().blockData);
						fb.setDropItem(false);
						plugin.gelManager.flyingGels.put(fb.getUniqueId(), e.getKey().<V10Location>getData("dispenser"));
					}
					plugin.util.nmsUtil.frozenSandManager.remove(e.getKey());
					it.remove();
				}
			}

			plugin.util.nmsUtil.frozenSandManager.remove(b);
		}
	}
	public void clearPortal() {
		Iterator<FrozenSand> i2 =  throughPortal.iterator();
		while (i2.hasNext())
		{
			FrozenSand s = i2.next();
			plugin.funnelBridgeManager.bridgeBlocks.remove(new V10Location(s.getLocation()));
			plugin.util.nmsUtil.frozenSandManager.remove(s);
			bridgeBlocks.remove(s);
			i2.remove();
		}
	}
	public void clear() {
		Iterator<FrozenSand> i2 =  bridgeBlocks.keySet().iterator();
		while (i2.hasNext())
		{
			FrozenSand s = i2.next();
			plugin.funnelBridgeManager.bridgeBlocks.remove(new V10Location(s.getLocation()));
			plugin.util.nmsUtil.frozenSandManager.remove(s);
			throughPortal.remove(s);
			i2.remove();
		}
	}
	@Override
	public void deactivate()
	{
		activated = false;
		clear();

		for (Portal p: involvedPortals)
			plugin.funnelBridgeManager.involvedPortals.remove(p);
		involvedPortals.clear();
	}
	@SuppressWarnings("deprecation")
	public MaterialData getBlock(BlockFace f) {
		switch (f) {
		case DOWN:
			return new MaterialData(Material.LOG,(byte) (reversed?3:15));
		case UP:
			return new MaterialData(Material.LOG,(byte) (reversed?11:7));
		case EAST:
			return new MaterialData(Material.LOG_2,(byte) (reversed?4:1));
		case WEST:
			return new MaterialData(Material.LOG_2,(byte) (reversed?0:5));
		case NORTH:
			return new MaterialData(Material.LOG_2,(byte) (reversed?8:13));
		case SOUTH:
			return new MaterialData(Material.LOG_2,(byte) (reversed?12:9));
		default:
			break;
		}
		return null;
	}
}
