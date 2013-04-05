package com.matejdro.bukkit.portalstick;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;
import org.libigot.LibigotLocation;

import com.matejdro.bukkit.portalstick.util.Config.Sound;
import com.matejdro.bukkit.portalstick.util.RegionSetting;

public class GrillManager {
	
	public final List<Grill> grills = new ArrayList<Grill>();
	public final HashMap<LibigotLocation, Grill> insideBlocks = new HashMap<LibigotLocation, Grill>();
	public final HashMap<LibigotLocation, Grill> borderBlocks = new HashMap<LibigotLocation, Grill>();
	private final PortalStick plugin; 
	
	private HashSet<LibigotLocation> border;
	private HashSet<LibigotLocation> inside;
	private boolean complete;
	private int max = 0;
	
	public GrillManager(PortalStick instance) {
		plugin = instance;
	}

	public void loadGrill(String blockloc) {
		String[] locarr = blockloc.split(",");
		String world = locarr[0];
		if (!placeRecursiveEmancipationGrill(new LibigotLocation(plugin.getServer().getWorld(world).getBlockAt((int)Double.parseDouble(locarr[1]), (int)Double.parseDouble(locarr[2]), (int)Double.parseDouble(locarr[3])))))
			plugin.config.deleteGrill(blockloc);
	}
	
	public void deleteAll() {
		for (Grill g : grills)
			g.deleteInside();
		grills.clear();
		insideBlocks.clear();
		borderBlocks.clear();
	}
    
    public boolean createGrill(Player player, LibigotLocation block) {
    	boolean ret;
    	if(!plugin.hasPermission(player, plugin.PERM_CREATE_GRILL))
    	  ret = false;
    	else if(placeRecursiveEmancipationGrill(block))
    	{
    	  plugin.config.saveAll();
    	  ret = true;
    	}
    	else
    	  ret = false;
    	return ret;
    }
    
    public boolean placeRecursiveEmancipationGrill(LibigotLocation initial) {
    	Region region = plugin.regionManager.getRegion(initial);
    	String borderID = region.getString(RegionSetting.GRILL_MATERIAL);
    	
    	if (!plugin.blockUtil.compareBlockToString(initial, borderID) || !region.getBoolean(RegionSetting.ENABLE_GRILLS))
    		return false;
    	
    	//Check if initial is already in a grill
    	for (Grill grill : grills)
    		if (grill.border.contains(initial))
    			return false;
    	
    	//Attempt to get complete border
    	border = new HashSet<LibigotLocation>();
    	inside = new HashSet<LibigotLocation>();
    	startRecurse(initial, borderID, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.DOWN, BlockFace.UP);
    	if (!complete)
    		startRecurse(initial, borderID, BlockFace.UP, BlockFace.WEST, BlockFace.EAST, BlockFace.DOWN, BlockFace.SOUTH, BlockFace.NORTH);
    	if (!complete)
    		startRecurse(initial, borderID, BlockFace.UP, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.DOWN, BlockFace.EAST, BlockFace.WEST);
    	if (!complete)
    		return false;
    	//Create grill
    	Grill grill = new Grill(plugin, border, inside, initial);
    	border = inside = null;
    	grills.add(grill);
    	grill.create();
    	return true;
    }
    
    
    private void startRecurse(LibigotLocation initial, String id, BlockFace one, BlockFace two, BlockFace three, BlockFace four, BlockFace iOne, BlockFace iTwo) {
    	border.clear();
    	inside.clear();
    	max = 0;
    	complete = false;
    	recurse(initial, id, initial, one, two, three, four);
    	generateInsideBlocks(id, initial, iOne, iTwo);

    	if (inside.size() == 0)
    		complete = false;
    }
    
