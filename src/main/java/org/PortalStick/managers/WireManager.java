package org.PortalStick.managers;

import java.util.ArrayList;
import java.util.Iterator;

import org.PortalStick.PortalStick;
import org.PortalStick.components.Wire;
import org.PortalStick.util.V10Location;
import org.bukkit.DyeColor;
import org.bukkit.block.Block;

public class WireManager {
    private final PortalStick plugin;
    public final ArrayList<V10Location> wire = new ArrayList<V10Location>();

    public WireManager(PortalStick plugin) {
        this.plugin = plugin;
    }

    public void cleanUpWire() {
        byte data1, data2, data3, data4, data5, data6, data7, data8;
        Iterator<V10Location> iter = wire.iterator();
        Block blk;
        data1 = DyeColor.LIME.getData();
        data2 = DyeColor.PINK.getData();
        data3 = DyeColor.GREEN.getData();
        data4 = DyeColor.RED.getData();
        data5 = DyeColor.YELLOW.getData();
        data6 = DyeColor.MAGENTA.getData();
        data7 = DyeColor.ORANGE.getData();
        data8 = DyeColor.PURPLE.getData();
        while (iter.hasNext()) {
            blk = iter.next().getHandle().getBlock();
            if (blk.getData() == data1) {
                blk.setData(data2);
                new Wire(plugin, blk, blk, false).runTaskLater(
                        plugin, 1L);
                return;
            } else if (blk.getData() == data3) {
                blk.setData(data4);
                new Wire(plugin, blk, blk, false).runTaskLater(
                        plugin, 1L);
                return;
            }else if (blk.getData() == data5) {
                blk.setData(data6);
                new Wire(plugin, blk, blk, false).runTaskLater(
                        plugin, 1L);
                return;
            }else if (blk.getData() == data7) {
                blk.setData(data8);
                new Wire(plugin, blk, blk, false).runTaskLater(
                        plugin, 1L);
                return;
            }
            iter.remove();
        }
    }
}
