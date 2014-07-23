package de.V10lator.PortalStick;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import com.bergerkiller.bukkit.common.utils.EntityUtil;
import com.sanjay900.fallingblocks.FrozenSand;

import de.V10lator.PortalStick.util.BlockStorage;
import de.V10lator.PortalStick.util.V10Location;
import de.V10lator.PortalStick.util.RegionSetting;
import de.V10lator.PortalStick.util.Config.Sound;

public class GelManager {
	private final PortalStick plugin;
	final HashMap<String, Float> onRedGel = new HashMap<String, Float>();
	private final HashSet<Entity> ignore = new HashSet<Entity>();
	final HashMap<String, Integer> redTasks = new HashMap<String, Integer>();
	public final HashMap<V10Location, Integer> tubePids = new HashMap<V10Location, Integer>();
	public final HashSet<V10Location> activeGelTubes = new HashSet<V10Location>();
	public final HashSet<FrozenSand> ignoreCube = new HashSet<FrozenSand>();
	public final HashMap<UUID, V10Location> flyingGels = new HashMap<UUID, V10Location>();
	public final HashMap<V10Location, ArrayList<BlockStorage>> gels = new HashMap<V10Location, ArrayList<BlockStorage>>();
	public final HashMap<V10Location, BlockStorage> gelMap = new HashMap<V10Location, BlockStorage>();
	
	GelManager(PortalStick plugin)
	{
		this.plugin = plugin;
	}
	
	public void useGel(Entity entity, V10Location locTo, Vector vector, Block block, Block under, HashMap<BlockFace, Block> faceMap)
	{
		Region region = plugin.regionManager.getRegion(locTo);
		
		if(region.getBoolean(RegionSetting.ENABLE_RED_GEL_BLOCKS))
		  redGel(entity, under, region);

		if (region.getBoolean(RegionSetting.ENABLE_BLUE_GEL_BLOCKS))
		{
			if(ignore.contains(entity) || (entity instanceof Player && ((Player)entity).isSneaking()))
			  return;
			String bg = region.getString(RegionSetting.BLUE_GEL_BLOCK);
			Block block2;
			for(BlockFace face: new BlockFace[] {null, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST})
			{
			  if(face == null)
				block2 = under;
			  else if(faceMap.containsKey(face))
				block2 = faceMap.get(face);
			  else
			  {
				block2 = block.getRelative(face);
				faceMap.put(face, block);
			  }
			  if(plugin.blockUtil.compareBlockToString(block2, bg))
			  {
				if(isPortal(new V10Location(block2)))
				  continue;
				byte dir;
				if(face == null)
				  dir = 0;
				else
				{
				  switch(face)
				  {
				  	case EAST:
				  	case WEST:
				  	  dir = 1;
				  	  break;
				  	default:
				  	  dir = 2;
				  }
				}
				blueGel(entity, region, dir, vector, region.getDouble(RegionSetting.BLUE_GEL_MIN_VELOCITY));
				break;
			  }
			}
		}
	}
	
	private boolean isPortal(V10Location vl)
	{
	  for(V10Location loc: plugin.portalManager.borderBlocks.keySet())
		if(loc.equals(vl))
		  return true;
	  for(V10Location loc: plugin.portalManager.insideBlocks.keySet())
		if(loc.equals(vl))
		  return true;
	  return false;
	}
	
