package org.PortalStick.listeners;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.PortalStick.PortalStick;
import org.PortalStick.components.Grill;
import org.PortalStick.components.Portal;
import org.PortalStick.components.Region;
import org.PortalStick.components.User;
import org.PortalStick.util.BlockStorage;
import org.PortalStick.util.RegionSetting;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.sanjay900.nmsUtil.events.CubeGroundTickEvent;
import com.sanjay900.nmsUtil.events.EntityCollidedWithEntityImplEvent;
import com.sanjay900.nmsUtil.events.EntityDespawnEvent;
import com.sanjay900.nmsUtil.events.EntityMoveEvent;
import com.sanjay900.nmsUtil.events.EntitySpawnEvent;
import com.sanjay900.nmsUtil.events.FrozenSandCollideWithBlockEvent;
import com.sanjay900.nmsUtil.util.V10Location;


public class PortalStickEntityListener implements Listener {
	private final PortalStick plugin;

	private final Material[] gelBlacklist = new Material[] {
			Material.ANVIL,
			Material.CHEST,
			Material.FENCE,
			Material.FENCE_GATE,
			Material.NETHER_FENCE,
			Material.IRON_FENCE,
			Material.GLASS,
			Material.THIN_GLASS,
			Material.BED_BLOCK,
			Material.TRAP_DOOR,
			Material.IRON_DOOR_BLOCK,
			Material.WOODEN_DOOR,
			Material.STONE_PLATE,
			Material.WOOD_PLATE,
			Material.DISPENSER,
			Material.NOTE_BLOCK,
			Material.WORKBENCH,
			Material.FURNACE,
			Material.PISTON_BASE,
			Material.PISTON_EXTENSION,
			Material.PISTON_MOVING_PIECE,
			Material.PISTON_STICKY_BASE,
			Material.BEACON,
			Material.GLOWSTONE,
			Material.REDSTONE_LAMP_OFF,
			Material.REDSTONE_LAMP_ON,
			Material.BEDROCK,
			Material.BURNING_FURNACE,
			Material.COMMAND,
			Material.DRAGON_EGG,
			Material.ENDER_CHEST,
			Material.JACK_O_LANTERN,
			Material.JUKEBOX,
			Material.CAKE_BLOCK,
			Material.ENCHANTMENT_TABLE,
			Material.BREWING_STAND,
			Material.WALL_SIGN,
			Material.SIGN_POST
	};

