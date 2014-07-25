package com.sanjay900.PortalStick;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;

import com.sanjay900.fallingblocks.FrozenSand;
 
public final class MoveEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
	private FrozenSand entity;
	private Location to;
	private Location from;
    private boolean cancelled = false;
	private Vector velocity;
    public MoveEvent(FrozenSand entity, Location to, Location from, Vector velocity) {
        this.entity = entity;
        this.to = to;
        this.from = from;
        this.velocity = velocity;
    }
 
    public FrozenSand getEntity() {
        return entity;
    }
    public Location getFrom() {
        return from;
    }
    public Location getTo() {
        return to;
    }
    public boolean isCancelled() {
    	return cancelled;
    }
    public void setCancelled(boolean c) {
    	this.cancelled = c;
    }
    
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }

	public Vector getVelocity() {
		// TODO Auto-generated method stub
		return velocity ;
	}

	public void setVelocity(Vector multiply) {
		// TODO Auto-generated method stub
		this.velocity = multiply;
	}
}
