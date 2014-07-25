package de.V10lator.PortalStick;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;

import com.bergerkiller.bukkit.common.utils.FaceUtil;

import de.V10lator.PortalStick.util.V10Location;
import de.V10lator.PortalStick.util.RegionSetting;

public class FunnelBridgeManager {
	private final PortalStick plugin;
	
	FunnelBridgeManager(PortalStick plugin)
	{
	  this.plugin = plugin;
	}
	
	public HashSet<Bridge> bridges = new HashSet<Bridge>();
	public HashMap<Portal, Bridge> involvedPortals = new HashMap<Portal, Bridge>();
	public HashMap<V10Location, Bridge> bridgeBlocks = new HashMap<V10Location, Bridge>();
	public HashMap<V10Location, Bridge> bridgeMachineBlocks = new HashMap<V10Location, Bridge>();
	//private HashSet<Entity> inFunnel = new HashSet<Entity>();
	private HashMap<Entity,Funnel> inFunnel = new HashMap<Entity,Funnel>();
	HashMap<Entity, List<V10Location>> glassBlocks = new HashMap<Entity, List<V10Location>>();
//	private HashMap<LibigotLocation, Entity> glassBlockOwners = new HashMap<LibigotLocation, Entity>();

	public boolean placeGlassBridge(Player player, V10Location first)
	{
		if (player != null && !plugin.hasPermission(player, plugin.PERM_CREATE_BRIDGE))
		  return false;
		
		Region region = plugin.regionManager.getRegion(first);
		if (!region.getBoolean(RegionSetting.ENABLE_HARD_GLASS_BRIDGES))
		  return false;
		
		HashSet<V10Location> machineBlocks = new HashSet<V10Location>();

		//Check if two blocks are iron
		if (!plugin.blockUtil.compareBlockToString(first, region.getString(RegionSetting.HARD_GLASS_BRIDGE_BASE_MATERIAL)) && !plugin.blockUtil.compareBlockToString(first, region.getString(RegionSetting.FUNNEL_BASE_MATERIAL))) return false;
		BlockFace face = null;
		Block firstIron = first.getHandle().getBlock();
		for (BlockFace check : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST})
		{
			if (plugin.blockUtil.compareBlockToString(firstIron.getRelative(check).getRelative(check), region.getString(RegionSetting.HARD_GLASS_BRIDGE_BASE_MATERIAL)) || plugin.blockUtil.compareBlockToString(firstIron.getRelative(check).getRelative(check), region.getString(RegionSetting.FUNNEL_BASE_MATERIAL)))
			{
				face = check;
				break;
			}
		}
		
		if (face == null) return false;
		
		Block startingBlock = firstIron.getRelative(face);
		Block secondIron = startingBlock.getRelative(face);
		
		machineBlocks.add(new V10Location(firstIron));
		machineBlocks.add(new V10Location(secondIron));
		
