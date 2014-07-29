package org.PortalStick.fallingblocks;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.PortalStick.PortalStick;
import org.PortalStick.Region;
import org.PortalStick.util.RegionSetting;
import org.PortalStick.util.V10Location;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import com.bergerkiller.bukkit.common.utils.FaceUtil;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;


public class FrozenSand implements Entity {

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
	public BukkitTask velocitytask = null;
	private final UUID uuid = UUID.randomUUID();
	
	protected FrozenSand(final PortalStick plugin, Integer id2, String worldName, double x, double y, double z, Player attachPlayer, Player ridePlayer, String id) {
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
		final FrozenSand sand = this;
		velocitytask = Bukkit.getScheduler().runTaskTimer(Bukkit.getPluginManager().getPlugin("PortalStick"), new Runnable(){

			@Override
			public void run() {
				if (!plugin.util.isSolid(getLocation().getBlock().getRelative(BlockFace.DOWN).getType())) {
					if (plugin.cubeManager.flyingBlocks.containsValue(sand)) {
					
					if (!plugin.funnelBridgeManager.cubeinFunnel.containsKey(sand)) {
						FallingBlock f = getLocation()
								.getWorld()
								.spawnFallingBlock(
										getLocation(),
										getMaterial(),
										getData());
						final UUID uuid = f.getUniqueId();
						for (Entry<V10Location, FrozenSand> e: plugin.cubeManager.flyingBlocks.entrySet()) {
							if (e.getValue() == sand) {
								plugin.cubeManager.cubes.put(e.getKey(), uuid);
								break;
							}
						}
						
						
						f.setDropItem(false);
						remove();
					}
					
					}
				}
				if (motion != null) {
					if (!plugin.funnelBridgeManager.cubeinFunnel.containsKey(sand)) {
					    
					    
					    Location to = getLocation().clone().add(motion);
					    Location from = getLocation().clone();
				        if (plugin.cubeManager.flyingBlocks.containsValue(sand)) {
				            Block under = to.getBlock().getRelative(BlockFace.DOWN);
				            BlockFace face = FaceUtil.getDirection(motion);
				            Vector half = FaceUtil.faceToVector(face).multiply(0.5);
				            Block bTo = to.clone().add(half).getBlock();
				            Block bFrom = from.getBlock();
				            Iterator<Entry<V10Location, FrozenSand>> fb = plugin.cubeManager.flyingBlocks.entrySet().iterator();
				            V10Location respawnLoc = null;
				            Entry<V10Location, FrozenSand> e;
				            while (fb.hasNext()) {
				                e = fb.next();
				                if (e.getValue() == sand) {
				                    respawnLoc = e.getKey();
				                break;
				                }
				            }

				            V10Location vloc = new V10Location(to);
				            byte useGel = plugin.gelManager.useGelCube(sand, vloc, motion, under);
				            Region region = plugin.regionManager.getRegion(vloc);
				            String rg = region.getString(RegionSetting.RED_GEL_BLOCK);
				            if (plugin.util.isSolid(bTo.getType())) {
				                if (region.getBoolean(RegionSetting.ENABLE_RED_GEL_BLOCKS) && plugin.blockUtil.compareBlockToString(bFrom.getRelative(BlockFace.DOWN), rg)){

				                    for (BlockFace rface : FaceUtil.getFaces(face)) {
				                        if (!plugin.util.isSolid(bFrom.getRelative(rface).getType())) {
				                            sand.setVelocity(FaceUtil.faceToVector(rface));
				                            motion = null;
				                            return;
				                        }
				                    }
				                } else {
				                    motion = null;
                                    return;
				                }
				            } else {
				                if (region.getBoolean(RegionSetting.ENABLE_RED_GEL_BLOCKS) && plugin.blockUtil.compareBlockToString(under, rg))
				                    motion = motion.multiply(0.9D);
				                else {

				                    if (useGel != -1){

				                        if (useGel == 0) {
				                            sand.remove();
				                            to.setY(to.getBlockY()+1);
				                            FallingBlock f = to
				                                    .getWorld()
				                                    .spawnFallingBlock(
				                                            to,
				                                            sand.getMaterial(),
				                                            sand.getData());
				                            final UUID uuid = f.getUniqueId();
				                            plugin.cubeManager.cubes.put(respawnLoc, uuid);
				                            plugin.gelManager.ignore.add(uuid);
				                            f.setDropItem(false);
				                            Vector v = motion;
				                            motion = null;
				                            v.setY(region.getDouble(RegionSetting.BLUE_GEL_MIN_VELOCITY));
				                            f.setVelocity(v);

				                            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() { public void run() { plugin.gelManager.ignore.remove(uuid); }}, 20L);
				                        }
				                        return;
				                    }

				                    if (under.getType() == Material.WOOL
				                            && (under.getData() == (byte) 15
				                            || under.getData() == (byte) 14 || under
				                            .getData() == (byte) 5)) {

				                        Block middle = plugin.util.chkBtn(bTo.getLocation());
				                        if (middle != null) {
				                            V10Location loc = new V10Location(middle);
				                            if(!plugin.cubeManager.buttons.containsKey(loc)) {

				                                plugin.util.changeBtn(middle, true);
				                                plugin.cubeManager.buttons.put(loc, sand);
				                            }
				                        }
				                    }

				                    motion = motion.multiply(0.2);

				                    if (motion.length() < 0.00001)
				                        motion = null;
				                }
				            }
				        }
				        
				        if(motion != null)
				            move(getLocation().add(motion));
					} else {
						if (!plugin.util.isSolid(getLocation().clone().add(motion).getBlock().getType())
								&&!plugin.util.isSolid(getLocation().clone().add(motion).add(-0.5,0,0).getBlock().getType())
								&&!plugin.util.isSolid(getLocation().clone().add(motion).add(0,0,0.5).getBlock().getType())) {
							move(getLocation().add(motion));
						}
					}
				}
				


			}
		}
		,2l,1l);
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
	public void clearTags(Player observer, int... entityIds) {
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
			motion = velocity;
		}
		
