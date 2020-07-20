package org.PortalStick.util;

import org.bukkit.Location;
import org.bukkit.block.Block;

import com.sanjay900.nmsUtil.util.V10Location;

public class BlockStorage {

    private final int id;
    private final V10Location location;
    private final byte data;
    
    @SuppressWarnings("deprecation")
	public BlockStorage(Block block) {
        this.id = block.getTypeId(); // TODO: Deprecated...
        this.location = new V10Location(block);
        this.data = block.getData();
    }
    
    public int getID() {
        return id;
    }
    
    public byte getData() {
        return data;
    }
    
    public void set() {
        Location loc = location.getHandle();
        if(loc != null) {
            Block block = loc.getBlock();
            block.setTypeIdAndData(id, data, true);
        }
    }
    
    public V10Location getLocation() {
        return location;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        result = prime * result
                + ((location == null) ? 0 : location.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if(obj != null && obj instanceof BlockStorage) {
            BlockStorage other = (BlockStorage)obj;
            return id == other.id && location.equals(other.location);
        }
        return false;
    }
}