		//Check if two irons have redstone torches on them
		Boolean havetorch = false;
		for (BlockFace check : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP})
		{
			if (firstIron.getRelative(check).getType() == Material.REDSTONE_TORCH_ON)
			{
				havetorch = true;
				machineBlocks.add(new V10Location(firstIron.getRelative(check)));
				break;
			}
		}
		if (!havetorch) return false;
		havetorch = false;
		for (BlockFace check : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP})
		{
			if (secondIron.getRelative(check).getType() == Material.REDSTONE_TORCH_ON)
			{
				havetorch = true;
				machineBlocks.add(new V10Location(secondIron.getRelative(check)));
				break;
			}
		}
		if (!havetorch) return false;
		
		//Which way should we create bridge to
		face = null;
		for (BlockFace check : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.DOWN, BlockFace.UP})
		{
			if (startingBlock.getRelative(check).isEmpty() || startingBlock.getRelative(check).isLiquid())
			{
				face = check;
				break;
			}
		}
		if (face == null) return false;
		
		Bridge bridge;
		first = new V10Location(firstIron);
		if (plugin.blockUtil.compareBlockToString(firstIron, region.getString(RegionSetting.HARD_GLASS_BRIDGE_BASE_MATERIAL)))
			bridge = new Bridge(plugin, first, new V10Location(startingBlock), face, machineBlocks);
		else
			bridge = new Funnel(plugin, first, new V10Location(startingBlock), face, machineBlocks);
		bridge.activate();
		
		for (V10Location b: machineBlocks)
			bridgeMachineBlocks.put(b, bridge);
		bridges.add(bridge);
		plugin.config.saveAll();
		return true;
	}
	
	public void reorientBridge(Portal portal)
	{
		Bridge bridge = involvedPortals.get(portal);
		if (bridge != null)
			bridge.activate();
		
		for (Bridge cbridge : bridges)
		{
			for (V10Location b: portal.coord.inside)
			{
			  if(b != null && cbridge.isBlockNextToBridge(b))
				cbridge.activate();
			}
			for (V10Location b: portal.coord.border)
			{
				if (cbridge.isBlockNextToBridge(b))
					cbridge.activate();
			}
		}
	}
	
	public void updateBridge(final V10Location block)
	{
		//delay to make sure all blocks have updated
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

		    public void run() {
		    	for (Bridge cbridge : bridges)
				{
					if (cbridge.isBlockNextToBridge(block))
						cbridge.activate();
				}
		    }
		}, 1L);
		
	}
	
	public void loadBridge(String blockloc) {
		String[] locarr = blockloc.split(",");
		if (!placeGlassBridge(null, new V10Location(plugin.getServer().getWorld(locarr[0]).getBlockAt((int)Double.parseDouble(locarr[1]), (int)Double.parseDouble(locarr[2]), (int)Double.parseDouble(locarr[3])))))
			plugin.config.deleteBridge(blockloc);
	}
	
	public void deleteAll()
	{
		for (Bridge bridge: bridges.toArray(new Bridge[0]))
			bridge.deactivate();
	}
	public Funnel getFunnelInEntity(Entity entity)
	{
		Bridge bridge = bridgeBlocks.get(new V10Location(entity.getLocation()));
		if (bridge == null )bridge = bridgeBlocks.get(new V10Location(entity.getLocation().getBlock().getRelative(BlockFace.UP)));
		if (bridge == null && ((entity.getLocation().getZ() - (double) entity.getLocation().getBlockZ()) < 0.5)) bridge = bridgeBlocks.get(new V10Location(entity.getLocation().getBlock().getRelative(0,0,-1)));
		if (bridge == null && ((entity.getLocation().getZ() - (double) entity.getLocation().getBlockZ()) > 0.5)) bridge = bridgeBlocks.get(new V10Location(entity.getLocation().getBlock().getRelative(0,0,1)));
		if (bridge == null && ((entity.getLocation().getX() - (double) entity.getLocation().getBlockX()) < 0.5)) bridge = bridgeBlocks.get(new V10Location(entity.getLocation().getBlock().getRelative(-1,0,0)));
		if (bridge == null && ((entity.getLocation().getX() - (double) entity.getLocation().getBlockX()) > 0.5)) bridge = bridgeBlocks.get(new V10Location(entity.getLocation().getBlock().getRelative(1,0,0)));
		if (bridge == null && ((entity.getLocation().getZ() - (double) entity.getLocation().getBlockZ()) < 0.5)) bridge = bridgeBlocks.get(new V10Location(entity.getLocation().getBlock().getRelative(0,1,-1)));
		if (bridge == null && ((entity.getLocation().getZ() - (double) entity.getLocation().getBlockZ()) > 0.5)) bridge = bridgeBlocks.get(new V10Location(entity.getLocation().getBlock().getRelative(0,1,1)));
		if (bridge == null && ((entity.getLocation().getX() - (double) entity.getLocation().getBlockX()) < 0.5)) bridge = bridgeBlocks.get(new V10Location(entity.getLocation().getBlock().getRelative(-1,1,0)));
		if (bridge == null && ((entity.getLocation().getX() - (double) entity.getLocation().getBlockX()) > 0.5)) bridge = bridgeBlocks.get(new V10Location(entity.getLocation().getBlock().getRelative(1,1,0)));

		
		
		if (bridge != null && bridge instanceof Funnel)
			return (Funnel) bridge;
		else
			return null;
	}
	
	public void EntityMoveCheck(Entity entity)
	{
		Funnel funnel = getFunnelInEntity(entity);
		
		if (funnel == null && inFunnel.containsKey(entity))
		{
			if (inFunnel.get(entity).activated) {
			EntityExitsFunnel(entity);
			}
		}
		else if (funnel != null)
		{

			if (!inFunnel.containsKey(entity)) EntityEntersFunnel(entity, funnel);
			EntityMoveInFunnel(entity, funnel);
		}
	}
	
	private void EntityEntersFunnel(final Entity entity, final Funnel funnel)
	{
		
		if (entity instanceof Player) {
			Player p = (Player)entity;
			p.setVelocity(p.getVelocity().multiply(-2));
			p.setAllowFlight(true);
			 p.setFlying(true);
			//delay to give the player a split second of upwards momentum to cancel out falling
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

				    public void run() {
				    	inFunnel.put(entity,funnel);
				    }
				}, 1L);
			 
		}
		if (entity instanceof FallingBlock) {
			Bridge bridge = bridgeBlocks.get(new V10Location(entity.getLocation()));
			if (bridge == null) return;
			/*FlyingBlock fblock = new FlyingBlock(((FallingBlock) entity).getMaterial(),((FallingBlock) entity).getBlockData()) {
				
				@Override
				public void onTick() {
					final FlyingBlock fblock = this;
					
					Location l = this.getBukkitEntity().getLocation();
					final Block b = l.getBlock().getRelative(BlockFace.DOWN, (int) this.getHeightOffset());
					if (!funnel.activated) {
						plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

						    
							public void run() {
						    	if (!funnel.activated) {
						    		FallingBlock fb = b.getWorld().spawnFallingBlock(b.getLocation(), fblock.getMaterial(), fblock.getMaterialData());
						    		fb.setDropItem(false);
						    		Block k = null;
						    		for (Entry<Block, FlyingBlock> b : plugin.eventListener.FlyingBlocks.entrySet()) {
										if (b.getValue() == fblock) {
											plugin.eventListener.cubes.put(b.getKey(), fb);
											k = b.getKey();
										}
									}
									if (k != null) plugin.eventListener.FlyingBlocks.remove(k);
						    		fblock.remove();
						    		return;
						    	}
						    }
						}, 1L);	
					}
					BlockFace face = null;
					
					if (funnel.getDirection(b) != null)
						face = funnel.getDirection(b);
							else {
								face =funnel.facingSide;
							}
					V10Location nextV10Location = new V10Location(b.getRelative(face));
					
					V10Location nextV10Location2 = new V10Location(b.getRelative(face).getLocation().add(FaceUtil.faceToVector(face).multiply(2.1)));
					if (nextV10Location2.getHandle().getBlockX() == funnel.startBlock.getHandle().getBlockX() &&nextV10Location2.getHandle().getBlockY() == funnel.startBlock.getHandle().getBlockY()  &&nextV10Location2.getHandle().getBlockZ() == funnel.startBlock.getHandle().getBlockZ() ) {
						this.setVelocity(new Vector(0,0,0).zero());
						return;
					}
					if ((b.getRelative(face).getType() != Material.WATER && b.getRelative(face).getType() != Material.STATIONARY_WATER)) {
						Portal portal = null;
						if(plugin.portalManager.insideBlocks.containsKey(nextV10Location))
						{
						  portal = plugin.portalManager.insideBlocks.get(nextV10Location);
						  if(portal.open)
						  {
							Portal destP = portal.getDestination();
							if (destP.horizontal) {
								nextV10Location = destP.teleport[1];
							} else 
								nextV10Location = new V10Location(destP.teleport[1].getHandle().add(0,-2,0));
						  }
						  else
							return;
						}
						else if(plugin.portalManager.borderBlocks.containsKey(nextV10Location))
						{
						  portal = plugin.portalManager.borderBlocks.get(nextV10Location);
						  if(portal.open)
							nextV10Location = new V10Location(portal.getDestination().teleport[0].getHandle().getBlock().getRelative(BlockFace.DOWN));
						  else
							return;
						}
						
						if(portal != null && portal.open)
						{
						  
						  face = portal.getDestination().teleportFace.getOppositeFace();
						  final BlockFace face2 = portal.getDestination().teleportFace.getOppositeFace();
						  final FlyingBlock fb = this;
						  final Location pr = nextV10Location.getHandle();
						  plugin.getServer().getScheduler()
							.scheduleSyncDelayedTask(plugin, new Runnable() {
								public void run() {
									
									fb.setBlockLocation(new Location (pr.getWorld(),pr.getBlockX()+0.5,pr.getBlockY()+0.5,pr.getBlockZ()+0.5));
										fb.setVelocity(FaceUtil.faceToVector(face2).multiply(0.1));
									
								}
							});
						}
						else {
						this.setVelocity(new Vector(0,0,0).zero());
						}
					} else {
						this.setVelocity(FaceUtil.faceToVector(face).multiply(0.1));
						}
				}
				
				
			};
			
			





			// spawn block
			fblock.spawn(new Location (entity.getLocation().getWorld(),entity.getLocation().getBlockX()+0.5,entity.getLocation().getBlockY()+0.5,entity.getLocation().getBlockZ()+0.5));
			fblock.setVelocity(FaceUtil.faceToVector(funnel.facingSide).multiply(0.1));
			Block k = null;
			for (Entry<Block, FallingBlock> b : plugin.eventListener.cubes.entrySet()) {
				if (b.getValue() == (FallingBlock) entity) {
					plugin.eventListener.FlyingBlocks.put(b.getKey(), fblock);
					k = b.getKey();
				}
			}
			if (k != null) plugin.eventListener.cubes.remove(k);
			*/
			((FallingBlock) entity).remove();
			
		}
		
	}
	
	public void EntityExitsFunnel(Entity entity)
	{
		if (entity instanceof Player) {
			Player p = (Player)entity;
			if (p.getGameMode() != GameMode.CREATIVE)
			p.setAllowFlight(false);
			p.setFlying(false);
		}
		inFunnel.remove(entity);
	}

	private void EntityMoveInFunnel(Entity entity, Funnel funnel)
	{
		
	}

	public void bridgeCheck() {
		for (Entity entity: inFunnel.keySet()) {
			Bridge bridge = bridgeBlocks.get(new V10Location(entity.getLocation()));
			if (bridge != null && bridge instanceof Funnel) {
				if (((Funnel)bridge).getDirection(entity.getLocation().getBlock()) != null)
			entity.setVelocity(FaceUtil.faceToVector(((Funnel)bridge).getDirection(entity.getLocation().getBlock())).multiply(0.1));
				else {
					entity.setVelocity(FaceUtil.faceToVector(((Funnel)bridge).facingSide).multiply(0.1));	
				}
			} else {
				if (bridge == null )bridge = bridgeBlocks.get(new V10Location(entity.getLocation().getBlock().getRelative(BlockFace.UP)));
				if (bridge != null && bridge instanceof Funnel) {
					if (((Funnel)bridge).getDirection(entity.getLocation().getBlock().getRelative(BlockFace.UP)) != null)
				entity.setVelocity(FaceUtil.faceToVector(((Funnel)bridge).getDirection(entity.getLocation().getBlock().getRelative(BlockFace.UP))).multiply(0.1).setY(0.1));
					else {
						entity.setVelocity(FaceUtil.faceToVector(((Funnel)bridge).facingSide).multiply(0.1));	
					}
				}
				
			}
		}
		
	}

