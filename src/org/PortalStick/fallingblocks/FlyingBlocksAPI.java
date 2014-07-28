package org.PortalStick.fallingblocks;

import java.util.ArrayList;

public class FlyingBlocksAPI {
	public ArrayList<FrozenSand> fakeBlocks = new ArrayList<FrozenSand>();
	public int lastId = 0;

	public int getNextId() {
		return ++lastId;
	}
}
