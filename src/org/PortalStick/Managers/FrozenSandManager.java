package org.PortalStick.Managers;

import java.util.ArrayList;

import org.PortalStick.fallingblocks.FrozenSand;

public class FrozenSandManager {
	public ArrayList<FrozenSand> fakeBlocks = new ArrayList<FrozenSand>();
	public int lastId = 0;

	public int getNextId() {
		return ++lastId;
	}
}
