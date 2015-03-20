package org.PortalStick.util;

import java.util.HashMap;

import org.PortalStick.PortalStick;
import org.PortalStick.util.Config.Sound;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Lever;
import org.bukkit.plugin.Plugin;

import com.sanjay900.nmsUtil.EntityCubeImpl;
import com.sanjay900.nmsUtil.NMSUtil;
import com.sanjay900.nmsUtil.util.Utils;
import com.sanjay900.nmsUtil.util.V10Location;

public class Util {
	private final PortalStick plugin;
	public final NMSUtil nmsUtil;
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
		this.nmsUtil = (NMSUtil) Bukkit.getPluginManager().getPlugin("nmsUtils");
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

	@SuppressWarnings("deprecation")
	public boolean isPortalGun(ItemStack item) {
		return item != null && item.getTypeId() == plugin.config.PortalTool &&
				item.getDurability() == plugin.config.portalToolData &&
				item.getItemMeta().getDisplayName() != null &&
				item.getItemMeta().getDisplayName().equals(plugin.config.portalToolName);
	}

	public ItemStack createPortalGun() {
		@SuppressWarnings("deprecation")
		ItemStack gun = new ItemStack(plugin.config.PortalTool, 1, plugin.config.portalToolData);
		Utils.setItemNameAndDesc(gun, plugin.config.portalToolName, plugin.config.portalToolDesc);
		return gun;
	}

	public void changeBtn(V10Location middle, boolean on) {
		changeBtn(middle.getHandle().getBlock(), on);
	}

	@SuppressWarnings("deprecation")
	public void changeBtn(Block middle, boolean on) {
		Block under = middle.getRelative(BlockFace.DOWN);
		byte data;
		if(on) {
			data = 5;
		} else {
			data = 14;
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

	@SuppressWarnings("deprecation")
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
	
	public void clear(Block hatchMiddle, boolean powered, int id, int data, Block sign, EntityCubeImpl en) {
		clear(hatchMiddle, powered, id, data, sign, false, en);
	}
	@SuppressWarnings("deprecation")
	public void clear(Block hatchMiddle, boolean powered, int id, int data, Block sign, boolean first, EntityCubeImpl en) {
		V10Location loc = new V10Location(hatchMiddle);
		if (en == null) {
			for (Entity ent: hatchMiddle.getWorld().getEntities()) {
				EntityCubeImpl ec = nmsUtil.getCube(ent);
				if (ec != null) {
					if (ec.<V10Location>getStored("respawnLoc").getHandle().getBlockX() == hatchMiddle.getX()&&ec.<V10Location>getStored("respawnLoc").getHandle().getBlockY() == hatchMiddle.getY()&&ec.<V10Location>getStored("respawnLoc").getHandle().getBlockZ() == hatchMiddle.getZ()) {				
						en = ec;
					}
				}
			}
		}
		if (en != null) {
			if(plugin.cubeManager.buttonsToEntity.containsKey(en.getUniqueID())) {
				V10Location vloc = plugin.cubeManager.buttonsToEntity.get(en.getUniqueID());
				plugin.cubeManager.buttonsToEntity.remove(en.getUniqueID());
				if(!plugin.cubeManager.buttonsToEntity.containsValue(vloc)) {
					plugin.util.changeBtn(vloc, false);
				}
			}
		en.getBukkitEntity().remove();
		}

		if (plugin.cubeManager.cubesPlayer.containsKey(loc)) {
			Player p = Bukkit.getPlayer(plugin.cubeManager.cubesPlayer.get(loc));
			Utils.remove(p.getInventory(),
					plugin.cubeManager.cubesPlayerItem.get(loc)
					.getType(), 1, plugin.cubeManager.cubesPlayerItem
					.get(loc).getData()
					.getData());
			Utils.doInventoryUpdate(p, plugin);

			plugin.cubeManager.cubesPlayer.remove(loc);

			plugin.cubeManager.cubesPlayerItem.remove(loc);

		}
		if (powered) {
			Sign s = (Sign) sign.getState();
			if ((!s.getLine(2).equals("norespawn")||first)&&plugin.util.nmsUtil.checkVersion()) {
				HashMap<String,Object> storedData = new HashMap<>();
				storedData.put("respawnLoc", loc);

				nmsUtil.createCube(hatchMiddle.getLocation().add(0.5,0,0.5), id, data, storedData);
				plugin.cubeManager.cubesign.put(loc, new V10Location(sign));
			}
		}
	}

	@SuppressWarnings("deprecation")
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



	@SuppressWarnings("deprecation")
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
