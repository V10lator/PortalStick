package org.PortalStick.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.PortalStick.PortalStick;
import org.PortalStick.components.Grill;
import org.PortalStick.components.Portal;
import org.PortalStick.components.Region;
import org.PortalStick.components.Wire;
import org.PortalStick.fallingblocks.FrozenSand;
import org.PortalStick.util.LocationIterator;
import org.PortalStick.util.RegionSetting;
import org.PortalStick.util.V10Location;
import org.PortalStick.util.V10Teleport;
import org.PortalStick.util.Config.Sound;
import org.PortalStick.util.VelocityUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.material.Directional;
import org.bukkit.util.Vector;


public class EntityManager implements Runnable {
	private final PortalStick plugin;
	
	public EntityManager(PortalStick instance)
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
		  if((entity instanceof FrozenSand||entity instanceof FallingBlock || entity instanceof TNTPrimed) && vector.getX() == 0.0D && vector.getZ() == 0.0D)
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
		  else if((Math.abs(vector.getX()) > 0.5 || (Math.abs(vector.getY()) > 1 || (Math.abs(vector.getZ()) > 0.5))) || entity instanceof Boat || entity instanceof FrozenSand)
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
		destination.disabled = true;
		if(really)
		{
		  if(!plugin.util.nmsUtil.teleport(entity,teleport))
			return null;
		  entity.setVelocity(outvector);
		}
		
		
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
		ArrayList<Entity> entityMoveEntities = new ArrayList<>();
		for (World world : Bukkit.getWorlds()) {
			entityMoveEntities.addAll(world.getEntities());
		}
		for (Entity entity : entityMoveEntities) {
			plugin.util.nmsUtil.checkMvt(entity);
			
		}
		entityMoveEntities.clear();
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
					new Wire(plugin, block, block, true).runTaskLater(
							plugin, 1L); 
					new Wire(plugin, block, block, false).runTaskLater(
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
			checkPiston(locTo, entity);
		}
		
		boolean isPlayer = entity instanceof Player;
		boolean living = isPlayer || entity instanceof LivingEntity;
		//Turrets
/*        if(regionTo.getBoolean(RegionSetting.ENABLE_TURRETS) && (
                isPlayer ||
                (regionTo.getBoolean(RegionSetting.TURRETS_ATTACK_EVERYTHING) &&
                        living))) {
            plugin.turretManager.check(entity, vlocTo);
        }
*/
		
		if(living) {
		    //Buttons
		    UUID uuid = entity.getUniqueId();
		    if (!plugin.cubeManager.buttonsToEntity.containsKey(uuid)) {
		        Block middle = plugin.util.chkBtn(locTo);
		        if (middle != null) {
		            V10Location loc = new V10Location(middle);
		            if (!plugin.cubeManager.buttonsToEntity.containsValue(loc)) {
		                plugin.util.changeBtn(middle, true);
		            }
		            plugin.cubeManager.buttonsToEntity.put(uuid, loc);
		        }
		    } else {
		        Block middle = plugin.util.chkBtn(locTo);
		        if (middle == null) {
		            V10Location loc2 = plugin.cubeManager.buttonsToEntity.get(uuid);
		            plugin.cubeManager.buttonsToEntity.remove(uuid);
		            Block middle2 = loc2.getHandle().getBlock();
		            if (!plugin.cubeManager.buttonsToEntity.containsValue(loc2)) {
		            plugin.util.changeBtn(middle2,false);
		            }
		            
		        }
		    }
		    
		    /*
	        for (FrozenSand s : plugin.cubeManager.flyingBlocks.values()) {
	            if (vlocTo.equals(new V10Location(s.getLocation()))) {
	                
	                s.setVelocity(FaceUtil.faceToVector(FaceUtil.getDirection(locTo.toVector().subtract(locFrom.toVector()))));
	            }
	        }
		    */
		}

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
		
		//Update FrozenSand view
		if(isPlayer)
			plugin.util.nmsUtil.frozenSandManager.checkSight((Player)entity, ret);
		
		//Gel
		if(!plugin.gelManager.flyingGels.containsKey(entity.getUniqueId()))
		  plugin.gelManager.useGel(entity, vlocTo, vector, blockIn, blockUnder, faceMap);
		
		//Funnel
 		plugin.funnelBridgeManager.EntityMoveCheck(entity);
		
