package com.sanjay900.fallingblocks;

import java.util.Iterator;
import java.util.NoSuchElementException;
 
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
 
public class LocationIterator implements Iterator<Location>{
    private final World world;
    @SuppressWarnings("unused")
    private final int maxDistance;
   
    private static final int gridSize = 1 << 24;
   
    private boolean end = false;
   
    private Location[] locationQueue = new Location[3];
    private int currentLocation = 0;
    private int currentDistance = 0;
    private int maxDistanceInt;
   
    private int secondError;
    private int thirdError;
   
    private int secondStep;
    private int thirdStep;
   
    private BlockFace mainFace;
    private BlockFace secondFace;
    private BlockFace thirdFace;
   
    public LocationIterator(World world, Vector start, Vector direction, double yOffset, int maxDistance) {
        this.world = world;
        this.maxDistance = maxDistance;
       
        Vector startClone = start.clone();
       
        startClone.setY(startClone.getY() + yOffset);
       
        currentDistance = 0;
       
        double mainDirection = 0;
        double secondDirection = 0;
        double thirdDirection = 0;
       
        double mainPosition = 0;
        double secondPosition = 0;
        double thirdPosition = 0;
       
        Location startLocation = new Location(this.world, (int) Math.floor(startClone.getX()), (int) Math.floor(startClone.getY()), (int) Math.floor(startClone.getZ()));
        mainFace = getXFace(direction);
        mainDirection = getXLength(direction);
        mainPosition = getXPosition(direction, startClone, startLocation);
       
        secondFace = getYFace(direction);
        secondDirection = getYLength(direction);
        secondPosition = getYPosition(direction, startClone, startLocation);
       
        thirdFace = getZFace(direction);
        thirdDirection = getZLength(direction);
        thirdPosition = getZPosition(direction, startClone, startLocation);
        if(getYLength(direction) > mainDirection) {
            mainFace = getYFace(direction);
            mainDirection = getYLength(direction);
            mainPosition = getYPosition(direction, startClone, startLocation);
           
            secondFace = getZFace(direction);
            secondDirection = getZLength(direction);
            secondPosition = getZPosition(direction, startClone, startLocation);
           
            thirdFace = getXFace(direction);
            thirdDirection = getXLength(direction);
            thirdPosition = getXPosition(direction, startClone, startLocation);
        }
        if(getZLength(direction) > mainDirection) {
            mainFace = getZFace(direction);
            mainDirection = getZLength(direction);
            mainPosition = getZPosition(direction, startClone, startLocation);
           
            secondFace = getXFace(direction);
            secondDirection = getXLength(direction);
            secondPosition = getXPosition(direction, startClone, startLocation);
           
            thirdFace = getYFace(direction);
            thirdDirection = getYLength(direction);
            thirdPosition = getYPosition(direction, startClone, startLocation);
        }
       
        // trace line backwards to find intercept with plane perpendicular to the main axis
       
        double d = mainPosition / mainDirection; // how far to hit face behind
        double secondd = secondPosition - secondDirection * d;
        double thirdd = thirdPosition - thirdDirection * d;
       
        // Guarantee that the ray will pass though the start block.
        // It is possible that it would miss due to rounding
        // This should only move the ray by 1 grid position
        secondError = (int) (Math.floor(secondd * gridSize));
        secondStep = (int) (Math.round(secondDirection / mainDirection * gridSize));
        thirdError = (int) (Math.floor(thirdd * gridSize));
        thirdStep = (int) (Math.round(thirdDirection / mainDirection * gridSize));
       
        if(secondError + secondStep <= 0) {
            secondError = -secondStep + 1;
        }
       
        if(thirdError + thirdStep <= 0) {
            thirdError = -thirdStep + 1;
        }
       
        Location lastLocation;
       
        lastLocation = getRelativeLocation(startLocation, reverseFace(mainFace));
 
        if(secondError < 0) {
            secondError += gridSize;
            lastLocation = getRelativeLocation(lastLocation, reverseFace(secondFace));
        }
 
        if(thirdError < 0) {
            thirdError += gridSize;
            lastLocation = getRelativeLocation(lastLocation, reverseFace(thirdFace));
        }
 
        // This means that when the variables are positive, it means that the coord=1 boundary has been crossed
        secondError -= gridSize;
        thirdError -= gridSize;
 
        locationQueue[0] = lastLocation;
        currentLocation = -1;
 
        scan();
 
        boolean startLocationFound = false;
 
        for (int cnt = currentLocation; cnt >= 0; cnt--) {
            if(locationEquals(locationQueue[cnt], startLocation)) {
                currentLocation = cnt;
                startLocationFound = true;
                break;
            }
        }
 
        if(!startLocationFound) {
            throw new IllegalStateException("Start location missed in LocationIterator");
        }
 
        // Calculate the number of planes passed to give max distance
        maxDistanceInt = (int) Math.round(maxDistance / (Math.sqrt(mainDirection * mainDirection + secondDirection * secondDirection + thirdDirection * thirdDirection) / mainDirection));
 
    }
 
    private boolean locationEquals(Location a, Location b) {
        return a.getBlockX() == b.getBlockX() && a.getBlockY() == b.getBlockY() && a.getBlockZ() == b.getBlockZ();
    }
 
