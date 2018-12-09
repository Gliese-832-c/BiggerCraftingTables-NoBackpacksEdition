package wanion.biggercraftingtables.common;

/*
 * Created by WanionCane(https://github.com/WanionCane).
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public interface IShapedContainer
{
	void defineShape(short key, @Nonnull ItemStack output);

	void clearShape();
}