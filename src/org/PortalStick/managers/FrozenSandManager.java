package org.PortalStick.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map.Entry;

import org.PortalStick.Portal;
import org.PortalStick.PortalStick;
import org.PortalStick.User;
import org.PortalStick.fallingblocks.FrozenSand;
import org.PortalStick.util.V10Location;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;

public class FrozenSandManager {
	public ArrayList<FrozenSand> fakeBlocks = new ArrayList<FrozenSand>();
	public int lastId = 0;
	public int getNextId() {
		return ++lastId;
	}
	
	public FrozenSandManager(final PortalStick portalstick) {
		ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
		protocolManager.addPacketListener(new PacketAdapter(portalstick,
		        ListenerPriority.NORMAL, 
		        PacketType.Play.Client.USE_ENTITY) {
		    @Override
		    public void onPacketReceiving(PacketEvent event) {
		            PacketContainer packet = event.getPacket();
		            int entityID = packet.getIntegers().read(0);
		            for (FrozenSand f: fakeBlocks) {	
		            	if (f.entityId+2 == entityID) {
		            		if (portalstick.cubeManager.flyingBlocks.containsValue(f)) {
		        				Iterator<Entry<V10Location, FrozenSand>> it = portalstick.cubeManager.flyingBlocks.entrySet().iterator();

		        				while (it.hasNext()) {
		        					Entry<V10Location, FrozenSand> fb = it.next();
		        					V10Location loc = fb.getKey();
		        					portalstick.cubeManager.flyingBlocks.remove(loc);
		        					portalstick.cubeManager.cubesPlayer.put(loc, event.getPlayer().getUniqueId());
		        					ItemStack item = new ItemStack(Material.getMaterial(Integer.parseInt(fb.getValue().id.split(":")[0])), 1, (byte)Integer.parseInt(fb.getValue().id.split(":")[1]));
		        					portalstick.cubeManager.cubesPlayerItem.put(loc, item);

		        					event.getPlayer().getInventory().addItem(item);
		        					portalstick.util.doInventoryUpdate(event.getPlayer(), portalstick);
		        					fb.getValue().clearAllPlayerViews();
		        					V10Location middle;
		        					if (portalstick.cubeManager.buttons.containsValue(fb.getValue())) {
		        						Iterator<Entry<V10Location, FrozenSand>> iter = portalstick.cubeManager.buttons.entrySet().iterator();
		        						while (iter.hasNext()) {
		        							Entry<V10Location, FrozenSand> e = iter.next();
		        							if (e.getValue() == fb.getValue()) {
		        								middle = e.getKey();
		        								portalstick.util.changeBtn(middle, false);
		        								iter.remove();
		        							}
		        						}
		        					}
		        					return;

		        				}

		        			} else {
		        				Player player = event.getPlayer();
		        				User user = portalstick.userManager.getUser(player);
		        				for (Portal p :portalstick.portalManager.portals) {
		        					if (p.open && p.owner.name == event.getPlayer().getName()){
		        						
		        						if (Arrays.asList(p.coord.insideFrozen).contains(f)) {
		        							if (packet.getEntityUseActions().read(0) == EntityUseAction.ATTACK)
		        							p.delete();
		        							else if (player.getItemInHand().getTypeId() == 0)
		        							{

		        								int preset = user.colorPreset;
		        								if (preset == portalstick.config.ColorPresets.size() - 1)
		        									preset = 0;
		        								else
		        									preset++;
		        								
		        								user.colorPreset = preset;
		        								user.recreatePortals();

		        								String color1 = DyeColor.values()[portalstick.util.getLeftPortalColor(preset)].toString().replace("_", " ");
		        								String color2 = DyeColor.values()[portalstick.util.getRightPortalColor(preset)].toString().replace("_", " ");

		        								portalstick.util.sendMessage(player, portalstick.i18n.getString("SwitchedPortalColor", player.getName(), color1, color2));
		        							}
		        						}
		        					}
		        				}
		        			}
		            	}
		            }
		            
		        
		    }
		});
	}
}