    private BlockFace reverseFace(BlockFace face) {
        switch(face) {
        case UP:
            return BlockFace.DOWN;
 
        case DOWN:
            return BlockFace.UP;
 
        case NORTH:
            return BlockFace.SOUTH;
 
        case SOUTH:
            return BlockFace.NORTH;
 
        case EAST:
            return BlockFace.WEST;
 
        case WEST:
            return BlockFace.EAST;
 
        default:
            return null;
        }
    }
 
    private BlockFace getXFace(Vector direction) {
        return ((direction.getX() > 0) ? BlockFace.SOUTH : BlockFace.NORTH);
    }
 
    private BlockFace getYFace(Vector direction) {
        return ((direction.getY() > 0) ? BlockFace.UP : BlockFace.DOWN);
    }
 
    private BlockFace getZFace(Vector direction) {
        return ((direction.getZ() > 0) ? BlockFace.WEST : BlockFace.EAST);
    }
 
    private double getXLength(Vector direction) {
        return Math.abs(direction.getX());
    }
 
    private double getYLength(Vector direction) {
        return Math.abs(direction.getY());
    }
 
    private double getZLength(Vector direction) {
        return Math.abs(direction.getZ());
    }
 
    private double getPosition(double direction, double position, int locationPosition) {
        return direction > 0 ? (position - locationPosition) : (locationPosition + 1 - position);
    }
 
    private double getXPosition(Vector direction, Vector position, Location location) {
        return getPosition(direction.getX(), position.getX(), location.getBlockX());
    }
 
    private double getYPosition(Vector direction, Vector position, Location location) {
        return getPosition(direction.getY(), position.getY(), location.getBlockY());
    }
 
    private double getZPosition(Vector direction, Vector position, Location location) {
        return getPosition(direction.getZ(), position.getZ(), location.getBlockZ());
    }
 
    public LocationIterator(Location loc, double yOffset, int maxDistance) {
        this(loc.getWorld(), loc.toVector(), loc.getDirection(), yOffset, maxDistance);
    }
 
    public LocationIterator(Location loc, double yOffset) {
        this(loc.getWorld(), loc.toVector(), loc.getDirection(), yOffset, 0);
    }
 
    public LocationIterator(Location loc) {
        this(loc, 0D);
    }
 
    public LocationIterator(LivingEntity entity, int maxDistance) {
        this(entity.getLocation(), entity.getEyeHeight(), maxDistance);
    }
 
    public LocationIterator(LivingEntity entity) {
        this(entity, 0);
    }
   
    public Location getRelativeLocation(Location location, BlockFace face) {
        switch (face) {
        case UP:
            return location.clone().add(0, 1, 0);
 
        case DOWN:
            return location.clone().add(0, -1, 0);
 
        case NORTH:
            return location.clone().add(-1, 0, 0);
 
        case SOUTH:
            return location.clone().add(1, 0, 0);
 
        case EAST:
            return location.clone().add(0, 0, -1);
 
        case WEST:
            return location.clone().add(0, 0, 1);
 
        default:
            return null;
        }
    }
   
    public boolean hasNext() {
        scan();
        return currentLocation != -1;
    }
 
    public Location next() {
        scan();
        if(currentLocation <= -1) {
            throw new NoSuchElementException();
        } else {
            return locationQueue[currentLocation--];
        }
    }
 
    public void remove() {
        throw new UnsupportedOperationException("[LocationIterator] doesn't support location removal");
    }
 
    private void scan() {
        if(currentLocation >= 0) {
            return;
        }
        //if(maxDistance != 0 && currentDistance > maxDistanceInt) {
        if(currentDistance > maxDistanceInt) {
            end = true;
            return;
        }
        if(end) {
            return;
        }
 
        currentDistance++;
 
        secondError += secondStep;
        thirdError += thirdStep;
 
        if(secondError > 0 && thirdError > 0) {
            locationQueue[2] = getRelativeLocation(locationQueue[0], mainFace);
            if(((long) secondStep) * ((long) thirdError) < ((long) thirdStep) * ((long) secondError)) {
                locationQueue[1] = getRelativeLocation(locationQueue[2], secondFace);
                locationQueue[0] = getRelativeLocation(locationQueue[1], thirdFace);
            } else {
                locationQueue[1] = getRelativeLocation(locationQueue[2], thirdFace);
                locationQueue[0] = getRelativeLocation(locationQueue[1], secondFace);
            }
            thirdError -= gridSize;
            secondError -= gridSize;
            currentLocation = 2;
            return;
        } else if(secondError > 0) {
            locationQueue[1] = getRelativeLocation(locationQueue[0], mainFace);
            locationQueue[0] = getRelativeLocation(locationQueue[1], secondFace);
            secondError -= gridSize;
            currentLocation = 1;
            return;
        } else if(thirdError > 0) {
            locationQueue[1] = getRelativeLocation(locationQueue[0], mainFace);
            locationQueue[0] = getRelativeLocation(locationQueue[1], thirdFace);
            thirdError -= gridSize;
            currentLocation = 1;
            return;
        } else {
            locationQueue[0] = getRelativeLocation(locationQueue[0], mainFace);
            currentLocation = 0;
            return;
        }
    }
}