	private void blueGel(final Entity entity, Region region, byte dir, Vector vector, double min)
	{
//		Vector vector = player.getVelocity(); //We need a self-calculated vector from the player move event as this has 0.0 everywhere.
		Location loc = entity.getLocation();
		double y = vector.getY();
		if(dir == 0)
		{
		  y = -y;
		  if(entity instanceof Player && onRedGel.containsKey(((Player)entity).getName()) && y < min)
			y = min;
		  else if(y < 0.1D)
			return;
		  if(y < min)
			y = min;
		  vector.setY(y);
		}
		else
		{
		  if(y < min/3.0D)
			vector.setY(min / 3.0D);
		  boolean m;
		  if(dir == 1)
			y = vector.getX();
		  else
			y = vector.getZ();
		  if(y == 0)
			return;
		  if(y < 0)
		  {
			m = true;
			y = -y;
		  }
		  else
			m = false;
		  if(y < min)
			y = min;
		  if(!m)
			y = -y;
		  if(dir == 1)
			vector.setX(y);
		  else
			vector.setZ(y);
		  loc.setY(loc.getY()+0.01D);
		  EntityUtil.teleport(entity, loc);
		}
		entity.setVelocity(vector);
		
		plugin.util.playSound(Sound.GEL_BLUE_BOUNCE, new V10Location(loc));
		
		ignore.add(entity);
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() { public void run() { ignore.remove(entity); }}, 5L);
	}
	public boolean blueGelCube(final FrozenSand entity, Region region /*byte dir,*/)
	{	
		if (ignoreCube.contains(entity)) return false;
		double min = region.getDouble(RegionSetting.BLUE_GEL_MIN_VELOCITY);
		byte dir = 0;
		Vector vector = entity.getVelocity(); //We need a self-calculated vector from the player move event as this has 0.0 everywhere.
		Location loc = entity.getLocation();
		double y = vector.getY();
		if(dir == 0)
		{
		  y = -y;
		  if(y < 0.1D)
			return false;
		  if(y < min)
			y = min;
		  vector.setY(y);
		}
		else
		{
		  if(y < min/3.0D)
			vector.setY(min / 3.0D);
		  boolean m;
		  if(dir == 1)
			y = vector.getX();
		  else
			y = vector.getZ();
		  if(y == 0)
			return false;
		  if(y < 0)
		  {
			m = true;
			y = -y;
		  }
		  else
			m = false;
		  if(y < min)
			y = min;
		  if(!m)
			y = -y;
		  if(dir == 1)
			vector.setX(y);
		  else
			vector.setZ(y);
		  loc.setY(loc.getY()+0.01D);
		}
		entity.setVelocity(vector);
		
		plugin.util.playSound(Sound.GEL_BLUE_BOUNCE, new V10Location(loc));
		
		ignoreCube.add(entity);
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() { public void run() { ignoreCube.remove(entity); }}, 5L);
		return true;
	}
	private boolean redGel(Entity entity, Block under, Region region)
	{
	  if(!(entity instanceof Player)) // TODO
		return false;
	  
	  final Player player = (Player)entity;
	  if(isPortal(new V10Location(under)))
	  {
		resetPlayer(player);
		return false;
	  }
	  
	  final String pn = player.getName();
	  String rg = region.getString(RegionSetting.RED_GEL_BLOCK);
	  
	  if(!plugin.blockUtil.compareBlockToString(under, rg))
		return false;
	  
	  BukkitScheduler s = plugin.getServer().getScheduler();
	  if(redTasks.containsKey(pn))
		s.cancelTask(redTasks.get(pn));
	  redTasks.put(pn, s.scheduleSyncDelayedTask(plugin, new Runnable(){public void run(){resetPlayer(player);}} , 10L));
	  
	  float os = player.getWalkSpeed();
	  float ns = os * (float)region.getDouble(RegionSetting.RED_GEL_VELOCITY_MULTIPLIER);
	  if(ns > (float)region.getDouble(RegionSetting.RED_GEL_MAX_VELOCITY))
		return true;
	  player.setWalkSpeed(ns);
	  if(!onRedGel.containsKey(pn))
		onRedGel.put(pn, os);
	  return true;
	}
	
	public void resetPlayer(Player player)
	{
	  String pn = player.getName();
	  if(!onRedGel.containsKey(pn))
		return;
	  player.setWalkSpeed(onRedGel.get(pn));
	  onRedGel.remove(pn);
	  redTasks.remove(pn);
	}
	
	public void stopGelTube(V10Location loc)
	{
	  if(!tubePids.containsKey(loc))
		return;
	  plugin.getServer().getScheduler().cancelTask(tubePids.get(loc));
	  tubePids.remove(loc);
	  activeGelTubes.remove(loc);
	  ArrayList<BlockStorage> tc = new ArrayList<BlockStorage>();
	  Portal portal;
	  if(gels.containsKey(loc))
	  {
		for(BlockStorage bh: gels.get(loc))
		{
		  if(plugin.portalManager.insideBlocks.containsKey(loc))
		  {
			portal = plugin.portalManager.insideBlocks.get(loc);
			if(portal.open)
			  loc.getHandle().getBlock().setType(Material.AIR);
			else
			  portal.close();
		  }
		  else
			bh.set();
		  gelMap.remove(bh.getLocation());
		  tc.add(bh);
		}
		gels.remove(loc);
		Iterator<BlockStorage> iter;
		BlockStorage bs;
		for(ArrayList<BlockStorage> blocks: gels.values()) {
		    for(BlockStorage bh: tc) {
		        iter = blocks.iterator();
		        while(iter.hasNext()) {
		            bs = iter.next();
		            if(bh.getLocation().equals(bs.getLocation())) {
		                iter.remove();
		            }
		        }
		    }
		}
	  }
	  World world = loc.getHandle().getWorld();
	  UUID uuid;
	  for(Chunk c: world.getLoadedChunks())
		for(Entity e: c.getEntities())
		{
		  uuid = e.getUniqueId();
		  if(flyingGels.containsKey(uuid))
		  {
			e.remove();
			flyingGels.remove(uuid);
		  }
		}
	}
	
	public void removeGel(BlockStorage bh)
	{
	  gelMap.remove(bh.getLocation());
	  Iterator<BlockStorage> iter;
      BlockStorage bs;
	  for(ArrayList<BlockStorage> blocks: gels.values()) {
	      iter = blocks.iterator();
          while(iter.hasNext()) {
              bs = iter.next();
              if(bh.getLocation().equals(bs.getLocation())) {
                  iter.remove();
              }
          }
	  }
	}
}
