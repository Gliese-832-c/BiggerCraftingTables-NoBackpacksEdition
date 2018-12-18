package wanion.biggercraftingtables.inventory.slot;

/*
 * Created by WanionCane(https://github.com/WanionCane).
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import wanion.biggercraftingtables.block.TileEntityBiggerCreatingTable;
import wanion.lib.common.matching.Matching;

import javax.annotation.Nonnull;

public class MatchingSlot extends ShapeSlot
{
	private final Matching matching;

	public MatchingSlot( @Nonnull final TileEntityBiggerCreatingTable tileEntityBiggerCreatingTable, final int id, final int x, final int y)
	{
		super(tileEntityBiggerCreatingTable, id, x, y);
		this.matching = tileEntityBiggerCreatingTable.getMatchingController().getMatching(id);
	}

	public Matching getMatching()
	{
		return matching;
	}
}