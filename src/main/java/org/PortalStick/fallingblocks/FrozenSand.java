package org.PortalStick.fallingblocks;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.PortalStick.PortalStick;
import org.PortalStick.util.V10Location;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

public class FrozenSand {

	private final PortalStick plugin;
	public int entityId = 0;
	double x = 0;
	double y = 0;
	double z = 0;
	public int blockId;
	public int blockData;
	public int storageId = 0;
	String worldName = "";
	ProtocolManager pm;
	private Vector motion;
	public BukkitTask velocitytask = null;
	private final UUID uuid = UUID.randomUUID();
	public V10Location spawnloc = null;

	protected FrozenSand(final PortalStick plugin, Integer id2, String worldName, double x, double y, double z, int blockid, int blockdata) {
		this.plugin = plugin;
		entityId = plugin.frozenSandManager.nextId();
		this.x = x;
		this.y = y;
		this.z = z;
		this.blockId = blockid;
		this.blockData = blockdata;
		this.worldName = worldName;
		this.pm = ProtocolLibrary.getProtocolManager();
		this.storageId = id2;
		velocitytask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable(){

			@Override
			public void run() {
				if (motion != null) {
					if (!plugin.util.isSolid(getLocation().clone().add(motion).getBlock().getType())
							&&!plugin.util.isSolid(getLocation().clone().add(motion).add(-0.5,0,0).getBlock().getType())
							&&!plugin.util.isSolid(getLocation().clone().add(motion).add(0,0,0.5).getBlock().getType())) {
						move(getLocation().add(motion));
					}
				}
			}
		}
		,2l,1l);


	}

	protected void generate(Player observer) {
		x = Math.floor(x)+0.5;
		y = Math.floor(y);
		z = Math.floor(z)+0.5;
		try {
			PacketContainer attach2 = pm.createPacket(PacketType.Play.Server.ATTACH_ENTITY);
			PacketContainer horse = pm.createPacket(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
			StructureModifier<Integer> modifier = horse.getIntegers();
			byte i = Byte.valueOf((byte)0);
			i &= -5;
			i = (byte)(i | 8);
			i = (byte)(i | 1);
			modifier.write(0, this.getArmourStandIndex());
			horse.getIntegers().write(1, 30);
			horse.getIntegers().write(2, floor(x * 32.0D));
			horse.getIntegers().write(3, floor((y-1.5) * 32.0D));
			horse.getIntegers().write(4, floor(z * 32.0D));
			WrappedDataWatcher watcher = new WrappedDataWatcher();
			watcher.setObject(0, (byte) 32);
			watcher.setObject(10, i);
			horse.getDataWatcherModifier().write(0,watcher);
			PacketContainer item = pm.createPacket(PacketType.Play.Server.SPAWN_ENTITY);

			modifier = item.getIntegers();
			modifier.write(0, this.getFallingBlockIndex());
			modifier.write(1, (int) Math.floor(x * 32.0D));
			modifier.write(2, (int) Math.floor( (y-1.5)* 32.0D));
			modifier.write(3, (int) Math.floor(z * 32.0D));
			modifier.write(9, 70);
			modifier.write(10, blockId + (blockData << 12));
			modifier = attach2.getIntegers();
			modifier.write(0, 0);
			modifier.write(1, item.getIntegers().read(0));
			attach2.getIntegers().write(2,this.getArmourStandIndex());

			pm.sendServerPacket(observer, horse);
			pm.sendServerPacket(observer, item);
			pm.sendServerPacket(observer, attach2);
		} catch (InvocationTargetException e) {
		}

	}
	public static int floor(double paramDouble)
	{
		int i = (int)paramDouble;
		return paramDouble < i ? i - 1 : i;
	}

	public void clearTags(Player observer) {
		if (this.getAllEntityIds().length > 0) {
			PacketContainer packet = pm.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
			packet.getIntegerArrays().write(0, this.getAllEntityIds());
			try {
				pm.sendServerPacket(observer, packet);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(
						"Cannot send packet " + packet, e);
			}

		}
	}
	public int[] getAllEntityIds() {
		int[] entityIdList = new int[3];
		for (int i = 0; i < 3; i++) {
			entityIdList[i] = this.getArmourStandIndex() + i;
		}

		return entityIdList;
	}

	protected void moveTag(Player observer) {
		PacketContainer teleportSkull = pm.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
		StructureModifier<Integer> modifier = teleportSkull.getIntegers();
		modifier.write(0, getArmourStandIndex());

		modifier.write(1, floor( x* 32.0D));
		modifier.write(2, floor((y-1.5) * 32.0D));
		modifier.write(3, floor( z * 32.0D));

		try {
			pm.sendServerPacket(observer, teleportSkull);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} 

	}

	public int getArmourStandIndex() {
		return entityId;
	}
	public int getSkullIndex() {
		return entityId+1;
	}
	public int getFallingBlockIndex() {
		return entityId+2;
	}
	public void setVelocity(final Vector velocity) {			
		motion = velocity;
	}

	private void move(Location add) {
		this.x = add.getX();
		this.y = add.getY();
		this.z = add.getZ();
		for (Player p : add.getWorld().getPlayers()) {
			this.moveTag(p);
		}
	}
	public void show(Player observer) {
		this.generate(observer);
	}
	public Location getLocation() {
		return new Location(Bukkit.getWorld(worldName),x,y,z);
	}
	public void remove() {
		plugin.frozenSandManager.remove(this);
	}

	public UUID getUniqueID() {
		return uuid;
	}

	public int getMaterial() {
		return blockId;
	}
	public int getData() {
		return blockData;
	}



}