package org.PortalStick.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.PortalStick.PortalStick;
import org.PortalStick.fallingblocks.FrozenSand;
import org.PortalStick.util.Config.Sound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Lever;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockIterator;
import org.getspout.spoutapi.SpoutManager;

import com.bergerkiller.bukkit.common.utils.EntityUtil;

public class Util {
	private final PortalStick plugin;
	private int maxLength = 105;
	
	BlockFace[] blockfaces = new BlockFace[] { BlockFace.WEST,
	        BlockFace.NORTH_WEST, BlockFace.NORTH, BlockFace.NORTH_EAST,
	        BlockFace.EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST,
	        BlockFace.SOUTH_EAST };
	BlockFace[] blockfacesn = new BlockFace[] { BlockFace.WEST,
	        BlockFace.NORTH, 
	        BlockFace.EAST, BlockFace.SOUTH};
	public Util(PortalStick plugin)
	{
		this.plugin = plugin;
	}
	
	public void sendMessage(CommandSender player, String msg) {
		int i;
		String part;
		ChatColor lastColor = ChatColor.RESET;
		for (String line : msg.split("`n")) {
			i = 0;
			while (i < line.length()) {
				part = getMaxString(line.substring(i));
				if (i+part.length() < line.length() && part.contains(" "))
					part = part.substring(0, part.lastIndexOf(" "));
				part = lastColor + part;
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', part));
				lastColor = getLastColor(part);
				i = i + part.length() -1;
			}
		}
	}
	
	public Location getSimpleLocation(Location location) {
		location.setX((double)Math.round(location.getX() * 10) / 10);
		location.setY((double)Math.round(location.getY() * 10) / 10);
		location.setZ((double)Math.round(location.getZ() * 10) / 10);
		return location;
	}
	
	public ChatColor getLastColor(String str) {
		int i = 0;
		ChatColor lastColor = ChatColor.RESET;
		while (i < str.length()-2) {
			for (ChatColor color: ChatColor.values()) {
				if (str.substring(i, i+2).equalsIgnoreCase(color.toString()))
					lastColor = color;
			}
			i = i+2;
		}
		return lastColor;
	}
    
    private String getMaxString(String str) {
    	for (int i = 0; i < str.length(); i++) {
    		if (str.substring(0, i).length() == maxLength) {
    			if (str.substring(i, i+1) == "")
    				return str.substring(0, i-1);
    			else
    				return str.substring(0, i);
    		}
    	}
    	return str;
    }
    
    private void playNativeSound(Sound sound, V10Location loc)
    {
      boolean oldState = plugin.config.useSpoutSounds;
      plugin.config.useSpoutSounds = false;
      playSound(sound, loc);
      plugin.config.useSpoutSounds = oldState;
    }
    
    public void playSound(Sound sound, V10Location loc)
    {
      if (!plugin.regionManager.getRegion(loc).getBoolean(RegionSetting.ENABLE_SOUNDS))
    	return;
      
      Plugin spoutPlugin = plugin.getServer().getPluginManager().getPlugin("Spout");
      if(spoutPlugin == null || !plugin.config.useSpoutSounds)
      {
        if(plugin.config.useNativeSounds)
        {
          String raw = plugin.config.soundNative[sound.ordinal()];
          if(raw == null || raw.equals(""))
          {
        	if(plugin.config.debug)
        	  plugin.getLogger().info("Sound "+sound.toString()+" not found!");
        	return;
          }
          String[] split = raw.split(":");
          float volume = 1.0F;
          float pitch = volume;
          if(split.length > 1)
        	try
          	{
        	  volume = Float.parseFloat(split[1]);
          	}
          	catch(Exception e)
          	{
          	  if(plugin.config.debug)
          		  plugin.getLogger().info("Warning: Invalid volume \""+split[1]+"\" for sound "+split[0]);
          	  volume = 1.0F;
          	}
          if(split.length > 2)
          {
        	try
        	{
        	  pitch = Float.parseFloat(split[2]);
        	}
        	catch(Exception e)
          	{
        	  if(plugin.config.debug)
        		  plugin.getLogger().info("Warning: Invalid pitch \""+split[2]+"\" for sound "+split[0]);
          	  pitch = 1.0F;
          	}
          }
          try
          {
        	org.bukkit.Sound s = org.bukkit.Sound.valueOf(split[0]);
        	loc.getHandle().getWorld().playSound(loc.getHandle(), s, volume, pitch);
          }
          catch(IllegalArgumentException e)
          {
        	e.printStackTrace();
          }
        }
      }
      else
      {
    	String url = plugin.config.soundUrls[sound.ordinal()];
    	if(url != null && url.length() > 4 && url.length() < 257)
    	  SpoutManager.getSoundManager().playGlobalCustomSoundEffect(plugin, url, false, loc.getHandle(), plugin.config.soundRange);
    	else
    	  playNativeSound(sound, loc);
      }
    }
    