    private void generateInsideBlocks(String borderID, LibigotLocation initial, BlockFace iOne, BlockFace iTwo) {
    	
    	//Work out maximums and minimums
    	Vector max = border.toArray(new LibigotLocation[0])[0].getHandle().toVector();
    	Vector min = border.toArray(new LibigotLocation[0])[0].getHandle().toVector();
    	
    	for (LibigotLocation block : border.toArray(new LibigotLocation[0])) {
    		if (block.getX() >= max.getX()) max.setX(block.getX());
    		if (block.getY() >= max.getY()) max.setY(block.getY());
    		if (block.getZ() >= max.getZ()) max.setZ(block.getZ());
    		if (block.getX() <= min.getX()) min.setX(block.getX());
    		if (block.getY() <= min.getY()) min.setY(block.getY());
    		if (block.getZ() <= min.getZ()) min.setZ(block.getZ());
    	}
    	
    	//Loop through all blocks in the min-max range checking for 'inside' blocks
    	BlockFace[] faces = new BlockFace[]{BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
    	World world = initial.getHandle().getWorld();
    	Block rb;
    	for (int y = (int)min.getY(); y <= (int)max.getY(); y++) {
    		for (int x = (int)min.getX(); x <= (int)max.getX(); x++) {
    			for (int z = (int)min.getZ(); z <= (int)max.getZ(); z++) {
    				rb = world.getBlockAt(x, y, z);
    				initial = new LibigotLocation(rb);
    				if (border.contains(initial) || inside.contains(initial))
    	    			continue;
    	    		boolean add = true;
    	    		for (BlockFace face : faces) {
    	    			if (face == iOne || face == iTwo)
    	    				continue;
    	    			Block temp = rb.getRelative(face);
    	    			while (temp.getLocation().toVector().isInAABB(min, max)) {
    	    				
    	    				if (plugin.blockUtil.compareBlockToString(temp, borderID))
    	    					break;
    	    				temp = temp.getRelative(face);
    	    			}
    	    			if (!plugin.blockUtil.compareBlockToString(temp, borderID)) {
    	    				add = false;
    	    				break;
    	    			}
    	    		}
    	    		
    	    		if (add)
    	    			inside.add(initial);
    			}
    		}
    	}
    }
    
    private void recurse(LibigotLocation initial, String id, LibigotLocation vb, BlockFace one, BlockFace two, BlockFace three, BlockFace four) {
    	if (max >= 100) return;
    	if (vb.equals(initial) && border.size() > 2) {
    		complete = true;
    		return;
    	}
    	if (plugin.blockUtil.compareBlockToString(vb, id) && !border.contains(vb)) {
    		border.add(vb);
    		max++;
    		Block b = vb.getHandle().getBlock();
    		recurse(initial, id, new LibigotLocation(b.getRelative(one)), one, two, three, four);
    		recurse(initial, id, new LibigotLocation(b.getRelative(two)), one, two, three, four);
    		recurse(initial, id, new LibigotLocation(b.getRelative(three)), one, two, three, four);
    		recurse(initial, id, new LibigotLocation(b.getRelative(four)), one, two, three, four);
    	}
    }

	public void emancipate(Region region, Entity entity)
	{
	  User user = plugin.userManager.getUser(entity);
	  boolean clear = region.getBoolean(RegionSetting.GRILLS_REMOVE_ITEMS) && !user.usingTool;
	  if(region.getBoolean(RegionSetting.GRILLS_CLEAR_ITEM_DROPS))
		plugin.userManager.deleteDroppedItems(user);

	  if(clear && !(entity instanceof InventoryHolder))
	  {
		if(entity instanceof Item)
		{
		  ItemStack item = ((Item)entity).getItemStack();
		  for(Object is: region.getList(RegionSetting.GRILL_REMOVE_EXCEPTIONS))
		  {
			if(item.getTypeId() == (Integer)is)
			  return;
		  }
		  entity.remove();
		  playGrillAnimation(entity.getLocation());
		}
		else if(entity instanceof FallingBlock)
		{
		  FallingBlock fb = (FallingBlock)entity;
		  int id = fb.getBlockId();
		  for(Object is: region.getList(RegionSetting.GRILL_REMOVE_EXCEPTIONS))
		  {
			if(id == (Integer)is)
			  return;
		  }
		  entity.remove();
		  playGrillAnimation(entity.getLocation());
		}
		return;
	  }
	  plugin.portalManager.deletePortals(user);
	  InventoryHolder ih = (InventoryHolder)entity;
	  Inventory inv = ih.getInventory();
	  ItemStack[] inv2 = null;
	  boolean roe = region.getBoolean(RegionSetting.GRILL_ONE_EXCEPTION);
	  boolean changed = false;
	  if(clear)
	  {
		playGrillAnimation(entity.getLocation());
		List<?> ice = region.getList(RegionSetting.GRILL_REMOVE_EXCEPTIONS);
		HashSet<Integer> rm;
		if(roe)
		  rm = new HashSet<Integer>();
		else
		  rm = null;
		ItemStack newSlot;
		if(inv instanceof PlayerInventory)
		{
		  PlayerInventory pi = (PlayerInventory)inv;
		  inv2 = pi.getArmorContents();
		  
		  for(int i = 0; i < inv2.length; i++)
		  {
			if(inv2[i] == null)
			  continue;
			newSlot = checkItemSlot(inv2[i], ice, roe, rm);
			if(newSlot != inv2[i])
			{
			  inv2[i] = newSlot;
			  changed = true;
			}
		  }
		  if(changed)
		  {
			pi.setArmorContents(inv2);
			changed = false;
		  }
		}
		inv2 = inv.getContents();
		for(int i = 0; i < inv2.length; i++)
		{
		  if(inv2[i] == null)
			continue;
		  newSlot = checkItemSlot(inv2[i], ice, roe, rm);
		  if(newSlot != inv2[i])
		  {
			inv2[i] = newSlot;
			changed = true;
		  }
		}
	  }
	  if(inv2 == null)
		inv2 = inv.getContents();
	  if(region.getBoolean(RegionSetting.GRILL_GIVE_GUN_IF_NEEDED))
	  {
		boolean hasGun = false;
		for(int i = 0; i < inv2.length; i++)
		{
		  if(inv2[i] != null && inv2[i].getTypeId() == plugin.config.PortalTool && inv2[i].getDurability() == plugin.config.portalToolData)
		  {
			hasGun = true;
			break;
		  }
		}
		if(!hasGun)
		{
		  for(int i = 0; i < inv2.length; i++)
		  {
			if(inv2[i] == null)
			{
			  inv2[i] = new ItemStack(plugin.config.PortalTool, 1, plugin.config.portalToolData);
			  changed = hasGun = true;
			  break;
			}
		  }
		  if(!hasGun)
			entity.getLocation().getWorld().dropItemNaturally(entity.getLocation(), new ItemStack(plugin.config.PortalTool, 1, plugin.config.portalToolData));
		}
	  }
	  if(region.getBoolean(RegionSetting.GRILL_GIVE_BOOTS_IF_NEEDED))
	  {
		boolean hasBoots = false;
		int boots = region.getInt(RegionSetting.FALL_DAMAGE_BOOTS);
		if(inv instanceof PlayerInventory)
		{
		  PlayerInventory pi = (PlayerInventory)inv;
		  ItemStack[] armor = pi.getArmorContents();
		  for(int i = 0; i < armor.length; i++)
		  {
			if(armor[i] != null && armor[i].getTypeId() == boots)
			{
			  hasBoots = true;
			  break;
			}
		  }
		}
		if(!hasBoots)
		{
		  for(int i = 0; i < inv2.length; i++)
		  {
			if(inv2[i] != null && inv2[i].getTypeId() == boots)
			{
			  hasBoots = true;
			  break;
			}
		  }
		  if(!hasBoots)
		  {
			if(inv instanceof PlayerInventory)
			{
			  PlayerInventory pi = (PlayerInventory)inv;
			  if(pi.getBoots() == null)
			  {
				pi.setBoots(new ItemStack(boots));
				hasBoots = true;
			  }
			}
			if(!hasBoots)
			{
			  for(int i = 0; i < inv2.length; i++)
			  {
				if(inv2[i] == null)
				{
				  inv2[i] = new ItemStack(boots);
				  changed = hasBoots = true;
				  break;
				}
			  }
			}
		  }
		  if(!hasBoots)
			entity.getLocation().getWorld().dropItemNaturally(entity.getLocation(), new ItemStack(plugin.config.PortalTool, 1, plugin.config.portalToolData));
		}
	  }
	  
	  if(region.getBoolean(RegionSetting.GRILL_REMOVE_EXTRA_GUNS))
	  {
		boolean hasGun = false;
		for(int i = 0; i < inv2.length; i++)
		{
		  if(inv2[i] != null && inv2[i].getTypeId() == plugin.config.PortalTool && inv2[i].getDurability() == plugin.config.portalToolData)
		  {
			if(hasGun)
			{
			  inv2[i] = null;
			  changed = true;
			}
			else
			{
			  if(inv2[i].getAmount() != 1)
				inv2[i].setAmount(1);
			  hasGun = true;
			}
		  }
		}
	  }
	  if(changed)
		inv.setContents(inv2);
	}
	
	private ItemStack checkItemSlot(ItemStack slot, List<?> ice, boolean roe, HashSet<Integer> rm)
	{
	  boolean remove = true;
	  int slotId = slot.getTypeId();
	  int id;
	  for (Object is: ice)
	  {
		id = (Integer)is;
		if(slotId == id)
		{
		  remove = false;
		  break;
		}
	  }
	  if(remove)
		slot = null;
	  else if(roe)
	  {
		if(rm.contains(slotId))
		  slot = null;
		else
		{
		  if(slot.getAmount() > 1)
		    slot.setAmount(1);
		  rm.add(slotId);
		}
	  }
	  return slot;
	}
	
	public void playGrillAnimation(Location loc)
	{
	  World world = loc.getWorld();
	  for(int i = 0; i < 9; i++)
		world.playEffect(loc, Effect.SMOKE, i, 16);
	  plugin.util.playSound(Sound.GRILL_EMANCIPATE, new LibigotLocation(loc));
	}
}