/*	public Funnel getFunnelInEntity(Entity entity)
	{
		Bridge bridge = bridgeBlocks.get(new LibigotLocation(entity.getLocation()));
		if (bridge == null && ((entity.getLocation().getZ() - (double) entity.getLocation().getBlockZ()) < 0.5)) bridge = bridgeBlocks.get(new LibigotLocation(entity.getLocation().getBlock().getRelative(0,0,-1)));
		if (bridge == null && ((entity.getLocation().getZ() - (double) entity.getLocation().getBlockZ()) > 0.5)) bridge = bridgeBlocks.get(new LibigotLocation(entity.getLocation().getBlock().getRelative(0,0,1)));
		if (bridge == null && ((entity.getLocation().getX() - (double) entity.getLocation().getBlockX()) < 0.5)) bridge = bridgeBlocks.get(new LibigotLocation(entity.getLocation().getBlock().getRelative(-1,0,0)));
		if (bridge == null && ((entity.getLocation().getX() - (double) entity.getLocation().getBlockX()) > 0.5)) bridge = bridgeBlocks.get(new LibigotLocation(entity.getLocation().getBlock().getRelative(1,0,0)));

		if (bridge == null)
		{
			Location loc = entity.getLocation();
			for (int i = 1; i < 6; i++)
			{
				loc.subtract(0, 1, 0);
				
				bridge = bridgeBlocks.get(loc.getBlock());
				if (bridge == null && ((loc.getZ() - (double) loc.getBlockZ()) < 0.5)) bridge = bridgeBlocks.get(new LibigotLocation(loc.getBlock().getRelative(0,0,-1)));
				if (bridge == null && ((loc.getZ() - (double) loc.getBlockZ()) > 0.5)) bridge = bridgeBlocks.get(new LibigotLocation(loc.getBlock().getRelative(0,0,1)));
				if (bridge == null && ((loc.getX() - (double) loc.getBlockX()) < 0.5)) bridge = bridgeBlocks.get(new LibigotLocation(loc.getBlock().getRelative(-1,0,0)));
				if (bridge == null && ((loc.getX() - (double) loc.getBlockX()) > 0.5)) bridge = bridgeBlocks.get(new LibigotLocation(loc.getBlock().getRelative(1,0,0)));

				if (bridge != null && bridge instanceof Funnel)
				{
					List<LibigotLocation> list = glassBlocks.get(entity);
					if (list == null)
					{
						list = new ArrayList<LibigotLocation>();
						glassBlocks.put(entity, list);
					}
						
					Block block = entity.getLocation().getBlock().getRelative(BlockFace.DOWN, i + 1);
					if (block.isEmpty())
					{
						block.setType(Material.GLASS);
						list.add(new LibigotLocation(block));
					}
					
					break;

				}
			}
		}
		
		if (bridge != null && bridge instanceof Funnel)
			return (Funnel) bridge;
		else
			return null;
	}
	
	public void EntityMoveCheck(Entity entity)
	{
		Funnel funnel = getFunnelInEntity(entity);
		if (funnel == null && inFunnel.contains(entity))
		{
			EntityExitsFunnel(entity);
		}
		else if (funnel != null)
		{
			if (!inFunnel.contains(entity)) EntityEntersFunnel(entity);
			EntityMoveInFunnel(entity, funnel);
		}
	}
	
	private void EntityEntersFunnel(Entity entity)
	{
		inFunnel.add(entity);
		List<LibigotLocation> list = glassBlocks.get(entity);
		if (list == null)
			glassBlocks.put(entity, new ArrayList<LibigotLocation>());
	}
	
	public void EntityExitsFunnel(Entity entity)
	{
		List<LibigotLocation> list = glassBlocks.get(entity);
		if (list != null) 
			for (LibigotLocation b : list)
				b.getHandle().getBlock().setType(Material.AIR);
		inFunnel.remove(entity);
	
	}

	private void EntityMoveInFunnel(Entity entity, Funnel funnel)
	{
		BlockFace face = funnel.getDirection(entity);
		if (face == null) return;
				
		if (face == BlockFace.UP)
			entity.setVelocity(entity.getVelocity().setY(0.2));
		else if (face == BlockFace.DOWN)
			entity.setVelocity(entity.getVelocity().setY(-0.2));
		else
		{
			if (face.getModX() != 0) entity.setVelocity(entity.getVelocity().setX(((double)face.getModX()) * 0.2));
			if (face.getModZ() != 0) entity.setVelocity(entity.getVelocity().setZ(((double)face.getModZ()) * 0.2));
			
			//Generate glass
			
			Block pblock = entity.getLocation().getBlock().getRelative(BlockFace.DOWN);
			
			if (face != BlockFace.UP && face != BlockFace.DOWN && funnel.bridgeBlocks.containsKey(new LibigotLocation(pblock.getRelative(BlockFace.UP))))
			{
				if (pblock.getRelative(face).getType() == Material.AIR) 
				{
						Block block = pblock.getRelative(face);
						block.setType(Material.GLASS);
						LibigotLocation loc = new LibigotLocation(block);
						glassBlocks.get(entity).add(loc);
						glassBlockOwners.put(loc, entity);
				}
				else if (pblock.getRelative(face).getType() == Material.GLASS)
				{
					glassBlockOwners.put(new LibigotLocation(pblock.getRelative(face)), entity);
				}
								
				if (pblock.getRelative(face, 2).getType() == Material.AIR) 
				{
						Block block = pblock.getRelative(face, 2);
						block.setType(Material.GLASS);
						LibigotLocation loc = new LibigotLocation(block);
						glassBlocks.get(entity).add(loc);
						glassBlockOwners.put(loc, entity);
				}
				else if (pblock.getRelative(face, 2).getType() == Material.GLASS)
				{
					glassBlockOwners.put(new LibigotLocation(pblock.getRelative(face, 2)), entity);
				}
				Block block;
				for (LibigotLocation loc : glassBlocks.get(entity).toArray(new LibigotLocation[0]))
				{
					if (loc.getHandle().distanceSquared(entity.getLocation()) > 4) 
					{
						block = loc.getHandle().getBlock();
						if (glassBlockOwners.get(block) == entity)
						{
							block.setType(Material.AIR);
							glassBlocks.get(entity).remove(block);
						}
						if (block.getType() == Material.AIR) glassBlocks.get(entity).remove(block);
						
					}
				}
			}
		}
	}*/
}
