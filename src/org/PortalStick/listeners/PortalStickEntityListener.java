package org.PortalStick.listeners;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

import org.PortalStick.Grill;
import org.PortalStick.Portal;
import org.PortalStick.PortalStick;
import org.PortalStick.Region;
import org.PortalStick.User;
import org.PortalStick.fallingblocks.FrozenSand;
import org.PortalStick.fallingblocks.FrozenSandFactory;
import org.PortalStick.util.BlockStorage;
import org.PortalStick.util.RegionSetting;
import org.PortalStick.util.V10Location;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.bergerkiller.bukkit.common.events.EntityAddEvent;
import com.bergerkiller.bukkit.common.events.EntityMoveEvent;
import com.bergerkiller.bukkit.common.events.EntityRemoveEvent;

public class PortalStickEntityListener implements Listener {
	private final PortalStick plugin;
	
	private final int[] gelBlacklist = new int[] {
	  Material.ANVIL.getId(),
	  Material.CHEST.getId(),
	  Material.FENCE.getId(),
	  Material.FENCE_GATE.getId(),
	  Material.NETHER_FENCE.getId(),
	  Material.IRON_FENCE.getId(),
	  Material.GLASS.getId(),
	  Material.THIN_GLASS.getId(),
	  Material.BED_BLOCK.getId(),
	  Material.TRAP_DOOR.getId(),
	  Material.IRON_DOOR_BLOCK.getId(),
	  Material.WOODEN_DOOR.getId(),
	  Material.STONE_PLATE.getId(),
	  Material.WOOD_PLATE.getId(),
	  Material.DISPENSER.getId(),
	  Material.NOTE_BLOCK.getId(),
	  Material.WORKBENCH.getId(),
	  Material.FURNACE.getId(),
	  Material.PISTON_BASE.getId(),
	  Material.PISTON_EXTENSION.getId(),
	  Material.PISTON_MOVING_PIECE.getId(),
	  Material.PISTON_STICKY_BASE.getId(),
	  Material.BEACON.getId(),
	  Material.GLOWSTONE.getId(),
	  Material.REDSTONE_LAMP_OFF.getId(),
	  Material.REDSTONE_LAMP_ON.getId(),
	  Material.BEDROCK.getId(),
	  Material.BURNING_FURNACE.getId(),
	  Material.COMMAND.getId(),
	  Material.DRAGON_EGG.getId(),
	  Material.ENDER_CHEST.getId(),
	  Material.JACK_O_LANTERN.getId(),
	  Material.JUKEBOX.getId(),
	  Material.CAKE_BLOCK.getId(),
	  Material.ENCHANTMENT_TABLE.getId(),
	  Material.BREWING_STAND.getId(),
	  Material.WALL_SIGN.getId(),
	  Material.SIGN_POST.getId()
	};
	
