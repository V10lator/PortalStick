package org.PortalStick.listeners;

import org.PortalStick.PortalStick;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;

public class PortalStickVehicleListener implements Listener {
	private final PortalStick plugin;
	
	public PortalStickVehicleListener(PortalStick plugin)
	{
		this.plugin = plugin;
	}
	
	@EventHandler()
	public void onVehicleMove(VehicleMoveEvent event)
	{
	  if(plugin.config.DisabledWorlds.contains(event.getTo().getWorld().getName()))
		return;
	  plugin.entityManager.onEntityMove(event.getVehicle(), event.getFrom(), event.getTo(), true);
	}
}
