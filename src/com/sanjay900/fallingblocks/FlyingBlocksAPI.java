package com.sanjay900.fallingblocks;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import de.V10lator.PortalStick.PortalStick;

public class FlyingBlocksAPI implements Listener {
	public PortalStick plugin;
	public static ArrayList<FrozenSand> fakeBlocks = new ArrayList<>();
	public static int lastId = 0;
	public FlyingBlocksAPI(PortalStick portalStick) {
		this.plugin = portalStick;
	}

	public static int getNextId() {
		lastId ++;
		return lastId;
	}
	
	@EventHandler
	public void login(final PlayerJoinEvent event) {
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable(){

			@Override
			public void run() {
				for (FrozenSand h : fakeBlocks) {
					h.show(event.getPlayer());
				}
			}},80L);
		
	}
	
	

	@EventHandler
	public void onTeleport(final PlayerTeleportEvent event) {
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable(){

			@Override
			public void run() {
				for (FrozenSand h : fakeBlocks) {
					h.show(event.getPlayer());
				}
			}},10L);
		
	}
	@EventHandler
	public void worldMove (final PlayerChangedWorldEvent event) {
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable(){

			@Override
			public void run() {
				for (FrozenSand h : fakeBlocks) {
					h.show(event.getPlayer());
				}
			}},10L);
		
	}

	

	


}
