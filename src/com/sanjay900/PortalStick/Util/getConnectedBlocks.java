package com.sanjay900.PortalStick.Util;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class getConnectedBlocks{
	static List<Block>
	unchecked = new LinkedList<Block>(),
	checked = new LinkedList<Block>(),
	confirmed = new LinkedList<Block>();

	public static List<Block> getConnectedBlocks(Block block){
		BlockFace bf = null;

		unchecked.clear();
		checked.clear();
		confirmed.clear();
		unchecked.add(block);
		while(unchecked.size() > 0){
			if(!isChecked(unchecked.get(0))){
				for(int i = 0; i < 6; i++){
					if(i == 0){
						bf = BlockFace.DOWN;
					}else if(i == 1){
						bf = BlockFace.EAST;
					}else if(i == 2){
						bf = BlockFace.NORTH;
					}else if(i == 3){
						bf = BlockFace.SOUTH;
					}else if(i == 4){
						bf = BlockFace.UP;
					}else if(i == 5){
						bf = BlockFace.WEST;
					}
					if(unchecked.get(0).getRelative(bf).getType().equals(block.getType()) &&unchecked.get(0).getRelative(bf).getData() == block.getData()  && !isChecked(unchecked.get(0).getRelative(bf))){
						unchecked.add(unchecked.get(0).getRelative(bf));
					}
				}
				checked.add(unchecked.get(0));
			}
			unchecked.remove(0);
		}
		for(int i = 0; i < checked.size(); i++){
			if(checked.get(i).getType().equals(block.getType()) && checked.get(i).getData() == block.getData()){
				confirmed.add(checked.get(i));
			}
		}
		return confirmed;
	}

	public static boolean isChecked(Block block){
		for(int i = 0; i < checked.size(); i++){
			if(checked.get(i) != null && checked.get(i).equals(block)){
				return true;
			}
		}
		return false;
	}
}