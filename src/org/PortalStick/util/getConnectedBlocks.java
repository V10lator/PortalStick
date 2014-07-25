package org.PortalStick.util;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class getConnectedBlocks{
	List<Block>
	unchecked = new LinkedList<Block>(),
	checked = new LinkedList<Block>(),
	confirmed = new LinkedList<Block>();

	public List<Block> getConnectedBlocks(Block block){

		unchecked.clear();
		checked.clear();
		confirmed.clear();
		unchecked.add(block);
		BlockFace[] faces = new BlockFace[] {BlockFace.DOWN, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.UP, BlockFace.DOWN};
		Block block1, block2;
		while(unchecked.size() > 0){
		    block1 = unchecked.get(0);
			if(!isChecked(block1)){
				for(BlockFace bf: faces){
				    block2 = block1.getRelative(bf);
					if(block2.getType().equals(block.getType()) &&block2.getData() == block.getData()  && !isChecked(block2)){
						unchecked.add(block2);
					}
				}
				checked.add(block1);
			}
			unchecked.remove(0);
		}
		for(Block block3: checked)
			if(block3.getType().equals(block.getType()) && block3.getData() == block.getData())
				confirmed.add(block3);
		return confirmed;
	}

	public boolean isChecked(Block block){
		for(Block block1: checked){
			if(block1 != null && block1.equals(block)){
				return true;
			}
		}
		return false;
	}
}