	    private void move(Location add) {
	        Location from = getLocation();
			this.x = add.getX();
			this.y = add.getY();
			this.z = add.getZ();
			Location to = getLocation();
			Location newTo = plugin.entityManager.onEntityMove(this, from, to, false);
			if(!to.equals(newTo)) {
			    this.x = newTo.getX();
			    this.y = newTo.getY();
			    this.z = newTo.getZ();
			}
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
        @Override
        public List<MetadataValue> getMetadata(String arg0) {
            return null;
        }
        @Override
        public boolean hasMetadata(String arg0) {
            return false;
        }
        @Override
        public void removeMetadata(String arg0, Plugin arg1) {
        }
        @Override
        public void setMetadata(String arg0, MetadataValue arg1) {
        }
        @Override
        public boolean eject() {
            // TODO Auto-generated method stub
            return false;
        }
        @Override
        public int getEntityId() {
            return entityId + 2;
        }
        @Override
        public float getFallDistance() {
            return 0;
        }
        @Override
        public int getFireTicks() {
            return 0;
        }
        @Override
        public EntityDamageEvent getLastDamageCause() {
            return null;
        }
        @Override
        public Location getLocation(Location arg0) {
            return this.getLocation();
        }
        @Override
        public int getMaxFireTicks() {
            return 0;
        }
        @Override
        public List<Entity> getNearbyEntities(double arg0, double arg1,
                double arg2) {
            // TODO Auto-generated method stub
            return null;
        }
        @Override
        public Entity getPassenger() {
            return attachPlayer;
        }
        @Override
        public Server getServer() {
            return plugin.getServer();
        }
        @Override
        public int getTicksLived() {
            return 0;
        }
        @Override
        public EntityType getType() {
            return EntityType.FALLING_BLOCK;
        }
        @Override
        public UUID getUniqueId() {
            return uuid;
        }
        @Override
        public Entity getVehicle() {
            return ridePlayer;
        }
        @Override
        public World getWorld() {
            return getLocation().getWorld();
        }
        @Override
        public boolean isDead() {
            return plugin.frozenSandManager.fakeBlocks.contains(this);
        }
        @Override
        public boolean isEmpty() {
            return attachPlayer == null;
        }
        @Override
        public boolean isInsideVehicle() {
            return ridePlayer != null;
        }
        @Override
        public boolean isOnGround() {
            return false;
        }
        @Override
        public boolean isValid() {
            return isDead();
        }
        @Override
        public boolean leaveVehicle() {
            return false;
        }
        @Override
        public void playEffect(EntityEffect arg0) {
        }
        @Override
        public void remove() {
            plugin.frozenSandManager.remove(this);
        }
        @Override
        public void setFallDistance(float arg0) {
        }
        @Override
        public void setFireTicks(int arg0) {
        }
        @Override
        public void setLastDamageCause(EntityDamageEvent arg0) {
        }
        @Override
        public boolean setPassenger(Entity arg0) {
            return false;
        }
        @Override
        public void setTicksLived(int arg0) {
        }
        @Override
        public boolean teleport(Location arg0) {
            // TODO Auto-generated method stub
            return false;
        }
        @Override
        public boolean teleport(Entity arg0) {
            return teleport(arg0.getLocation());
        }
        @Override
        public boolean teleport(Location arg0, TeleportCause arg1) {
            // TODO Auto-generated method stub
            return false;
        }
        @Override
        public boolean teleport(Entity arg0, TeleportCause arg1) {
            // TODO Auto-generated method stub
            return false;
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + entityId;
            result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if(this == obj)
                return true;
            if(obj != null && obj instanceof FrozenSand) {
                FrozenSand other = (FrozenSand)obj;
                return (entityId == other.entityId &&
                        uuid.equals(other.uuid));
            }
            return false;
        }
	}