	public PortalStickEntityListener(PortalStick plugin)
	{
		this.plugin = plugin;
	}
	@EventHandler
	public void gelCollide(FrozenSandCollideWithBlockEvent evt) {
		V10Location from = evt.getFallingSand().<V10Location>getData("dispenser");
		if (from != null) {
			if (plugin.entityManager.teleportFallingFunnel(evt.getFallingSand(),new V10Location(evt.getBlock()))) return;
			Location loc = evt.getFallingSand().getLocation();
			V10Location vloc = new V10Location(loc.getWorld(), (int)loc.getX(), (int)loc.getY(), (int)loc.getZ());
			ArrayList<BlockStorage> blocks;
			if(plugin.gelManager.gels.containsKey(from))
				blocks = plugin.gelManager.gels.get(from);
			else
			{
				blocks = new ArrayList<BlockStorage>();
				plugin.gelManager.gels.put(from, blocks);
			}
			Block b = loc.getBlock();
			int mat = evt.getFallingSand().blockId;
			byte data = (byte) evt.getFallingSand().blockData;
			BlockStorage bh;
			Block b2;
			boolean bl;
			for(BlockFace face: new BlockFace[] {BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP})
			{
				b2 = b.getRelative(face);
				if(b2.getType() != Material.AIR && !b2.isLiquid() && b2.getType().isSolid())
				{
					bl = false;
					for(Material mat3: gelBlacklist)
						if(b2.getType() == mat3)
						{
							bl = true;
							break;
						}
					if(bl)
						continue;
					vloc = new V10Location(b2);
					if(plugin.portalManager.insideBlocks.containsKey(vloc) ||
							plugin.portalManager.behindBlocks.containsKey(vloc) ||
							plugin.grillManager.borderBlocks.containsKey(vloc) ||
							plugin.grillManager.insideBlocks.containsKey(vloc) ||
							plugin.funnelBridgeManager.bridgeBlocks.containsKey(vloc) ||
							plugin.funnelBridgeManager.bridgeMachineBlocks.containsKey(vloc))
						continue;
					bh = new BlockStorage(b2);
					boolean contains = false;
					for(BlockStorage bs: blocks) {
						if(bh.getLocation().equals(bs.getLocation())) {
							contains = true;
							break;
						}
					}
					if(!contains)
					{
						if(plugin.gelManager.gelMap.containsKey(vloc))
							bh = plugin.gelManager.gelMap.get(vloc);
						else
							plugin.gelManager.gelMap.put(vloc, bh);
						blocks.add(bh);
						b2.setTypeIdAndData(mat, data, true);
					}
				}
			}
			plugin.util.nmsUtil.frozenSandManager.remove(evt.getFallingSand());
			plugin.funnelBridgeManager.cubeinFunnel.remove(evt.getFallingSand());
		}
	}
	@EventHandler(ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event) {
		if(plugin.config.DisabledWorlds.contains(event.getEntity().getLocation().getWorld().getName()))
			return;

		if (event.getEntity() instanceof Player)
		{
			Player player = (Player)event.getEntity();
			if (!plugin.hasPermission(player, plugin.PERM_DAMAGE_BOOTS))
				return;
			Location loc = player.getLocation();
			Region region = plugin.regionManager.getRegion(new V10Location(loc.getWorld(), (int)loc.getX(), (int)loc.getY(), (int)loc.getZ()));
			ItemStack is = player.getInventory().getBoots();
			if (event.getCause() == DamageCause.FALL && region.getBoolean(RegionSetting.ENABLE_FALL_DAMAGE_BOOTS))
			{
				boolean ok;
				if(is == null)
					ok = false;
				else
					ok = region.getInt(RegionSetting.FALL_DAMAGE_BOOTS) == is.getTypeId();
				if(ok)
					event.setCancelled(true);
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent event)
	{
		if(plugin.config.DisabledWorlds.contains(event.getLocation().getWorld().getName()))
			return;
		Location bloc = event.getLocation();
		Region region = plugin.regionManager.getRegion(new V10Location(bloc.getWorld(), (int)bloc.getX(), (int)bloc.getY(), (int)bloc.getZ()));
		Iterator<Block> iter = event.blockList().iterator();
		Block block;
		V10Location loc;
		Portal portal;
		while(iter.hasNext())
		{
			block = iter.next();
			loc = new V10Location(block.getLocation());
			if (block.getType() == Material.WOOL)
			{
				portal = plugin.portalManager.insideBlocks.get(loc);
				if (portal == null)
					portal = plugin.portalManager.behindBlocks.get(loc);
				if (portal != null)
				{
					if (region.getBoolean(RegionSetting.PROTECT_PORTALS_FROM_TNT))
						iter.remove();
					else
					{
						portal.delete();
						return;
					}
				}
			}
			else if (plugin.blockUtil.compareBlockToString(block, region.getString(RegionSetting.GRILL_MATERIAL)))
			{
				Grill grill = plugin.grillManager.insideBlocks.get(loc);
				if (grill == null) grill = plugin.grillManager.borderBlocks.get(loc);
				if (grill != null )
				{
					event.setCancelled(true);
					return;
				}
			}
		}
	}
	@EventHandler
	public void spawn(EntitySpawnEvent evt)
	{
		Entity entity =evt.getEntity();
		if(plugin.config.DisabledWorlds.contains(entity.getLocation().getWorld().getName()))
			return;
		//	  System.out.print("Spawned: "+entity.getType());
		Location loc = entity.getLocation();
		try {
			File f = new File(plugin.getDataFolder(), "debug.txt");
			if(!f.exists())
				f.createNewFile();
			BufferedWriter writer = new BufferedWriter(new FileWriter(f, true));
			writer.write(entity.getType().name()+" at "+loc.getX()+"/"+loc.getY()+"/"+loc.getZ()+"\r\n");
			writer.flush();
			writer.close();
		} catch(Exception e) {}

		plugin.userManager.createUser(entity);
		Region region = plugin.regionManager.getRegion(new V10Location(loc.getWorld(), (int)loc.getX(), (int)loc.getY(), (int)loc.getZ()));
		if(entity instanceof InventoryHolder && !region.name.equals("global") && region.getBoolean(RegionSetting.UNIQUE_INVENTORY))
			plugin.userManager.getUser(entity).saveInventory((InventoryHolder)entity);
	}

	@EventHandler(ignoreCancelled = true)
	public void blockLand(EntityChangeBlockEvent event)
	{
		Entity entity = event.getEntity();
		if(plugin.config.DisabledWorlds.contains(entity.getLocation().getWorld().getName()))
			return;
		if(entity instanceof FallingBlock) {
			if(plugin.gelManager.flyingGels.containsKey(entity.getUniqueId())) {
				event.setCancelled(true);
				return;
			}
			FallingBlock fb = (FallingBlock) event.getEntity();
			UUID uuid = fb.getUniqueId();
			if(plugin.cubeManager.blockMap.contains(uuid)) {
				event.setCancelled(true);
				plugin.cubeManager.blockMap.remove(uuid);
				return;
			}
		} else {
			plugin.entityManager.checkPiston(event.getBlock().getLocation(), event.getEntity());
		}
	}
	@SuppressWarnings("deprecation")
	@EventHandler
	public void despawn(EntityDespawnEvent evt)
	{
		Entity entity =evt.getEntity();
		if(plugin.config.DisabledWorlds.contains(entity.getLocation().getWorld().getName()))
			return;
		if(entity instanceof FallingBlock) {
			UUID uuid = entity.getUniqueId();
			if(plugin.gelManager.flyingGels.containsKey(uuid))
			{
				V10Location from = plugin.gelManager.flyingGels.get(uuid);
				plugin.gelManager.flyingGels.remove(uuid);
				Location loc = entity.getLocation();
				V10Location vloc = new V10Location(loc.getWorld(), (int)loc.getX(), (int)loc.getY(), (int)loc.getZ());
				ArrayList<BlockStorage> blocks;
				if(plugin.gelManager.gels.containsKey(from))
					blocks = plugin.gelManager.gels.get(from);
				else
				{
					blocks = new ArrayList<BlockStorage>();
					plugin.gelManager.gels.put(from, blocks);
				}
				FallingBlock fb = (FallingBlock)entity;
				Block b = loc.getBlock();
				int mat = fb.getBlockId();
				byte data = fb.getBlockData();
				BlockStorage bh;
				Block b2;
				boolean bl;
				for(BlockFace face: new BlockFace[] {BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP})
				{
					b2 = b.getRelative(face);
					if(b2.getType() != Material.AIR && !b2.isLiquid() && b2.getType().isSolid())
					{
						bl = false;
						for(Material mat3: gelBlacklist)
							if(b2.getType() == mat3)
							{
								bl = true;
								break;
							}
						if(bl)
							continue;
						vloc = new V10Location(b2);
						if(plugin.portalManager.insideBlocks.containsKey(vloc) ||
								plugin.portalManager.behindBlocks.containsKey(vloc) ||
								plugin.grillManager.borderBlocks.containsKey(vloc) ||
								plugin.grillManager.insideBlocks.containsKey(vloc) ||
								plugin.funnelBridgeManager.bridgeBlocks.containsKey(vloc) ||
								plugin.funnelBridgeManager.bridgeMachineBlocks.containsKey(vloc))
							continue;
						bh = new BlockStorage(b2);
						boolean contains = false;
						for(BlockStorage bs: blocks) {
							if(bh.getLocation().equals(bs.getLocation())) {
								contains = true;
								break;
							}
						}
						if(!contains)
						{
							if(plugin.gelManager.gelMap.containsKey(vloc))
								bh = plugin.gelManager.gelMap.get(vloc);
							else
								plugin.gelManager.gelMap.put(vloc, bh);
							blocks.add(bh);
							b2.setTypeIdAndData(mat, data, true);
						}
					}
				}
			} 
		} else {
			plugin.entityManager.checkPiston(entity.getLocation(), entity);
		}

		User user = plugin.userManager.getUser(entity);
		Location loc = entity.getLocation();
		Region region = plugin.regionManager.getRegion(new V10Location(loc.getWorld(), (int)loc.getX(), (int)loc.getY(), (int)loc.getZ()));
		if(entity instanceof InventoryHolder && region.name != "global" && region.getBoolean(RegionSetting.UNIQUE_INVENTORY))
			user.revertInventory((InventoryHolder)entity);
		plugin.userManager.deleteUser(entity);
		if(entity instanceof Player) { //TODO
			Player player = (Player)entity;
			plugin.gelManager.resetPlayer(player);
			plugin.util.nmsUtil.frozenSandManager.clearFrozenSand(player);
		}
	}
	//Temporary because fallingblock.setItemDrop(false) is bugged.
	//TODO: Remove when fixed by spigot, or we work out what causes it.
	@EventHandler
	public void onItemSpawn(ItemSpawnEvent event){
	    List<Entity> ents = event.getEntity().getNearbyEntities(1, 1, 1);
	    for(Entity e : ents){
	       if (plugin.gelManager.flyingGels.containsKey(e.getUniqueId())) {
	    	   event.setCancelled(true);
	       }
	    }
	}

	@EventHandler
	public void entityMove(EntityMoveEvent evt)
	{
		Entity entity =evt.getEntity();
		if(entity instanceof Player || (entity instanceof Vehicle && !(entity instanceof Pig)))
			return;
		plugin.entityManager.onEntityMove(entity, evt.getFrom(), evt.getTo(), true);
	}

	@EventHandler
	public void tp(EntityTeleportEvent event) {
		String oldWorld = event.getFrom().getWorld().getName();
		String newWorld = event.getTo().getWorld().getName();
		if(oldWorld.equals(newWorld)) {
			return;
		}
		Player player = event.getEntity() instanceof Player ? (Player)event.getEntity() : null;
		boolean oldEnabled = plugin.config.DisabledWorlds.contains(oldWorld);
		if(oldEnabled && player != null)
			plugin.util.nmsUtil.frozenSandManager.clearFrozenSand(player);
		boolean newEnabled = plugin.config.DisabledWorlds.contains(newWorld);
		if(oldEnabled == newEnabled) {
			return;
		}
		if(newEnabled) {
			plugin.userManager.createUser(event.getEntity());
		} else {
			plugin.userManager.deleteUser(event.getEntity());
		}
	}
	@EventHandler
	public void cubeCollideBlock(EntityCollidedWithEntityImplEvent evt) {
		Entity en = evt.getImplementedEntity().getBukkitEntity();
		Vector v = en.getLocation().toVector().subtract(evt.getCollisionEntity().getLocation().toVector());
		Location l = en.getLocation().add(v).getBlock().getLocation();
		plugin.entityManager.teleport(en, en.getLocation(), en.getLocation(), new V10Location(l), en.getVelocity(), true);
		
	}
	@SuppressWarnings("deprecation")
	@EventHandler
	public void cubeGroundTick(CubeGroundTickEvent evt) {
		Block blockloc = evt.getLocation().getBlock();
		org.bukkit.block.Block blockUnder = evt.getCube().getBukkitEntity().getLocation().getBlock().getRelative(BlockFace.DOWN);
		if (blockUnder.getType().name().contains("LAVA")|| blockloc.getType().name().contains("LAVA")) {
			plugin.util.clear(evt.getCube().<V10Location>getStored("respawnLoc").getHandle().getBlock(), true, ((FallingBlock)evt.getCube().getBukkitEntity()).getBlockId(), ((FallingBlock)evt.getCube().getBukkitEntity()).getBlockData(), plugin.cubeManager.cubesign.get(evt.getCube().<V10Location>getStored("respawnLoc")).getHandle().getBlock(), evt.getCube());
			evt.getCube().getBukkitEntity().remove();
			return;
		}
		if (blockUnder.getType() == Material.WOOL
				&& (blockUnder.getData() == (byte) 1)) {

			evt.getCube().getBukkitEntity().setVelocity(evt.getCube().getBukkitEntity().getVelocity());
			return;
		} else if (blockUnder.getType() == Material.WOOL
				&& (blockUnder.getData() == (byte) 3)){
			Vector vel = evt.getCube().getBukkitEntity().getVelocity();
			Region region = plugin.regionManager.getRegion(new V10Location(blockloc.getLocation()));
			double min = region.getDouble(RegionSetting.BLUE_GEL_MIN_VELOCITY);
			double y = -vel.getY();
			if (y < min) y = min; 
			vel.setY(y);
			evt.getCube().getBukkitEntity().setVelocity(vel);
		}
		else {
			evt.getCube().getBukkitEntity().setVelocity(evt.getCube().getBukkitEntity().getVelocity().multiply(0.5));

			if (blockUnder.getType() == Material.WOOL
					&& (blockUnder.getData() == (byte) 15
					|| blockUnder.getData() == (byte) 14 || blockUnder
					.getData() == (byte) 5)) {

				org.bukkit.block.Block middle = plugin.util.chkBtn(blockloc.getLocation());

				if (middle != null) {
					V10Location loc = new V10Location(middle);
					if(!plugin.cubeManager.buttonsToEntity.containsValue(loc)) {
						plugin.util.changeBtn(loc, true);
					}
					plugin.cubeManager.buttonsToEntity.put(evt.getCube().getUniqueID(), loc);
				}
			} else {
				if(plugin.cubeManager.buttonsToEntity.containsKey(evt.getCube().getUniqueID())) {
					V10Location loc = plugin.cubeManager.buttonsToEntity.get(evt.getCube().getUniqueID());
					plugin.cubeManager.buttonsToEntity.remove(evt.getCube().getUniqueID());
					if(!plugin.cubeManager.buttonsToEntity.containsValue(loc)) {
						plugin.util.changeBtn(loc, false);
					}
				}

			}
		}

	}
}