    public int getLeftPortalColor(int preset)
    {
    	return Integer.parseInt(plugin.config.ColorPresets.get(preset).split("-")[0]);
    }
    
    public int getRightPortalColor(int preset)
    {
    	return Integer.parseInt(plugin.config.ColorPresets.get(preset).split("-")[1]);
    }
    
    public ItemStack getItemData(String itemString)
    {
    	int num;
    	int id;
    	short data;
    	
    	String[] split = itemString.split(",");
    	if (split.length < 2)
    		num = 1;
    	else
    		num = Integer.parseInt(split[1]);
    	split = split[0].split(":");
    	if (split.length < 2)
    		data = 0;
    	else
    		data = Short.parseShort(split[1]);

    	id = Integer.parseInt(split[0]);
    	return new ItemStack(id, num, data);
    }
    
    public void setItemNameAndDesc(ItemStack item, String name, String desc) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if(desc != null)
            meta.setLore(Arrays.asList(desc.split("\n")));
        item.setItemMeta(meta);
    }
    
    public boolean isPortalGun(ItemStack item) {
        return item != null && item.getTypeId() == plugin.config.PortalTool &&
                item.getDurability() == plugin.config.portalToolData &&
                item.getItemMeta().getDisplayName() != null &&
                item.getItemMeta().getDisplayName().equals(plugin.config.portalToolName);
    }
    
    public ItemStack createPortalGun() {
        ItemStack gun = new ItemStack(plugin.config.PortalTool, 1, plugin.config.portalToolData);
        plugin.util.setItemNameAndDesc(gun, plugin.config.portalToolName, plugin.config.portalToolDesc);
        return gun;
    }
    
    public boolean compareLocation(Location l, Location l2) {
        return (l.getWorld().equals(l2.getWorld())
                && l.getX() == l2.getX())
                && (l.getY() == l2.getY())
                && (l.getZ() == l2.getZ());

    }
    public Entity getTarget(final Player player) {

        BlockIterator iterator = new BlockIterator(player.getWorld(), player
                .getLocation().toVector(), player.getEyeLocation()
                .getDirection(), 0, 100);
        Entity target = null;
        Block item;
        List<Entity> entityList = null;
        if(iterator.hasNext())
            entityList = player.getNearbyEntities(100, 100, 100);
        while (iterator.hasNext()) {
            item = iterator.next();
            for (Entity entity : entityList) {
                int acc = 1;
                for (int x = -acc; x < acc; x++)
                    for (int z = -acc; z < acc; z++)
                        for (int y = -acc; y < acc; y++)
                            if (entity.getLocation().getBlock()
                                    .getRelative(x, y, z).equals(item)) {
                                return target = entity;
                            }
            }
        }
        return target;
    }
    public Entry<Block, FrozenSand> getTargetFlying(final Player player, PortalStick plugin) {

        BlockIterator iterator = new BlockIterator(player.getWorld(), player
                .getLocation().toVector(), player.getEyeLocation()
                .getDirection(), 0, 2);
        Block item;
        while (iterator.hasNext()) {
            item = iterator.next();
            for (Entry<Block, FrozenSand> fb : plugin.eventListener.FlyingBlocks.entrySet()) {
                int acc = 2;
                for (int x = -acc; x < acc; x++)
                    for (int z = -acc; z < acc; z++)
                        for (int y = -acc; y < acc; y++) {
                            
                            if (fb.getValue().getLocation().getBlock()
                                    .getRelative(x, y, z).equals(item)) {
                                return fb;
                            }
                        }
            }
        }
        return null;
    }
    /**
     * Removes a item from a inventory
     * 
     * @param inventory
     *            The inventory to remove from.
     * @param mat
     *            The material to remove .
     * @param amount
     *            The amount to remove.
     * @param damage
     *            The data value or -1 if this does not matter.
     */
    
    public void remove(Inventory inv, Material type, int amount,
            short damage) {
        ItemStack[] items = inv.getContents();
        for (int i = 0; i < items.length; i++) {
            ItemStack is = items[i];
            if (is != null && is.getType() == type
                    && is.getData().getData() == damage) {
                int newamount = is.getAmount() - amount;
                if (newamount > 0) {
                    is.setAmount(newamount);
                    break;
                } else {
                    items[i] = null;
                    amount = -newamount;
                    if (amount == 0)
                        break;
                }
            }
        }
        inv.setContents(items);

    }

    /**
     * Checks weather the inventory contains a item or not.
     * 
     * @param inventory
     *            The inventory to check..
     * @param mat
     *            The material to check .
     * @param amount
     *            The amount to check.
     * @param damage
     *            The data value or -1 if this does not matter.
     * @return The amount of items the player has not. If this return 0 then the
     *         check was successfull.
     */
    public int contains(Inventory inventory, Material mat, int amount,
            short damage) {
        int searchAmount = 0;
        for (ItemStack item : inventory.getContents()) {

            if (item == null || !item.getType().equals(mat)) {
                continue;
            }

            if (damage != -1 && item.getDurability() == damage) {
                continue;
            }

            searchAmount += item.getAmount();
        }
        return searchAmount - amount;
    }
    
    public void changeBtn(Block middle, boolean on) {
        Block under = middle.getRelative(BlockFace.DOWN);
        byte data;
        byte ldata;
        if(on) {
            data = 5;
            ldata = 8;
        } else {
            data = 14;
            ldata = 0;
        }
        for (BlockFace f : blockfaces)
            middle.getRelative(f).setTypeIdAndData(Material.WOOL.getId(), data, true);
        for (BlockFace f : blockfacesn) {
        	Block block = under.getRelative(f,2);
        	Lever lever;
        	BlockState state;
            Block supportBlock;
            if (!block.getRelative(BlockFace.UP).getType().isSolid()) {
            	byte lrdata;
            	switch (f) {
            	case EAST:
                    lrdata = 0x1;
                    break;

                case WEST:
                    lrdata = 0x2;
                    break;

                case SOUTH:
                    lrdata = 0x3;
                    break;

                case NORTH:
                    lrdata = 0x4;
                    break;
				default:
					lrdata = 0x1;
					break;
            	}
            	block = under.getRelative(f,1);
            	block.setType(Material.LEVER);
            	state = block.getState(); 
            	state.getData().setData(lrdata);
            	state.update();
                lever = (Lever) state.getData();
            	supportBlock = block.getRelative(f.getOppositeFace());
            	supportBlock.setType(Material.EMERALD_BLOCK);
            } else {
            	block.setType(Material.LEVER);
            	state = block.getState(); 
               lever = (Lever) state.getData();
               supportBlock = block.getRelative(lever.getAttachedFace());
            }
            lever.setPowered(on);
            state.setData(lever);
            state.update();

            BlockState initialSupportState = supportBlock.getState();
            BlockState supportState = supportBlock.getState();
            supportState.setType(Material.AIR);
            supportState.update(true, false);
            initialSupportState.update(true);
        	
        }
    }
    
    public void changeBtnInner(Block middle, boolean on) {
        Material mat;
        byte data;
        if (on) {
            mat = Material.EMERALD_BLOCK;
            data = 5;
        } else {
            mat = Material.REDSTONE_BLOCK;
            data = 14;
        }
            
        for (BlockFace f : blockfaces) {
            middle.getRelative(f).setType(Material.WOOL);
            middle.getRelative(f).setData(data);

        }
        middle.getRelative(BlockFace.DOWN).getRelative(BlockFace.EAST, 2)
        .setType(mat);
        middle.getRelative(BlockFace.DOWN).getRelative(BlockFace.WEST, 2)
        .setType(mat);
        middle.getRelative(BlockFace.DOWN).getRelative(BlockFace.NORTH, 2)
        .setType(mat);
        middle.getRelative(BlockFace.DOWN).getRelative(BlockFace.SOUTH, 2)
        .setType(mat);
    }
    public void doInventoryUpdate(final Player player, Plugin plugin) {
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            
            @Override
            public void run() {
                player.updateInventory();
            }
        }, 1L);
    }
    
    public void clear(Block hatchMiddle, boolean powered, PortalStick plugin, int id, int data, Block sign) {
        if (plugin.eventListener.cubes.containsKey(hatchMiddle)) {
            if (EntityUtil.getEntity(hatchMiddle.getWorld(),plugin.eventListener.cubes.get(hatchMiddle)) != null)
                EntityUtil.getEntity(hatchMiddle.getWorld(),plugin.eventListener.cubes.get(hatchMiddle)).remove();
            plugin.eventListener.cubes.remove(hatchMiddle);
        } else if (plugin.eventListener.FlyingBlocks.containsKey(hatchMiddle)) {
            if (plugin.eventListener.buttons.containsValue(plugin.eventListener.FlyingBlocks.get(hatchMiddle))) {
                Iterator<Entry<Block, FrozenSand>> it = plugin.eventListener.buttons.entrySet().iterator();
                Entry<Block, FrozenSand> e;
                Block middle;
                while (it.hasNext()) {
                    e = it.next();
                    if (e.getValue() == plugin.eventListener.FlyingBlocks.get(hatchMiddle)) {
                        middle = e.getKey();
                        changeBtn(middle, !plugin.eventListener.buttons.containsKey(middle));
                        it.remove();
                    }
                }
            }
            plugin.eventListener.FlyingBlocks.get(hatchMiddle).clearAllPlayerViews();
            plugin.eventListener.FlyingBlocks.remove(hatchMiddle);
            

        } else if (plugin.eventListener.cubesPlayer.containsKey(hatchMiddle)) {

            // remove cubesPlayerItem.get(hatchMiddle)
            // from cubesPlayer.get(hatchMiddle)
            remove(plugin.eventListener.cubesPlayer.get(hatchMiddle)
                    .getInventory(),
                    plugin.eventListener.cubesPlayerItem.get(hatchMiddle)
                    .getType(), 1, plugin.eventListener.cubesPlayerItem
                    .get(hatchMiddle).getData()
                    .getData());
            doInventoryUpdate(
                    plugin.eventListener.cubesPlayer.get(hatchMiddle), plugin);

            plugin.eventListener.cubesPlayer.remove(hatchMiddle);

            plugin.eventListener.cubesPlayerItem.remove(hatchMiddle);

        }
        BlockFace[] blockfaces = new BlockFace[] {
                BlockFace.NORTH_EAST, BlockFace.EAST,
                BlockFace.SOUTH_EAST, BlockFace.SOUTH,
                BlockFace.SOUTH_WEST, BlockFace.WEST,
                BlockFace.NORTH_WEST, BlockFace.NORTH,
                BlockFace.SELF};
        Block blk = hatchMiddle.getRelative(BlockFace.DOWN,3);
        for (Entry<BukkitTask, Block> t : plugin.eventListener.hatches.entrySet()) {
            if (t.getValue() == blk) {
                t.getKey().cancel();
                plugin.eventListener.hatches.remove(t.getKey());
                }
            }
        /*if (respawn) {

    for (int i = 0; i < blockfaces.length; i++) {

    plugin.eventListener.hatches.put(
    new AnimateHatch(blk
    .getRelative(blockfaces[i]),
    false, plugin.eventListener).runTaskLater(
    plugin, 3L * i), blk);

    }

    } else {

    for (int i = 0; i < blockfaces.length; i++) {

    plugin.eventListener.hatches.put(
    new AnimateHatch(blk
    .getRelative(blockfaces[i]),
    true, plugin.eventListener).runTaskLater(
    plugin, 3L * i), blk);
    }

    }*/
        if (powered) {
            FallingBlock f = hatchMiddle.getWorld()
                    .spawnFallingBlock(
                            hatchMiddle.getLocation(), id,
                            (byte) data);
            f.setDropItem(false);
            plugin.eventListener.cubes.put(hatchMiddle, f.getUniqueId());
            plugin.eventListener.cubesign.put(hatchMiddle, sign);
        }
    }
    
    public Block chkBtn (Location l) {
        Block blockUnder = l.getBlock().getRelative(BlockFace.DOWN);
        if (!(blockUnder.getType()==Material.WOOL)) return null;
        Block middle = blockUnder;
        boolean error = false;
        // red wool - find closest black and check around
        if (blockUnder.getData() == (byte) 14) {
            for (BlockFace f : blockfaces) {
                if (blockUnder.getRelative(f).getType() == Material.WOOL
                        && blockUnder.getRelative(f).getData() == (byte) 15) {
                    middle = blockUnder.getRelative(f);

                }
            }
        }
        // middle - already have relative
        else if (blockUnder.getData() == (byte) 15) {

            if (blockUnder.getRelative(BlockFace.NORTH).getType() == Material.WOOL
                    && blockUnder.getRelative(BlockFace.NORTH).getData() == (byte) 14||blockUnder.getRelative(BlockFace.NORTH).getData() == (byte) 5) {
                middle = blockUnder;
            }

        } else
            // lime
            if (blockUnder.getData() == (byte) 5) {
                for (BlockFace f : blockfaces) {
                    if (blockUnder.getRelative(f).getType() == Material.WOOL
                            && blockUnder.getRelative(f).getData() == (byte) 15) {
                        middle = blockUnder.getRelative(f);

                    }
                }
            } else {
                error = true;
            }

        if(!error)
            for (BlockFace f : blockfaces) {
                Block rel = middle.getRelative(f);
                if (rel.getType() == Material.WOOL
                        && (rel.getData() == (byte) 14 || rel.getData() == (byte) 5)) {

                } else {
                    error = true;
                    break;
                }
            }
        return error ? null : middle;
    }
    

    
    public Block chkBtnInner (Location l) {
        Block blockUnder = l.getBlock().getRelative(BlockFace.DOWN);
        if (!(blockUnder.getType()==Material.WOOL)) return null;
        Block middle = l.getBlock();
        boolean error = false;
        if (blockUnder.getData() == (byte) 15) {

            if (l.getBlock().getRelative(BlockFace.NORTH).getType() == Material.WOOL
                    && l.getBlock().getRelative(BlockFace.NORTH).getData() == (byte) 14 ||l.getBlock().getRelative(BlockFace.NORTH).getData() == (byte) 5) {
                middle = l.getBlock();
            } else {error = true;}

        } else {error = true;}

        if(!error) {
            Block rel;
            for (BlockFace f : blockfaces) {
                rel = middle.getRelative(f);
                if (rel.getType() == Material.WOOL
                        && (rel.getData() == (byte) 14 || rel.getData() == (byte) 5)) {

                } else {
                    error = true;
                    break;
                }
            }   
        }
        return error ? null : middle;
    }
}
