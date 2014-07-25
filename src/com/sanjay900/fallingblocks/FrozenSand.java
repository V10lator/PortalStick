package com.sanjay900.fallingblocks;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.sanjay900.PortalStick.MoveEvent;

import de.V10lator.PortalStick.PortalStick;


public class FrozenSand {

    private final PortalStick plugin;
	private Player attachPlayer;
	private Player ridePlayer;
	public String id = "";
	public int entityId = 0;
	double x = 0;
	double y = 0;
	double z = 0;
	public int storageId = 0;
	String worldName = "";
	ProtocolManager pm;
	private Vector motion;
	private BukkitTask velocitytask = null;
	protected FrozenSand(PortalStick plugin, Integer id2, String worldName, double x, double y, double z, Player attachPlayer, Player ridePlayer, String id) {
	    this.plugin = plugin;
		entityId = plugin.tagIdGenerator.nextId(1);
		this.x = x;
		this.y = y;
		this.z = z;
		this.id = id;
		this.worldName = worldName;
		this.attachPlayer = attachPlayer;
		this.ridePlayer = ridePlayer;
		this.pm = ProtocolLibrary.getProtocolManager();
		this.storageId = id2;
	}
	protected void generate(Player observer, String message, double diffY, double x, double y, double z) {

		this.id = message;
		try {
			if (ridePlayer == null) {
			x = Math.floor(x)+0.5;
			y = Math.floor(y);
			z = Math.floor(z)+0.5;
			} else {
				x = ridePlayer.getLocation().getX();
				y = ridePlayer.getLocation().getY()+5;
				z = ridePlayer.getLocation().getZ();
			}
			
			PacketContainer attach = pm.createPacket(PacketType.Play.Server.ATTACH_ENTITY);
			PacketContainer attach2 = pm.createPacket(PacketType.Play.Server.ATTACH_ENTITY);
			PacketContainer horse = pm.createPacket(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
			StructureModifier<Integer> modifier = horse.getIntegers();
			modifier.write(0,this.getHorseIndex());
			modifier.write(1,(int)EntityType.HORSE.getTypeId());
			modifier.write(2, (int) Math.floor((y +(ridePlayer == null? diffY + 41:0)) * 32.0D));
			modifier.write(3, (int)Math.floor((y + diffY+ 41)* 32.0D));
			modifier.write(4, (int)Math.floor(z* 32.0D));

			WrappedDataWatcher dw = new WrappedDataWatcher();
			dw.setObject(0, Byte.valueOf((byte) 0x20));

			dw.setObject(10, "");
			dw.setObject(11, Byte.valueOf((byte) 0));
			if (ridePlayer == null) {
			dw.setObject(12, Integer.valueOf(-1696975));
			} else {
				if (ridePlayer.getName().equals(observer.getName())) {
					dw.setObject(12, Integer.valueOf(-166575));	
				} else {
					dw.setObject(12, Integer.valueOf(-101575));	
				}
				
			}
			horse.getDataWatcherModifier().write(0,dw);
			
			PacketContainer skull = pm.createPacket(PacketType.Play.Server.SPAWN_ENTITY);
			modifier = skull.getIntegers();
			modifier.write(0, this.getSkullIndex());
			modifier.write(1, (int) Math.floor(x * 32.0D));
			modifier.write(2, (int) Math.floor((y +(ridePlayer == null? diffY + 41:0)) * 32.0D));
			modifier.write(3, (int) Math.floor(z * 32.0D));
			modifier.write(9, 66);
			

			PacketContainer item = pm.createPacket(PacketType.Play.Server.SPAWN_ENTITY);

			if (!message.equals("none")) {
			    modifier = item.getIntegers();
			    modifier.write(0, this.getTouchSlimeIndex());
			    modifier.write(1, (int) Math.floor(x * 32.0D));
			    modifier.write(2, (int) Math.floor((y +(ridePlayer == null? diffY + 41:0)) * 32.0D));
			    modifier.write(3, (int) Math.floor(z * 32.0D));
			    modifier.write(9, 70);

				String[] datas = message.split(":");
				int blockID = Integer.parseInt(datas[0]);
				byte data = datas.length > 1 ? Byte.valueOf(datas[1]) : 0;
				modifier.write(10, blockID | (data << 0x10));
				modifier = attach2.getIntegers();
				modifier.write(0, 0);
				modifier.write(1, item.getIntegers().read(0));
				attach2.getIntegers().write(2, horse.getIntegers().read(0));
			}
			
			modifier = attach.getIntegers();
			modifier.write(0, 0);
			modifier.write(1, horse.getIntegers().read(0));
			modifier.write(2, ridePlayer == null ? skull.getIntegers().read(0) : ridePlayer.getEntityId());
			
			pm.sendServerPacket(observer, horse);
			if (ridePlayer == null) {
				pm.sendServerPacket(observer, skull);
			}
			pm.sendServerPacket(observer, attach);
			if (!message.equals("none"))  {
				pm.sendServerPacket(observer, item);
			}
			if (!message.equals("none")) {
				pm.sendServerPacket(observer, attach2);
			}
			if (attachPlayer != null) {
				PacketContainer attach3 = pm.createPacket(PacketType.Play.Server.ATTACH_ENTITY);
				modifier = attach3.getIntegers();
				modifier.write(0, 0);
				modifier.write(1, attachPlayer.getEntityId());

				modifier.write(2, message.equals("none") ? horse.getIntegers().read(0) : item.getIntegers().read(0));
				
				pm.sendServerPacket(observer, attach3);

			}
			pm.sendServerPacket(observer, attach);
		} 
		catch (Exception ex) {
		    if(plugin.config.debug) {
		        plugin.getLogger().warning("Hologram of ID "+String.valueOf(id)+" is invalid"+ex.getMessage());
		        ex.printStackTrace();
		    }
		}
	}
	protected void clearTags(Player observer, int... entityIds) {
        if (entityIds.length > 0) {
        	PacketContainer packet = pm.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
            packet.getIntegerArrays().write(0, entityIds);
            try {
            	pm.sendServerPacket(observer, packet);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(
                    "Cannot send packet " + packet, e);
            }

        }
    }
    public void clearAllPlayerViews() {
    	 if (velocitytask!=null)velocitytask.cancel();
    	 int[] entityIDs = this.getAllEntityIds();
       for (Player p : Bukkit.getOnlinePlayers()) {
    	   if (p != null) {
               this.clearTags(p, entityIDs);
           } 
       }
       if ( plugin.flyingBlocksAPI.fakeBlocks.contains(this)) {
       plugin.flyingBlocksAPI.fakeBlocks.remove(this);
       }
      
        
    }
    public int[] getAllEntityIds() {
        int[] entityIdList = new int[2];
            for (int i = 0; i < 2; i++) {
                entityIdList[i] = this.getHorseIndex() + i;
            }
            
        return entityIdList;
    }
		protected void updateNametag(Player observer, String message) {
			y = Math.floor(y);
			this.id = message;
			clearTags(observer,this.getTouchSlimeIndex());
			PacketContainer attach2 = pm.createPacket(PacketType.Play.Server.ATTACH_ENTITY);
			PacketContainer item = pm.createPacket(PacketType.Play.Server.SPAWN_ENTITY);

			if (!message.equals("none")) {
			    StructureModifier<Integer> modifier = item.getIntegers();
			    modifier.write(0, this.getTouchSlimeIndex());
			    modifier.write(1, (int) Math.floor(x * 32.0D));
			    modifier.write(2, (int) (Math.floor(y + 41) * 32.0D));
			    modifier.write(3, (int) Math.floor(z * 32.0D));
			    modifier.write(9, 70);

				String[] datas = message.split(":");
				modifier.write(10, Integer.parseInt(datas[0])| (Byte.valueOf(datas[1]) << 0x10));
				modifier = attach2.getIntegers();
				modifier.write(0, 0);
				modifier.write(1, item.getIntegers().read(0));
				modifier.write(2, this.getHorseIndex());
				try {
					pm.sendServerPacket(observer, item);
					pm.sendServerPacket(observer, attach2);
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
		public void updateRider(Player observer) {
			if (!(ridePlayer == null)) {
			
				PacketContainer attach = pm.createPacket(PacketType.Play.Server.ATTACH_ENTITY);
				StructureModifier<Integer> modifier = attach.getIntegers();
				modifier.write(0, 0);
				modifier.write(1, this.getHorseIndex());
				modifier.write(2, ridePlayer.getEntityId());
				try {
					pm.sendServerPacket(observer, attach);
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}

			
		}
		protected void moveTag(Player observer) {

	        PacketContainer teleportSkull = pm.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
	        StructureModifier<Integer> modifier = teleportSkull.getIntegers();
	        modifier.write(0, getSkullIndex());
	        modifier.write(1, (int) Math.floor( x* 32.0D));
	        modifier.write(2, (int) Math.floor((y+41)* 32.0D));
	        modifier.write(3,(int) Math.floor( z * 32.0D));
	        
	       
	        PacketContainer metadata = pm.createPacket(PacketType.Play.Server.ENTITY_METADATA);
	        WrappedDataWatcher dw = new WrappedDataWatcher();
			dw.setObject(12, Integer.valueOf(-1716550));	

	        metadata.getIntegers().write(0, getHorseIndex());
	        metadata.getWatchableCollectionModifier().write(0, dw.getWatchableObjects());
	        try {
				pm.sendServerPacket(observer, teleportSkull);
				pm.sendServerPacket(observer, metadata);
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
		}

		 public int getHorseIndex() {
			 return entityId;
		 }
		 public int getSkullIndex() {
			 return entityId+1;
		 }
		 public int getTouchSlimeIndex() {
			 return entityId+2;
		 }
		public void setVelocity(final Vector velocity) {
			final FrozenSand sand = this;
			motion = velocity;
			velocitytask = Bukkit.getScheduler().runTaskTimer(Bukkit.getPluginManager().getPlugin("PortalStick"), new Runnable(){
				
				@Override
				public void run() {
					MoveEvent event = new MoveEvent(sand,getLocation().clone().add(motion),getLocation().clone(), motion);
					Bukkit.getServer().getPluginManager().callEvent(event);
					if (!event.isCancelled()) {
						move(getLocation().add(motion));
						motion = event.getVelocity();
					} else {
						velocitytask.cancel();
					}
						
					
				}},1l,1l);
			
				
			
		}
		
	    private void move(Location add) {
			this.x = add.getX();
			this.y = add.getY();
			this.z = add.getZ();
			for (Player p : Bukkit.getOnlinePlayers()) {
				this.moveTag(p);
			}
		}
		public void show(Player observer) {
	        this.show(observer, this.x, this.y, this.z);
	    }
 
	    public void show(Player observer, Location location) {
	        this.show(observer, location.getBlockX(), location.getBlockY(), location.getBlockZ());
	    }

	   

	    public void show(Player observer, double x, double y, double z) {
	            this.generate(observer, id, 0, x, y, z);
	    }
		public Location getLocation() {
			return new Location(Bukkit.getWorld(worldName),x,y,z);
		}
		public int getId() {
			return storageId;
		}
		public Vector getVelocity() {
			return motion;
		}
		public Material getMaterial() {
			return Material.getMaterial(Integer.parseInt(id.split(":")[0]));
		}
		public byte getData() {
		    String[] split = id.split(":");
            if (split.length < 2)
                return 0;
            return Byte.parseByte(split[1]);
		}

	}