		return ret;
	}
	
	public boolean checkPiston(Location to, Entity entity) {
	    BlockFace pistonBlockFace = null;
	    if (plugin.cubeManager.blockMap.contains(entity.getUniqueId())) return false;

	    Block orig = to.getBlock();
	    Block pistonBlock = null;
	    BlockFace[] BlockFaces = new BlockFace[] { BlockFace.UP,
	            BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST,
	            BlockFace.SOUTH, BlockFace.WEST };
	    for (BlockFace bf : BlockFaces) {
	        if (orig.getRelative(bf).getType() == Material.PISTON_BASE
	                || orig.getRelative(bf).getType() == Material.PISTON_STICKY_BASE) {

	            pistonBlock = orig.getRelative(bf);
	            pistonBlockFace = bf;
	            break;
	        }
	    }
	    if (pistonBlock == null) {
	        return false;
	    }
	    final Location entityLoc = pistonBlock.getLocation();
	    BlockFace pistondir = ((Directional) pistonBlock.getState().getData())
	            .getFacing().getOppositeFace();
	    Block sBlock = pistonBlock.getRelative(pistondir);
	    if (pistondir == pistonBlockFace && (sBlock.getType()
	            .equals(Material.WALL_SIGN) || sBlock.getType()
	            .equals(Material.SIGN_POST))) {
	        Sign s = (Sign) sBlock.getState();

	        if(!s.getLine(0).equalsIgnoreCase("[PortalStick]"))
	            return false;
	        double x = 0.0D;
	        double y = 0.0D;
	        double z = 0.0D;
	        int height = 0;
	        final boolean pos = !s.getLine(1).contains("direction");
	        boolean ok = true;
	        if (!pos) {
	            y = entity.getLocation().getDirection().getY();
	            double tmp = y;
	            if (s.getLine(1).contains(",")) {

	                String[] text = s.getLine(1).split(",");
	                if (y < Double.parseDouble(text[1]))
	                {
	                    try {
	                        y = Double.parseDouble(text[1]);
	                    } catch (Exception nfe) {
	                        y = tmp;
	                    }
	                }
	            }
	        } else {
	            String[] text = s.getLine(1).split(",");
	            try {
	                x = Double.parseDouble(text[0]);
	                y = Double.parseDouble(text[1]);
	                z = Double.parseDouble(text[2]);
	            } catch (Exception nfe) {
	                ok = false;
	            }
	            try {
	                height = Integer.parseInt(s.getLine(2));
	            } catch (Exception nfe) {
	                height=10;
	            }
	        }
	            if (ok) {

	                Bukkit.getScheduler().scheduleSyncDelayedTask(
	                        plugin,
	                        new SignResetter(s),
	                        2L);

	                if (entity.isInsideVehicle()) {
	                    entity.getVehicle().eject();
	                }
	                Vector vector = new Vector(0, 0, 0);
	                if (pos) {
	                    Location dest = new Location(
	                    		entityLoc.getWorld(), x, y, z);
	                    vector = VelocityUtil.calculateVelocity(entityLoc.toVector(), dest.toVector(), height);

	                } else {
	                    vector = entity.getLocation().getDirection();
	                }
	                FallingBlock as = entity.getWorld().spawnFallingBlock(entityLoc, 166, (byte) 0);
	                as.setPassenger(entity);
	                as.setVelocity(vector);
	                as.setDropItem(false);
	                plugin.cubeManager.blockMap.add(as.getUniqueId());
	                final V10Location loc = new V10Location(sBlock);
	                Bukkit.getScheduler().scheduleSyncDelayedTask(
	                        plugin, new Runnable() {
	                            @Override
	                            public void run() {
	                                Block block = loc.getHandle().getBlock();
	                                Sign s = (Sign) block.getState();
	                                block.setType(
	                                        Material.REDSTONE_BLOCK);
	                                Bukkit.getScheduler()
	                                .scheduleSyncDelayedTask(
	                                        plugin,
	                                        new SignResetter(s),
	                                        2L);
	                            }
	                        }, 2L);
	            
	        }
	        return ok;
	    }



	    return false;
	}

	private class SignResetter implements Runnable {
	    final V10Location v10loc;
	    final Material type;
	    final String[] lines;
	    final byte data;

	    SignResetter(Sign s) {
	        this.v10loc = new V10Location(s.getLocation());
	        this.type = s.getType();
	        this.data = s.getRawData();
	        this.lines = s.getLines();
	    }
	    public void run() {
	        Block block = v10loc.getHandle().getBlock();
	        block.setType(type);
	        block.setData(data);
	        Sign newSign = (Sign) block.getState();
	        for (int i = 0; i < 4; i++) {
	            newSign.setLine(i, lines[i]);
	        }
	        newSign.update();
	    }
	}
}
