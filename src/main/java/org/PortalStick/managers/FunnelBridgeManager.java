package org.PortalStick.managers;

import java.util.HashMap;
import java.util.HashSet;

import org.PortalStick.PortalStick;
import org.PortalStick.components.Bridge;
import org.PortalStick.components.Funnel;
import org.PortalStick.components.Portal;
import org.PortalStick.components.Region;
import org.PortalStick.util.RegionSetting;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.sanjay900.nmsUtil.EntityCubeImpl;
import com.sanjay900.nmsUtil.fallingblocks.FrozenSand;
import com.sanjay900.nmsUtil.fallingblocks.FrozenSandFactory;
import com.sanjay900.nmsUtil.util.Utils;
import com.sanjay900.nmsUtil.util.V10Location;


public class FunnelBridgeManager {
	private final PortalStick plugin;
	
	public FunnelBridgeManager(PortalStick plugin)
	{
	  this.plugin = plugin;
	}
	
	public HashSet<Bridge> bridges = new HashSet<Bridge>();
	public HashMap<Portal, Bridge> involvedPortals = new HashMap<Portal, Bridge>();
	public HashMap<V10Location, Bridge> bridgeBlocks = new HashMap<V10Location, Bridge>();
	public HashMap<V10Location, Bridge> bridgeMachineBlocks = new HashMap<V10Location, Bridge>();
	private HashMap<Entity,Funnel> inFunnel = new HashMap<Entity,Funnel>();
	public HashMap<FrozenSand,Funnel> cubeinFunnel = new HashMap<FrozenSand,Funnel>();

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
		Boolean torchOn = true;
		for (BlockFace check : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP})
		{
			if (firstIron.getRelative(check).getType() == Material.REDSTONE_TORCH_ON)
			{
				havetorch = true;
				machineBlocks.add(new V10Location(firstIron.getRelative(check)));
				break;
			}
			if (firstIron.getRelative(check).getType() == Material.REDSTONE_TORCH_OFF)
			{
				havetorch = true;
				torchOn = false;
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
			if (secondIron.getRelative(check).getType() == Material.REDSTONE_TORCH_OFF)
			{
				havetorch = true;
				torchOn = false;
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
		
		if (torchOn) {
		bridge.activate();
		}
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
			bridge.reorient(portal.coord.inside);
		
		for (Bridge cbridge : bridges)
		{
			for (V10Location b: portal.coord.inside)
			{
			    if(b != null && (cbridge.isBlockNextToBridge(b) || cbridge.isBlockNextToBridge(new V10Location(b.getWorldName(), b.getX(), b.getY() - 1, b.getZ()))))
			        cbridge.portal = true;
			    	cbridge.reorient(portal.coord.inside);
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
						cbridge.reorient(block);
				}
		    }
		}, 1L);
		
	}
	
	public void loadBridge(String blockloc) {
		String[] locarr = blockloc.split(",");
		if (plugin.getServer().getWorld(locarr[0]) == null) return;
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
	
	@SuppressWarnings("deprecation")
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
			FallingBlock fb = (FallingBlock) entity;
			EntityCubeImpl c = plugin.util.nmsUtil.getCube(entity);
			if (c != null) {
				Bridge bridge = bridgeBlocks.get(new V10Location(entity.getLocation()));
				if (bridge == null) return;
				String id = String.valueOf(fb
						.getMaterial().getId())+":"+String.valueOf(fb
								.getBlockData());
				FrozenSand fblock = new FrozenSandFactory(plugin, plugin.util.nmsUtil).withLocation(fb.getLocation()).withText(id).build();
				cubeinFunnel.put(fblock, funnel);
				fblock.setData("respawnLoc",c.<V10Location>getStored("respawnLoc"));
				entity.remove();
			} else {
				if (plugin.gelManager.flyingGels.containsKey(fb.getUniqueId())) {
					Bridge bridge = bridgeBlocks.get(new V10Location(entity.getLocation()));
					if (bridge == null) return;
					String id = String.valueOf(fb
							.getMaterial().getId())+":"+String.valueOf(fb
									.getBlockData());
					FrozenSand fblock = new FrozenSandFactory(plugin, plugin.util.nmsUtil).withLocation(fb.getLocation()).withText(id).build();
					fblock.setData("dispenser", plugin.gelManager.flyingGels.get(fb.getUniqueId()));
					cubeinFunnel.put(fblock, funnel);
					entity.remove();
				}
			}
			
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
		for (FrozenSand entity : cubeinFunnel.keySet()) {
			Bridge bridge = bridgeBlocks.get(new V10Location(entity.getLocation()));
			if (bridge != null && bridge instanceof Funnel) {
				if (((Funnel)bridge).getDirection(entity.getLocation().getBlock()) != null)
			entity.setVelocity(Utils.faceToVector(((Funnel)bridge).getDirection(entity.getLocation().getBlock())).multiply(0.1));
				else {
					entity.setVelocity(Utils.faceToVector(((Funnel)bridge).facingSide).multiply(0.1));	
				}
			} else {
				if (bridge == null )bridge = bridgeBlocks.get(new V10Location(entity.getLocation().getBlock().getRelative(BlockFace.UP)));
				if (bridge != null && bridge instanceof Funnel) {
					if (((Funnel)bridge).getDirection(entity.getLocation().getBlock().getRelative(BlockFace.UP)) != null)
				entity.setVelocity(Utils.faceToVector(((Funnel)bridge).getDirection(entity.getLocation().getBlock().getRelative(BlockFace.UP))).multiply(0.1).setY(0.1));
					else {
						entity.setVelocity(Utils.faceToVector(((Funnel)bridge).facingSide).multiply(0.1));	
					}
				} else {
					entity.setVelocity(new Vector(0,0,0));
				}
				
			}
		}
		for (Entity entity: inFunnel.keySet()) {
			Bridge bridge = bridgeBlocks.get(new V10Location(entity.getLocation()));
			if (bridge != null && bridge instanceof Funnel) {
				if (((Funnel)bridge).getDirection(entity.getLocation().getBlock()) != null)
			entity.setVelocity(Utils.faceToVector(((Funnel)bridge).getDirection(entity.getLocation().getBlock())).multiply(0.1));
				else {
					entity.setVelocity(Utils.faceToVector(((Funnel)bridge).facingSide).multiply(0.1));	
				}
			} else {
				if (bridge == null )bridge = bridgeBlocks.get(new V10Location(entity.getLocation().getBlock().getRelative(BlockFace.UP)));
				if (bridge != null && bridge instanceof Funnel) {
					if (((Funnel)bridge).getDirection(entity.getLocation().getBlock().getRelative(BlockFace.UP)) != null)
				entity.setVelocity(Utils.faceToVector(((Funnel)bridge).getDirection(entity.getLocation().getBlock().getRelative(BlockFace.UP))).multiply(0.1).setY(0.1));
					else {
						entity.setVelocity(Utils.faceToVector(((Funnel)bridge).facingSide).multiply(0.1));	
					}
				}
				
			}
		}
		
	}

}
