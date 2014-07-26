package org.PortalStick.fallingblocks;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;
 
public final class FlyingBlockMoveEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
	private FrozenSand entity;
	private Location to;
	private Location from;
    private boolean cancelled = false;
	private Vector velocity;
    public FlyingBlockMoveEvent(FrozenSand entity, Location to, Location from, Vector velocity) {
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
		return velocity ;
	}

	public void setVelocity(Vector multiply) {
		this.velocity = multiply;
	}
}
