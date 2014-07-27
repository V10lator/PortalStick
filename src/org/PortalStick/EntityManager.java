package org.PortalStick;

import java.util.HashMap;

import org.PortalStick.fallingblocks.LocationIterator;
import org.PortalStick.util.RegionSetting;
import org.PortalStick.util.V10Location;
import org.PortalStick.util.Config.Sound;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.util.Vector;











import com.bergerkiller.bukkit.common.utils.EntityUtil;

public class EntityManager implements Runnable {
	private final PortalStick plugin;
	
	EntityManager(PortalStick instance)
	{
		plugin = instance;
	}

	public V10Teleport teleport(Entity entity, Location locFrom, Location oloc, V10Location locTo, Vector vector, boolean really)
	{
		if (entity == null || entity.isDead())
		  return null;

		Region regionTo = plugin.regionManager.getRegion(locTo);
		Portal portal = plugin.portalManager.insideBlocks.get(locTo);
		V10Location lTeleport;
		Portal destination;
		boolean ab = portal == null;
		if(!ab)
		{
		  if(!portal.open)
			return null;
		  destination = portal.getDestination();
		  if(destination.coord.horizontal || portal.coord.inside[0].equals(locTo))
			lTeleport = destination.coord.teleport[0];
		  else
			lTeleport = destination.coord.teleport[1];
		}
		else
		{
			boolean foundportal = false;
			LocationIterator blocksToAdd = new LocationIterator(locTo
					.getHandle().getWorld(), locTo.getHandle().toVector()
					.subtract(vector), vector, 0, (int) Math.floor(vector
					.length()));
			Location blockToAdd;
			while (blocksToAdd.hasNext()) {
				blockToAdd = blocksToAdd.next();
				portal = plugin.portalManager.insideBlocks.get(new V10Location(
						blockToAdd));
				ab = portal == null;
				if (!ab)
					if (portal.open) {
						foundportal = true;
						break;
					}
			}
			if (foundportal) {
				destination = portal.getDestination();
				if(destination.coord.horizontal || portal.coord.inside[0].equals(locTo))
					lTeleport = destination.coord.teleport[0];
				  else
					lTeleport = destination.coord.teleport[1];
			}
		  if((entity instanceof FallingBlock || entity instanceof TNTPrimed) && vector.getX() == 0.0D && vector.getZ() == 0.0D)
		  {
			portal = plugin.portalManager.awayBlocksY.get(locTo);
			if(!plugin.portalManager.awayBlocksY.containsKey(locTo))
			  return null;
			portal = plugin.portalManager.awayBlocksY.get(locTo);
			if(!portal.open)
			  return null;
			destination = portal.getDestination();
			lTeleport = destination.coord.teleport[0];
		  }
		  else if((Math.abs(vector.getX()) > 0.5 || (Math.abs(vector.getY()) > 1 || (Math.abs(vector.getZ()) > 0.5))) || entity instanceof Boat)
		  {
			if(!plugin.portalManager.awayBlocks.containsKey(locTo))
			  return null;
			portal = plugin.portalManager.awayBlocks.get(locTo);
			
			if(!portal.open)
			  return null;
			
			destination = portal.getDestination();
			if(destination.coord.horizontal || portal.coord.teleport[0].getY() <= locTo.getY())
			  lTeleport = destination.coord.teleport[0];
			else
			  lTeleport = destination.coord.teleport[1];
			
			Block to = locTo.getHandle().getBlock();
			for(int i = 0; i < 2; i++)
			{
			  BlockFace face = portal.awayBlocksY[i].getHandle().getBlock().getFace(to);
			  if(face == null)
				continue;
			  if(face != BlockFace.SELF)
			  {
				double x = 1.0D, z = 1.0D;
				boolean nef = false;
				boolean nwf = false;
				switch(face)
				{
			  	  case NORTH_WEST:
			  		x = 0.5D;
			  	  case NORTH:
			  		z = 0.5D;
			  		nwf = true;
				  break;
			  	  case NORTH_EAST:
			  		z = 1.5D;
			  	  case EAST:
			  		x = 0.5D;
			  		nef = true;
			  		break;
			  	  case SOUTH_EAST:
			  		x = 1.5D;
			  	  case SOUTH:
			  		z = 0.5D;
			  		break;
			  	  case SOUTH_WEST:
			  		z = 0.5D;
			  	  default:
			  		x = 0.5D;
				}
				if(nef)
				{
				  if(oloc.getX() - locTo.getX() > x || oloc.getZ() - locTo.getZ() < z)
					return null;
				}
				else if(nwf)
				{
				  if(oloc.getX() - locTo.getX() < x || oloc.getZ() - locTo.getZ() > z)
					return null;
				}
				else if(oloc.getX() - locTo.getX() > x || oloc.getZ() - locTo.getZ() > z)
				  return null; 
			  }
			  else
				break;
			}
		  }
		  else
			return null;
		}
		
		Location teleport = lTeleport.getHandle();
		Block block = teleport.getBlock();
        Material mat = block.getType();
        boolean valid = mat == Material.AIR;
        if(!valid) {
            valid = !mat.isSolid();
        }
        if(!valid) {
            valid = block.isLiquid();
        }
        if(!valid) {
            return new V10Teleport(locFrom, null);
        }
		
		if(portal.disabled || (Math.abs(vector.getY()) > 1 && !portal.coord.horizontal))
		  return null;
		
		teleport.setX(teleport.getX() + 0.5D);
		teleport.setZ(teleport.getZ() + 0.5D);
							 
		float yaw = entity.getLocation().getYaw();
		float pitch = entity.getLocation().getPitch();
		float startyaw = yaw;
		double momentum = 0.0;
		switch(portal.coord.teleportFace)
	       {
	       	case WEST:
	       		momentum = vector.getX();
	       		break;
	       	case NORTH:
	       		yaw -= 90;
	       		momentum = vector.getZ();
	       		break;
	       	case EAST:
	       		yaw -= 180;
	       		momentum = vector.getX();
	       		break;
	       	case SOUTH:
	       		yaw -= 270;
	       		momentum = vector.getZ();
	       		break;
	       	case DOWN:
	       	default:
	       		momentum = vector.getY();
	       		yaw = pitch;
	       		pitch = 0;
	       }
			
		momentum = Math.abs(momentum);
		momentum *= regionTo.getDouble(RegionSetting.VELOCITY_MULTIPLIER);
			//reposition velocity to match output portal's orientation
		Vector outvector = entity.getVelocity().zero();
		switch(destination.coord.teleportFace)
        {
        	case WEST:
        		yaw += 180;
        		outvector = outvector.setX(momentum);
        		break;
        	case NORTH:
        		yaw += 270;
        		outvector = outvector.setZ(momentum);
        		break;
        	case EAST:
        		yaw += 360;
        		outvector = outvector.setX(-momentum);
        		break;
        	case SOUTH:
        		yaw += 450;
        		outvector = outvector.setZ(-momentum);
        		break;
        	case DOWN:
        		if (portal.coord.teleportFace != BlockFace.UP && portal.coord.teleportFace != BlockFace.DOWN)
        		{
        			yaw = pitch;
	        		pitch = startyaw;
        		}
        		else
        		{
        			pitch = yaw;
        			yaw = startyaw;
        		}
        		outvector = outvector.setY(momentum);
        		break;
        	default:
        		if (portal.coord.teleportFace != BlockFace.UP && portal.coord.teleportFace != BlockFace.DOWN)
        		{
        			yaw = pitch;
	        		pitch = startyaw + 180;
        		}
        		else
        		{
        			pitch = yaw;
        			yaw = startyaw;
        		}
        		outvector = outvector.setY(-momentum);
        		break;
        }
		
		if (!(entity instanceof Player) && !(entity instanceof Chicken) && !(entity instanceof Bat) && (portal.coord.teleportFace == BlockFace.UP || portal.coord.teleportFace == BlockFace.DOWN) && (destination.coord.teleportFace == BlockFace.UP || destination.coord.teleportFace == BlockFace.DOWN) && plugin.rand.nextInt(100) < 5)
		{
		  double d = plugin.rand.nextDouble();
		  if(d > 0.5D)
			d -= 0.5D;
		  if(ab)
			d += 0.5D;
		  if(plugin.rand.nextBoolean())
			d = -d;
		  if(plugin.rand.nextBoolean())
			teleport.setX(teleport.getX() + d);
		  else
			teleport.setZ(teleport.getZ() + d);
		}
		
		entity.setFallDistance(0);
		
		teleport.setPitch(pitch);
		teleport.setYaw(yaw);
		
		if (entity instanceof Arrow)
			teleport.setY(teleport.getY() + 0.5);
		
		if(really)
		{
		  if(!EntityUtil.teleport(entity, teleport))
			return null;
		  entity.setVelocity(outvector);
		}
		
		destination.disabled = true;
		final Portal destination2 = destination;
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){public void run(){destination2.disabled = false;}}, 10L);
		
		if (portal.orange)
			plugin.util.playSound(Sound.PORTAL_EXIT_ORANGE, new V10Location(teleport));
		else
			plugin.util.playSound(Sound.PORTAL_EXIT_BLUE, new V10Location(teleport));
		
		return new V10Teleport(teleport, outvector);
	}
	
	@Override
	public void run()
	{
		faceCache.clear();
		plugin.funnelBridgeManager.bridgeCheck();
	}
	
	HashMap<V10Location, HashMap<BlockFace, Block>> faceCache = new HashMap<V10Location, HashMap<BlockFace, Block>>();
	
	public Location onEntityMove(final Entity entity, Location locFrom, Location locTo, boolean tp)
	{
		double d = locTo.getBlockY();
		if(d > locTo.getWorld().getMaxHeight() - 1 || d < 0)
		  return null;
		
		Vector vec2 = locTo.toVector();
		Location oloc = locTo;
		locTo = new Location(locTo.getWorld(), locTo.getBlockX(), locTo.getBlockY(), locTo.getBlockZ());
		V10Location vlocTo = new V10Location(locTo);
		Vector vec1 = locFrom.toVector();
		V10Location vlocFrom = new V10Location(locFrom.getWorld(), locFrom.getBlockX(), locFrom.getBlockY(), locFrom.getBlockZ());
		if(vlocTo.equals(vlocFrom))
		  return null;
		
	    Vector vector = vec2.subtract(vec1);
	    vector.setY(entity.getVelocity().getY());
	    
	    Region regionTo = plugin.regionManager.getRegion(vlocTo);
		Region regionFrom = plugin.regionManager.getRegion(vlocFrom);
		
		//Check for changing regions
	    plugin.portalManager.checkEntityMove(entity, regionFrom, regionTo);
		
		//Emancipation grill
		if (regionTo.getBoolean(RegionSetting.ENABLE_GRILLS))
		{
			Grill grill = plugin.grillManager.insideBlocks.get(vlocTo);
			if (grill != null && !grill.disabled)
			{
				plugin.grillManager.emancipate(regionTo, entity);
				for (V10Location loc: grill.border) {
					Block block = loc.getHandle().getBlock();
					new CheckWireTask(plugin, block, block, true).runTaskLater(
							plugin, 1L); 
					new CheckWireTask(plugin, block, block, false).runTaskLater(
							plugin, 4L); 
				}
				return null;
				
			}
				
			
		}
		
		//Aerial faith plate
		Block blockIn = locTo.getBlock();
		HashMap<BlockFace, Block> faceMap;
		if(faceCache.containsKey(vlocTo))
		  faceMap = faceCache.get(vlocTo);
		else
		{
		  faceMap = new HashMap<BlockFace, Block>();
		  faceCache.put(vlocTo, faceMap);
		}
		Block blockUnder;
		if(faceMap.containsKey(BlockFace.DOWN))
		  blockUnder = faceMap.get(BlockFace.DOWN);
		else
		{
		  blockUnder = blockIn.getRelative(BlockFace.DOWN);
		  faceMap.put(BlockFace.DOWN, blockUnder);
		}
		  
		//  blockUnder = blockIn.getRelative(BlockFace.DOWN);
		  
		if (regionTo.getBoolean(RegionSetting.ENABLE_AERIAL_FAITH_PLATES))
		{
			Block blockStart = null;
			d = Double.parseDouble(regionTo.getString(RegionSetting.FAITH_PLATE_POWER).split("-")[0]);
			String faithBlock = regionTo.getString(RegionSetting.FAITH_PLATE_BLOCK);
			Vector velocity = new Vector(0, Double.parseDouble(regionTo.getString(RegionSetting.FAITH_PLATE_POWER).split("-")[1]),0);
			
			if (blockIn.getType() == Material.STONE_PLATE && plugin.blockUtil.compareBlockToString(blockUnder, faithBlock))
				blockStart = blockUnder;
			else
				blockStart = blockIn;
			if (blockStart != null) {
				BlockFace[] faces = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
				BlockFace face = plugin.blockUtil.getFaceOfMaterial(blockStart, faces, faithBlock, faceMap);
				if (face != null) {
					switch (face) {
						case NORTH:
							velocity.setZ(d);//
							break;
						case EAST:
							velocity.setX(-d);
							break;
						case SOUTH:
							velocity.setZ(-d);
							break;
						default:
							velocity.setX(d);
					}
					if (blockStart == blockUnder) {
						velocity.setX(-velocity.getX());
						velocity.setZ(-velocity.getZ());
					}
					entity.setVelocity(velocity);
					plugin.util.playSound(Sound.FAITHPLATE_LAUNCH, new V10Location(blockStart.getLocation()));
					return null;
				}
			}
		
		}
		
		boolean isPlayer = entity instanceof Player;
		//Turrets
/*        if(regionTo.getBoolean(RegionSetting.ENABLE_TURRETS) && (
                isPlayer ||
                (regionTo.getBoolean(RegionSetting.TURRETS_ATTACK_EVERYTHING) &&
                        entity instanceof LivingEntity))) {
            plugin.turretManager.check(entity, vlocTo);
        }
*/		
		Location ret = null;
		//Teleport
		if (!isPlayer || plugin.hasPermission((Player)entity, plugin.PERM_TELEPORT))
		{
		  final V10Teleport to = teleport(entity, locFrom, oloc, vlocTo, vector, tp);
		  if(to != null)
		  {
			ret = to.to;
			vlocTo = new V10Location(ret);
			if(to.velocity != null) {
			    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){public void run(){entity.setVelocity(to.velocity);}});
			}
		  }
		}
		
		//Gel
		if(!plugin.gelManager.flyingGels.containsKey(entity.getUniqueId()))
		  plugin.gelManager.useGel(entity, vlocTo, vector, blockIn, blockUnder, faceMap);
		
		//Funnel
 		plugin.funnelBridgeManager.EntityMoveCheck(entity);
		
		return ret;
	}
}
