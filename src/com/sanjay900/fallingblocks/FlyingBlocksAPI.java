/**
 * flyingblocksapi Copyright (C) 2014 ase34 and contributors
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
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
