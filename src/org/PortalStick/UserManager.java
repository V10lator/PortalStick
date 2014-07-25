package org.PortalStick;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

public class UserManager implements Runnable {
	private final PortalStick plugin;
	
	UserManager(PortalStick plugin)
	{
		this.plugin = plugin;
	}
	
	public void createUser(Entity entity) {
	    MetadataValue value = new FixedMetadataValue(plugin, new User(entity));
	    entity.setMetadata("PortalStickUser", value);
	}
	
	public User getUser(Entity entity) {
	    // TODO: BKCommonLib/Bukkit/MC/Whatever workaround
	    if(!entity.hasMetadata("PortalStickUser"))
	        createUser(entity);
	    
	    return (User) entity.getMetadata("PortalStickUser").get(0).value();
	}
	
	public void deleteUser(Entity entity) {
	    // TODO: BKCommonLib/Bukkit/MC/Whatever workaround
        if(!entity.hasMetadata("PortalStickUser"))
            return;
        
	    User user = getUser(entity);
	    plugin.portalManager.deletePortals(user);
	    deleteDroppedItems(user);
	    entity.removeMetadata("PortalStickUser", plugin);
	}

	public void deleteDroppedItems(User user)
	{
	  Location loc;
	  for(Item item: user.droppedItems)
		if(item.isValid() && !item.isDead())
		{
		  loc = item.getLocation();
		  item.remove();
		  loc.getWorld().playEffect(loc, Effect.SMOKE, 4);
		}
	  user.droppedItems.clear();
	}
	
	public void run()
	{
	  Iterator<Item> iter;
	  Item item;
	  for(World world: Bukkit.getWorlds()) {
	      for(Entity entity: world.getEntities()) {
	          if(!entity.hasMetadata("PortalStickUser")) {
	              continue;
	          }
	          iter = getUser(entity).droppedItems.iterator();
	          while(iter.hasNext()) {
	              item = iter.next();
	              if(!item.isValid() || item.isDead()) {
	                  iter.remove();
	              }
	          }
	      }
	  }
	}
}
