package wanion.biggercraftingtables.block;

/*
 * Created by WanionCane(https://github.com/WanionCane).
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import wanion.biggercraftingtables.Reference;

public final class ItemBlockBiggerCraftingTable extends ItemBlock
{
	public static final ItemBlockBiggerCraftingTable INSTANCE = new ItemBlockBiggerCraftingTable();

	public ItemBlockBiggerCraftingTable()
	{
		super(BlockBiggerCraftingTable.INSTANCE);
		setRegistryName(new ResourceLocation(Reference.MOD_ID, "biggercraftingtable"));
		setHasSubtypes(true);
	}

	@Override
	public String getUnlocalizedNameInefficiently(final ItemStack itemStack)
	{
		return "container." + Reference.TableTypes.getName(getDamage(itemStack)) + "craftingtable";
	}

	@Override
	public int getMetadata(final int damage)
	{
		return MathHelper.clamp(damage, 0, Reference.TableTypes.values().length);
	}
}