	public PortalStickEntityListener(PortalStick plugin)
	{
		this.plugin = plugin;
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
				portal = plugin.portalManager.borderBlocks.get(loc);
				if (portal == null)
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
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void spawn(EntityAddEvent event)
	{
	  Entity entity = event.getEntity();
	  if(plugin.config.DisabledWorlds.contains(entity.getLocation().getWorld().getName()))
		return;
//	  System.out.print("Spawned: "+entity.getType());
	  Location loc = entity.getLocation();
	  try {
	      File f = new File(plugin.getDataFolder(), "debug.txt");
	      if(!f.exists())
	          f.createNewFile();
	      BufferedWriter writer = new BufferedWriter(new FileWriter(f, true));
	      writer.write(entity.getType().getName()+" at "+loc.getX()+"/"+loc.getY()+"/"+loc.getZ()+"\r\n");
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
          
          if(plugin.cubeManager.cubes.containsValue(uuid)) {
              Block up = event.getBlock();
              if (up.getType().name().contains("LAVA")) {
                  for (Entry<V10Location, UUID> entry: plugin.cubeManager.cubes.entrySet()) {
                      if (uuid.equals(entry.getValue())) {
                          plugin.util.clear(entry.getKey().getHandle().getBlock(), true, fb.getBlockId(), fb.getBlockData(), plugin.cubeManager.cubesign.get(entry.getKey()).getHandle().getBlock());
                              event.getEntity().remove();
                              event.setCancelled(true);
                              return;
                          
                      }
                  }
              }
          }
          FrozenSand fblock = null;
          for (Entry<V10Location, UUID> entry : plugin.cubeManager.cubes.entrySet()) {
              if (uuid.equals(entry.getValue())) {
                  event.setCancelled(true);
                  String id = String.valueOf(((FallingBlock) event.getEntity())
                          .getMaterial().getId())+":"+String.valueOf(((FallingBlock) event.getEntity())
                                  .getBlockData());
                  fblock = new FrozenSandFactory(plugin).withLocation(event.getEntity().getLocation()).withText(id).build();

                  plugin.cubeManager.flyingBlocks.put(entry.getKey(), fblock);
                  event.getEntity().remove();
                  plugin.cubeManager.cubes.remove(entry.getKey());
                  break;
              }
          }
          if (fblock == null) return;
          Block blockUnder = event.getBlock().getRelative(BlockFace.DOWN);
          if (blockUnder.getType() == Material.WOOL
                  && (blockUnder.getData() == (byte) 15
                  || blockUnder.getData() == (byte) 14 || blockUnder
                  .getData() == (byte) 5)) {

              Block middle = plugin.util.chkBtn(event.getBlock().getLocation());

              if (middle != null) {
                  V10Location loc = new V10Location(middle);
                  if(!plugin.cubeManager.buttons.containsKey(loc)) {

                      plugin.util.changeBtn(middle, true);
                      plugin.cubeManager.buttons.put(loc, fblock);
                  }
              }
          } else if (blockUnder.getType() == Material.WOOL
                  && (blockUnder.getData() == (byte) 1)) {

              fblock.setVelocity(event.getEntity().getVelocity());
              return;
          }
	  } else {
	      plugin.entityManager.checkPiston(event.getBlock().getLocation(), event.getEntity());
	  }
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void despawn(EntityRemoveEvent event)
	{
	    Entity entity = event.getEntity();
	    if(plugin.config.DisabledWorlds.contains(entity.getLocation().getWorld().getName()))
	        return;

	    //Remove flying gels from the map. We can't do this if they don't try to place themself in the event above...
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
	            int mat2;
	            for(BlockFace face: new BlockFace[] {BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP})
	            {
	                b2 = b.getRelative(face);
	                if(b2.getType() != Material.AIR && !b2.isLiquid() && b2.getType().isSolid())
	                {
	                    bl = false;
	                    mat2 = b2.getTypeId();
	                    for(int mat3: gelBlacklist)
	                        if(mat2 == mat3)
	                        {
	                            bl = true;
	                            break;
	                        }
	                    if(bl)
	                        continue;
	                    vloc = new V10Location(b2);
	                    if(plugin.portalManager.borderBlocks.containsKey(vloc) ||
	                            plugin.portalManager.insideBlocks.containsKey(vloc) ||
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
	        } else {
	            FallingBlock fb = (FallingBlock)entity;
	            FrozenSand fblock = null;
	            for (Entry<V10Location, UUID> entry : plugin.cubeManager.cubes.entrySet()) {
	                if (uuid.equals(entry.getValue())) {
	                    if(!plugin.cubeManager.respawnCubes.contains(uuid)) {
	                        String id = String.valueOf(fb
	                                .getMaterial().getId())+":"+String.valueOf(fb
	                                        .getBlockData());
	                        fblock = new FrozenSandFactory(plugin).withLocation(fb.getLocation()).withText(id).build();

	                        plugin.cubeManager.flyingBlocks.put(entry.getKey(), fblock);
	                    } else {
	                        plugin.cubeManager.respawnCubes.remove(uuid);
	                        plugin.util.clear(entry.getKey().getHandle().getBlock(), true, fb.getBlockId(), fb.getBlockData(), plugin.cubeManager.cubesign.get(entry.getKey()).getHandle().getBlock());
	                    }
	                    event.getEntity().remove();
	                    plugin.cubeManager.cubes.remove(entry.getKey());
	                    break;
	                }
	            }
	            if (fblock == null) return;
	            Block blockUnder = entity.getLocation().getBlock().getRelative(BlockFace.DOWN);
	            if (blockUnder.getType() == Material.WOOL
	                    && (blockUnder.getData() == (byte) 15
	                    || blockUnder.getData() == (byte) 14 || blockUnder
	                    .getData() == (byte) 5)) {

	                Block middle = plugin.util.chkBtn(event.getEntity().getLocation());
	                if (middle != null) {
	                    V10Location loc = new V10Location(middle);
	                    if(!plugin.cubeManager.buttons.containsKey(loc)) {

	                        plugin.util.changeBtn(middle, true);
	                        plugin.cubeManager.buttons.put(loc, fblock);
	                    }
	                }
	            } else if (blockUnder.getType() == Material.WOOL
	                    && (blockUnder.getData() == (byte) 1)) {

	                fblock.setVelocity(entity.getVelocity());
	                return;
	            } 
	        }
	    } else {
            plugin.entityManager.checkPiston(entity.getLocation(), event.getEntity());
        }

	    User user = plugin.userManager.getUser(entity);
	    // TODO: Check if fixed
	    //if(user == null) //TODO: Workaround against BKCommonLib bugs.
	    //return;

	    Location loc = entity.getLocation();
	    Region region = plugin.regionManager.getRegion(new V10Location(loc.getWorld(), (int)loc.getX(), (int)loc.getY(), (int)loc.getZ()));
	    if(entity instanceof InventoryHolder && region.name != "global" && region.getBoolean(RegionSetting.UNIQUE_INVENTORY))
	        user.revertInventory((InventoryHolder)entity);
	    plugin.userManager.deleteUser(entity);
	    if(entity instanceof Player) //TODO
	        plugin.gelManager.resetPlayer((Player)entity);
	}
	
	@EventHandler
	public void entityMove(EntityMoveEvent event)
	{
	  Entity entity = event.getEntity();
	  if(entity instanceof Player || (entity instanceof Vehicle && !(entity instanceof Pig)))
		return;
	  plugin.entityManager.onEntityMove(entity, new Location(event.getWorld(), event.getFromX(), event.getFromY(), event.getFromZ()), new Location(event.getWorld(), event.getToX(), event.getToY(), event.getToZ()), true);
	}
	
	@EventHandler
	public void tp(EntityTeleportEvent event) {
	    String oldWorld = event.getFrom().getWorld().getName();
	    String newWorld = event.getTo().getWorld().getName();
	    if(oldWorld.equals(newWorld)) {
	        return;
	    }
	    boolean oldEnabled = plugin.config.DisabledWorlds.contains(oldWorld);
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
}
