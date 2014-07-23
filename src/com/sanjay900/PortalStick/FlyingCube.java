package com.sanjay900.PortalStick;

import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

import com.bergerkiller.bukkit.common.utils.FaceUtil;
import  de.V10lator.PortalStick.PortalStick;
import com.sanjay900.PortalStick.Util.Util;

import de.V10lator.PortalStick.Portal;

public class FlyingCube //extends FlyingBlock
{

	private PortalStick plugin;

	public FlyingCube(Material material, byte materialData, PortalStick plugin) {
		//super(material, materialData);
		this.plugin = plugin;
	}

	@SuppressWarnings("deprecation")
	public void onTick() {
		/*Location l = this.getBukkitEntity()
				.getLocation();
		l.setY(l.getY() - this.getHeightOffset());
		Block Under = l.getBlock().getRelative(
				BlockFace.DOWN);
		
		BlockFace face = FaceUtil.getDirection(this
				.getBukkitEntity().getVelocity());
		if (!(l.getBlock()
				.getRelative(face).getType() == Material.AIR)) {

			if ((l.getBlock().getRelative(face).getType() == Material.WOOL ) && l.getBlock().getRelative(face).getData() == (byte) 3) {
				for (Entry<Block, FlyingBlock> entry : plugin.eventListener.FlyingBlocks
						.entrySet()) {
					if (this.equals(entry.getValue())) {
						if (FaceUtil.isSubCardinal(face)) {
							if (this.equals(entry.getValue())) {
								
								FallingBlock f = l
										.getWorld()
										.spawnFallingBlock(
												l,
												this.getMaterial(),
												this.getMaterialData());
								f.setDropItem(false);
								
								Vector opp = FaceUtil.faceToVector(FaceUtil.rotate(face, 2));
								f.setVelocity(opp);
								plugin.eventListener.cubes.put(entry.getKey(), f);
								plugin.eventListener.FlyingBlocks.remove(entry.getKey());
								this.remove();
								
						}
						this.remove();
						
					} else {
						Vector opp = FaceUtil.faceToVector(FaceUtil.rotate(face, 4));
						plugin.eventListener.FlyingBlocks.remove(entry.getKey());
						FlyingBlock fblock = new FlyingCube(
								this.getMaterial(),
								this.getMaterialData(), plugin);

							
							

						
						// spawn block
						fblock.spawn(l);
						fblock.setVelocity(opp);
						plugin.eventListener.FlyingBlocks.put(entry.getKey(), fblock);
						this.remove();
						
					}
					}
				}
			} else {
				
				for (Entry<Block, FlyingBlock> entry : plugin.eventListener.FlyingBlocks
						.entrySet()) {
					if (this.equals(entry.getValue())) {
						
						
					if (FaceUtil.isSubCardinal(face)) {
							if (this.equals(entry.getValue())) {
								
								
								
								for (BlockFace b: FaceUtil.getFaces(face)) {
									if (l.getBlock().getRelative(b).getType() == Material.AIR) {
										FallingBlock f = l
												.getWorld()
												.spawnFallingBlock(
														l,
														this.getMaterial(),
														this.getMaterialData());
										f.setDropItem(false);
								Vector opp = FaceUtil.faceToVector(b);
								f.setVelocity(opp);
								plugin.eventListener.cubes.put(entry.getKey(), f);
								plugin.eventListener.FlyingBlocks.remove(entry.getKey());
								this.remove();
									}
								}
							
						}
						this.remove();
						
					} else {
						l.getBlock().setType(
								this.getMaterial());
						l.getBlock().setData(
								this.getMaterialData());
						plugin.eventListener.cubesFallen.put(entry.getKey(),
								l.getBlock());
						plugin.eventListener.FlyingBlocks.remove(entry.getKey());
						this.remove();
						
					}
						
					}
				}
			}
			

		} else if ((Under.getType() == Material.WOOL)
				&& (Under.getData() == (byte) 3)) {
			for (Entry<Block, FlyingBlock> entry : plugin.eventListener.FlyingBlocks
					.entrySet()) {
				if (this.equals(entry.getValue())) {
					
					FallingBlock f = l
							.getWorld()
							.spawnFallingBlock(
									l,
									this.getMaterial(),
									this.getMaterialData());
					f.setDropItem(false);
					f.setVelocity(this
							.getBukkitEntity()
							.getVelocity().setY(1));

					plugin.eventListener.cubes.put(entry.getKey(), f);
					plugin.eventListener.FlyingBlocks.remove(entry.getKey());
				}
			}
			this.remove();
		} else if (Under.getType() == Material.WOOL
				&& (Under.getData() == (byte) 15
				|| Under.getData() == (byte) 14 || Under
				.getData() == (byte) 5)) {
			
			Block middle = Util.chkBtn(l);
			if (!(middle == null)
					&& !plugin.eventListener.buttons.containsKey(middle)) {

				Util.changeBtn(middle,
						!plugin.eventListener.buttons.containsKey(middle));
				plugin.eventListener.buttons.put(middle, l.getBlock());

			}
			for (Entry<Block, FlyingBlock> entry : plugin.eventListener.FlyingBlocks
					.entrySet()) {
				if (this.equals(entry.getValue())) {
					l.getBlock().setType(
							this.getMaterial());
					l.getBlock().setData(
							this.getMaterialData());
					plugin.eventListener.cubesFallen.put(entry.getKey(),
							l.getBlock());
					plugin.eventListener.FlyingBlocks.remove(entry.getKey());
				}

			}
			this.remove();
		} else if (!(Under.getType() == Material.WOOL)
				|| !(Under.getData() == (byte) 1)) {
			
			for (Entry<Block, FlyingBlock> entry : plugin.eventListener.FlyingBlocks
					.entrySet()) {
				if (this.equals(entry.getValue())) {

					l.getBlock().setType(
							this.getMaterial());
					l.getBlock().setData(
							this.getMaterialData());
					plugin.eventListener.cubesFallen.put(entry.getKey(),
							l.getBlock());
					plugin.eventListener.FlyingBlocks.remove(entry.getKey());

				}
			}

			this.remove();

		} else if (!(Under.getType() == Material.WOOL)
							|| !(Under.getData() == (byte) 1)) {
						
					for (Entry<Block, FlyingBlock> entry : plugin.eventListener.FlyingBlocks
							.entrySet()) {
						if (this.equals(entry.getValue())) {

							l.getBlock().setType(
									this.getMaterial());
							l.getBlock().setData(
									this.getMaterialData());
							plugin.eventListener.cubesFallen.put(entry.getKey(),
									l.getBlock());
							plugin.eventListener.FlyingBlocks.remove(entry.getKey());

						}
					}
					this.remove();
				
		} else {
			this.setVelocity(this.getBukkitEntity()
					.getVelocity().multiply(0.99));
			

		}
		*/
		}
		

}
